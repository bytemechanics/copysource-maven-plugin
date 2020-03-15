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
package org.bytemechanics.maven.plugin.copyclasses.enums;

import java.nio.file.Path;
import org.apache.maven.project.MavenProject;

/**
 *
 * @author afarre
 */
public enum Scope {

	SRC("generated-sources"){
		@Override
		public void registerSourceFolder(final MavenProject _project,final Path _folder){
			_project.addCompileSourceRoot(_folder.toString());
		}
	},
	TEST("generated-test-sources"){
		@Override
		public void registerSourceFolder(final MavenProject _project,final Path _folder){
			_project.addTestCompileSourceRoot(_folder.toString());
		}
	},
	;
	
	private final String folder;
	
	Scope(final String _folder){
		this.folder=_folder;
	}

	public String getFolder() {
		return folder;
	}
	
	public abstract void registerSourceFolder(final MavenProject _project,final Path _folder);
}
