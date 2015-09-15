package com.microsoft.azure.shortcuts.creation;

import com.microsoft.azure.shortcuts.creation.StorageAccountDefinitionProvisionable;
import com.microsoft.azure.shortcuts.updating.StorageAccountUpdatable;

// Encapsulates all the optional settings for creating a storage account, and the provision method
public interface StorageAccountDefinitionProvisionable extends Provisionable<StorageAccountUpdatable> {
	StorageAccountDefinitionProvisionable withType(String type);
	StorageAccountDefinitionProvisionable withLabel(String label);
	StorageAccountDefinitionProvisionable withDescription(String description);
}
