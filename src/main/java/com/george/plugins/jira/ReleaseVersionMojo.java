package com.george.plugins.jira;

import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;

import org.apache.maven.plugin.logging.Log;

import com.atlassian.jira.rpc.soap.client.JiraSoapService;
import com.atlassian.jira.rpc.soap.client.RemoteAuthenticationException;
import com.atlassian.jira.rpc.soap.client.RemotePermissionException;
import com.atlassian.jira.rpc.soap.client.RemoteVersion;

/**
 * Goal that creates a version in a JIRA project . NOTE: SOAP access must be
 * enabled in your JIRA installation. Check JIRA docs for more info.
 * 
 * @goal release-jira-version
 * @phase deploy
 * 
 * @author George Gastaldi
 */
public class ReleaseVersionMojo extends AbstractJiraMojo {

	/**
	 * Released Version
	 * 
	 * @parameter expression="${releaseVersion}"
	 *            default-value="${project.version}"
	 */
	String releaseVersion;

	/**
	 * Auto Discover latest release and release it.
	 * 
	 * @parameter expression="${autoDiscoverLatestRelease}" default-value="true"
	 */
	boolean autoDiscoverLatestRelease;

	/**
	 * Comparator for discovering the latest release
	 * 
	 * @parameter 
	 *            implementation="com.george.plugins.jira.RemoteVersionComparator"
	 */
	Comparator<RemoteVersion> remoteVersionComparator = new RemoteVersionComparator();

	@Override
	public void doExecute(JiraSoapService jiraService, String loginToken)
			throws Exception {
		Log log = getLog();
		log.debug("Login Token returned: " + loginToken);
		RemoteVersion[] versions = jiraService.getVersions(loginToken,
				jiraProjectKey);
		String thisReleaseVersion = (autoDiscoverLatestRelease)
				? calculateLatestReleaseVersion(versions)
				: releaseVersion;
		if (thisReleaseVersion != null) {
			log.info("Releasing Version " + this.releaseVersion);
			markVersionAsReleased(jiraService, loginToken, versions,
					thisReleaseVersion);
		}
	}

	/**
	 * Returns the latest unreleased version
	 * 
	 * @param versions
	 * @return
	 */
	String calculateLatestReleaseVersion(RemoteVersion[] versions) {
		Arrays.sort(versions, remoteVersionComparator);

		for (RemoteVersion remoteVersion : versions) {
			if (!remoteVersion.isReleased())
				return remoteVersion.getName();
		}
		return null;
	}

	/**
	 * Check if version is already present on array
	 * 
	 * @param versions
	 * @param newDevVersion
	 * @return
	 */
	boolean isVersionAlreadyPresent(RemoteVersion[] versions,
			String newDevVersion) {
		boolean versionExists = false;
		if (versions != null) {
			// Creating new Version (if not already created)
			for (RemoteVersion remoteVersion : versions) {
				if (remoteVersion.getName().equalsIgnoreCase(newDevVersion)) {
					versionExists = true;
					break;
				}
			}
		}
		// existant
		return versionExists;
	}

	/**
	 * Release Version
	 * 
	 * @param log
	 * @param jiraService
	 * @param loginToken
	 * @throws RemoteException
	 * @throws RemotePermissionException
	 * @throws RemoteAuthenticationException
	 * @throws com.atlassian.jira.rpc.soap.client.RemoteException
	 */
	RemoteVersion markVersionAsReleased(JiraSoapService jiraService,
			String loginToken, RemoteVersion[] versions, String releaseVersion)
			throws RemoteException, RemotePermissionException,
			RemoteAuthenticationException,
			com.atlassian.jira.rpc.soap.client.RemoteException {
		RemoteVersion ret = null;
		if (versions != null) {
			for (RemoteVersion remoteReleasedVersion : versions) {
				if (releaseVersion.equalsIgnoreCase(remoteReleasedVersion
						.getName()) && !remoteReleasedVersion.isReleased()) {
					// Mark as released
					remoteReleasedVersion.setReleased(true);
					remoteReleasedVersion
							.setReleaseDate(Calendar.getInstance());
					jiraService.releaseVersion(loginToken, jiraProjectKey,
							remoteReleasedVersion);
					getLog().info(
							"Version " + remoteReleasedVersion.getName()
									+ " was released in JIRA.");
					ret = remoteReleasedVersion;
					break;
				}
			}
		}
		return ret;
	}
}
