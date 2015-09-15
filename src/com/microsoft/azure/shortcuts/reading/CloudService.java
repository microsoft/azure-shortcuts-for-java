package com.microsoft.azure.shortcuts.reading;

import java.util.Calendar;

// Encapsulates the readable properties of a cloud service
public interface CloudService extends Named {
	String region();
	String description();
	String label();
	String reverseDnsFqdn();
	Calendar created();
	Calendar modified();
	String affinityGroup();
}
