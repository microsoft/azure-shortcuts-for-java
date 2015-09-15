package com.microsoft.azure.shortcuts.creation;


public interface CloudServiceDefinitionBlank {
	CloudServiceDefinitionProvisionable withRegion(String region);
	CloudServiceDefinitionProvisionable withAffinityGroup(String affinityGroup);
}
