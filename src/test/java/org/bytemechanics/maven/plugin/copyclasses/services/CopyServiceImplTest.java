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
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.stream.Stream;
import java.util.zip.ZipException;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;
import mockit.Tested;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.shared.transfer.artifact.ArtifactCoordinate;
import org.bytemechanics.maven.plugin.copyclasses.beans.CopyDefinition;
import org.bytemechanics.maven.plugin.copyclasses.enums.Scope;
import org.bytemechanics.maven.plugin.copyclasses.mocks.LogMock;
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
public class CopyServiceImplTest {
	
	@BeforeAll
	public static void setup() throws IOException{
		System.out.println(">>>>> CopyServiceImplTest >>>> setupSpec");
		try(InputStream inputStream = CopyServiceImplTest.class.getResourceAsStream("/logging.properties")){
			LogManager.getLogManager().readConfiguration(inputStream);
		}catch (final IOException e){
			Logger.getAnonymousLogger().severe("Could not load default logging.properties file");
			Logger.getAnonymousLogger().severe(e.getMessage());
		}
	}

	@BeforeEach
    void beforeEachTest(final TestInfo testInfo) {
        System.out.println(">>>>> "+this.getClass().getSimpleName()+" >>>> "+testInfo.getTestMethod().map(Method::getName).orElse("Unkown")+""+testInfo.getTags().toString()+" >>>> "+testInfo.getDisplayName());
	}

	@Injectable()
	Log logger=new LogMock(CopyServiceImpl.class,LogMock.Level.DEBUG);
	@Injectable( "target/generated-sources")
	String _targetFolder;
	@Injectable("copies")
	String _generatedSourceFolder;
	@Injectable
	Charset _encoding=StandardCharsets.UTF_8;
	
	
	@Tested
	@Mocked
	CopyServiceImpl instance;
	
	static Stream<Arguments> generatePackageDataPack() {
	    return Stream.of(
			Arguments.of("com.mypackage.match.myclass","com/mypackage2/matched/true/myclass.java"),
			Arguments.of("com.mypackage.notmatch.myclass","com/mypackage/notmatch/myclass.java")
		);
	}
	@ParameterizedTest(name = "generateSourceFile() for class {0} should create {1}")
	@MethodSource("generatePackageDataPack")
	public void generateSourceFile(final String _class,final String _newFile,final @Mocked CopyDefinition _copy){
		
		final Path myPath=Paths.get("first","second");
		final Path expected=myPath.resolve(_newFile);
		
		new Expectations() {{
			_copy.getFromPackageRegex(); result="com\\.mypackage\\.match";
			_copy.getToPackage(); result="com.mypackage2.matched.true";
		}};
		Optional<Path> actual=instance.generateSourceFile(myPath, _class, _copy);
		Assertions.assertTrue(actual.isPresent());
		Assertions.assertEquals(expected, actual.get());
	}
	@Test
	@DisplayName("generatePackage() for null class should return empty Optional<Path>")
	public void generateSourceFile_null(final @Mocked CopyDefinition _copy){
		
		final Path myPath=Paths.get("first","second");
		
		Optional<Path> actual=instance.generateSourceFile(myPath, null, _copy);
		Assertions.assertFalse(actual.isPresent());
	}

	@ParameterizedTest(name = "generateSourcePath() for scope {0}")
	@EnumSource(Scope.class)
	public void generateSourcePath(final Scope _scope) throws MojoExecutionException{
		
		
		final Path expectedGeneratedFolder=Paths.get("target/tests/generateSourcePath")
												.resolve(_scope.getFolder())
												.resolve("copies");

		new Expectations() {{
			instance.getTargetFolder(); result="target/tests/generateSourcePath";
		}};

		Assertions.assertEquals(expectedGeneratedFolder,instance.generateSourcePath(_scope));
		Assertions.assertTrue(Files.exists(expectedGeneratedFolder));
		Assertions.assertTrue(Files.isDirectory(expectedGeneratedFolder));
	}
	@ParameterizedTest(name = "generateSourcePath() for scope {0} should raise MojoExecutionException when can not create folders")
	@EnumSource(Scope.class)
	@SuppressWarnings("ThrowableResultIgnored")
	public void generateSourcePath_failure(final Scope _scope) throws MojoExecutionException{
		
		
		new Expectations() {{
			instance.getTargetFolder(); result=new IOException("Unable to create folders");
		}};

		Assertions.assertThrows(MojoExecutionException.class,() -> instance.generateSourcePath(_scope));
	}
	
	@Test
	@DisplayName("createManifest() success execution")
	public void createManifest(final @Mocked CopyDefinition _copy1,final @Mocked CopyDefinition _copy2) throws MojoExecutionException, IOException{
		
		final CopyDefinition[] copies=new CopyDefinition[]{_copy1,_copy2};
		final Path generatedFolder=Paths.get("target/tests/createManifest");
		Files.createDirectories(generatedFolder);
		final Path actualManifestPath=generatedFolder.resolve(CopyServiceImpl.METAINF).resolve("copy-manifest.info");
		final String expected="The following classes has been copied from external libraries:\n" +
								"\n" +
								"From artifact [myFirstArtifact]:\n" +
								"	[first.destiny.package.class1] repackaged from [my.first.original.package.class1]\n" +
								"	[first.destiny.package.class2] repackaged from [my.first.original.package.class2]\n" +
								"	[first.destiny.package.class3] repackaged from [my.first.original.package.class3]\n" +
								"From artifact [mySecondArtifact]:\n" +
								"	[second.destiny.package.class1] repackaged from [my.second.original.package.class1]\n" +
								"	[second.destiny.package.class2] repackaged from [my.second.original.package.class2]\n" +
								"	[second.destiny.package.class3] repackaged from [my.second.original.package.class3]\n" +
								"From artifact [org.bytemechanics.maven:copysource-maven-plugin:"+this.getClass().getPackage().getImplementationVersion()+"]:\n" +
								"	["+CopyServiceImpl.CUSTOM_ANNOTATION_CLASS+"] generated";
		new Expectations() {{
			_copy1.getArtifact(); result="myFirstArtifact"; minTimes=1;
			_copy1.getClasses(); result=new String[]{"my.first.original.package.class1","my.first.original.package.class2","my.first.original.package.class3"}; minTimes=1;
			_copy1.getFromPackage(); result="my.first.original.package"; minTimes=1;
			_copy1.getToPackage(); result="first.destiny.package"; minTimes=1;
			_copy2.getArtifact(); result="mySecondArtifact"; minTimes=1;
			_copy2.getClasses(); result=new String[]{"my.second.original.package.class1","my.second.original.package.class2","my.second.original.package.class3"}; minTimes=1;
			_copy2.getFromPackage(); result="my.second.original.package"; minTimes=1;
			_copy2.getToPackage(); result="second.destiny.package"; minTimes=1;
		}};

		instance.createManifest(copies,generatedFolder);
		String actual=new String(Files.readAllBytes(actualManifestPath),StandardCharsets.UTF_8);
		Assertions.assertEquals(expected, actual);
	}
	@Test
	@DisplayName("createManifest() unable to write should raise MojoExecutionException")
	@SuppressWarnings("ThrowableResultIgnored")
	public void createManifest_unabletoWrite(final @Mocked CopyDefinition _copy1,final @Mocked CopyDefinition _copy2) throws IOException, MojoExecutionException{
		
		final CopyDefinition[] copies=new CopyDefinition[]{_copy1,_copy2};
		final Path generatedFolder=Paths.get("target/tests/createManifest_unabletoWrite");
		new Expectations() {{
			_copy1.getArtifact(); result=new IOException("exception");
		}};

		Assertions.assertThrows(MojoExecutionException.class,() -> instance.createManifest(copies, generatedFolder),"Unable create manifest file");
	}
	
	@Test
	@DisplayName("processDownloadedSource() for should return MojoExecutionException if zip failure happens")
	@SuppressWarnings("ThrowableResultIgnored")
	public void processDownloadedSource_jar_failure(final @Mocked CopyDefinition _copy) throws IOException, MojoExecutionException{
	
		final Path downloadedFile=Paths.get("src/test/resources/files/original/LambdaUnchecker.javacode");
		final Path generatedSourcesPath=Paths.get("target/tests/processDownloadedSource_zip_failure"); 

		MojoExecutionException exception=Assertions.assertThrows(MojoExecutionException.class,() -> instance.processDownloadedSource(downloadedFile, _copy, generatedSourcesPath));
		Assertions.assertTrue(exception.getCause() instanceof ZipException);
	}
	@Test
	@DisplayName("processDownloadedSource() for should return MojoExecutionException if copy failure happens")
	@SuppressWarnings("ThrowableResultIgnored")
	public void processDownloadedSource_copy_failure(final @Mocked CopyDefinition _copy) throws IOException, MojoExecutionException{
	
		final Path downloadedFile=Paths.get("src/test/resources/files/fakeJar.jar");
		final Path generatedSourcesPath=Paths.get("target/tests/processDownloadedSource_copy_failure"); 

		new Expectations() {{
			_copy.getClasses(); result=new String[]{"com.notfound.generate.failure"};
		}};
		MojoExecutionException exception=Assertions.assertThrows(MojoExecutionException.class,() -> instance.processDownloadedSource(downloadedFile, _copy, generatedSourcesPath));
		Assertions.assertTrue(exception.getCause() instanceof MojoExecutionException);
	}	
	@Test
	@DisplayName("processDownloadedSource() for should return MojoExecutionException if copy failure happens")
	@SuppressWarnings("ThrowableResultIgnored")
	public void processDownloadedSource(final @Mocked CopyDefinition _copy) throws IOException, MojoExecutionException{
	
		final Path downloadedFile=Paths.get("src/test/resources/files/fakeJar.jar");
		final Path generatedSourcesPath=Paths.get("target/tests/processDownloadedSource"); 
		final Path generatedSourcesQueuePath=generatedSourcesPath.resolve("FastDropLastQueue.java"); 
		final Path generatedSourcesArrayPath=generatedSourcesPath.resolve("ArrayUtils.java"); 
		final Path generatedSourcesFigletPath=generatedSourcesPath.resolve("Figlet.java"); 

		new Expectations() {{
			_copy.getClasses(); result=new String[]{"org.bytemechanics.commons.collections.FastDropLastQueue","org.bytemechanics.commons.lang.ArrayUtils","org.bytemechanics.commons.string.Figlet"};
			instance.generateSourceFile(generatedSourcesPath, "org.bytemechanics.commons.collections.FastDropLastQueue", _copy); result=Optional.of(generatedSourcesQueuePath); times=1;
			instance.copySource((InputStream)any,generatedSourcesQueuePath,"org.bytemechanics.commons.collections.FastDropLastQueue",_copy,true); times=1;
			instance.generateSourceFile(generatedSourcesPath, "org.bytemechanics.commons.lang.ArrayUtils", _copy); result=Optional.of(generatedSourcesArrayPath); times=1;
			instance.copySource((InputStream)any,generatedSourcesArrayPath,"org.bytemechanics.commons.lang.ArrayUtils",_copy,true); times=1;
			instance.generateSourceFile(generatedSourcesPath, "org.bytemechanics.commons.string.Figlet", _copy); result=Optional.of(generatedSourcesFigletPath); times=1;
			instance.copySource((InputStream)any,generatedSourcesFigletPath,"org.bytemechanics.commons.string.Figlet",_copy,true); times=1;
		}};
		instance.processDownloadedSource(downloadedFile, _copy, generatedSourcesPath);
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
	
	@Test
	@DisplayName("Verify getAnnotation() is correctly generated as: @Generated(value=\"generated-by-copy-plugin\", comments = \"Copied from myGroupId:myArtifactId:myVersion:myClassifier\", date = \"***\")")
	public void getAnnotation(final @Mocked CopyDefinition _copy, final @Mocked ArtifactCoordinate _artifactCoordinate){
		System.out.println(">>> GeneratedFactoryTest >>> getAnnotation");
		final LocalDateTime executionTime=LocalDateTime.now();
		String expected="@CopiedSource(tool=\"org.bytemechanics.maven.copysource-maven-plugin\", toolVersion=\"null\", originGroupId=\"myGroupId\", originArtifactId=\"myArtifactId\", originVersion=\"myVersion\", originClassifier=\"myClassifier\", copyDate = \""+executionTime.toString()+"\")";

		new Expectations() {{
			_copy.toCoordinate(); result=_artifactCoordinate; times=1;
			_artifactCoordinate.getGroupId(); result="myGroupId"; times=1;
			_artifactCoordinate.getArtifactId(); result="myArtifactId"; times=1;
			_artifactCoordinate.getVersion(); result="myVersion"; times=1;
			_artifactCoordinate.getClassifier(); result="myClassifier"; times=1;
		}};
		
		final String actual=instance.getAnnotation(_copy, executionTime);
		Assertions.assertEquals(expected, actual);
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
	public void copySource(final String _clazz,final String _target,final @Mocked CopyDefinition _copy,final @Mocked ArtifactCoordinate _artifactCoordinate) throws MojoExecutionException, IOException {
		
		final Path originalFolder=Paths.get("src/test/resources/files/original");
		final Path expectedFolder=Paths.get("src/test/resources/files/expected");
		final Path generatedFolder=Paths.get("target/tests/copySource");
		final String fileName=Paths.get(_target).getFileName().toString()+"code";
		final Path originalSourceFile=originalFolder.resolve(fileName);
		final Path expectedSourceFile=expectedFolder.resolve(fileName);
		final Path generatedSourceFile=generatedFolder.resolve(_target);
		Files.createDirectories(generatedSourceFile.getParent());
		
		new Expectations() {{
			instance.getEncoding(); result=StandardCharsets.UTF_8;
			instance.getAnnotation(_copy,(LocalDateTime)any); result="@CopiedSource(tool=\"org.bytemechanics.maven.copysource-maven-plugin\", toolVersion=\"null\", originGroupId=\"org.bytemechanics\", originArtifactId=\"copy-commons\", originVersion=\"1.5.0\", originClassifier=\"null\", copyDate = \"2022-08-08T10:54:19.782697\")";
			_copy.getFromPackageRegex(); result="org\\.bytemechanics\\.commons\\.functional";
			_copy.getToPackage(); result="com.mypackage2.matched.true";
			_copy.getSourceCharset(); result="UTF-8";
		}};
		try(InputStream inputStream=Files.newInputStream(originalSourceFile)){
			instance.copySource(inputStream,generatedSourceFile,_clazz,_copy,true); 
			Assertions.assertEquals(Files.readAllLines(expectedSourceFile,StandardCharsets.UTF_8),Files.readAllLines(generatedSourceFile,StandardCharsets.UTF_8));
		}catch(Exception e){
			e.printStackTrace();
			Assertions.fail("Should not raise an exception here",e);
		}
	}
	@Test
	@DisplayName("copySource() with non readable source file should raise MojoExecutionException")
	@SuppressWarnings({"CallToPrintStackTrace", "CallToPrintStackTrace", "ThrowableResultIgnored", "ThrowableResultIgnored"})
	public void copySource_read_failure(final @Mocked CopyDefinition _copy,final @Mocked InputStream _istream) throws MojoExecutionException, IOException {
		
		final String clazz="org.bytemechanics.commons.functional.LambdaUnchecker";
		final String target="com/mypackage2/matched/true/LambdaUncheckerBinary.java";
		final Path originalFolder=Paths.get("src/test/resources/files/original");
		final Path generatedFolder=Paths.get("target/tests/copySource_read_failure");
		final String fileName=Paths.get(target).getFileName().toString()+"code";
		final Path originalSourceFile=originalFolder.resolve(fileName);
		final Path generatedSourceFile=generatedFolder.resolve(target);
		Files.createDirectories(generatedSourceFile.getParent());
		
		new Expectations() {{
			_copy.getSourceCharset(); result="UTF-8"; minTimes=0;
		}};
		
		try{
			Assertions.assertThrows(MojoExecutionException.class,() -> instance.copySource(_istream,originalSourceFile,clazz,_copy,true));
		}catch(Exception e){
			e.printStackTrace();
			Assertions.fail("Should not raise an exception here",e);
		}
	}
	@Test
	@DisplayName("copySource() with non valid charset should raise MojoExecutionException")
	@SuppressWarnings({"CallToPrintStackTrace", "CallToPrintStackTrace", "ThrowableResultIgnored", "ThrowableResultIgnored"})
	public void copySource_charset_failure(final @Mocked CopyDefinition _copy) throws MojoExecutionException, IOException {
		
		final String clazz="org.bytemechanics.commons.functional.LambdaUnchecker";
		final String target="com/mypackage2/matched/true/LambdaUnchecker.java";
		final Path originalFolder=Paths.get("src/test/resources/files/original");
		final Path generatedFolder=Paths.get("target/tests/copySource_charset_failure");
		final String fileName=Paths.get(target).getFileName().toString()+"code";
		final Path originalSourceFile=originalFolder.resolve(fileName);
		final Path generatedSourceFile=generatedFolder.resolve(target);
		Files.createDirectories(generatedSourceFile.getParent());
		
		
		new Expectations() {{
			_copy.getSourceCharset(); result="my-failure-charset";
		}};
		
		try(InputStream inputStream=Files.newInputStream(originalSourceFile)){
			Assertions.assertThrows(MojoExecutionException.class,() -> instance.copySource(inputStream,originalSourceFile,clazz,_copy,true));
		}catch(Exception e){
			e.printStackTrace();
			Assertions.fail("Should not raise an exception here",e);
		}
	}	
	@Test
	@DisplayName("copySource() with unsuported charset should raise MojoExecutionException")
	@SuppressWarnings({"CallToPrintStackTrace", "CallToPrintStackTrace", "ThrowableResultIgnored", "ThrowableResultIgnored"})
	public void copySource_charset_unsuported_failure(final @Mocked CopyDefinition _copy) throws MojoExecutionException, IOException {
		
		final String clazz="org.bytemechanics.commons.functional.LambdaUnchecker";
		final String target="com/mypackage2/matched/true/LambdaUnchecker.java";
		final Path originalFolder=Paths.get("src/test/resources/files/original");
		final Path generatedFolder=Paths.get("target/tests/copySource_charset_unsuported_failure");
		final String fileName=Paths.get(target).getFileName().toString()+"code";
		final Path originalSourceFile=originalFolder.resolve(fileName);
		final Path generatedSourceFile=generatedFolder.resolve(target);
		Files.createDirectories(generatedSourceFile.getParent());
		
		
		new Expectations() {{
			_copy.getSourceCharset(); result="UTF-128";
		}};
		
		try(InputStream inputStream=Files.newInputStream(originalSourceFile)){
			Assertions.assertThrows(MojoExecutionException.class,() -> instance.copySource(inputStream,originalSourceFile,clazz,_copy,true)); 
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
}
