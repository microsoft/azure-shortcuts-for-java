package com.microsoft.azure.shortcuts.creation;

import com.microsoft.azure.shortcuts.creation.StorageAccountDefinitionProvisionable;

// creating a storage account
public interface StorageAccountDefinitionBlank {
	StorageAccountDefinitionProvisionable withRegion(String region);
	StorageAccountDefinitionProvisionable withAffinityGroup(String affinityGroup);
}
