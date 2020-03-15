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
package org.bytemechanics.maven.plugin.copyclasses.beans;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.stream.Stream;
import org.apache.maven.shared.transfer.artifact.ArtifactCoordinate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * @author afarre
 */
public class CopyDefinitionTest {

	@BeforeAll
	public static void setup() throws IOException{
		System.out.println(">>>>> CopyDefinitionTest >>>> setupSpec");
		try(InputStream inputStream = CopyDefinitionTest.class.getResourceAsStream("/logging.properties")){
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
	

	@Test
	@DisplayName("getArtifact() should  be null if used empty constructor")
	public void getArtifact_null(){
		final CopyDefinition copy=new CopyDefinition();
		Assertions.assertNull(copy.getArtifact());
	}
	@Test
	@DisplayName("getArtifact() should be the same used in constructor")
	public void getArtifact_full(){
		final CopyDefinition copy=new CopyDefinition("myArtifact",new String[]{"myclass1","myclass2"},"mycharset","myFrompackage","myToPackage");
		Assertions.assertEquals("myArtifact",copy.getArtifact());
	}
	@Test
	@DisplayName("getArtifact() should be distinct from provided in constructor once replaced with setArtifact()")
	public void setArtifact(){
		final CopyDefinition copy=new CopyDefinition("myArtifact",new String[]{"myclass1","myclass2"},"mycharset","myFrompackage","myToPackage");
		Assertions.assertEquals("myArtifact",copy.getArtifact());
		copy.setArtifact("myArtifact2");
		Assertions.assertEquals("myArtifact2",copy.getArtifact());
	}

	@Test
	@DisplayName("getClasses() should be null if used empty constructor")
	public void getClasses_null(){
		final CopyDefinition copy=new CopyDefinition();
		Assertions.assertNull(copy.getClasses());
	}
	@Test
	@DisplayName("getClasses() should be the same used in constructor")
	public void getClasses_full(){
		final CopyDefinition copy=new CopyDefinition("myArtifact",new String[]{"myclass1","myclass2"},"mycharset","myFrompackage","myToPackage");
		Assertions.assertArrayEquals(new String[]{"myclass1","myclass2"},copy.getClasses());
	}
	@Test
	@DisplayName("getClasses() should be distinct from provided in constructor once replaced with setArtifact()")
	public void setClasses(){
		final CopyDefinition copy=new CopyDefinition("myArtifact",new String[]{"myclass1","myclass2"},"mycharset","myFrompackage","myToPackage");
		Assertions.assertArrayEquals(new String[]{"myclass1","myclass2"},copy.getClasses());
		copy.setClasses(new String[]{"myclass1bis","myclass2bis"});
		Assertions.assertArrayEquals(new String[]{"myclass1bis","myclass2bis"},copy.getClasses());
	}

	@Test
	@DisplayName("getSourceCharset() should be UTF-8 if used empty constructor")
	public void getSourceCharset_null(){
		final CopyDefinition copy=new CopyDefinition();
		Assertions.assertEquals("UTF-8",copy.getSourceCharset());
	}
	@Test
	@DisplayName("getSourceCharset() should be the same used in constructor")
	public void getSourceCharset_full(){
		final CopyDefinition copy=new CopyDefinition("myArtifact",new String[]{"myclass1","myclass2"},"mycharset","myFrompackage","myToPackage");
		Assertions.assertEquals("mycharset",copy.getSourceCharset());
	}
	@Test
	@DisplayName("getSourceCharset() should be distinct from provided in constructor once replaced with setArtifact()")
	public void setSourceCharset(){
		final CopyDefinition copy=new CopyDefinition("myArtifact",new String[]{"myclass1","myclass2"},"mycharset","myFrompackage","myToPackage");
		Assertions.assertEquals("mycharset",copy.getSourceCharset());
		copy.setSourceCharset("mycharset2");
		Assertions.assertEquals("mycharset2",copy.getSourceCharset());
	}

	@Test
	@DisplayName("getFromPackage() should be the same used in constructor")
	public void getFromPackage_null(){
		final CopyDefinition copy=new CopyDefinition();
		Assertions.assertNull(copy.getFromPackage());
	}
	@Test
	@DisplayName("getFromPackage() should be the same used in constructor")
	public void getFromPackage_full(){
		final CopyDefinition copy=new CopyDefinition("myArtifact",new String[]{"myclass1","myclass2"},"mycharset","myFrompackage","myToPackage");
		Assertions.assertEquals("myFrompackage",copy.getFromPackage());
	}
	@Test
	@DisplayName("getFromPackage() should be distinct from provided in constructor once replaced with setArtifact()")
	public void setFromPackage(){
		final CopyDefinition copy=new CopyDefinition("myArtifact",new String[]{"myclass1","myclass2"},"mycharset","myFrompackage","myToPackage");
		Assertions.assertEquals("myFrompackage",copy.getFromPackage());
		copy.setFromPackage("myFrompackage2");
		Assertions.assertEquals("myFrompackage2",copy.getFromPackage());
	}

	@Test
	@DisplayName("getToPackage() should be the same used in constructor")
	public void getToPackage_null(){
		final CopyDefinition copy=new CopyDefinition();
		Assertions.assertNull(copy.getToPackage());
	}
	@Test
	@DisplayName("getToPackage() should be the same used in constructor")
	public void getToPackage_full(){
		final CopyDefinition copy=new CopyDefinition("myArtifact",new String[]{"myclass1","myclass2"},"mycharset","myFrompackage","myToPackage");
		Assertions.assertEquals("myToPackage",copy.getToPackage());
	}
	@Test
	@DisplayName("getToPackage() should be distinct from provided in constructor once replaced with setArtifact()")
	public void setToPackage(){
		final CopyDefinition copy=new CopyDefinition("myArtifact",new String[]{"myclass1","myclass2"},"mycharset","myFrompackage","myToPackage");
		Assertions.assertEquals("myToPackage",copy.getToPackage());
		copy.setToPackage("myToPackage2");
		Assertions.assertEquals("myToPackage2",copy.getToPackage());
	}

	@Test
	@DisplayName("getFromPackageRegex() should replace all dots with double bars the fromPackage value")
	public void getFromPackageRegex(){
		final CopyDefinition copy=new CopyDefinition("myArtifact",new String[]{"myclass1","myclass2"},"mycharset","mypacakge1.mypacakge2.mypacakge3","myToPackage");
		Assertions.assertEquals("mypacakge1\\.mypacakge2\\.mypacakge3",copy.getFromPackageRegex());
	}

	@Test
	@DisplayName("toString() should generate the clear description")
	public void tostring(){
		final CopyDefinition copy=new CopyDefinition("myArtifact",new String[]{"myclass1","myclass2"},"mycharset","mypacakge1.mypacakge2.mypacakge3","myToPackage");
		final String expected="Copy:	From: myArtifact\n	Classes:\n		[myclass1]\n		[myclass2]\n	Transforming from package [mypacakge1.mypacakge2.mypacakge3] to package [myToPackage]";
		Assertions.assertEquals(expected,copy.toString());
	}

	static Stream<Arguments> coordinateDataPack() {
	    return Stream.of(
			Arguments.of(null, null,null,null,null),
			Arguments.of("", null,null,null,null),
			Arguments.of("myGroupId", "myGroupId","*","*","sources"),
			Arguments.of("myGroupId:myArtifactId", "myGroupId","myArtifactId","*","sources"),
			Arguments.of("myGroupId:myArtifactId:myVersion", "myGroupId","myArtifactId","myVersion","sources"),
			Arguments.of("myGroupId:myArtifactId:myVersion:myClassifier", "myGroupId","myArtifactId","myVersion","myClassifier")
		);
	}
	
	@ParameterizedTest(name = "From a copyDefinition with {0} as artifact when toCoordinate() is called should generate a coordinate with groupId {1}, artifactId {2}, version {3} and classifier {4}")
	@MethodSource("coordinateDataPack")
	public void toCoordinate(final String _coordinate, final String _groupId,final String _artifactId,final String _version,final String _classifier){
		final CopyDefinition copy=new CopyDefinition(_coordinate,new String[]{"myclass1","myclass2"},"mycharset","myFrompackage","myToPackage");
		final ArtifactCoordinate coordinate=copy.toCoordinate();
		Assertions.assertEquals(_groupId,coordinate.getGroupId());
		Assertions.assertEquals(_artifactId,coordinate.getArtifactId());
		Assertions.assertEquals(_version,coordinate.getVersion());
		Assertions.assertEquals(_classifier,coordinate.getClassifier());
	}
	
	static Stream<Arguments> distinctDataPack() {
	    return Stream.of(
			Arguments.of("myArtifact",new String[]{"myclass1","myclass2"},"mycharset","myFrompackage","myToPackage","myArtifact2",new String[]{"myclass1","myclass2"},"mycharset","myFrompackage","myToPackage"),
			Arguments.of("myArtifact",new String[]{"myclass1","myclass2"},"mycharset","myFrompackage","myToPackage","myArtifact",new String[]{"myclass2","myclass2"},"mycharset","myFrompackage","myToPackage"),
			Arguments.of("myArtifact",new String[]{"myclass1","myclass2"},"mycharset","myFrompackage","myToPackage","myArtifact",new String[]{"myclass1","myclass2"},"mycharset2","myFrompackage","myToPackage"),
			Arguments.of("myArtifact",new String[]{"myclass1","myclass2"},"mycharset","myFrompackage","myToPackage","myArtifact",new String[]{"myclass1","myclass2"},"mycharset","myFrompackage2","myToPackage"),
			Arguments.of("myArtifact",new String[]{"myclass1","myclass2"},"mycharset","myFrompackage","myToPackage","myArtifact",new String[]{"myclass1","myclass2"},"mycharset","myFrompackage","myToPackage2")
		);
	}

	@Test
	@DisplayName("Two equal copyDefinitions should return true to equals()")
	public void equals_true(){
		final CopyDefinition copyA=new CopyDefinition("myArtifact",new String[]{"myclass1","myclass2"},"mycharset","myFrompackage","myToPackage");
		final CopyDefinition copyB=new CopyDefinition("myArtifact",new String[]{"myclass1","myclass2"},"mycharset","myFrompackage","myToPackage");
		Assertions.assertTrue(copyA.equals(copyB));
	}
	@ParameterizedTest(name = "Two distinct copyDefinition({0},{1},{2},{3},{4}),copyDefinition({5},{6},{7},{8},{9}) should return false to equals()")
	@MethodSource("distinctDataPack")
	public void equals_false(final String _artifactA, final String[] _classesA, final String _sourceCharsetA, final String _fromPackageA, final String _toPackageA,final String _artifactB, final String[] _classesB, final String _sourceCharsetB, final String _fromPackageB, final String _toPackageB){
		final CopyDefinition copyA=new CopyDefinition(_artifactA,_classesA,_sourceCharsetA,_fromPackageA,_toPackageA);
		final CopyDefinition copyB=new CopyDefinition(_artifactB,_classesB,_sourceCharsetB,_fromPackageB,_toPackageB);
		Assertions.assertFalse(copyA.equals(copyB));
	}

	@Test
	@DisplayName("Two equal copyDefinitions should return have the same hashCode()")
	public void hashcode_equals(){
		final CopyDefinition copyA=new CopyDefinition("myArtifact",new String[]{"myclass1","myclass2"},"mycharset","myFrompackage","myToPackage");
		final CopyDefinition copyB=new CopyDefinition("myArtifact",new String[]{"myclass1","myclass2"},"mycharset","myFrompackage","myToPackage");
		Assertions.assertEquals(copyA.hashCode(),copyB.hashCode());
	}
	@ParameterizedTest(name = "Two distinct copyDefinition({0},{1},{2},{3},{4}),copyDefinition({5},{6},{7},{8},{9}) should have distinct hashCode()")
	@MethodSource("distinctDataPack")
	public void hashcode_notEquals(final String _artifactA, final String[] _classesA, final String _sourceCharsetA, final String _fromPackageA, final String _toPackageA,final String _artifactB, final String[] _classesB, final String _sourceCharsetB, final String _fromPackageB, final String _toPackageB){
		final CopyDefinition copyA=new CopyDefinition(_artifactA,_classesA,_sourceCharsetA,_fromPackageA,_toPackageA);
		final CopyDefinition copyB=new CopyDefinition(_artifactB,_classesB,_sourceCharsetB,_fromPackageB,_toPackageB);
		Assertions.assertNotEquals(copyA.hashCode(),copyB.hashCode());
	}
}
