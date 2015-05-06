package com.github.gastaldi.jira;

import java.io.File;

import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.github.gastaldi.jira.GenerateReleaseNotesMojo;

@RunWith(JUnit4.class)
public class GenerateReleaseNotesMojoTest extends AbstractMojoTestCase {

	private GenerateReleaseNotesMojo mojo;

	@Override
	public void setUp() throws Exception {
		super.setUp();

		mojo = (GenerateReleaseNotesMojo) lookupMojo("generate-release-notes",
				"src/test/resources/GenerateReleaseNotesMojoTest.xml");
	}

	@Ignore
	@Test
	public void testDoExecute() throws Exception {
		mojo.execute();
		assertTrue("Release Notes not generated", new File(
				"target/releaseNotes.txt").exists());
	}

}
