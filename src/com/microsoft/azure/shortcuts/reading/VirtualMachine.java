package com.microsoft.azure.shortcuts.reading;

import com.microsoft.windowsazure.management.compute.models.DeploymentStatus;

public interface VirtualMachine extends Named {
	String size();
	String deployment();
	String cloudService();
	String network();
	String region();
	String affinityGroup();
	DeploymentStatus status();
	boolean isLinux();
	boolean isWindows();
	String roleName();
}

