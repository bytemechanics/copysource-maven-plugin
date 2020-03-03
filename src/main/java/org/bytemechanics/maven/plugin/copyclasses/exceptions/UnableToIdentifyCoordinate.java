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
package org.bytemechanics.maven.plugin.copyclasses.exceptions;

import java.text.MessageFormat;
import org.bytemechanics.maven.plugin.copyclasses.beans.CopyDefinition;

/**
 *
 * @author afarre
 */
public class UnableToIdentifyCoordinate extends RuntimeException{

	private static final long serialVersionUID = 1L;
	
	public static final String MESSAGE="Unable to identify coordinate from {0}";
	
	public UnableToIdentifyCoordinate(final CopyDefinition _copy){
		super(MessageFormat.format(MESSAGE,_copy));
	}
}
