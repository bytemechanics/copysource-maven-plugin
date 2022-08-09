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

import java.nio.charset.Charset;
import java.nio.file.Path;
import java.time.LocalDateTime;
import org.apache.maven.plugin.MojoExecutionException;
import org.bytemechanics.maven.plugin.copyclasses.beans.CopyDefinition;
import org.bytemechanics.maven.plugin.copyclasses.enums.Scope;

/**
 * Service to copy files and from an existing jar/zip
 * @author afarre
 */
public interface CopyService {

	public LocalDateTime getExecutionTime();
	public String getTargetFolder();
	public String getGeneratedSourceFolder();
	public Charset getEncoding();

	public default void prepareEnvironment(final Path _generatedSourcesPath,final CopyDefinition _copy) throws MojoExecutionException{}
	public Path generateSourcePath(final Scope _scope) throws MojoExecutionException;
	public void createManifest(final CopyDefinition[] _copies,final Path generatedSourcesPath) throws MojoExecutionException;
	public void processDownloadedSource(final Path _sourceFile,final CopyDefinition _copy,final Path _generatedSourcesPath) throws MojoExecutionException;
}
