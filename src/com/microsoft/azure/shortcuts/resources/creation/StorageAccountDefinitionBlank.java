package com.microsoft.azure.shortcuts.resources.creation;

import com.microsoft.azure.shortcuts.reading.StorageAccount;

public interface StorageAccountDefinitionBlank {
    StorageAccountDefinitionProvisionable withRegion(String region);
}
