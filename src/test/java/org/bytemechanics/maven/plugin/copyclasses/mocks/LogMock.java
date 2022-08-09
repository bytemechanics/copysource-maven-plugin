/*
 * Copyright 2022 Byte Mechanics.
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
package org.bytemechanics.maven.plugin.copyclasses.mocks;

import org.apache.maven.plugin.logging.Log;

/**
 *
 * @author afarre
 */
public class LogMock implements Log{

	public static enum Level{ ERROR, WARN,INFO,DEBUG};
	
	final String testClass;
	final Level level;
	
	public LogMock(final Class _testClass,final Level _level){
		this.testClass=_testClass.getSimpleName();
		this.level=_level;
	}
	
	@Override
	public boolean isDebugEnabled() {
		return this.level.equals(Level.DEBUG);
	}
	@Override
	public void debug(CharSequence cs) {
		System.out.println("Plugin log >>> [DEBUG] ["+testClass+"] "+cs);
	}
	@Override
	public void debug(CharSequence cs, Throwable thrwbl) {
		System.out.println("Plugin log >>> [DEBUG] ["+testClass+"] "+cs);
		thrwbl.printStackTrace();
	}
	@Override
	public void debug(Throwable thrwbl) {
		System.out.println("Plugin log >>> [DEBUG] ["+testClass+"] ");
		thrwbl.printStackTrace();
	}

	@Override
	public boolean isInfoEnabled() {
		return this.level.equals(Level.DEBUG)||this.level.equals(Level.INFO);
	}
	@Override
	public void info(CharSequence cs) {
		System.out.println("Plugin log >>> [INFO] ["+testClass+"] "+cs);
	}
	@Override
	public void info(CharSequence cs, Throwable thrwbl) {
		System.out.println("Plugin log >>> [INFO] ["+testClass+"] "+cs);
		thrwbl.printStackTrace();
	}
	@Override
	public void info(Throwable thrwbl) {
		System.out.println("Plugin log >>> [INFO] ["+testClass+"] ");
		thrwbl.printStackTrace();
	}

	@Override
	public boolean isWarnEnabled() {
		return this.level.equals(Level.DEBUG)||this.level.equals(Level.INFO)||this.level.equals(Level.WARN);
	}
	@Override
	public void warn(CharSequence cs) {
		System.out.println("Plugin log >>> [WARN] ["+testClass+"] "+cs);
	}
	@Override
	public void warn(CharSequence cs, Throwable thrwbl) {
		System.out.println("Plugin log >>> [WARN] ["+testClass+"] "+cs);
		thrwbl.printStackTrace();
	}
	@Override
	public void warn(Throwable thrwbl) {
		System.out.println("Plugin log >>> [WARN] ["+testClass+"] ");
		thrwbl.printStackTrace();
	}

	@Override
	public boolean isErrorEnabled() {
		return this.level.equals(Level.DEBUG)||this.level.equals(Level.INFO)||this.level.equals(Level.WARN)||this.level.equals(Level.ERROR);
	}
	@Override
	public void error(CharSequence cs) {
		System.out.println("Plugin log >>> [ERROR] ["+testClass+"] "+cs);
	}
	@Override
	public void error(CharSequence cs, Throwable thrwbl) {
		System.out.println("Plugin log >>> [ERROR] ["+testClass+"] "+cs);
		thrwbl.printStackTrace();
	}
	@Override
	public void error(Throwable thrwbl) {
		System.out.println("Plugin log >>> [ERROR] ["+testClass+"] ");
		thrwbl.printStackTrace();
	}
}
