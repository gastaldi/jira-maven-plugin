package com.github.gastaldi.jira;

import java.util.Comparator;

import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.util.StringUtils;

import com.atlassian.jira.rpc.soap.client.JiraSoapService;
import com.atlassian.jira.rpc.soap.client.RemoteVersion;

/**
 * Goal that creates a version in a JIRA project . NOTE: SOAP access must be
 * enabled in your JIRA installation. Check JIRA docs for more info.
 * 
 * @goal create-new-jira-version
 * @phase deploy
 * 
 * @author George Gastaldi
 */
public class CreateNewVersionMojo extends AbstractJiraMojo {

	/**
	 * Next Development Version
	 * 
	 * @parameter expression="${developmentVersion}"
	 *            default-value="${project.version}"
	 * @required
	 */
	String developmentVersion;

	/**
	 * @parameter default-value="${project.build.finalName}"
	 */
	String finalName;

	/**
	 * Whether the final name is to be used for the version; defaults to false.
	 * 
	 * @parameter expression="${finalNameUsedForVersion}"
	 */
	boolean finalNameUsedForVersion;

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
		String newDevVersion;
		if (finalNameUsedForVersion) {
			newDevVersion = finalName;
		} else {
			newDevVersion = developmentVersion;
		}
		// Removing -SNAPSHOT suffix for safety and sensible formatting
		newDevVersion = StringUtils.capitaliseAllWords(newDevVersion.replace(
				"-SNAPSHOT", "").replace("-", " "));
		boolean versionExists = isVersionAlreadyPresent(versions, newDevVersion);
		if (!versionExists) {
			RemoteVersion newVersion = new RemoteVersion();
			log.debug("New Development version in JIRA is: " + newDevVersion);
			newVersion.setName(newDevVersion);
			jiraService.addVersion(loginToken, jiraProjectKey, newVersion);
			log.info("Version created in JIRA for project key "
					+ jiraProjectKey + " : " + newDevVersion);
		} else {
			log.warn(String.format(
					"Version %s is already created in JIRA. Nothing to do.",
					newDevVersion));
		}
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
}
