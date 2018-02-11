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
package org.bytemechanics.maven.plugin.copyclasses.beans;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.maven.shared.artifact.ArtifactCoordinate;
import org.apache.maven.shared.artifact.DefaultArtifactCoordinate;

/**
 * Describes the copy to do
 * Example:
 * 	&lt;copies&gt;
 *		&lt;copy&gt;
 *			&lt;artifact&gt;${project.groupId}:copy-commons&lt;/artifact&gt;
 *			&lt;classes&gt;
 *				&lt;class&gt;org.bytemechanics.commons.string.GenericTextParser&lt;/class&gt;
 *				&lt;class&gt;org.bytemechanics.commons.functional.LambdaUnchecker&lt;/class&gt;
 *			&lt;/classes&gt; 
 *			&lt;fromPackage&gt;org.bytemechanics.commons&lt;/fromPackage&gt;
 *			&lt;toPackage&gt;org.bytemechanics.standalone.ignite.internal.commons&lt;/toPackage&gt;
 *		&lt;/copy&gt;
 *	&lt;/copies&gt;
 * @author afarre
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CopyDefinition{
	
	private String artifact;
	private String[] classes;
	private String sourceCharset="UTF-8";
	private String fromPackage;
	private String toPackage;
		
	public ArtifactCoordinate toCoordinate(){
		
		DefaultArtifactCoordinate reply = new DefaultArtifactCoordinate();

        if(artifact!=null && !artifact.isEmpty()){
			final String[] tokens = artifact.split( ":", -1 );
			reply.setGroupId( ( tokens.length > 0 ) ? tokens[0] : "");
			reply.setArtifactId(( tokens.length > 1 ) ? tokens[1] : "*");
			reply.setVersion(( tokens.length > 2 ) ? tokens[2] : "*");
			reply.setClassifier( "sources" );
        }
		
		return reply;
	}
	public String toRegexPackage(){
		return this.fromPackage.replaceAll("\\.", "\\.");
	}
	
	@Override
	public String toString(){
		
		StringBuilder reply=new StringBuilder("Copy:");
		
		reply.append("\tFrom: ").append(this.artifact).append('\n');
		reply.append("\tClasses:\n");
		if(this.classes!=null){
			for(String clazz:this.classes){
				reply.append("\t\t[").append(clazz).append("]\n");
			}
		}
		reply.append("\tTransforming from package [").append(this.fromPackage).append("] to package [").append(this.toPackage).append(']');
		
		return reply.toString();
	}
}
