package com.george.plugins.jira;

import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.apache.maven.wagon.util.FileUtils;
import org.junit.Ignore;
import org.junit.Test;

public class GenerateReleaseNotesMojoTest extends AbstractMojoTestCase {

	private GenerateReleaseNotesMojo mojo;
	 
	public void setUp() throws Exception {
		super.setUp();
		
		mojo = (GenerateReleaseNotesMojo) lookupMojo("generate-release-notes", "src/test/resources/GenerateReleaseNotesMojoTest.xml");
	}

	@Ignore
	@Test
	public void testDoExecute() throws Exception {
		mojo.execute();
		assertTrue("Release Notes not generated",FileUtils.fileExists("target/releaseNotes.txt"));
	}

}
