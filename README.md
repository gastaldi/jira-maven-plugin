Maven JIRA Plugin
=================

The initial code and older versions was originally placed in http://code.google.com/p/jira-maven-plugins/.

This Maven plugin allows performing of JIRA common actions, like releasing a version, create a new version and generate the release notes:

Before you start using this plugin, you must have two configurations already set on your pom.xml:

issueManagement tag
=====================

        <issueManagement>
           <system>JIRA</system>
           <url>http://www.myjira.com/jira/browse/PROJECTKEY</url>
        </issueManagement>

Note: This is extremely important, as will use this information to connect on JIRA.

<server> entry in settings.xml with the authentication information
=====================

Put the following in the settings.xml file: 

    <servers>
        <server>
            <id>jira</id>
            <username>your_user</username>
            <password>your_password</password>
        </server>
    </servers>


Also, make sure your JIRA has SOAP access enabled.


release-jira-version goal
=====================

Add the following profile to be executed when released:

    <profile>
	    <id>release</id>
	    <activation>
		    <property>
			    <name>performRelease</name>
			    <value>true</value>
		    </property>
	    </activation>
	    <build>
		    <plugins>
			    <plugin>
				    <groupId>com.george.app</groupId>
				    <artifactId>maven-jira-plugin</artifactId>
				    <version>1.1</version>
				    <inherited>false</inherited>
				    <configuration>
					    <!- <server> entry in settings.xml -->
					    <settingsKey>jira</settingsKey>
				    </configuration>
				    <executions>
					    <execution>
						    <phase>deploy</phase>
						    <goals>
							    <goal>release-jira-version</goal>
						    </goals>
					    </execution>
				    </executions>
			    </plugin>
		    </plugins>
	    </build>
    </profile>

create-new-version
=====================

Creates a new JIRA version of this project (without the -SNAPSHOT suffix)

Place it on your pom.xml:

    <plugin>
	    <groupId>com.george.app</groupId>
	    <artifactId>maven-jira-plugin</artifactId>
	    <version>1.1</version>
	    <inherited>false</inherited>
	    <configuration>
		    <!- <server> entry in settings.xml -->
		    <settingsKey>jira</settingsKey>
	    </configuration>
	    <executions>
		    <execution>
			    <phase>deploy</phase>
			    <goals>
				    <goal>create-new-jira-version</goal>
			    </goals>
		    </execution>
	    </executions>
    </plugin>

