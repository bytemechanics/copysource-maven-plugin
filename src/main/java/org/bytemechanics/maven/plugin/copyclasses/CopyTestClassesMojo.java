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

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.transfer.artifact.resolve.ArtifactResolver;
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

@Mojo(name = "copy-test-classes", defaultPhase = LifecyclePhase.GENERATE_TEST_SOURCES)
public class CopyTestClassesMojo extends CopyClassesBase {

	public CopyTestClassesMojo() {
	}
	public CopyTestClassesMojo(ArtifactResolver artifactResolver, MavenSession session, MavenProject project, CopyDefinition[] copies, String generatedSourceFolder) {
		super(artifactResolver, session, project, copies, generatedSourceFolder);
	}
	
	@Override
	public void execute() throws MojoExecutionException {
		generateSources(Scope.TEST);
	}
}
