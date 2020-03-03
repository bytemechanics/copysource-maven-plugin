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
package org.bytemechanics.maven.plugin.copyclasses.enums;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import mockit.Expectations;
import mockit.Mocked;
import org.apache.maven.shared.transfer.artifact.ArtifactCoordinate;
import org.bytemechanics.maven.plugin.copyclasses.beans.CopyDefinition;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 *
 * @author afarre
 */
public class GeneratedFactoryTest {

	@BeforeAll
	public static void setup() throws IOException{
		System.out.println(">>>>> GeneratedFactoryTest >>>> setupSpec");
		try(InputStream inputStream = GeneratedFactoryTest.class.getResourceAsStream("/logging.properties")){
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
	

	@ParameterizedTest(name = "Verify {0} correct identification with from method")
	@EnumSource(GeneratedFactory.class)
	public void from(final GeneratedFactory _expected){
		GeneratedFactory actual=GeneratedFactory.from(_expected.version);
		Assertions.assertSame(_expected, actual);
	}
	@ParameterizedTest(name = "Verify {0} identification as JDK9 (default)")
	@ValueSource(strings = {"1.10","1.11","1.12","a"})
	@NullSource
	public void from(final String _value){
		Assertions.assertSame(GeneratedFactory.JDK9, GeneratedFactory.from(_value));
	}

	@ParameterizedTest(name = "Verify {0} getImport() is import javax.annotation.Generated before jdk9 and import javax.annotation.processing.Generated after")
	@EnumSource(GeneratedFactory.class)
	public void getImport(final GeneratedFactory _factory){
		final String actual=_factory.getImport();
		if(GeneratedFactory.JDK9!=_factory){
			Assertions.assertEquals("import javax.annotation.Generated;\n", actual);
		}else{
			Assertions.assertEquals("import javax.annotation.processing.Generated;\n", actual);
		}
	}

	@ParameterizedTest(name = "Verify {0} getAnnotation() is correctly generated as: @Generated(value=\"generated-by-copy-plugin\", comments = \"Copied from myGroupId:myArtifactId:myVersion:myClassifier\", date = \"***\")")
	@EnumSource(GeneratedFactory.class)
	public void getAnnotation(final GeneratedFactory _factory,final @Mocked CopyDefinition _copy, final @Mocked ArtifactCoordinate _artifactCoordinate){
		System.out.println(">>> GeneratedFactoryTest >>> getAnnotation");
		final LocalDateTime executionTime=LocalDateTime.now();
		final String expected="@Generated(value=\"generated-by-copy-plugin\", comments = \"Copied from myGroupId:myArtifactId:myVersion:myClassifier\", date = \""+executionTime.toString()+"\")\n";
		
		new Expectations() {{
			_copy.toCoordinate(); result=_artifactCoordinate; times=1;
			_artifactCoordinate.getGroupId(); result="myGroupId"; times=1;
			_artifactCoordinate.getArtifactId(); result="myArtifactId"; times=1;
			_artifactCoordinate.getVersion(); result="myVersion"; times=1;
			_artifactCoordinate.getClassifier(); result="myClassifier"; times=1;
		}};
		
		final String actual=_factory.getAnnotation(_copy, executionTime);
		Assertions.assertEquals(expected, actual);
	}
}
