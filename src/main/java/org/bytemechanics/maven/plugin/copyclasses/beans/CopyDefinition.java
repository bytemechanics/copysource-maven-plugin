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

import java.util.Arrays;
import java.util.Objects;
import org.apache.maven.shared.transfer.artifact.ArtifactCoordinate;
import org.apache.maven.shared.transfer.artifact.DefaultArtifactCoordinate;

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
public class CopyDefinition{
	
	private String artifact;
	private String[] classes;
	private String sourceCharset="UTF-8";
	private String fromPackage;
	private String toPackage;


	public CopyDefinition() {
	}
	public CopyDefinition(final String _artifact, final String[] _classes, final String _sourceCharset, final String _fromPackage, final String _toPackage) {
		this.artifact = _artifact;
		this.classes = _classes;
		this.sourceCharset = _sourceCharset;
		this.fromPackage = _fromPackage;
		this.toPackage = _toPackage;
	}

	
	public String getArtifact() {
		return artifact;
	}
	public void setArtifact(String artifact) {
		this.artifact = artifact;
	}

	public String[] getClasses() {
		return classes;
	}
	public void setClasses(String[] classes) {
		this.classes = classes;
	}

	public String getSourceCharset() {
		return sourceCharset;
	}
	public void setSourceCharset(String sourceCharset) {
		this.sourceCharset = sourceCharset;
	}

	public String getFromPackage() {
		return fromPackage;
	}
	public String getFromPackageRegex(){
		return getFromPackage().replaceAll("\\.", "\\\\.");
	}
	public void setFromPackage(String fromPackage) {
		this.fromPackage = fromPackage;
	}

	public String getToPackage() {
		return toPackage;
	}
	public void setToPackage(String toPackage) {
		this.toPackage = toPackage;
	}
	
	public ArtifactCoordinate toCoordinate(){
		
		final DefaultArtifactCoordinate reply = new DefaultArtifactCoordinate();

        if(artifact!=null && !artifact.isEmpty()){
			final String[] tokens = artifact.split( ":", -1 );
			reply.setGroupId(( tokens.length > 0 ) ? tokens[0] : "");
			reply.setArtifactId(( tokens.length > 1 ) ? tokens[1] : "*");
			reply.setVersion(( tokens.length > 2 ) ? tokens[2] : "*");
			reply.setClassifier(( tokens.length > 3 ) ? tokens[3] : "sources");
        }
		
		return reply;
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


	@Override
	public int hashCode() {
		int hash = 3;
		hash = 41 * hash + Objects.hashCode(this.artifact);
		hash = 41 * hash + Arrays.deepHashCode(this.classes);
		hash = 41 * hash + Objects.hashCode(this.sourceCharset);
		hash = 41 * hash + Objects.hashCode(this.fromPackage);
		hash = 41 * hash + Objects.hashCode(this.toPackage);
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final CopyDefinition other = (CopyDefinition) obj;
		if (!Objects.equals(this.artifact, other.artifact)) {
			return false;
		}
		if (!Objects.equals(this.sourceCharset, other.sourceCharset)) {
			return false;
		}
		if (!Objects.equals(this.fromPackage, other.fromPackage)) {
			return false;
		}
		if (!Objects.equals(this.toPackage, other.toPackage)) {
			return false;
		}
		return Arrays.deepEquals(this.classes, other.classes);
	}
}
