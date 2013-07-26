package com.george.plugins.jira;

import java.util.Comparator;

import com.atlassian.jira.rpc.soap.client.RemoteVersion;

public class RemoteVersionComparator implements Comparator<RemoteVersion> {

	@Override
	public int compare(RemoteVersion o1, RemoteVersion o2) {
		return doComparison(o1, o2);
	}

	public static int doComparison(RemoteVersion o1, RemoteVersion o2) {
		return -1 * o1.getName().compareToIgnoreCase(o2.getName());
	}

}
