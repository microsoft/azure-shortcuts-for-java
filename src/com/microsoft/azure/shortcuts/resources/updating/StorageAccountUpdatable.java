package com.microsoft.azure.shortcuts.resources.updating;

import com.microsoft.azure.management.storage.models.AccountType;
import com.microsoft.azure.management.storage.models.CustomDomain;

import java.util.HashMap;

public interface StorageAccountUpdatable extends Updatable<StorageAccountUpdatable> {
    StorageAccountUpdatable withRegion(String region);

    StorageAccountUpdatable withType(AccountType type);

    StorageAccountUpdatable withCustomDomain(CustomDomain customDomain);

    StorageAccountUpdatable withTags(HashMap<String, String> tags);
}
