package com.microsoft.azure.shortcuts.resources.creation;

import com.microsoft.azure.management.storage.models.AccountType;
import com.microsoft.azure.shortcuts.creation.Provisionable;
import com.microsoft.azure.shortcuts.resources.updating.StorageAccountUpdatable;

public interface StorageAccountDefinitionProvisionable extends Provisionable<StorageAccountUpdatable> {
    StorageAccountDefinitionProvisionable withType(AccountType type);
}
