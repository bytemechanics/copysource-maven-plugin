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

import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
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
import org.apache.maven.shared.transfer.artifact.resolve.ArtifactResolver;
import org.bytemechanics.maven.plugin.copyclasses.beans.CopyDefinition;
import org.bytemechanics.maven.plugin.copyclasses.enums.Scope;
import org.bytemechanics.maven.plugin.copyclasses.services.CopyService;
import org.bytemechanics.maven.plugin.copyclasses.services.CopyServiceImpl;

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
public abstract class CopyClassesBase extends AbstractMojo {

	
	protected static final String METAINF = "META-INF";
	
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
	
	public CopyClassesBase() {
		super();
	}
	public CopyClassesBase(final ArtifactResolver _artifactResolver, final MavenSession _session, final MavenProject _project, final CopyDefinition[] _copies, final String _generatedSourceFolder) {
		this();
		this.artifactResolver = _artifactResolver;
		this.session = _session;
		this.project = _project;
		this.copies = _copies;
		this.generatedSourceFolder = _generatedSourceFolder;
	}

	
	public ArtifactResolver getArtifactResolver() {
		return artifactResolver;
	}
	public void setArtifactResolver(ArtifactResolver artifactResolver) {
		this.artifactResolver = artifactResolver;
	}

	public MavenSession getSession() {
		return session;
	}
	public void setSession(MavenSession session) {
		this.session = session;
	}

	public MavenProject getProject() {
		return project;
	}
	public void setProject(MavenProject project) {
		this.project = project;
	}

	public CopyDefinition[] getCopies() {
		return copies;
	}
	public void setCopies(CopyDefinition[] copies) {
		this.copies = copies;
	}

	public String getGeneratedSourceFolder() {
		return generatedSourceFolder;
	}
	public void setGeneratedSourceFolder(String generatedSourceFolder) {
		this.generatedSourceFolder = generatedSourceFolder;
	}

	protected CopyService instantiateCopyService(){

		final String javaVersion=getProject()
									.getProperties()
										.getProperty("maven.compiler.target","1.8");
		getLog().debug(MessageFormat.format("Java version: {0}",javaVersion));
		final String encoding=getProject()
									.getProperties()
										.getProperty("project.build.sourceEncoding",Charset.defaultCharset().name());
		getLog().debug(MessageFormat.format("Source encoding: {0}",encoding));
		final String targetFolder=getProject()
									.getBuild()
										.getDirectory();
		getLog().debug(MessageFormat.format("Target folder: {0}",targetFolder));
		return new CopyServiceImpl(getLog(),javaVersion,targetFolder,getGeneratedSourceFolder(),Charset.forName(encoding));
	}
	
	protected void generateSources(final Scope _scope) throws MojoExecutionException {
		
		final ProjectBuildingRequest buildingRequest=new DefaultProjectBuildingRequest( session.getProjectBuildingRequest() );
		final CopyService copyService=instantiateCopyService();
		
		getLog().debug("Generate source destiny path");
		final Path generatedSourcesPath=copyService.generateSourcePath(_scope);
		_scope.registerSourceFolder(getProject(), generatedSourcesPath);
		getLog().debug(MessageFormat.format("Generate source destiny path >> {0}",generatedSourcesPath));

		getLog().debug("Process copies");
		for(CopyDefinition copy:getCopies()){
			getLog().info(MessageFormat.format("Process copy: {0}",copy));
			final Path downloadedFile=downloadSource(buildingRequest, copy, generatedSourcesPath);
			getLog().debug(MessageFormat.format("Process copy {0} >> Downloaded source: {1}",copy,downloadedFile));
			copyService.processDownloadedSource(downloadedFile, copy, generatedSourcesPath);
			getLog().debug(MessageFormat.format("Process copy {0} >> Downloaded source: {1} >> processed",copy,downloadedFile));
		}
		
		getLog().debug("Write copy manifest");
		copyService.createManifest(getCopies(),generatedSourcesPath);
		
		getLog().debug("Register manifest resource");
		final Resource resource=new Resource();
		resource.setDirectory(generatedSourcesPath.resolve(METAINF).toString());
		resource.setTargetPath(METAINF);
		getProject().addResource(resource);
	}


	@SuppressWarnings("UseSpecificCatch")
	protected Path downloadSource(final ProjectBuildingRequest _buildingRequest,final CopyDefinition _copy, final Path _generatedSourcesPath) throws MojoExecutionException {
		
		Path reply;
		
		try{
			final Artifact artifact=getArtifactResolver()
											.resolveArtifact(_buildingRequest, _copy.toCoordinate())
											.getArtifact();
			getLog().debug(MessageFormat.format("Found: {0}",artifact));
			reply=Paths.get(artifact.getFile().getAbsolutePath());
		}catch(Exception e){
			throw new MojoExecutionException(MessageFormat.format("Failed processing copy: {0}",_copy.getArtifact()), e);
		}
		
		return reply;
	}
}
