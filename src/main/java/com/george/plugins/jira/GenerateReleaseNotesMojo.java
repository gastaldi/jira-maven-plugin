package com.george.plugins.jira;

import static java.text.MessageFormat.format;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.rmi.RemoteException;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.wagon.util.IoUtils;

import com.atlassian.jira.rpc.soap.client.JiraSoapService;
import com.atlassian.jira.rpc.soap.client.RemoteIssue;

/**
 * Goal that generates release notes based on a version in a JIRA project.
 * 
 * NOTE: SOAP access must be enabled in your JIRA installation. Check JIRA docs
 * for more info.
 * 
 * @goal generate-release-notes
 * @phase deploy
 * 
 * @author George Gastaldi
 */
public class GenerateReleaseNotesMojo extends AbstractJiraMojo {

	/**
	 * JQL Template to generate release notes. 
	 * Parameter 0 = Project Key
	 * Parameter 1 = Fix version
	 * 
	 * @parameter expression="${jqlTemplate}"
	 * @required
	 */
	String jqlTemplate = "project = ''{0}'' AND status in (Resolved, Closed) AND fixVersion = ''{1}''";

	/**
	 * Template used on each issue found by JQL Template. 
	 * Parameter 0 = Issue Key
	 * Parameter 1 = Issue Summary
	 * 
	 * @parameter expression="${issueTemplate}"
	 * @required
	 */
	String issueTemplate = "[{0}] {1}";

	/**
	 * Max number of issues to return
	 * 
	 * @parameter expression="${maxIssues}" default-value="100"
	 * @required
	 */
	int maxIssues = 100;

	/**
	 * Released Version
	 * 
	 * @parameter expression="${releaseVersion}"
	 *            default-value="${project.version}"
	 * @required
	 */
	String releaseVersion;

	/**
	 * Target file
	 * 
	 * @parameter expression="${targetFile}"
	 *            default-value="${outputDirectory}/releaseNotes.txt"
	 * @required
	 */
	File targetFile;

	
	/**
	 * Text to be appended BEFORE all issues details.
	 * 
	 * @parameter expression="${beforeText}"
	 */
	String beforeText;
	
	/**
	 * Text to be appended AFTER all issues details.
	 * 
	 * @parameter expression="${afterText}"
	 */
	String afterText;
	
	@Override
	public void doExecute(JiraSoapService jiraService, String loginToken)
			throws Exception {
		RemoteIssue[] issues = getIssues(jiraService, loginToken);
		output(issues);
	}

	/**
	 * Recover issues from JIRA based on JQL Filter
	 * 
	 * @param jiraService
	 * @param loginToken
	 * @return
	 * @throws RemoteException
	 * @throws com.atlassian.jira.rpc.soap.client.RemoteException
	 */
	RemoteIssue[] getIssues(JiraSoapService jiraService, String loginToken)
			throws RemoteException,
			com.atlassian.jira.rpc.soap.client.RemoteException {
		Log log = getLog();
		String jql = format(jqlTemplate, jiraProjectKey, releaseVersion);
		if (log.isInfoEnabled()) {
			log.info("JQL: " + jql);
		}
		RemoteIssue[] issues = jiraService.getIssuesFromJqlSearch(loginToken,jql, maxIssues);
		if (log.isInfoEnabled()) {
			log.info("Issues: " + issues.length);
		}
		return issues;
	}

	/**
	 * Writes issues to output
	 * 
	 * @param issues
	 */
	void output(RemoteIssue[] issues) throws IOException {
		Log log = getLog();
		if (targetFile == null) {
			log.warn("No targetFile specified. Ignoring");
			return;
		}
		if (issues == null) {
			log.warn("No issues found. File will not be generated.");
			return;
		}
		OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(targetFile,true), "Cp1252");
		PrintWriter ps = new PrintWriter(writer);
		try {
			if (beforeText != null) {
				ps.println(beforeText);
			}
			for (RemoteIssue issue : issues) {
				String issueDesc = format(issueTemplate,issue.getKey(),issue.getSummary());
				ps.println(issueDesc);
			}
			if (afterText != null) {
				ps.println(afterText);
			}
		} finally {
			ps.flush();
			IoUtils.close(ps);
		}
	}

	public void setAfterText(String afterText) {
		this.afterText = afterText;
	}
	public void setBeforeText(String beforeText) {
		this.beforeText = beforeText;
	}
	public void setReleaseVersion(String releaseVersion) {
		this.releaseVersion = releaseVersion;
	}
	public void setIssueTemplate(String issueTemplate) {
		this.issueTemplate = issueTemplate;
	}
	public void setJqlTemplate(String jqlTemplate) {
		this.jqlTemplate = jqlTemplate;
	}
}
