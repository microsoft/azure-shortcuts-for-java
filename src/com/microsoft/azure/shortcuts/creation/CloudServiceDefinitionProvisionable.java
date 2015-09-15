package com.microsoft.azure.shortcuts.creation;

import com.microsoft.azure.shortcuts.creation.CloudServiceDefinitionProvisionable;
import com.microsoft.azure.shortcuts.updating.CloudServiceUpdatable;

// Optional parameter settings and provisioning
public interface CloudServiceDefinitionProvisionable extends Provisionable<CloudServiceUpdatable> {
	public CloudServiceDefinitionProvisionable withDescription(String description);
	public CloudServiceDefinitionProvisionable withLabel(String label);
	public CloudServiceDefinitionProvisionable withReverseDnsFqdn(String fqdn);
}
