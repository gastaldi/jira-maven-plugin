/**
 * 
 */
package com.george.plugins.jira;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;

import java.rmi.RemoteException;
import java.util.Calendar;

import org.easymock.EasyMock;
import org.easymock.internal.matchers.ArrayEquals;
import org.easymock.internal.matchers.Equals;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.atlassian.jira.rpc.soap.client.JiraSoapService;
import com.atlassian.jira.rpc.soap.client.RemoteAuthenticationException;
import com.atlassian.jira.rpc.soap.client.RemoteVersion;

/**
 * JUnit test case for Jira version MOJO
 * 
 * @author george
 * 
 */
public class ReleaseVersionMojoTest {

	private static final String LOGIN_TOKEN = "TEST_TOKEN";
	private static final RemoteVersion[] VERSIONS = new RemoteVersion[]{
			new RemoteVersion("1", "1.0", false, null, false, null),
			new RemoteVersion("2", "2.0", false, null, false, null),
			new RemoteVersion("3", "3.0", false, null, false, null),
			new RemoteVersion("3", "3.1", false, null, false, null)};

	private Calendar cal = Calendar.getInstance();
	private RemoteVersion RELEASED_VERSION = new RemoteVersion("3", "3.0",
			false, cal, true, null);
	private ReleaseVersionMojo jiraVersionMojo;
	private JiraSoapService jiraStub;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		this.jiraVersionMojo = new ReleaseVersionMojo();
		jiraVersionMojo.jiraUser = "user";
		jiraVersionMojo.jiraPassword = "password";

		// This removes the locator coupling
		jiraStub = EasyMock.createStrictMock(JiraSoapService.class);
		this.jiraVersionMojo.jiraService = jiraStub;
	}

	/**
	 * Test method for
	 * {@link com.george.plugins.jira.ReleaseVersionMojo#discoverJiraWSURL()}.
	 */
	@Test
	public void testDiscoverJiraWSURL() {
		jiraVersionMojo.jiraURL = "http://jira.george.com";
		// Exercise the SUT (System under test)
		String actual = jiraVersionMojo.discoverJiraWSURL();
		String expected = jiraVersionMojo.jiraURL
				+ AbstractJiraMojo.JIRA_SOAP_SUFFIX;
		assertEquals("JIRA WS URL does not match", expected, actual);
	}

	/**
	 * Test method for
	 * {@link com.george.plugins.jira.ReleaseVersionMojo#isVersionAlreadyPresent()}
	 * .
	 */
	@Test
	public void testVersionAlreadyPresent() {
		boolean actual = jiraVersionMojo.isVersionAlreadyPresent(VERSIONS,
				"1.0");
		boolean expected = true;
		assertEquals(expected, actual);
	}

	/**
	 * Test method for
	 * {@link com.george.plugins.jira.ReleaseVersionMojo#isVersionAlreadyPresent()}
	 * .
	 */
	@Test
	public void testVersionNotPresent() {
		boolean actual = jiraVersionMojo.isVersionAlreadyPresent(VERSIONS,
				"4.0");
		boolean expected = false;
		assertEquals(expected, actual);
	}

	@Test
	public void testLatestVersionInfo() throws Exception {
		jiraVersionMojo.remoteVersionComparator = new RemoteVersionComparator();
		String expected = "3.1";
		String actual = jiraVersionMojo.calculateLatestReleaseVersion(VERSIONS);
		assertEquals(expected, actual);
	}

	@Test
	public void testDiscoverWithProjectKey() throws Exception {
		String expected = "http://www.trt12.jus.br/jira"
				+ AbstractJiraMojo.JIRA_SOAP_SUFFIX;
		jiraVersionMojo.jiraURL = "http://www.trt12.jus.br/jira/browse/FERIAS";
		String actual = jiraVersionMojo.discoverJiraWSURL();
		assertEquals(expected, actual);
		assertEquals("FERIAS", jiraVersionMojo.jiraProjectKey);
	}

	@Test
	public void testDiscoverWithoutProjectKey() throws Exception {
		jiraVersionMojo.jiraProjectKey = "FERIAS";
		String expected = "http://www.trt12.jus.br/jira"
				+ AbstractJiraMojo.JIRA_SOAP_SUFFIX;
		jiraVersionMojo.jiraURL = "http://www.trt12.jus.br/jira";
		String actual = jiraVersionMojo.discoverJiraWSURL();
		assertEquals(expected, actual);
		assertEquals("FERIAS", jiraVersionMojo.jiraProjectKey);
	}

	@Test
	public void testExecuteWithReleaseVersion() throws Exception {
		jiraVersionMojo.releaseVersion = "3.0";

		// Chama o login
		doLoginBehavior();
		// Chama o getVersions
		expect(
				jiraStub.getVersions(LOGIN_TOKEN,
						jiraVersionMojo.jiraProjectKey)).andReturn(VERSIONS)
				.once();

		// Chama o releaseVersion
		EasyMock.reportMatcher(new Equals(LOGIN_TOKEN));
		EasyMock.reportMatcher(new Equals(jiraVersionMojo.jiraProjectKey));
		// Tem que fazer assim porque o equals compara com Calendar
		EasyMock.reportMatcher(new RemoteVersionMatcher(RELEASED_VERSION));
		jiraStub.releaseVersion(LOGIN_TOKEN, jiraVersionMojo.jiraProjectKey,
				RELEASED_VERSION);
		expectLastCall();
		// Faz logout
		doLogoutBehavior();
		// Habilita o controle para inicio dos testes
		replay(jiraStub);

		jiraVersionMojo.execute();
	}

	/**
	 * Set up logout mock behavior
	 * 
	 * @throws RemoteException
	 */
	private void doLogoutBehavior() throws RemoteException {
		expect(jiraStub.logout(LOGIN_TOKEN)).andReturn(Boolean.TRUE).once();
	}

	/**
	 * Set up login mock behavior
	 */
	private void doLoginBehavior() throws RemoteException,
			RemoteAuthenticationException,
			com.atlassian.jira.rpc.soap.client.RemoteException {
		expect(jiraStub.login("user", "password")).andReturn(LOGIN_TOKEN)
				.once();
	}

	@After
	public void tearDown() {
		this.jiraVersionMojo = null;
	}

	private static class RemoteVersionMatcher extends ArrayEquals {

		public RemoteVersionMatcher(Object obj) {
			super(obj);
		}

		@Override
		public boolean matches(Object actual) {
			if (actual instanceof RemoteVersion) {
				return (RemoteVersionComparator.doComparison(
						(RemoteVersion) getExpected(), (RemoteVersion) actual) == 0);
			}
			return super.matches(actual);
		}

	}
}
