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
import java.nio.file.Path;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import mockit.Expectations;
import mockit.Mocked;
import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

/**
 *
 * @author afarre
 */
public class ScopeTest {

	@BeforeAll
	public static void setup() throws IOException{
		System.out.println(">>>>> ScopeTest >>>> setupSpec");
		try(InputStream inputStream = ScopeTest.class.getResourceAsStream("/logging.properties")){
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
	@DisplayName("Source scope getFolder() should return generated-sources")
	public void getFolder_Source(){
		Assertions.assertSame("generated-sources",Scope.SRC.getFolder());
	}
	@Test
	@DisplayName("Test scope getFolder() should return generated-test-sources")
	public void getFolder_TestSource(){
		Assertions.assertSame("generated-test-sources",Scope.TEST.getFolder());
	}

	@Test
	@DisplayName("Source scope registerSourceFolder should use compilerSource")
	public void registerSourceFolder_Source(final @Mocked MavenProject _project,final @Mocked Path _folder){
		
		new Expectations() {{
			_folder.toString(); result="mySourceFolder"; times=1;
			_project.addCompileSourceRoot("mySourceFolder"); times=1;
		}};
		Scope.SRC.registerSourceFolder(_project, _folder);
	}
	@Test
	@DisplayName("Source scope registerSourceFolder should use compilerTestSource")
	public void registerSourceFolder_TestSource(final @Mocked MavenProject _project,final @Mocked Path _folder){
		
		new Expectations() {{
			_folder.toString(); result="mySourceFolder"; times=1;
			_project.addTestCompileSourceRoot("mySourceFolder"); times=1;
		}};
		Scope.TEST.registerSourceFolder(_project, _folder);
	}
}
