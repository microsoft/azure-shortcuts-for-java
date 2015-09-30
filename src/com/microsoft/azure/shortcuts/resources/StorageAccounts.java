/**
* Copyright (c) Microsoft Corporation
* 
* All rights reserved. 
* 
* MIT License
* 
* Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files 
* (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, 
* publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, 
* subject to the following conditions:
* 
* The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
* 
* THE SOFTWARE IS PROVIDED *AS IS*, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF 
* MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR 
* ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH 
* THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

package com.microsoft.azure.shortcuts.resources;

import com.microsoft.azure.management.resources.models.ResourceGroup;
import com.microsoft.azure.management.storage.models.*;
import com.microsoft.azure.shortcuts.common.implementation.SupportsListing;
import com.microsoft.azure.shortcuts.resources.creation.StorageAccountDefinitionBlank;
import com.microsoft.azure.shortcuts.resources.creation.StorageAccountDefinitionProvisionable;
import com.microsoft.azure.shortcuts.resources.updating.StorageAccountUpdatable;
import com.microsoft.windowsazure.exception.ServiceException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;

public class StorageAccounts implements 
		SupportsListing
			/*
            SupportsCreating<StorageAccountDefinitionBlank>,
            SupportsUpdating<StorageAccountUpdatable>,
            SupportsReading<StorageAccount>,
            SupportsDeleting*/ {

    private final Azure azure;

    StorageAccounts(Azure azure) {
        this.azure = azure;
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
    	this.azure.storageManagementClient().getStorageAccountsOperations().delete(resourceGroup, accountName);
    }
    
    
    // Return the list of storage account names
    @Override
    public String[] list() {
        ArrayList<StorageAccount> storageAccounts;
		try {
			storageAccounts = this.azure.storageManagementClient().getStorageAccountsOperations().list().getStorageAccounts();
			String[] names = new String[storageAccounts.size()];
			int i = 0;
			for(StorageAccount store: storageAccounts) {
				names[i++]= store.getName();
			}
			return names;
		} catch (IOException | ServiceException | URISyntaxException e) {
			// Not very actionable so return empty array
			return new String[0];
		}
    }
    

    // Return the list of storage accounts in a resource group
    public String[] list(String resourceGroup) {
        ArrayList<StorageAccount> storageAccounts;
		try {
			
			storageAccounts = this.azure.storageManagementClient().getStorageAccountsOperations().listByResourceGroup(resourceGroup).getStorageAccounts();
			String[] names = new String[storageAccounts.size()];
			int i = 0;
			for(StorageAccount store: storageAccounts) {
				names[i++]= store.getName();
			}
			return names;
		} catch (IOException | ServiceException | URISyntaxException e) {
			// Not very actionable so return empty array
			return new String[0];
		}
    }
    

    // Gets storage account information
    public StorageAccount get(String resourceGroup, String name) throws Exception {
        return this.azure.storageManagementClient().getStorageAccountsOperations().getProperties(resourceGroup, name).getStorageAccount();
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
            if (!azure.resourceManagementClient().getResourceGroupsOperations().checkExistence(resourceGroup).isExists()) {
            	azure.resourceManagementClient().getResourceGroupsOperations().createOrUpdate(resourceGroup, new ResourceGroup("West US"));
            }
            final StorageAccountCreateParameters params = new StorageAccountCreateParameters();
            params.setLocation(this.region);
            params.setAccountType((this.type == null) ? AccountType.STANDARDLRS : this.type);
            azure.storageManagementClient().getStorageAccountsOperations().create(resourceGroup, name, params);
            return this;
        }

        // Apply changes to the storage account
        public StorageAccountImpl apply() throws Exception {
            StorageAccountUpdateParameters params = new StorageAccountUpdateParameters();
            params.setAccountType(this.type);
            params.setCustomDomain(customDomain);
            params.setTags(tags);
            azure.storageManagementClient().getStorageAccountsOperations().update(resourceGroup, name, params);
            return this;
        }

        // Deletes this storage account
        public void delete() throws Exception {
        	azure.storageManagementClient().getStorageAccountsOperations().delete(resourceGroup, name);
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
