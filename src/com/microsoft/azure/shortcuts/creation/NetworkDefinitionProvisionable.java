package com.microsoft.azure.shortcuts.creation;

import com.microsoft.azure.shortcuts.updating.NetworkUpdatable;

public interface NetworkDefinitionProvisionable extends Provisionable<NetworkUpdatable> {
	NetworkDefinitionProvisionable withSubnet(String name, String cidr);
}
