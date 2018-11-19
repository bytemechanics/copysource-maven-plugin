/*
 * Copyright 2018 Byte Mechanics.
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
package org.bytemechanics.maven.plugin.copyclasses;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
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
import java.util.jar.JarEntry;
import java.util.jar.JarException;
import java.util.jar.JarFile;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.DefaultProjectBuildingRequest;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.shared.artifact.resolve.ArtifactResolver;
import org.bytemechanics.maven.plugin.copyclasses.beans.CopyDefinition;
import org.bytemechanics.maven.plugin.copyclasses.enums.Scope;

/**
 * @author afarre
 * usage:
 *  <code>
 *		&lt;plugin&gt;
 *			&lt;groupId&gt;org.bytemechanics.maven&lt;/groupId&gt;
 *			&lt;artifactId&gt;copyclasses-maven-project&lt;/artifactId&gt;
 *			&lt;version&gt;0.1.0-SNAPSHOT&lt;/version&gt;
 *			&lt;executions&gt;
 *				&lt;execution&gt;
 *					&lt;goals&gt;
 *						&lt;goal&gt;copy-classes&lt;/goal&gt;
 *					&lt;/goals&gt;
 *					&lt;configuration&gt;
 *						&lt;copies&gt;
 *							&lt;copy&gt;
 *								&lt;artifact&gt;org.bytemechanics:copy-commons&lt;/artifact&gt;
 *								&lt;classes&gt;
 *									&lt;class&gt;org.bytemechanics.commons.string.GenericTextParser&lt;/class&gt;
 *									&lt;class&gt;org.bytemechanics.commons.functional.LambdaUnchecker&lt;/class&gt;
 *								&lt;/classes&gt;
 *								&lt;fromPackage&gt;org.bytemechanics.commons&lt;/fromPackage&gt;
 *								&lt;toPackage&gt;org.bytemechanics.standalone.ignite.internal.commons&lt;/toPackage&gt;
 *							&lt;/copy&gt;
 *						&lt;/copies&gt;
 *					&lt;/configuration&gt;
 *				&lt;/execution&gt;
 *			&lt;/executions&gt;
 *		&lt;/plugin&gt;
 *	</code>
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public abstract class CopyClassesBase extends AbstractMojo {

	
	private static final String METAINF = "META-INF";
	
	/**
	 * Artifact resolver, needed to download source jars for inclusion in classpath.
	 */
	@Component
	protected ArtifactResolver artifactResolver;
	
	/**
	 * Maven session
	 */
	@Parameter(defaultValue = "${session}", readonly = true, required = true)
	protected MavenSession session;

	/**
	 * Maven project
	 */
	@Parameter(defaultValue = "${project}", readonly = true, required = true)
	protected MavenProject project;
	
	/**
	 * Define the copies to do
	 * @see CopyDefinition
	 */
	@Parameter(required = true)
	protected CopyDefinition[] copies;
	
	@Parameter(defaultValue = "copies", required = true)
	protected String generatedSourceFolder;
	
	
	protected void generateSources(final Scope _scope) throws MojoExecutionException {
		
		Path generatedSourcesPath=null;
		
		final ProjectBuildingRequest buildingRequest=new DefaultProjectBuildingRequest( session.getProjectBuildingRequest() );
		final Charset sourceEncoding=Charset.forName(this.project.getProperties().getProperty("project.build.sourceEncoding"));
		getLog().debug(MessageFormat.format("Source encoding: {0}",sourceEncoding));

		try{
			generatedSourcesPath=Paths.get(this.project.getBuild().getDirectory())
										.resolve(_scope.getFolder())
										.resolve(this.generatedSourceFolder);
			Files.createDirectories(generatedSourcesPath.resolve(METAINF));
			_scope.registerSourceFolder(this.project, generatedSourcesPath);
			getLog().debug(MessageFormat.format("Generated source folder: {0}",generatedSourcesPath));
		}catch(IOException e){
			throw new MojoExecutionException(MessageFormat.format("Unable to create folder {0}",generatedSourcesPath), e);
		}

		for(CopyDefinition copy:copies){
			getLog().info(copy.toString());
			try{
				Artifact artifact=this.artifactResolver
											.resolveArtifact(buildingRequest, copy.toCoordinate())
											.getArtifact();
				getLog().debug(MessageFormat.format("Found: {0}",artifact));
				final File file=artifact.getFile();
				getLog().debug(MessageFormat.format("Downloaded source: {0}",file));
				processDownloadedSource(file, copy, generatedSourcesPath, sourceEncoding);
			}catch(MojoExecutionException e){
				throw e;
			}catch(Exception e){
				throw new MojoExecutionException(MessageFormat.format("Failed processing copy: {0}",copy.getArtifact()), e);
			}
		}
		
		getLog().debug("Write copy manifest");
		try(BufferedWriter sourceWriter=new BufferedWriter(Files.newBufferedWriter(generatedSourcesPath.resolve(METAINF).resolve("copy-manifest.info"),sourceEncoding, StandardOpenOption.CREATE,StandardOpenOption.WRITE,StandardOpenOption.TRUNCATE_EXISTING))){
			sourceWriter.write("The following classes has been copied from external libraries:\n\n");
			for(CopyDefinition copy:copies){
				sourceWriter.write(MessageFormat.format("From artifact [{0}]:\n", copy.getArtifact()));
				for(String clazz:copy.getClasses()){
					sourceWriter.write(MessageFormat.format("\t[{0}] repackaged from [{1}]\n", clazz.replace(copy.getFromPackage(),copy.getToPackage()),clazz));
				}
			}
		}catch(IOException e){
			throw new MojoExecutionException("Unable create manifest file", e);
		}
		final Resource resource=new Resource();
		resource.setDirectory(generatedSourcesPath.resolve(METAINF).toString());
		resource.setTargetPath(METAINF);
		this.project.addResource(resource);
	}

	private void processDownloadedSource(final File _file,final CopyDefinition _copy,final Path _generatedSourcesPath, final Charset _sourceEncoding) throws IOException, MojoExecutionException {
		
		try(JarFile sourcePackage=new JarFile(_file,true,JarFile.OPEN_READ)){
			for(String clazz:_copy.getClasses()){
				getLog().debug(MessageFormat.format("Looking for class {0}",clazz));
				final JarEntry sourceEntry=sourcePackage.getJarEntry(clazz.replace('.','/')+".java");
				if(sourceEntry==null){
					throw new MojoExecutionException(MessageFormat.format("Unable find class {0} at source {1} from artifact {2}",clazz,_file,_copy.getArtifact()));
				}
				getLog().debug(MessageFormat.format("Creating package {0} destiny",clazz));
				final Path generatedSourceFile=generatePackage(_generatedSourcesPath, clazz, _copy);
				getLog().debug(MessageFormat.format("Extracting class {0} source",clazz));
				copySource(sourcePackage, sourceEntry, _copy, generatedSourceFile, _sourceEncoding, _file, clazz);
			}
		}catch(JarException e){
			throw new MojoExecutionException(MessageFormat.format("Unable to open source {0} from artifact {1}",_file,_copy.getArtifact()), e);
		}
	}

	private Path generatePackage(final Path _generatedSourcesPath, final String _clazz,final CopyDefinition _copy) throws MojoExecutionException {
		
		final Path reply;
		
		try{
			reply=_generatedSourcesPath.resolve(_clazz
													.replaceAll(_copy.toRegexPackage(),_copy.getToPackage())
													.replace('.','/')+".java");
			Files.createDirectories(reply.getParent());
		}catch(IOException e){
			throw new MojoExecutionException(MessageFormat.format("Failed creating new package to copy: {0}",_copy), e);
		}
		
		return reply;
	}

	private void copySource(final JarFile _sourcePackage, final JarEntry _sourceEntry,final CopyDefinition _copy, final Path _generatedSourceFile, final Charset _sourceEncoding,final File _file,final String _clazz) throws MojoExecutionException {

		try(BufferedReader sourceReader=new BufferedReader(new InputStreamReader(_sourcePackage.getInputStream(_sourceEntry),Charset.forName(_copy.getSourceCharset())));
				BufferedWriter sourceWriter=new BufferedWriter(Files.newBufferedWriter(_generatedSourceFile,_sourceEncoding, StandardOpenOption.CREATE,StandardOpenOption.WRITE,StandardOpenOption.TRUNCATE_EXISTING))){
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
				getLog().debug(MessageFormat.format("Extracted class {0} line {1}",_clazz,line));
				line=line.replaceAll(_copy.toRegexPackage(),_copy.getToPackage());
				getLog().debug(MessageFormat.format("Modified class {0} line {1}",_clazz,line));
				if((!isInComment)&&(!mainFound)&&(isMainTypeDefinition(line))){
					sourceWriter.write(MessageFormat.format("@Generated(value=\"generated-by-copy-plugin\", comments = \"Copied from {0}\", date = \"{1}\")\n", _copy.toCoordinate(),LocalDateTime.now()));
					mainFound=true;
				}
				sourceWriter.write(line);
				sourceWriter.write('\n');
				if((!isInComment)&&(packageFound)&&(!importAdded)){
					sourceWriter.write("import javax.annotation.Generated;\n");
					importAdded=true;
				}
				line=sourceReader.readLine();
			}
		}catch(IOException|IllegalCharsetNameException|UnsupportedCharsetException e){
			throw new MojoExecutionException(MessageFormat.format("Unable read source {0} class {1} from artifact {2} with charset {3}",_file,_clazz,_copy.getArtifact(),_copy.getSourceCharset()), e);
		}
	}
	
	private boolean isPackage(final String _line){
		
		final String processingLine=_line.trim().toLowerCase();
		return processingLine.startsWith("package ");
	}
	private boolean isBeginComment(final String _line){
		
		final String processingLine=_line.trim().toLowerCase();
		return processingLine.contains("/*");
	}
	private boolean isEndComment(final String _line){
		
		final String processingLine=_line.trim().toLowerCase();
		return processingLine.contains("*/");
	}
	
	private boolean isMainTypeDefinition(final String _line){

		final String[] javaTypes={"class","interface","@interface","enum"};
		final String processingLine=_line.trim().toLowerCase();
		boolean reply=false;


		for(String javaType:javaTypes){
			reply|=(processingLine.startsWith(javaType+" "))||(processingLine.contains(" "+javaType+" "));
		}

		return reply;
	}
}
