# Copy Sources maven plugin
[![Latest version](https://maven-badges.herokuapp.com/maven-central/org.bytemechanics.maven/copysource-maven-plugin/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.bytemechanics.maven/copysource-maven-plugin/badge.svg)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=org.bytemechanics.maven%3Acopysource-maven-plugin&metric=alert_status)](https://sonarcloud.io/dashboard/index/org.bytemechanics.maven%3Acopysource-maven-plugin)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=org.bytemechanics.maven%3Acopysource-maven-plugin&metric=coverage)](https://sonarcloud.io/dashboard/index/org.bytemechanics.maven%3Acopysource-maven-plugin)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

Maven pluguin to copy and repackage sources to reduce library dependencies

## Motivation
To keep the dependency hell away from your projects its important to reduce at minimum the dependencies of each library. But at te same time this can break the code reutilization principle, to avoid this flag
the solution comes by copying the source code and repackaging in order to avoid collisions. But this is only necessary when you need ONLY some specific classes, if you need the full library then you should add
it as dependency.

## Quick start
_**IMPORTANT NOTE: We strongly recommends to use this plugin only for libraries, for final projects if you want to build a uber-jar maven already has it's shade plugin that works perfectly**_
1. Add the pluguin to your pom
   ```xml
   (...)
      <build>
         <plugins>
            (...)
            <plugin>
               <groupId>org.bytemechanics.maven</groupId>
               <artifactId>copyclasses-maven-plugin</artifactId>
               <version>X.X.X</version>
           </plugin>
           (...)
        </plugins>
   </build>
   (...)
   ```
2. Define the execution goal "copy-classes" (the phase it's not necessary, by default uses "generate-sources")
   ```xml
   (...)
   <build>
      <plugins>
         (...)
         <plugin>
            <groupId>org.bytemechanics.maven</groupId>
            <artifactId>copyclasses-maven-plugin</artifactId>
            <version>X.X.X</version>
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
3. Configure it with the copies that you want to do:
   ```xml
   (...)
   <build>
      <plugins>
         (...)
         <plugin>
            <groupId>org.bytemechanics.maven</groupId>
            <artifactId>copyclasses-maven-plugin</artifactId>
            <version>X.X.X</version>
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
