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

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.Optional;
import org.bytemechanics.maven.plugin.copyclasses.beans.CopyDefinition;
import org.bytemechanics.maven.plugin.copyclasses.exceptions.UnableToIdentifyCoordinate;

/**
 *
 * @author afarre
 */
public enum GeneratedFactory {
	
	JDK5("1.5"){
		@Override
		public String getImport(){
			return IMPORT_JAVAX_ANNOTATION;
		}
	},
	JDK6("1.6"){
		@Override
		public String getImport(){
			return IMPORT_JAVAX_ANNOTATION;
		}
	},
	JDK7("1.7"){
		@Override
		public String getImport(){
			return IMPORT_JAVAX_ANNOTATION;
		}
	},
	JDK8("1.8"){
		@Override
		public String getImport(){
			return IMPORT_JAVAX_ANNOTATION;
		}
	},
	JDK9("1.9");
	
	public static final String IMPORT_JAVAX_ANNOTATION="import javax.annotation.Generated;\n";
	public static final String IMPORT_JAVAX_PROCESSING_ANNOTATION="import javax.annotation.processing.Generated;\n";
	public static final String JAVAX_ANNOTATION_PATTERN="@Generated(value=\"generated-by-copy-plugin\", comments = \"Copied from {0}:{1}:{2}:{3}\", date = \"{4}\")\n";
	public final String version; 
	
	GeneratedFactory(final String _version){
		this.version=_version;
	}
	
	public String getImport(){
		return IMPORT_JAVAX_PROCESSING_ANNOTATION;
	}
	public String getAnnotation(final CopyDefinition _copy,final LocalDateTime _time){
		return Optional.ofNullable(_copy)
						.map(CopyDefinition::toCoordinate)
						.map(coordinate -> new Object[]{coordinate.getGroupId(),coordinate.getArtifactId(),coordinate.getVersion(),coordinate.getClassifier(),_time})
						.map(values -> MessageFormat.format(JAVAX_ANNOTATION_PATTERN, values))
						.orElseThrow(() -> new UnableToIdentifyCoordinate(_copy));
	}
	
	public static GeneratedFactory from(final String _version){
		
		GeneratedFactory reply=GeneratedFactory.JDK9;
		
		for(GeneratedFactory factory:GeneratedFactory.values()){
			if(factory.version.equals(_version)){
				reply=factory;
			}
		}
		
		return reply;
	}
}
