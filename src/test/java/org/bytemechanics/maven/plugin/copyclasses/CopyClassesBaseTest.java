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
package org.bytemechanics.maven.plugin.copyclasses;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import mockit.Tested;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Build;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.shared.transfer.artifact.ArtifactCoordinate;
import org.apache.maven.shared.transfer.artifact.resolve.ArtifactResolver;
import org.apache.maven.shared.transfer.artifact.resolve.ArtifactResolverException;
import org.apache.maven.shared.transfer.artifact.resolve.ArtifactResult;
import org.bytemechanics.maven.plugin.copyclasses.beans.CopyDefinition;
import org.bytemechanics.maven.plugin.copyclasses.enums.Scope;
import org.bytemechanics.maven.plugin.copyclasses.services.CopyService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/**
 *
 * @author afarre
 */
public class CopyClassesBaseTest {
	
	@BeforeAll
	public static void setup() throws IOException{
		System.out.println(">>>>> CopyClassesBaseTest >>>> setupSpec");
		try(InputStream inputStream = CopyClassesBaseTest.class.getResourceAsStream("/logging.properties")){
			LogManager.getLogManager().readConfiguration(inputStream);
		}catch (final IOException e){
			Logger.getAnonymousLogger().severe("Could not load default logging.properties file");
			Logger.getAnonymousLogger().severe(e.getMessage());
		}
	}

	@Mocked 
	@Injectable
	ArtifactResolver artifactResolver;
	@Mocked 
	@Injectable
	MavenSession session; 
	@Mocked 
	@Injectable
	MavenProject project; 

	@Mocked 
	CopyDefinition copy1; 
	@Mocked 
	CopyDefinition copy2;
	
	@Injectable
	CopyDefinition[] copies;
	@Injectable
	String generatedSourceFolder;

	@BeforeEach
    void beforeEachTest(final TestInfo testInfo) {
        System.out.println(">>>>> "+this.getClass().getSimpleName()+" >>>> "+testInfo.getTestMethod().map(Method::getName).orElse("Unkown")+""+testInfo.getTags().toString()+" >>>> "+testInfo.getDisplayName());
		this.copies=new CopyDefinition[]{copy1,copy2};
		this.generatedSourceFolder="copies";
	}
	
	@Tested
	@Mocked
	CopyClassesMojo instance;
	
	@Test
	@DisplayName("getArtifactResolver() should  be null if used empty constructor")
	public void getArtifactResolver_null(){
		final CopyClassesBaseImpl buildInstance=new CopyClassesBaseImpl();
		Assertions.assertNull(buildInstance.getArtifactResolver());
	}
	@Test
	@DisplayName("getArtifactResolver() should be the same used in constructor")
	public void getArtifactResolver_full(final @Mocked ArtifactResolver _artifactResolver, final @Mocked MavenSession _session, final @Mocked MavenProject _project, final @Mocked CopyDefinition[] _copies, final @Mocked String _generatedSourceFolder){
		final CopyClassesBase buildInstance=new CopyClassesBaseImpl(_artifactResolver,_session,_project,_copies,_generatedSourceFolder);
		Assertions.assertSame(_artifactResolver,buildInstance.getArtifactResolver());
	}
	@Test
	@DisplayName("getArtifactResolver() should be distinct from provided in constructor once replaced with setSession()")
	public void setArtifactResolver(final @Mocked ArtifactResolver _artifactResolver,final @Mocked ArtifactResolver _artifactResolver2, final @Mocked MavenSession _session, final @Mocked MavenProject _project, final @Mocked CopyDefinition[] _copies, final @Mocked String _generatedSourceFolder){
		final CopyClassesBase buildInstance=new CopyClassesBaseImpl(_artifactResolver,_session,_project,_copies,_generatedSourceFolder);
		Assertions.assertSame(_artifactResolver,buildInstance.getArtifactResolver());
		buildInstance.setArtifactResolver(_artifactResolver2);
		Assertions.assertSame(_artifactResolver2,buildInstance.getArtifactResolver());
	}

	@Test
	@DisplayName("getSession() should  be null if used empty constructor")
	public void getSession_null(){
		final CopyClassesBaseImpl buildInstance=new CopyClassesBaseImpl();
		Assertions.assertNull(buildInstance.getSession());
	}
	@Test
	@DisplayName("getSession() should be the same used in constructor")
	public void getSession_full(final @Mocked ArtifactResolver _artifactResolver, final @Mocked MavenSession _session, final @Mocked MavenProject _project, final @Mocked CopyDefinition[] _copies, final @Mocked String _generatedSourceFolder){
		final CopyClassesBase buildInstance=new CopyClassesBaseImpl(_artifactResolver,_session,_project,_copies,_generatedSourceFolder);
		Assertions.assertSame(_session,buildInstance.getSession());
	}
	@Test
	@DisplayName("getSession() should be distinct from provided in constructor once replaced with setSession()")
	public void setSession(final @Mocked ArtifactResolver _artifactResolver, final @Mocked MavenSession _session,final @Mocked MavenSession _session2, final @Mocked MavenProject _project, final @Mocked CopyDefinition[] _copies, final @Mocked String _generatedSourceFolder){
		final CopyClassesBase buildInstance=new CopyClassesBaseImpl(_artifactResolver,_session,_project,_copies,_generatedSourceFolder);
		Assertions.assertSame(_session,buildInstance.getSession());
		buildInstance.setSession(_session2);
		Assertions.assertSame(_session2,buildInstance.getSession());
	}

	@Test
	@DisplayName("getProject() should  be null if used empty constructor")
	public void getProject_null(){
		final CopyClassesBaseImpl buildInstance=new CopyClassesBaseImpl();
		Assertions.assertNull(buildInstance.getProject());
	}
	@Test
	@DisplayName("getProject() should be the same used in constructor")
	public void getProject_full(final @Mocked ArtifactResolver _artifactResolver, final @Mocked MavenSession _session, final @Mocked MavenProject _project, final @Mocked CopyDefinition[] _copies, final @Mocked String _generatedSourceFolder){
		final CopyClassesBase buildInstance=new CopyClassesBaseImpl(_artifactResolver,_session,_project,_copies,_generatedSourceFolder);
		Assertions.assertSame(_project,buildInstance.getProject());
	}
	@Test
	@DisplayName("getProject() should be distinct from provided in constructor once replaced with setProject()")
	public void setProject(final @Mocked ArtifactResolver _artifactResolver, final @Mocked MavenSession _session,final @Mocked MavenProject _project,final @Mocked MavenProject _project2, final @Mocked CopyDefinition[] _copies, final @Mocked String _generatedSourceFolder){
		final CopyClassesBase buildInstance=new CopyClassesBaseImpl(_artifactResolver,_session,_project,_copies,_generatedSourceFolder);
		Assertions.assertSame(_project,buildInstance.getProject());
		buildInstance.setProject(_project2);
		Assertions.assertSame(_project2,buildInstance.getProject());
	}

	@Test
	@DisplayName("getCopies() should  be null if used empty constructor")
	public void getCopies_null(){
		final CopyClassesBaseImpl buildInstance=new CopyClassesBaseImpl();
		Assertions.assertNull(buildInstance.getCopies());
	}
	@Test
	@DisplayName("getCopies() should be the same used in constructor")
	public void getCopies_full(final @Mocked ArtifactResolver _artifactResolver, final @Mocked MavenSession _session, final @Mocked MavenProject _project, final @Mocked CopyDefinition[] _copies, final @Mocked String _generatedSourceFolder){
		final CopyClassesBase buildInstance=new CopyClassesBaseImpl(_artifactResolver,_session,_project,_copies,_generatedSourceFolder);
		Assertions.assertSame(_copies,buildInstance.getCopies());
	}
	@Test
	@DisplayName("getCopies() should be distinct from provided in constructor once replaced with setCopies()")
	public void setCopies(final @Mocked ArtifactResolver _artifactResolver, final @Mocked MavenSession _session,final @Mocked MavenProject _project,final @Mocked CopyDefinition[] _copies,final @Mocked CopyDefinition[] _copies2, final @Mocked String _generatedSourceFolder){
		final CopyClassesBase buildInstance=new CopyClassesBaseImpl(_artifactResolver,_session,_project,_copies,_generatedSourceFolder);
		Assertions.assertSame(_copies,buildInstance.getCopies());
		buildInstance.setCopies(_copies2);
		Assertions.assertSame(_copies2,buildInstance.getCopies());
	}

	@Test
	@DisplayName("getGeneratedSourceFolder() should  be null if used empty constructor")
	public void getGeneratedSourceFolder_null(){
		final CopyClassesBaseImpl buildInstance=new CopyClassesBaseImpl();
		Assertions.assertNull(buildInstance.getGeneratedSourceFolder());
	}
	@Test
	@DisplayName("getGeneratedSourceFolder() should be the same used in constructor")
	public void getGeneratedSourceFolder_full(final @Mocked ArtifactResolver _artifactResolver, final @Mocked MavenSession _session, final @Mocked MavenProject _project, final @Mocked CopyDefinition[] _copies, final @Mocked String _generatedSourceFolder){
		final CopyClassesBase buildInstance=new CopyClassesBaseImpl(_artifactResolver,_session,_project,_copies,_generatedSourceFolder);
		Assertions.assertSame(_generatedSourceFolder,buildInstance.getGeneratedSourceFolder());
	}
	@Test
	@DisplayName("getGeneratedSourceFolder() should be distinct from provided in constructor once replaced with setGeneratedSourceFolder()")
	public void setGeneratedSourceFolder(final @Mocked ArtifactResolver _artifactResolver, final @Mocked MavenSession _session,final @Mocked MavenProject _project,final @Mocked CopyDefinition[] _copies, final @Mocked String _generatedSourceFolder, final @Mocked String _generatedSourceFolder2){
		final CopyClassesBase buildInstance=new CopyClassesBaseImpl(_artifactResolver,_session,_project,_copies,_generatedSourceFolder);
		Assertions.assertSame(_generatedSourceFolder,buildInstance.getGeneratedSourceFolder());
		buildInstance.setGeneratedSourceFolder(_generatedSourceFolder2);
		Assertions.assertSame(_generatedSourceFolder2,buildInstance.getGeneratedSourceFolder());
	}

	@Test
	@DisplayName("instantiateCopyService() should instantiate efectivelly the copy service")
	public void instantiateCopyService(final @Mocked Build _build){
		
		final Properties projectProperties=new Properties();
		projectProperties.setProperty("project.build.sourceEncoding", "UTF-16");

		new Expectations() {{
			project.getProperties(); result=projectProperties;
			project.getBuild(); result=_build;
			_build.getDirectory(); result="myDirectory";
		}};
		
		CopyService actual=instance.instantiateCopyService();
		Assertions.assertEquals(StandardCharsets.UTF_16, actual.getEncoding());
		Assertions.assertEquals(generatedSourceFolder, actual.getGeneratedSourceFolder());
		Assertions.assertEquals("myDirectory", actual.getTargetFolder());
	}
	@Test
	@DisplayName("instantiateCopyService() should instantiate efectivelly the copy service with default values")
	public void instantiateCopyService_defaults(final @Mocked Build _build){
		
		final Properties projectProperties=new Properties();

		new Expectations() {{
			project.getProperties(); result=projectProperties;
			project.getBuild(); result=_build;
			_build.getDirectory(); result="myDirectory";
		}};
		
		CopyService actual=instance.instantiateCopyService();
		Assertions.assertEquals(StandardCharsets.UTF_8, actual.getEncoding());
		Assertions.assertEquals(generatedSourceFolder, actual.getGeneratedSourceFolder());
		Assertions.assertEquals("myDirectory", actual.getTargetFolder());
	}
	
	@ParameterizedTest(name = "generateSources() for scope {0}")
	@EnumSource(Scope.class)
	public void generateSources(final Scope _scope,final @Mocked ProjectBuildingRequest _request,final @Mocked CopyService _copyService) throws MojoExecutionException{
		
		final Properties projectProperties=new Properties();
		projectProperties.setProperty("project.build.sourceEncoding", "UTF-8");
		final Path expectedGeneratedFolder=Paths.get("target/tests/generateSources")
												.resolve(_scope.getFolder())
												.resolve(this.generatedSourceFolder);
		final Path downloadedResource=Paths.get("myDownloadedResource");
		final Resource expectedResource=new Resource();
		expectedResource.setDirectory(expectedGeneratedFolder.resolve(CopyClassesBase.METAINF).toString());
		expectedResource.setTargetPath(CopyClassesBase.METAINF);

		final List<Resource> actualResource=new ArrayList<>();
		
		new Expectations() {{
			instance.instantiateCopyService(); result=_copyService;
			session.getProjectBuildingRequest(); result=_request;
			_copyService.generateSourcePath(_scope); result=expectedGeneratedFolder; times=1;
			
			instance.getCopies(); result=new CopyDefinition[]{copy1,copy2}; times=2;
			
			copy1.toString(); result="copy1"; 
			instance.downloadSource((ProjectBuildingRequest)any, copy1, expectedGeneratedFolder); result=downloadedResource; times=1;
			_copyService.processDownloadedSource(downloadedResource, copy1, expectedGeneratedFolder); times=1;
			
			copy2.toString(); result="copy2"; 
			instance.downloadSource((ProjectBuildingRequest)any, copy2, expectedGeneratedFolder); result=downloadedResource; times=1;
			_copyService.processDownloadedSource(downloadedResource, copy2, expectedGeneratedFolder); times=1;
			
			_copyService.createManifest(copies, expectedGeneratedFolder); times=1;

			project.addResource(withCapture(actualResource)); times=1;
		}};

		instance.generateSources(_scope);
		
		Assertions.assertEquals(1,actualResource.size());
		Assertions.assertEquals(expectedResource.toString(),actualResource.get(0).toString());
	}
	
	@Test
	@DisplayName("downloadSource() success execution")
	public void downloadSource(final @Mocked ProjectBuildingRequest _request,final @Mocked CopyDefinition _copy, final @Mocked ArtifactCoordinate _artifactCoordinate,final @Mocked ArtifactResult _artifactResult,final @Mocked Artifact _artifact) throws ArtifactResolverException, IOException, MojoExecutionException{
		
		final Path generatedFolder=Paths.get("target/tests/processClassesFromCopy");
		final File fileCopy=new File("myFileFromCopy");
		final Path expected=Paths.get("myFileFromCopy").toAbsolutePath();

		new Expectations() {{
			_copy.toCoordinate(); result=_artifactCoordinate; times=1;
			artifactResolver.resolveArtifact(_request, _artifactCoordinate); result=_artifactResult; times=1;
			_artifactResult.getArtifact(); result=_artifact; times=1;
			_artifact.toString(); result="copy:artifact";
			_artifact.getFile(); result=fileCopy; times=1;
		}};

		Assertions.assertEquals(expected,instance.downloadSource(_request, _copy, generatedFolder));
	}
	@Test
	@DisplayName("downloadSource() unexpected failure should raise MojoExecutionException")
	@SuppressWarnings("ThrowableResultIgnored")
	public void downloadSource_unexpected_failure(final @Mocked ProjectBuildingRequest _request,final @Mocked CopyDefinition _copy, final @Mocked ArtifactCoordinate _artifactCoordinate,final @Mocked ArtifactResult _artifactResult,final @Mocked Artifact _artifact) throws ArtifactResolverException, IOException, MojoExecutionException{
		
		
		final Path generatedFolder=Paths.get("target/tests/processClassesFromCopy_unexpected_failure");

		new Expectations() {{
			_copy.toCoordinate(); result=_artifactCoordinate; times=1;
			artifactResolver.resolveArtifact(_request, _artifactCoordinate); result=_artifactResult; times=1;
			_artifactResult.getArtifact(); result=new NullPointerException(); times=1;
		}};

		MojoExecutionException exception=Assertions.assertThrows(MojoExecutionException.class, () -> instance.downloadSource(_request, _copy, generatedFolder));
		Assertions.assertTrue(exception.getCause() instanceof NullPointerException);
	}
	
	class CopyClassesBaseImpl extends CopyClassesBase{

		public CopyClassesBaseImpl() {
			super();
		}
		public CopyClassesBaseImpl(final ArtifactResolver artifactResolver,final MavenSession session,final MavenProject project,final CopyDefinition[] copies,final String generatedSourceFolder) {
			super(artifactResolver, session, project, copies, generatedSourceFolder);
		}

	
		@Override
		public void execute() throws MojoExecutionException, MojoFailureException {
			throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
		}
	} 
}
