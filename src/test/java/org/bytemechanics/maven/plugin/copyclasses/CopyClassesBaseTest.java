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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;
import mockit.Tested;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.transfer.artifact.resolve.ArtifactResolver;
import org.bytemechanics.maven.plugin.copyclasses.beans.CopyDefinition;
import org.bytemechanics.maven.plugin.copyclasses.enums.GeneratedFactory;
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
	@SuppressWarnings("CallToPrintStackTrace")
	public void copySource(final String _clazz,final String _target) throws MojoExecutionException, IOException {
		
		final Path originalFolder=Paths.get("src/test/resources/files/original");
		final Path expectedFolder=Paths.get("src/test/resources/files/expected");
		final Path generatedFolder=Paths.get("target/tests/copySource");
		final Path originalSourceFile=originalFolder.resolve(_target);
		final Path expectedSourceFile=expectedFolder.resolve(_target);
		final Path generatedSourceFile=generatedFolder.resolve(_target);
		Files.createDirectories(generatedSourceFile);
		
		
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
			copy1.getSourceCharset(); result="UTF-8";
		}};
		
		final Path fakejar=Paths.get("src/test/resources/files/fakeJar.jar");	
		final File file=fakejar.toFile();
		try(JarFileMock jarFile=new JarFileMock(file)){
			instance.copySource(jarFile, new JarEntry("none"), copy1, generatedSourceFile, StandardCharsets.UTF_8, originalSourceFile.toFile(), _clazz);
			Assertions.assertEquals(Files.readAllLines(expectedSourceFile,StandardCharsets.UTF_8),Files.readAllLines(generatedSourceFile,StandardCharsets.UTF_8));
		}catch(Exception e){
			e.printStackTrace();
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
