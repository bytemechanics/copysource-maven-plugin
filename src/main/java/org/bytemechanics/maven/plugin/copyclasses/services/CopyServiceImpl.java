/*
 * Copyright 2020 Byte Mechanics.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.bytemechanics.maven.plugin.copyclasses.services;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.bytemechanics.maven.plugin.copyclasses.beans.CopyDefinition;
import org.bytemechanics.maven.plugin.copyclasses.enums.Scope;
import org.bytemechanics.maven.plugin.copyclasses.exceptions.UnableToIdentifyCoordinate;
import org.bytemechanics.maven.plugin.copyclasses.internal.commons.functional.LambdaUnchecker;

/**
 * Service to copy files
 * @author afarre
 */
public class CopyServiceImpl implements CopyService {

	public static final String METAINF = "META-INF";
	public static final String JAVA_SOURCE_EXTENSION=".java";
	public static final String CUSTOM_ANNOTATION_CLASS="org.bytemechanics.maven.plugin.copyclasses.annotations.CopiedSource";
	public static final String CUSTOM_IMPORT_ANNOTATION="import "+CUSTOM_ANNOTATION_CLASS+";";
	public static final String CUSTOM_ANNOTATION_PATTERN="@CopiedSource(tool=\"org.bytemechanics.maven.copysource-maven-plugin\", toolVersion=\"{5}\", originGroupId=\"{0}\", originArtifactId=\"{1}\", originVersion=\"{2}\", originClassifier=\"{3}\", copyDate = \"{4}\")";	
	
	private final Log logger;
	private final LocalDateTime executionTime;
	private final String targetFolder;
	private final String generatedSourceFolder;
	private final Charset encoding;
	
	public CopyServiceImpl(final Log _logger,final String _targetFolder,final String _generatedSourceFolder,final Charset _encoding) {
		this(_logger,_targetFolder,_generatedSourceFolder,_encoding,LocalDateTime.now());
	}
	public CopyServiceImpl(final Log _logger,final String _targetFolder,final String _generatedSourceFolder,final Charset _encoding,final LocalDateTime _executionTime) {
		this.logger=_logger;
		this.targetFolder=_targetFolder;
		this.generatedSourceFolder=_generatedSourceFolder;
		this.encoding=_encoding;
		this.executionTime=_executionTime;
	}

	@Override
	public LocalDateTime getExecutionTime() {
		return executionTime;
	}
	@Override
	public String getTargetFolder() {
		return targetFolder;
	}
	@Override
	public String getGeneratedSourceFolder() {
		return generatedSourceFolder;
	}
	@Override
	public Charset getEncoding() {
		return encoding;
	}

	
	private void copyAnnotation(final Path _sourceFile) throws MojoExecutionException{
		
		try(InputStream annotationSource=this.getClass().getClassLoader().getResourceAsStream(CUSTOM_ANNOTATION_CLASS.replace('.','/')+JAVA_SOURCE_EXTENSION)){
			this.logger.info("Copy copy-sources annotation");
			final CopyDefinition copy=Optional.of(CUSTOM_ANNOTATION_CLASS)
												.map(annotationClass -> CUSTOM_ANNOTATION_CLASS.lastIndexOf('.'))
												.map(index -> CUSTOM_ANNOTATION_CLASS.substring(0,index))
												.map(annotationPackage -> new CopyDefinition(METAINF, new String[]{CUSTOM_ANNOTATION_CLASS}, "UTF-8", annotationPackage, annotationPackage))
												.orElseThrow(() -> new MojoExecutionException("Unable create definition for copy-sources annotation"));
			copySource(annotationSource,_sourceFile,CUSTOM_ANNOTATION_CLASS,copy,false);
		}catch(IOException e){
			throw new MojoExecutionException("Unable create class CopiedSource from internal resource", e);
		} catch (MojoExecutionException e) {
			throw e;
		}
	}
	
	@Override
	public void prepareEnvironment(final Path _generatedSourcesPath,final CopyDefinition _copy) throws MojoExecutionException{

		this.logger.debug("prepareEnvironment");
		generateSourceFile(_generatedSourcesPath, CUSTOM_ANNOTATION_CLASS, _copy)
			.filter(sourceTargetFile -> !sourceTargetFile.toFile().exists())
			.ifPresent(LambdaUnchecker.uncheckedConsumer(this::copyAnnotation));
	}
	
	@Override
	public Path generateSourcePath(final Scope _scope) throws MojoExecutionException {
		
		Path reply=null;
		
		try{
			reply=Paths.get(getTargetFolder())
					.resolve(_scope.getFolder())
					.resolve(getGeneratedSourceFolder());
			Files.createDirectories(reply);
			this.logger.debug(MessageFormat.format("Generated source folder: {0}",reply));
		}catch(IOException e){
			throw new MojoExecutionException(MessageFormat.format("Unable to create folder {0}",reply), e);
		}
		
		return reply;
	}
	@Override
	public void createManifest(final CopyDefinition[] _copies,final Path _generatedSourcesPath) throws MojoExecutionException {
	
		try{
			final Path metainfFolder=_generatedSourcesPath.resolve(METAINF);
			Files.createDirectories(metainfFolder);
			writeManifest(metainfFolder, _copies);
		}catch(IOException e){
			throw new MojoExecutionException("Unable create manifest file", e);
		}
	}

	private void writeManifest(final Path _metainfFolder, final CopyDefinition[] _copies) throws IOException {
		try(BufferedWriter sourceWriter=new BufferedWriter(Files.newBufferedWriter(_metainfFolder.resolve("copy-manifest.info"),getEncoding(), StandardOpenOption.CREATE,StandardOpenOption.WRITE,StandardOpenOption.TRUNCATE_EXISTING))){
			sourceWriter.write("The following classes has been copied from external libraries:\n\n");
			for(CopyDefinition copy:_copies){
				sourceWriter.write(MessageFormat.format("From artifact [{0}]:\n", copy.getArtifact()));
				for(String clazz:copy.getClasses()){
					sourceWriter.write(MessageFormat.format("\t[{0}] repackaged from [{1}]\n", clazz.replace(copy.getFromPackage(),copy.getToPackage()),clazz));
				}
			}
			sourceWriter.write(MessageFormat.format("From artifact [org.bytemechanics.maven:copysource-maven-plugin:{0}]:\n", this.getClass().getPackage().getImplementationVersion()));
			sourceWriter.write(MessageFormat.format("\t[{0}] generated", CUSTOM_ANNOTATION_CLASS));
		}
	}

	@Override
	public void processDownloadedSource(final Path _sourceFile,final CopyDefinition _copy,final Path _generatedSourcesPath) throws MojoExecutionException {
		
		try(JarFile sourcePackage=new JarFile(_sourceFile.toFile(),true,JarFile.OPEN_READ)){
			for(String clazz:_copy.getClasses()){
				logger.debug(MessageFormat.format("Looking for class {0}",clazz));
				final JarEntry sourceEntry=sourcePackage.getJarEntry(clazz.replace('.','/')+JAVA_SOURCE_EXTENSION);
				if(sourceEntry==null){
					throw new MojoExecutionException(MessageFormat.format("Unable find class {0} at source {1} from artifact {2}",clazz,_sourceFile,_copy.getArtifact()));
				}
				logger.debug(MessageFormat.format("Creating package {0} destiny",clazz));
				final Optional<Path> generatedSourceFile=generateSourceFile(_generatedSourcesPath, clazz, _copy);
				if(generatedSourceFile.isPresent()){
					final Path targetFile=generatedSourceFile.get();
					copyDownloadedSource(sourcePackage, sourceEntry, clazz, targetFile, _copy, _sourceFile);
				}
			}
		}catch(IOException|MojoExecutionException e){
			throw new MojoExecutionException(MessageFormat.format("Unable to open source {0} from artifact {1}",_sourceFile,_copy.getArtifact()), e);
		}
	}

	private void copyDownloadedSource(final JarFile _sourcePackage, final JarEntry _sourceEntry, String _clazz, final Path _targetFile, final CopyDefinition _copy, final Path _sourceFile) throws IOException, MojoExecutionException {
		try(InputStream inputStream=_sourcePackage.getInputStream(_sourceEntry)){
			logger.debug(MessageFormat.format("Extracting class {0} source",_clazz));
			copySource(inputStream, _targetFile, _clazz, _copy, true);
		}catch(MojoExecutionException e){
			throw new MojoExecutionException(MessageFormat.format("Unable read source {0} class {1} from artifact {2} with charset {3}",_sourceFile,_clazz,_copy.getArtifact(),_copy.getSourceCharset()), e);
		}
	}
	
	protected Optional<Path> generateSourceFile(final Path _generatedSourcesPath, final String _className,final CopyDefinition _copy) {
		
		return Optional.ofNullable(_className)
							.map(className -> className.replaceAll(_copy.getFromPackageRegex(),_copy.getToPackage()))
							.map(className -> className.replace('.','/'))
							.map(className -> className.concat(JAVA_SOURCE_EXTENSION))
							.map(_generatedSourcesPath::resolve);
	}

	protected void generatePackage(final Path _generatedSource) throws MojoExecutionException {
		
		try{
			Files.createDirectories(_generatedSource.getParent());
		}catch(IOException e){
			throw new MojoExecutionException(MessageFormat.format("Failed creating new package for file: {0}",_generatedSource), e);
		}
	}
	
	protected String getAnnotation(final CopyDefinition _copy,final LocalDateTime _time){
		return Optional.ofNullable(_copy)
						.map(CopyDefinition::toCoordinate)
						.map(coordinate -> new Object[]{coordinate.getGroupId(),coordinate.getArtifactId(),coordinate.getVersion(),coordinate.getClassifier(),_time,this.getClass().getPackage().getImplementationVersion()})
						.map(values -> MessageFormat.format(CUSTOM_ANNOTATION_PATTERN, values))
						.orElseThrow(() -> new UnableToIdentifyCoordinate(_copy));
	}	
	
	protected void copySource(final InputStream _classInputStream,final Path _sourceTargetFile,final String _clazz,final CopyDefinition _copy,final boolean _annotate) throws MojoExecutionException{
		
		generatePackage(_sourceTargetFile);
		try(BufferedReader sourceReader=new BufferedReader(new InputStreamReader(_classInputStream,_copy.getSourceCharset()));
				BufferedWriter sourceWriter=new BufferedWriter(Files.newBufferedWriter(_sourceTargetFile,getEncoding(), StandardOpenOption.CREATE,StandardOpenOption.WRITE,StandardOpenOption.TRUNCATE_EXISTING))){
			String line=sourceReader.readLine();
			boolean mainFound=false;
			boolean packageFound=false;
			boolean isInComment=false;
			boolean importAdded=false;
			while(line!=null){
				packageFound|=isPackage(line);
				if((!isInComment)&&(isBeginComment(line))){
					isInComment=true;
				}else{
					if(isEndComment(line)){
						isInComment=false;
					}
				}
				logger.debug(MessageFormat.format("Extracted class {0} line {1}",_clazz,line));
				line=line.replaceAll(_copy.getFromPackageRegex(),_copy.getToPackage());
				if((_annotate)&&(packageFound)&&(!isInComment)&&(!mainFound)){
					if(isMainTypeDefinition(line)){
						sourceWriter.write(getAnnotation(_copy,getExecutionTime()));
						sourceWriter.write('\n');
						logger.debug(MessageFormat.format("Modified class {0} line {1}",_clazz,line));
						mainFound=true;
					}
					sourceWriter.write(line);
					sourceWriter.write('\n');
					if(!importAdded){
						sourceWriter.write(CUSTOM_IMPORT_ANNOTATION);
						sourceWriter.write('\n');
						importAdded=true;
					}
				}else{
					sourceWriter.write(line);
					sourceWriter.write('\n');
				}
				logger.debug(MessageFormat.format("Modified class {0} line {1}",_clazz,line));
				line=sourceReader.readLine();
			}
		}catch(IOException|IllegalCharsetNameException|UnsupportedCharsetException e){
			throw new MojoExecutionException(MessageFormat.format("Unable read class {0} from package {1} with charset {2}",_clazz,_copy.getFromPackage(),_copy.getSourceCharset()), e);
		}
	}
	
	protected boolean isPackage(final String _line){
		
		return Optional.ofNullable(_line)
						.map(String::trim)
						.map(String::toLowerCase)
						.map(val -> val.startsWith("package "))
						.orElse(false);
	}
	protected boolean isBeginComment(final String _line){
		
		return Optional.ofNullable(_line)
						.map(String::trim)
						.map(String::toLowerCase)
						.map(val -> val.contains("/*"))
						.orElse(false);
	}
	protected boolean isEndComment(final String _line){

		return Optional.ofNullable(_line)
						.map(String::trim)
						.map(String::toLowerCase)
						.map(val -> val.contains("*/"))
						.orElse(false);
	}
	
	protected boolean isMainTypeDefinition(final String _line){

		return Optional.ofNullable(_line)
						.map(String::trim)
						.map(String::toLowerCase)
						.flatMap(processingLine -> Stream.of("class ","interface ","@interface ","enum ")
															.filter(type -> (processingLine.startsWith(type))||(processingLine.contains(" "+type)))
															.map(type -> Boolean.TRUE)
															.findAny())
						.orElse(Boolean.FALSE);
	}
}
