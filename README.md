# Copy Sources maven plugin
Maven pluguin to copy and repackage sources to reduce library dependencies

## Motivation
To keep the dependency hell away from your projects its important to reduce at minimum the dependencies of each library. But at te same time this can break the code reutilization principle, to avoid this flag
the solution comes by copying the source code and repackaging in order to avoid collisions. But this is only necessary when you need ONLY some specific classes, if you need the full library then you should add
it as dependency.

## Quick start
_**IMPORTANT NOTE: We strongly recommends to use this plugin only for libraries, for final projects if you want to build a uber-jar maven already has it's shade plugin that works perfectly**_
1. Add the pluguin to your pom
**Maven**
```Maven
	(...)
	<build>
		<plugins>
			(...)
			<plugin>
				<groupId>org.bytemechanics.maven</groupId>
				<artifactId>copyclasses-maven-plugin</artifactId>
				<version>0.1.0-SNAPSHOT</version>
			</plugin>
			(...)
		</plugins>
	</build>
	(...)
```
2. Define the execution goal "copy-classes" (the phase it's not necessary, by default uses "generate-sources")
```Maven
	(...)
	<build>
		<plugins>
			(...)
			<plugin>
				<groupId>org.bytemechanics.maven</groupId>
				<artifactId>copyclasses-maven-plugin</artifactId>
				<version>0.1.0-SNAPSHOT</version>
				<executions>
					<execution>
						<goals>
							<goal>copy-classes</goal>
						</goals>
					</execution>
				</executions>
			</plugin>
			(...)
		</plugins>
	</build>
	(...)
```
2. Configure it with the copies that you want to do:
```Maven
	(...)
	<build>
		<plugins>
			(...)
			<plugin>
				<groupId>org.bytemechanics.maven</groupId>
				<artifactId>copyclasses-maven-plugin</artifactId>
				<version>0.1.0-SNAPSHOT</version>
				<executions>
					<execution>
						<goals>
							<goal>copy-classes</goal>
						</goals>
						<configuration>
							<copies>
								<copy>
									<artifact>[source-groupId]:[source-artifactId]:[source-version]</artifact>
									<classes>
										<class>[cannonical-name-of-origin-source. Example:org.bytemechanics.commons.functional.LambdaUnchecker]</class>
										(...)
									</classes>
									<fromPackage>[package-segment-to-replace. Example: org.bytemechanics.commons]</fromPackage>
									<toPackage>[package-segment-to-replace. Example: org.bytemechanics.standalone.ignite.internal.commons]</toPackage>
								</copy>
							</copies>
						</configuration>
					</execution>
				</executions>
			</plugin>
			(...)
		</plugins>
	</build>
	(...)
```

