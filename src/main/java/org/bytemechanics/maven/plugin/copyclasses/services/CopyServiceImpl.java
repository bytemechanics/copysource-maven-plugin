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
import org.bytemechanics.maven.plugin.copyclasses.enums.GeneratedFactory;
import org.bytemechanics.maven.plugin.copyclasses.enums.Scope;

/**
 * Service to copy files
 * @author afarre
 */
public class CopyServiceImpl implements CopyService {

	public static final String METAINF = "META-INF";

	private final Log logger;
	private final LocalDateTime executionTime;
	private final String javaVersion;
	private final String targetFolder;
	private final String generatedSourceFolder;
	private final Charset encoding;
	
	public CopyServiceImpl(final Log _logger,final String _javaVersion,final String _targetFolder,final String _generatedSourceFolder,final Charset _encoding) {
		this(_logger,_javaVersion,_targetFolder,_generatedSourceFolder,_encoding,LocalDateTime.now());
	}
	public CopyServiceImpl(final Log _logger,final String _javaVersion,final String _targetFolder,final String _generatedSourceFolder,final Charset _encoding,final LocalDateTime _executionTime) {
		this.logger=_logger;
		this.targetFolder=_targetFolder;
		this.generatedSourceFolder=_generatedSourceFolder;
		this.javaVersion=_javaVersion;
		this.encoding=_encoding;
		this.executionTime=_executionTime;
	}

	@Override
	public String getJavaVersion() {
		return javaVersion;
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
			try(BufferedWriter sourceWriter=new BufferedWriter(Files.newBufferedWriter(metainfFolder.resolve("copy-manifest.info"),getEncoding(), StandardOpenOption.CREATE,StandardOpenOption.WRITE,StandardOpenOption.TRUNCATE_EXISTING))){
				sourceWriter.write("The following classes has been copied from external libraries:\n\n");
				for(CopyDefinition copy:_copies){
					sourceWriter.write(MessageFormat.format("From artifact [{0}]:\n", copy.getArtifact()));
					for(String clazz:copy.getClasses()){
						sourceWriter.write(MessageFormat.format("\t[{0}] repackaged from [{1}]\n", clazz.replace(copy.getFromPackage(),copy.getToPackage()),clazz));
					}
				}
			}
		}catch(IOException e){
			throw new MojoExecutionException("Unable create manifest file", e);
		}
	}

	@Override
	public void processDownloadedSource(final Path _sourceFile,final CopyDefinition _copy,final Path _generatedSourcesPath) throws MojoExecutionException {
		
		try(JarFile sourcePackage=new JarFile(_sourceFile.toFile(),true,JarFile.OPEN_READ)){
			for(String clazz:_copy.getClasses()){
				logger.debug(MessageFormat.format("Looking for class {0}",clazz));
				final JarEntry sourceEntry=sourcePackage.getJarEntry(clazz.replace('.','/')+".java");
				if(sourceEntry==null){
					throw new MojoExecutionException(MessageFormat.format("Unable find class {0} at source {1} from artifact {2}",clazz,_sourceFile,_copy.getArtifact()));
				}
				logger.debug(MessageFormat.format("Creating package {0} destiny",clazz));
				final Optional<Path> generatedSourceFile=generateSourceFile(_generatedSourcesPath, clazz, _copy);
				if(generatedSourceFile.isPresent()){
					final Path sourceFile=generatedSourceFile.get();
					generatePackage(sourceFile);
					logger.debug(MessageFormat.format("Extracting class {0} source",clazz));
					copySource(sourcePackage, sourceEntry, _copy, sourceFile, _sourceFile, clazz);
				}
			}
		}catch(IOException|MojoExecutionException e){
			throw new MojoExecutionException(MessageFormat.format("Unable to open source {0} from artifact {1}",_sourceFile,_copy.getArtifact()), e);
		}
	}
	
	protected Optional<Path> generateSourceFile(final Path _generatedSourcesPath, final String _className,final CopyDefinition _copy) {
		
		return Optional.ofNullable(_className)
							.map(className -> className.replaceAll(_copy.getFromPackageRegex(),_copy.getToPackage()))
							.map(className -> className.replace('.','/'))
							.map(className -> className.concat(".java"))
							.map(_generatedSourcesPath::resolve);
	}

	protected void generatePackage(final Path _generatedSource) throws MojoExecutionException {
		
		try{
			Files.createDirectories(_generatedSource.getParent());
		}catch(IOException e){
			throw new MojoExecutionException(MessageFormat.format("Failed creating new package for file: {0}",_generatedSource), e);
		}
	}
	
	protected GeneratedFactory getGeneratedAnnotationFactory() {
		return GeneratedFactory.from(getJavaVersion());
	}

	protected void copySource(final JarFile _sourcePackage, final JarEntry _sourceEntry,final CopyDefinition _copy, final Path _generatedSourceFile,final Path _sourceFile,final String _clazz) throws MojoExecutionException {

		final GeneratedFactory factory=getGeneratedAnnotationFactory();
		
		try(BufferedReader sourceReader=new BufferedReader(new InputStreamReader(_sourcePackage.getInputStream(_sourceEntry),Charset.forName(_copy.getSourceCharset())));
				BufferedWriter sourceWriter=new BufferedWriter(Files.newBufferedWriter(_generatedSourceFile,getEncoding(), StandardOpenOption.CREATE,StandardOpenOption.WRITE,StandardOpenOption.TRUNCATE_EXISTING))){
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
				logger.debug(MessageFormat.format("Modified class {0} line {1}",_clazz,line));
				if((!isInComment)&&(!mainFound)&&(isMainTypeDefinition(line))){
					sourceWriter.write(factory.getAnnotation(_copy,getExecutionTime()));
					mainFound=true;
				}
				sourceWriter.write(line);
				sourceWriter.write('\n');
				if((!isInComment)&&(packageFound)&&(!importAdded)){
					sourceWriter.write(factory.getImport());
					importAdded=true;
				}
				line=sourceReader.readLine();
			}
		}catch(IOException|IllegalCharsetNameException|UnsupportedCharsetException e){
			throw new MojoExecutionException(MessageFormat.format("Unable read source {0} class {1} from artifact {2} with charset {3}",_sourceFile,_clazz,_copy.getArtifact(),_copy.getSourceCharset()), e);
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
