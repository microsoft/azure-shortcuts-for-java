/**
 * Copyright Microsoft Corp.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.microsoft.azure.shortcuts.resources;

import com.microsoft.azure.management.resources.ResourceManagementClient;
import com.microsoft.azure.management.resources.models.ResourceGroup;
import com.microsoft.azure.management.storage.StorageManagementClient;
import com.microsoft.azure.management.storage.models.*;
import com.microsoft.azure.shortcuts.resources.creation.StorageAccountDefinitionBlank;
import com.microsoft.azure.shortcuts.resources.creation.StorageAccountDefinitionProvisionable;
import com.microsoft.azure.shortcuts.resources.updating.StorageAccountUpdatable;
import com.microsoft.windowsazure.exception.ServiceException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;

public class StorageAccounts /*implements
            SupportsCreating<StorageAccountDefinitionBlank>,
            SupportsUpdating<StorageAccountUpdatable>,
            SupportsListing,
            SupportsReading<StorageAccount>,
            SupportsDeleting*/ {

    private final StorageManagementClient storage;
    private final ResourceManagementClient resources;

    StorageAccounts(StorageManagementClient storage, ResourceManagementClient resources) {
        this.storage = storage;
        this.resources = resources;
    }

    // Starts a new storage account update
    public StorageAccountUpdatable update(String resourceGroup, String name) {
        return new StorageAccountImpl(resourceGroup, name);
    }

    // Starts a new storage account definition
    public StorageAccountDefinitionBlank define(String resourceGroup, String name) {
        return new StorageAccountImpl(resourceGroup, name);
    }

    // Deletes the specified storage account
    public void delete(String resourceGroup, String accountName) throws IOException, ServiceException {
        storage.getStorageAccountsOperations().delete(resourceGroup, accountName);
    }

    // Return the list of storage accounts
    public List<StorageAccount> list() throws ServiceException, IOException, URISyntaxException {
        return storage.getStorageAccountsOperations().list().getStorageAccounts();
    }

    public List<StorageAccount> listByResourceGroup(String resourceGroup) throws ServiceException, IOException, URISyntaxException {
        return storage.getStorageAccountsOperations().listByResourceGroup(resourceGroup).getStorageAccounts();
    }

    // Gets storage account information
    public StorageAccount get(String resourceGroup, String name) throws Exception {
        return storage.getStorageAccountsOperations().getProperties(resourceGroup, name).getStorageAccount();
    }

    private class StorageAccountImpl
            extends ResourceImpl
            implements
            StorageAccountDefinitionBlank,
            StorageAccountDefinitionProvisionable,
            StorageAccountUpdatable {
        private String region;
        private AccountType type;
        private final String resourceGroup;
        private CustomDomain customDomain;
        private HashMap<String, String> tags;

        private StorageAccountImpl(String resourceGroup, String name) {
            super(resourceGroup.toLowerCase(), name.toLowerCase());
            this.resourceGroup = resourceGroup;
        }

        // Creates a new storage account
        public StorageAccountImpl provision() throws Exception {
            if (!resources.getResourceGroupsOperations().checkExistence(resourceGroup).isExists()) {
                resources.getResourceGroupsOperations().createOrUpdate(resourceGroup, new ResourceGroup("West US"));
            }
            final StorageAccountCreateParameters params = new StorageAccountCreateParameters();
            params.setLocation(this.region);
            params.setAccountType((this.type == null) ? AccountType.STANDARDLRS : this.type);
            storage.getStorageAccountsOperations().create(resourceGroup, name, params);
            return this;
        }

        // Apply changes to the storage account
        public StorageAccountImpl apply() throws Exception {
            StorageAccountUpdateParameters params = new StorageAccountUpdateParameters();
            params.setAccountType(this.type);
            params.setCustomDomain(customDomain);
            params.setTags(tags);
            storage.getStorageAccountsOperations().update(resourceGroup, name, params);
            return this;
        }

        // Deletes this storage account
        public void delete() throws Exception {
            storage.getStorageAccountsOperations().delete(resourceGroup, name);
        }

        public StorageAccountImpl withRegion(String region) {
            this.region = region;
            return this;
        }

        public StorageAccountImpl withType(AccountType type) {
            this.type = type;
            return this;
        }

        public StorageAccountImpl withCustomDomain(CustomDomain customDomain) {
            this.customDomain = customDomain;
            return this;
        }

        public StorageAccountImpl withTags(HashMap<String, String> tags) {
            this.tags = tags;
            return this;
        }

        public String region() {
            return this.region;
        }

        public AccountType type() {
            return this.type;
        }
    }
}
