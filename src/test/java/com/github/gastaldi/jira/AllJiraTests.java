package com.github.gastaldi.jira;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@SuiteClasses({CreateNewVersionMojoTest.class, ReleaseVersionMojoTest.class,
		PlexusJiraVersionMojoTest.class})
@RunWith(Suite.class)
public class AllJiraTests {

}
