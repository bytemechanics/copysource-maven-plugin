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
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Mock;
import mockit.MockUp;
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
import org.bytemechanics.maven.plugin.copyclasses.enums.GeneratedFactory;
import org.bytemechanics.maven.plugin.copyclasses.enums.Scope;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

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
	static Stream<Arguments> generatePackageDataPack() {
	    return Stream.of(
			Arguments.of("com.mypackage.match.myclass","com/mypackage2/matched/true/myclass.java"),
			Arguments.of("com.mypackage.notmatch.myclass","com/mypackage/notmatch/myclass.java")
		);
	}
	
	@ParameterizedTest(name = "generateSources() for scope {0}")
	@EnumSource(Scope.class)
	public void generateSources(final Scope _scope,final @Mocked ProjectBuildingRequest _request) throws MojoExecutionException{
		
		final Properties projectProperties=new Properties();
		projectProperties.setProperty("project.build.sourceEncoding", "UTF-8");
		final Path expectedGeneratedFolder=Paths.get("target/tests/generateSources")
												.resolve(_scope.getFolder())
												.resolve(this.generatedSourceFolder);
		final Resource expectedResource=new Resource();
		expectedResource.setDirectory(expectedGeneratedFolder.resolve(CopyClassesBase.METAINF).toString());
		expectedResource.setTargetPath(CopyClassesBase.METAINF);

		final List<Resource> actualResource=new ArrayList<>();
		
		new Expectations() {{
			session.getProjectBuildingRequest(); result=_request;
			instance.getProject(); result=project;
			project.getProperties(); result=projectProperties;
			instance.generateSourcePath(_scope); result=expectedGeneratedFolder; times=1;
			
			instance.getCopies(); result=new CopyDefinition[]{copy1,copy2}; times=1;
			
			copy1.toString(); result="copy1"; times=1;
			instance.processClassesFromCopy((ProjectBuildingRequest)any, copy1, expectedGeneratedFolder, StandardCharsets.UTF_8); times=1;
			
			copy2.toString(); result="copy2"; times=1;
			instance.processClassesFromCopy((ProjectBuildingRequest)any, copy2, expectedGeneratedFolder, StandardCharsets.UTF_8); times=1;
			
			instance.createManifest(expectedGeneratedFolder, StandardCharsets.UTF_8); times=1;

			project.addResource(withCapture(actualResource)); times=1;
		}};

		instance.generateSources(_scope);
		
		Assertions.assertEquals(1,actualResource.size());
		Assertions.assertEquals(expectedResource.toString(),actualResource.get(0).toString());
	}
	
	@ParameterizedTest(name = "generateSourceFile() for class {0} should create {1}")
	@MethodSource("generatePackageDataPack")
	public void generateSourceFile(final String _class,final String _newFile){
		
		final Path myPath=Paths.get("first","second");
		final Path expected=myPath.resolve(_newFile);
		
		new Expectations() {{
			copy1.getFromPackageRegex(); result="com\\.mypackage\\.match";
			copy1.getToPackage(); result="com.mypackage2.matched.true";
		}};
		Optional<Path> actual=instance.generateSourceFile(myPath, _class, copy1);
		Assertions.assertTrue(actual.isPresent());
		Assertions.assertEquals(expected, actual.get());
	}
	@Test
	@DisplayName("generatePackage() for null class should return empty Optional<Path>")
	public void generateSourceFile_null(){
		
		final Path myPath=Paths.get("first","second");
		
		Optional<Path> actual=instance.generateSourceFile(myPath, null, copy1);
		Assertions.assertFalse(actual.isPresent());
	}

	@ParameterizedTest(name = "generateSourcePath() for scope {0}")
	@EnumSource(Scope.class)
	public void generateSourcePath(final Scope _scope,final @Mocked ProjectBuildingRequest _request,final @Mocked Build _build) throws MojoExecutionException{
		
		
		final Path expectedGeneratedFolder=Paths.get("target/tests/generateSourcePath")
												.resolve(_scope.getFolder())
												.resolve(this.generatedSourceFolder);

		new Expectations() {{
			project.getBuild(); result=_build;
			_build.getDirectory(); result="target/tests/generateSourcePath";
		}};

		Assertions.assertEquals(expectedGeneratedFolder,instance.generateSourcePath(_scope));
		Assertions.assertTrue(Files.exists(expectedGeneratedFolder));
		Assertions.assertTrue(Files.isDirectory(expectedGeneratedFolder));
	}
	@ParameterizedTest(name = "generateSourcePath() for scope {0} should raise MojoExecutionException when can not create folders")
	@EnumSource(Scope.class)
	@SuppressWarnings("ThrowableResultIgnored")
	public void generateSourcePath_failure(final Scope _scope,final @Mocked ProjectBuildingRequest _request,final @Mocked Build _build) throws MojoExecutionException{
		
		
		new Expectations() {{
			project.getBuild(); result=new IOException("Unable to create folders");
		}};

		Assertions.assertThrows(MojoExecutionException.class,() -> instance.generateSourcePath(_scope));
	}

	@Test
	@DisplayName("processClassesFromCopy() success execution")
	public void processClassesFromCopy(final @Mocked ProjectBuildingRequest _request,final @Mocked CopyDefinition _copy, final @Mocked ArtifactCoordinate _artifactCoordinate,final @Mocked ArtifactResult _artifactResult,final @Mocked Artifact _artifact) throws ArtifactResolverException, IOException, MojoExecutionException{
		
		
		final Path generatedFolder=Paths.get("target/tests/processClassesFromCopy");
		final File fileCopy=new File("myFileFromCopy");

		new Expectations() {{
			_copy.toCoordinate(); result=_artifactCoordinate; times=1;
			artifactResolver.resolveArtifact(_request, _artifactCoordinate); result=_artifactResult; times=1;
			_artifactResult.getArtifact(); result=_artifact; times=1;
			_artifact.toString(); result="copy:artifact";
			_artifact.getFile(); result=fileCopy; times=1;
			instance.processDownloadedSource(fileCopy, _copy, generatedFolder, StandardCharsets.UTF_8); times=1;
		}};

		instance.processClassesFromCopy(_request, _copy, generatedFolder, StandardCharsets.UTF_8);
	}
	@Test
	@DisplayName("processClassesFromCopy() processing failure should resend MojoExecutionException")
	@SuppressWarnings("ThrowableResultIgnored")
	public void processClassesFromCopy_processing_failure(final @Mocked ProjectBuildingRequest _request,final @Mocked CopyDefinition _copy, final @Mocked ArtifactCoordinate _artifactCoordinate,final @Mocked ArtifactResult _artifactResult,final @Mocked Artifact _artifact) throws ArtifactResolverException, IOException, MojoExecutionException{
		
		
		final Path generatedFolder=Paths.get("target/tests/processClassesFromCopy_processing_failure");
		final File fileCopy=new File("myFileFromCopy");
		final MojoExecutionException exception=new MojoExecutionException("myFailure");

		new Expectations() {{
			_copy.toCoordinate(); result=_artifactCoordinate; times=1;
			artifactResolver.resolveArtifact(_request, _artifactCoordinate); result=_artifactResult; times=1;
			_artifactResult.getArtifact(); result=_artifact; times=1;
			_artifact.toString(); result="copy:artifact";
			_artifact.getFile(); result=fileCopy; times=1;
			instance.processDownloadedSource(fileCopy, _copy, generatedFolder, StandardCharsets.UTF_8); result=exception; times=1;
		}};

		Assertions.assertThrows(MojoExecutionException.class, () -> instance.processClassesFromCopy(_request, _copy, generatedFolder, StandardCharsets.UTF_8)
								,"myFailure") ;
	}
	@Test
	@DisplayName("processClassesFromCopy() unexpected failure should raise MojoExecutionException")
	@SuppressWarnings("ThrowableResultIgnored")
	public void processClassesFromCopy_unexpected_failure(final @Mocked ProjectBuildingRequest _request,final @Mocked CopyDefinition _copy, final @Mocked ArtifactCoordinate _artifactCoordinate,final @Mocked ArtifactResult _artifactResult,final @Mocked Artifact _artifact) throws ArtifactResolverException, IOException, MojoExecutionException{
		
		
		final Path generatedFolder=Paths.get("target/tests/processClassesFromCopy_unexpected_failure");

		new Expectations() {{
			_copy.toCoordinate(); result=_artifactCoordinate; times=1;
			artifactResolver.resolveArtifact(_request, _artifactCoordinate); result=_artifactResult; times=1;
			_artifactResult.getArtifact(); result=new NullPointerException(); times=1;
		}};

		MojoExecutionException exception=Assertions.assertThrows(MojoExecutionException.class, () -> instance.processClassesFromCopy(_request, _copy, generatedFolder, StandardCharsets.UTF_8));
		Assertions.assertTrue(exception.getCause() instanceof NullPointerException);
	}
	
	@Test
	@DisplayName("createManifest() success execution")
	public void createManifest() throws MojoExecutionException, IOException{
		
		final Path generatedFolder=Paths.get("target/tests/createManifest");
		Files.createDirectories(generatedFolder);
		final Path actualManifestPath=generatedFolder.resolve(CopyClassesBase.METAINF).resolve("copy-manifest.info");
		final String expected="The following classes has been copied from external libraries:\n" +
								"\n" +
								"From artifact [myFirstArtifact]:\n" +
								"	[first.destiny.package.class1] repackaged from [my.first.original.package.class1]\n" +
								"	[first.destiny.package.class2] repackaged from [my.first.original.package.class2]\n" +
								"	[first.destiny.package.class3] repackaged from [my.first.original.package.class3]\n" +
								"From artifact [mySecondArtifact]:\n" +
								"	[second.destiny.package.class1] repackaged from [my.second.original.package.class1]\n" +
								"	[second.destiny.package.class2] repackaged from [my.second.original.package.class2]\n" +
								"	[second.destiny.package.class3] repackaged from [my.second.original.package.class3]\n";

		new Expectations() {{
			copy1.getArtifact(); result="myFirstArtifact"; times=1;
			copy1.getClasses(); result=new String[]{"my.first.original.package.class1","my.first.original.package.class2","my.first.original.package.class3"}; times=1;
			copy1.getFromPackage(); result="my.first.original.package"; times=3;
			copy1.getToPackage(); result="first.destiny.package"; times=3;
			copy2.getArtifact(); result="mySecondArtifact"; times=1;
			copy2.getClasses(); result=new String[]{"my.second.original.package.class1","my.second.original.package.class2","my.second.original.package.class3"}; times=1;
			copy2.getFromPackage(); result="my.second.original.package"; times=3;
			copy2.getToPackage(); result="second.destiny.package"; times=3;
		}};

		instance.createManifest(generatedFolder,StandardCharsets.UTF_8);
		String actual=new String(Files.readAllBytes(actualManifestPath),StandardCharsets.UTF_8);
		Assertions.assertEquals(expected, actual);
	}
	@Test
	@DisplayName("createManifest() unable to write should raise MojoExecutionException")
	@SuppressWarnings("ThrowableResultIgnored")
	public void createManifest_unabletoWrite() throws IOException, MojoExecutionException{
		
		final Path generatedFolder=Paths.get("target/tests/createManifest_unabletoWrite");
		new Expectations() {{
			copy1.getArtifact(); result=new IOException("exception");
		}};

		Assertions.assertThrows(MojoExecutionException.class,() -> instance.createManifest(generatedFolder,StandardCharsets.UTF_8),"Unable create manifest file");
	}
	
	@Test
	@DisplayName("processDownloadedSource() for should return MojoExecutionException if zip failure happens")
	@SuppressWarnings("ThrowableResultIgnored")
	public void processDownloadedSource_jar_failure(final @Mocked CopyDefinition _copy) throws IOException, MojoExecutionException{
	
		final File file=Paths.get("src/test/resources/files/original/LambdaUnchecker.javacode").toFile();
		final Path generatedSourcesPath=Paths.get("target/tests/processDownloadedSource_zip_failure"); 
		final Charset sourceEncoding=StandardCharsets.UTF_8;

		MojoExecutionException exception=Assertions.assertThrows(MojoExecutionException.class,() -> instance.processDownloadedSource(file, _copy, generatedSourcesPath, sourceEncoding));
		Assertions.assertTrue(exception.getCause() instanceof ZipException);
	}
	@Test
	@DisplayName("processDownloadedSource() for should return MojoExecutionException if copy failure happens")
	@SuppressWarnings("ThrowableResultIgnored")
	public void processDownloadedSource_copy_failure(final @Mocked CopyDefinition _copy) throws IOException, MojoExecutionException{
	
		final File file=Paths.get("src/test/resources/files/fakeJar.jar").toFile();
		final Path generatedSourcesPath=Paths.get("target/tests/processDownloadedSource_copy_failure"); 
		final Charset sourceEncoding=StandardCharsets.UTF_8;

		new Expectations() {{
			_copy.getClasses(); result=new String[]{"com.notfound.generate.failure"};
		}};
		MojoExecutionException exception=Assertions.assertThrows(MojoExecutionException.class,() -> instance.processDownloadedSource(file, _copy, generatedSourcesPath, sourceEncoding));
		Assertions.assertTrue(exception.getCause() instanceof MojoExecutionException);
	}	
	@Test
	@DisplayName("processDownloadedSource() for should return MojoExecutionException if copy failure happens")
	@SuppressWarnings("ThrowableResultIgnored")
	public void processDownloadedSource(final @Mocked CopyDefinition _copy) throws IOException, MojoExecutionException{
	
		final File file=Paths.get("src/test/resources/files/fakeJar.jar").toFile();
		final Path generatedSourcesPath=Paths.get("target/tests/processDownloadedSource"); 
		final Path generatedSourcesQueuePath=generatedSourcesPath.resolve("FastDropLastQueue.java"); 
		final Path generatedSourcesArrayPath=generatedSourcesPath.resolve("ArrayUtils.java"); 
		final Path generatedSourcesFigletPath=generatedSourcesPath.resolve("Figlet.java"); 
		final Charset sourceEncoding=StandardCharsets.UTF_8;

		new Expectations() {{
			_copy.getClasses(); result=new String[]{"org.bytemechanics.commons.collections.FastDropLastQueue","org.bytemechanics.commons.lang.ArrayUtils","org.bytemechanics.commons.string.Figlet"};
			instance.generateSourceFile(generatedSourcesPath, "org.bytemechanics.commons.collections.FastDropLastQueue", _copy); result=Optional.of(generatedSourcesQueuePath); times=1;
			instance.generatePackage(generatedSourcesQueuePath); times=1;
			instance.copySource((JarFile)any,(JarEntry)any,_copy,generatedSourcesQueuePath,sourceEncoding,file,"org.bytemechanics.commons.collections.FastDropLastQueue"); times=1;
			instance.generateSourceFile(generatedSourcesPath, "org.bytemechanics.commons.lang.ArrayUtils", _copy); result=Optional.of(generatedSourcesArrayPath); times=1;
			instance.generatePackage(generatedSourcesArrayPath); times=1;
			instance.copySource((JarFile)any,(JarEntry)any,_copy,generatedSourcesArrayPath,sourceEncoding,file,"org.bytemechanics.commons.lang.ArrayUtils"); times=1;
			instance.generateSourceFile(generatedSourcesPath, "org.bytemechanics.commons.string.Figlet", _copy); result=Optional.of(generatedSourcesFigletPath); times=1;
			instance.generatePackage(generatedSourcesFigletPath); times=1;
			instance.copySource((JarFile)any,(JarEntry)any,_copy,generatedSourcesFigletPath,sourceEncoding,file,"org.bytemechanics.commons.string.Figlet"); times=1;
		}};
		instance.processDownloadedSource(file, _copy, generatedSourcesPath, sourceEncoding);
	}		
	
	@Test
	@DisplayName("generatePackage() for should generate the folders")
	public void generatePackage() throws MojoExecutionException{
		
		final Path myPath=Paths.get("first","second");
		AtomicBoolean passed=new AtomicBoolean(false);
		
		new MockUp<Files>() {           
			
            @Mock
            public Path createDirectories(final Path _path,FileAttribute<?>... _attrs){
				passed.set(true);
                return _path;
            }
        };
		instance.generatePackage(myPath);
		Assertions.assertTrue(passed.get());
	}
	@Test
	@DisplayName("generatePackage() for should raise MojoExecutionException if can not create the package")
	@SuppressWarnings("ThrowableResultIgnored")
	public void generatePackage_failure(){
		
		final Path myPath=Paths.get("first","second","myfile.java");
		
		new MockUp<Files>() {
            @Mock
            public Path createDirectories(final Path _path,FileAttribute<?>... _attrs) throws IOException{
                throw new IOException("unable to create folders");
            }
        };
		Assertions.assertThrows(MojoExecutionException.class
								, () -> instance.generatePackage(myPath)
								,"Failed creating new package for file: first/second/myfile.java}");
	}
	
	@ParameterizedTest(name = "getGeneratedFactory() for line {0} verification")
	@EnumSource(GeneratedFactory.class)
	public void getGeneratedFactory(final GeneratedFactory _generatedFactory){
		
		final Properties configuredProperties=new Properties();
		configuredProperties.setProperty("maven.compiler.target",_generatedFactory.version);
		
		new Expectations() {{
			project.getProperties(); result=configuredProperties; times=1;
		}};
		Assertions.assertSame(_generatedFactory, instance.getGeneratedFactory());
	}
	
	static Stream<Arguments> copySourceDataPack() {
	    return Stream.of(
			Arguments.of("org.bytemechanics.commons.functional.LambdaUnchecker","com/mypackage2/matched/true/LambdaUnchecker.java"),
			Arguments.of("org.bytemechanics.maven.plugin.copyclasses.enums.Scope","org/bytemechanics/maven/plugin/copyclasses/enums/Scope.java")
		);
	}
	@ParameterizedTest(name = "copySource() for file {0} should generate a file with @Generated annotation with the package changed at {1}")
	@MethodSource("copySourceDataPack")
	@SuppressWarnings({"CallToPrintStackTrace", "CallToPrintStackTrace", "ThrowableResultIgnored", "ThrowableResultIgnored"})
	public void copySource(final String _clazz,final String _target,final @Mocked GeneratedFactory _generatedfactory) throws MojoExecutionException, IOException {
		
		final Path originalFolder=Paths.get("src/test/resources/files/original");
		final Path expectedFolder=Paths.get("src/test/resources/files/expected");
		final Path generatedFolder=Paths.get("target/tests/copySource");
		final String fileName=Paths.get(_target).getFileName().toString()+"code";
		final Path originalSourceFile=originalFolder.resolve(fileName);
		final Path expectedSourceFile=expectedFolder.resolve(fileName);
		final Path generatedSourceFile=generatedFolder.resolve(_target);
		Files.createDirectories(generatedSourceFile.getParent());
		
		
		class JarFileMock extends JarFile{

			public JarFileMock(final File _file) throws IOException {
				super(_file);
			}

			@Override
			public synchronized InputStream getInputStream(ZipEntry ze) throws IOException {
				return Files.newInputStream(originalSourceFile);
			}
		}
		
		new Expectations() {{
			instance.getGeneratedFactory(); result=_generatedfactory;
			_generatedfactory.getAnnotation(copy1,(LocalDateTime) any); result="@Generated(value=\"generated-by-copy-plugin\", comments = \"Copied from org.bytemechanics:copy-commons:jar:sources:1.5.0\", date = \"2020-03-04T17:11:49.805\")\n";
			_generatedfactory.getImport(); result="import javax.annotation.Generated;\n";
			copy1.getFromPackageRegex(); result="org\\.bytemechanics\\.commons\\.functional";
			copy1.getToPackage(); result="com.mypackage2.matched.true";
			copy1.getSourceCharset(); result="UTF-8";
		}};
		
		final Path fakejar=Paths.get("src/test/resources/files/fakeJar.jar");	
		final File file=fakejar.toFile();
		try(JarFileMock jarFile=new JarFileMock(file)){
			instance.copySource(jarFile, new JarEntry("none"), copy1, generatedSourceFile, StandardCharsets.UTF_8, originalSourceFile.toFile(), _clazz);
			Assertions.assertEquals(Files.readAllLines(expectedSourceFile,StandardCharsets.UTF_8),Files.readAllLines(generatedSourceFile,StandardCharsets.UTF_8));
		}catch(Exception e){
			e.printStackTrace();
			Assertions.fail("Should not raise an exception here",e);
		}
	}
	@Test
	@DisplayName("copySource() with non readable source file should raise MojoExecutionException")
	@SuppressWarnings({"CallToPrintStackTrace", "CallToPrintStackTrace", "ThrowableResultIgnored", "ThrowableResultIgnored"})
	public void copySource_read_failure() throws MojoExecutionException, IOException {
		
		final String clazz="org.bytemechanics.commons.functional.LambdaUnchecker";
		final String target="com/mypackage2/matched/true/LambdaUnchecker.java";
		final Path originalFolder=Paths.get("src/test/resources/files/original");
		final Path generatedFolder=Paths.get("target/tests/copySource_read_failure");
		final String fileName=Paths.get(target).getFileName().toString()+"code";
		final Path originalSourceFile=originalFolder.resolve(fileName);
		final Path generatedSourceFile=generatedFolder.resolve(target);
		Files.createDirectories(generatedSourceFile.getParent());
		
		
		class JarFileMock extends JarFile{

			public JarFileMock(final File _file) throws IOException {
				super(_file);
			}

			@Override
			public synchronized InputStream getInputStream(ZipEntry ze) throws IOException {
				throw new IOException("input not readable");
			}
		}
		
		new Expectations() {{
			project.getProperties(); result=new Properties();
			copy1.getSourceCharset(); result="UTF-8";
		}};
		
		final Path fakejar=Paths.get("src/test/resources/files/fakeJar.jar");	
		final File file=fakejar.toFile();
		try(JarFileMock jarFile=new JarFileMock(file)){
			Assertions.assertThrows(MojoExecutionException.class,() -> instance.copySource(jarFile, new JarEntry("none"), copy1, generatedSourceFile, StandardCharsets.UTF_8, originalSourceFile.toFile(), clazz));
		}catch(Exception e){
			e.printStackTrace();
			Assertions.fail("Should not raise an exception here",e);
		}
	}
	@Test
	@DisplayName("copySource() with non valid charset should raise MojoExecutionException")
	@SuppressWarnings({"CallToPrintStackTrace", "CallToPrintStackTrace", "ThrowableResultIgnored", "ThrowableResultIgnored"})
	public void copySource_charset_failure() throws MojoExecutionException, IOException {
		
		final String clazz="org.bytemechanics.commons.functional.LambdaUnchecker";
		final String target="com/mypackage2/matched/true/LambdaUnchecker.java";
		final Path originalFolder=Paths.get("src/test/resources/files/original");
		final Path generatedFolder=Paths.get("target/tests/copySource_charset_failure");
		final String fileName=Paths.get(target).getFileName().toString()+"code";
		final Path originalSourceFile=originalFolder.resolve(fileName);
		final Path generatedSourceFile=generatedFolder.resolve(target);
		Files.createDirectories(generatedSourceFile.getParent());
		
		
		class JarFileMock extends JarFile{

			public JarFileMock(final File _file) throws IOException {
				super(_file);
			}

			@Override
			public synchronized InputStream getInputStream(ZipEntry ze) throws IOException {
				return Files.newInputStream(originalSourceFile);
			}
		}
		
		new Expectations() {{
			project.getProperties(); result=new Properties();
			copy1.getSourceCharset(); result="my-failure-charset";
		}};
		
		final Path fakejar=Paths.get("src/test/resources/files/fakeJar.jar");	
		final File file=fakejar.toFile();
		try(JarFileMock jarFile=new JarFileMock(file)){
			Assertions.assertThrows(MojoExecutionException.class,() -> instance.copySource(jarFile, new JarEntry("none"), copy1, generatedSourceFile, StandardCharsets.UTF_8, originalSourceFile.toFile(), clazz));
		}catch(Exception e){
			e.printStackTrace();
			Assertions.fail("Should not raise an exception here",e);
		}
	}	
	@Test
	@DisplayName("copySource() with unsuported charset should raise MojoExecutionException")
	@SuppressWarnings({"CallToPrintStackTrace", "CallToPrintStackTrace", "ThrowableResultIgnored", "ThrowableResultIgnored"})
	public void copySource_charset_unsuported_failure() throws MojoExecutionException, IOException {
		
		final String clazz="org.bytemechanics.commons.functional.LambdaUnchecker";
		final String target="com/mypackage2/matched/true/LambdaUnchecker.java";
		final Path originalFolder=Paths.get("src/test/resources/files/original");
		final Path generatedFolder=Paths.get("target/tests/copySource_charset_unsuported_failure");
		final String fileName=Paths.get(target).getFileName().toString()+"code";
		final Path originalSourceFile=originalFolder.resolve(fileName);
		final Path generatedSourceFile=generatedFolder.resolve(target);
		Files.createDirectories(generatedSourceFile.getParent());
		
		
		class JarFileMock extends JarFile{

			public JarFileMock(final File _file) throws IOException {
				super(_file);
			}

			@Override
			public synchronized InputStream getInputStream(ZipEntry ze) throws IOException {
				return Files.newInputStream(originalSourceFile);
			}
		}
		
		new Expectations() {{
			project.getProperties(); result=new Properties();
			copy1.getSourceCharset(); result="UTF-128";
		}};
		
		final Path fakejar=Paths.get("src/test/resources/files/fakeJar.jar");	
		final File file=fakejar.toFile();
		try(JarFileMock jarFile=new JarFileMock(file)){
			Assertions.assertThrows(MojoExecutionException.class,() -> instance.copySource(jarFile, new JarEntry("none"), copy1, generatedSourceFile, StandardCharsets.UTF_8, originalSourceFile.toFile(), clazz));
		}catch(Exception e){
			e.printStackTrace();
			Assertions.fail("Should not raise an exception here",e);
		}
	}
	
	static Stream<Arguments> isPackageDataPack() {
	    return Stream.of(
			Arguments.of("package myPackage",true),
			Arguments.of("myPackage",false),
			Arguments.of("",false)
		);
	}
	@ParameterizedTest(name = "isPackage() for line {0} should result as {1}")
	@MethodSource("isPackageDataPack")
	public void isPackage(final String _line,final boolean _result){
		Assertions.assertEquals(_result, instance.isPackage(_line));
	}

	static Stream<Arguments> isBeginCommentDataPack() {
	    return Stream.of(
			Arguments.of("/*fdsfds",true),
			Arguments.of("package /* myPackage",true),
			Arguments.of("myPackage/*",true),
			Arguments.of("fdsfds",false),
			Arguments.of("*/fdsfds",false),
			Arguments.of("",false)			
		);
	}
	@ParameterizedTest(name = "isBeginComment() for line {0} should result as {1}")
	@MethodSource("isBeginCommentDataPack")
	public void isBeginComment(final String _line,final boolean _result){
		Assertions.assertEquals(_result, instance.isBeginComment(_line));
	}

	static Stream<Arguments> isEndCommentDataPack() {
	    return Stream.of(
			Arguments.of("*/fdsfds",true),
			Arguments.of("package */ myPackage",true),
			Arguments.of("myPackage*/",true),
			Arguments.of("fdsfds",false),
			Arguments.of("/*fdsfds",false),
			Arguments.of("",false)
		);
	}
	@ParameterizedTest(name = "isEndComment() for line {0} should result as {1}")
	@MethodSource("isEndCommentDataPack")
	public void isEndComment(final String _line,final boolean _result){
		Assertions.assertEquals(_result, instance.isEndComment(_line));
	}

	static Stream<Arguments> isMainTypeDefinitionDataPack() {
	    return Stream.of(
			Arguments.of("class myPackage",true),
			Arguments.of("public class ",false),
			Arguments.of("public class myPackage",true),
			Arguments.of("publicclass myPackage",false),
			Arguments.of("classpublic myPackage",false),
			
			Arguments.of("interface myPackage",true),
			Arguments.of("public interface ",false),
			Arguments.of("public interface myPackage",true),
			Arguments.of("publicinterface myPackage",false),
			Arguments.of("public interfacemyPackage",false),

			Arguments.of("@interface myPackage",true),
			Arguments.of("public @interface ",false),
			Arguments.of("public @interface myPackage",true),
			Arguments.of("public@interface myPackage",false),
			Arguments.of("public @interfacemyPackage",false),

			Arguments.of("enum myPackage",true),
			Arguments.of("public enum ",false),
			Arguments.of("public enum myPackage",true),
			Arguments.of("publicenum myPackage",false),
			Arguments.of("public enummyPackage",false)
		);
	}
	@ParameterizedTest(name = "isMainTypeDefinition() for line {0} should result as {1}")
	@MethodSource("isMainTypeDefinitionDataPack")
	public void isMainTypeDefinition(final String _line,final boolean _result){
		Assertions.assertEquals(_result, instance.isMainTypeDefinition(_line));
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
