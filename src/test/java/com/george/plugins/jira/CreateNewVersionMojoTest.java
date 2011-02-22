/**
 * 
 */
package com.george.plugins.jira;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

import java.rmi.RemoteException;

import org.easymock.EasyMock;
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
public class CreateNewVersionMojoTest {

	private static final String LOGIN_TOKEN = "TEST_TOKEN";
	private static final RemoteVersion[] VERSIONS = new RemoteVersion[] {
			new RemoteVersion("1", "1.0", false, null, false, null),
			new RemoteVersion("2", "2.0", false, null, false, null),
			new RemoteVersion("3", "3.0", false, null, false, null),
			new RemoteVersion("3", "3.1", false, null, false, null) };

	private CreateNewVersionMojo jiraVersionMojo;
	private JiraSoapService jiraStub;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		this.jiraVersionMojo = new CreateNewVersionMojo();
		jiraVersionMojo.jiraUser = "user";
		jiraVersionMojo.jiraPassword = "password";

		// This removes the locator coupling
		jiraStub = EasyMock.createStrictMock(JiraSoapService.class);
		this.jiraVersionMojo.jiraService = jiraStub;
	}

	/**
	 * Test method for {@link CreateNewVersionMojo#execute()}
	 * 
	 * @throws Exception
	 */
	@Test
	public void testExecuteWithNewDevVersion() throws Exception {
		jiraVersionMojo.developmentVersion = "5.0";
		doLoginBehavior();
		// Chama o getVersions
		expect(
				jiraStub.getVersions(LOGIN_TOKEN,
						jiraVersionMojo.jiraProjectKey)).andReturn(VERSIONS)
				.once();

		// Adiciona a nova versao
		expect(
				jiraStub.addVersion(LOGIN_TOKEN,
						jiraVersionMojo.jiraProjectKey, new RemoteVersion(null,
								jiraVersionMojo.developmentVersion, false,
								null, false, null))).andReturn(VERSIONS[0]);
		doLogoutBehavior();
		// Habilita o controle para inicio dos testes
		EasyMock.replay(jiraStub);

		jiraVersionMojo.execute();
	}
	
	/**
	 * Test method for {@link ReleaseVersionMojo#execute()}
	 * 
	 * @throws Exception
	 */
	@Test
	public void testExecuteWithExistentDevVersion() throws Exception {
		jiraVersionMojo.developmentVersion = "2.0";
		doLoginBehavior();
		// Chama o getVersions
		expect(jiraStub.getVersions(LOGIN_TOKEN, jiraVersionMojo.jiraProjectKey)).andReturn(
				VERSIONS).once();
		
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
}
