package com.microsoft.azure.shortcuts.creation;

import com.microsoft.azure.shortcuts.creation.NetworkDefinitionProvisionable;

public interface NetworkDefinitionWithCidr {
	NetworkDefinitionProvisionable withCidr(String cidr);
}
