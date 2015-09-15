package com.microsoft.azure.shortcuts.reading;


public interface Network extends Named {
	String cidr();
	String region();
	String affinityGroup();
	String label();
	Subnet[] subnets();
	
	public interface Subnet extends Named {	
		String cidr();
	}
}
