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
package com.microsoft.azure.shortcuts.resources.implementation;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.microsoft.azure.management.resources.models.ResourceGroupExtended;
import com.microsoft.azure.management.storage.models.AccountType;
import com.microsoft.azure.management.storage.models.StorageAccountCreateParameters;
import com.microsoft.azure.shortcuts.common.implementation.EntitiesImpl;
import com.microsoft.azure.shortcuts.resources.Group;
import com.microsoft.azure.shortcuts.resources.Region;
import com.microsoft.azure.shortcuts.resources.StorageAccount;
import com.microsoft.azure.shortcuts.resources.StorageAccounts;
import com.microsoft.azure.shortcuts.resources.common.implementation.GroupResourceBaseImpl;


public class StorageAccountsImpl 
	extends EntitiesImpl<Azure>
	implements StorageAccounts {
	
	List<StorageAccount> storageAccounts = null;
	
	StorageAccountsImpl(Azure azure) {
		super(azure);
	}
	
	
	@Override
	public Map<String, StorageAccount> list() throws Exception {
		return this.list(null);
	}

	
	@Override
	public Map<String, StorageAccount> list(String groupName) throws Exception {
		HashMap<String, StorageAccount> wrappers = new HashMap<>();
		for(com.microsoft.azure.management.storage.models.StorageAccount nativeItem : getNativeEntities(groupName)) {
			wrappers.put(nativeItem.getId(), new StorageAccountImpl(nativeItem));
		}
		
		return Collections.unmodifiableMap(wrappers);
	}

	
	@Override
	public StorageAccountImpl get(String resourceId) throws Exception {
		return this.get(
			ResourcesImpl.groupFromResourceId(resourceId), 
			ResourcesImpl.nameFromResourceId(resourceId));
	}
	

	@Override
	public StorageAccountImpl get(String groupName, String name) throws Exception {
		return new StorageAccountImpl(this.getNativeEntity(groupName, name));
	}


	@Override
	public StorageAccountImpl define(String name) throws Exception {
		com.microsoft.azure.management.storage.models.StorageAccount nativeItem = new com.microsoft.azure.management.storage.models.StorageAccount();
		nativeItem.setName(name);
		return new StorageAccountImpl(nativeItem);
	}
	
	@Override
	public void delete(String id) throws Exception {
		this.delete(
			ResourcesImpl.groupFromResourceId(id),
			ResourcesImpl.nameFromResourceId(id));
	}


	@Override
	public void delete(String groupName, String name) throws Exception {
		azure.storageManagementClient().getStorageAccountsOperations().delete(groupName, name);
	}


	
	/***************************************************
	 * Helpers
	 ***************************************************/
	
	// Helper to get the storage accounts from Azure
	private ArrayList<com.microsoft.azure.management.storage.models.StorageAccount> getNativeEntities(String resourceGroupName) throws Exception {
		if(resourceGroupName == null) {
			return this.azure.storageManagementClient().getStorageAccountsOperations().list().getStorageAccounts();
		} else {
			return this.azure.storageManagementClient().getStorageAccountsOperations().listByResourceGroup(resourceGroupName).getStorageAccounts();
		}
	}
	
	// Helper to get a storage account from Azure
	private com.microsoft.azure.management.storage.models.StorageAccount getNativeEntity(String groupName, String name) throws Exception {
		return azure.storageManagementClient().getStorageAccountsOperations().getProperties(groupName, name).getStorageAccount();		
	}
	
	
	/***************************************************************
	 * Implements logic for individual resource group
	 ***************************************************************/
	private class StorageAccountImpl 
		extends 
			GroupResourceBaseImpl<StorageAccount, com.microsoft.azure.management.storage.models.StorageAccount>
		implements
			StorageAccount,
			StorageAccount.DefinitionBlank,
			StorageAccount.DefinitionProvisionable {
		
		private StorageAccountImpl(com.microsoft.azure.management.storage.models.StorageAccount azureStorageAccount) {
			super(azureStorageAccount.getId(), azureStorageAccount);
		}


		/***********************************************************
		 * Getters
		 ***********************************************************/
		
		@Override
		public URL primaryBlobEndpoint() {
			try {
				return this.inner().getPrimaryEndpoints().getBlob().toURL();
			} catch (MalformedURLException e) {
				return null;
			}
		}
		
		@Override
		public AccountType accountType() {
			return this.inner().getAccountType();
		}
		
		
		/**************************************************************
		 * Setters (fluent interface)
		 **************************************************************/
		
		@Override
		public StorageAccountImpl withGroupExisting(String groupName) {
			super.withGroupExisting(groupName);
			return this;
		}


		@Override
		public StorageAccountImpl withGroupExisting(Group group) {
			return this.withGroupExisting(group.name());
		}


		@Override
		public StorageAccountImpl withGroupExisting(ResourceGroupExtended group) {
			return this.withGroupExisting(group.getName());
		}


		@Override
		public StorageAccountImpl withGroupNew(String name) {
			super.withGroupNew(name);
			return this;
		}
		

		@Override
		public StorageAccountImpl withAccountType(AccountType type) {
			this.inner().setAccountType(type);
			return this;
		}


		@Override
		public StorageAccountImpl withRegion(String regionName) {
			super.withRegion(regionName);
			return this;
		}
		
		@Override
		public StorageAccountImpl withRegion(Region region) {
			return this.withRegion(region.toString());
		}


		@Override
		public StorageAccountImpl withTags(Map<String, String> tags) {
			super.withTags(tags);
			return this;
		}


		@Override
		public StorageAccountImpl withTag(String name, String value) {
			super.withTag(name, value);
			return this;
		}
		
		
		@Override
		public StorageAccountImpl withoutTag(String name) {
			super.withoutTag(name);
			return this;
		}

		
		/************************************************************
		 * Verbs
		 ************************************************************/

		@Override
		public StorageAccountImpl provision() throws Exception {
			// Create group if needed
			ensureGroup(azure);

			// Assume default account type if needed
			if(this.accountType() == null) {
				this.withAccountType(AccountType.StandardLRS);
			}
			
			StorageAccountCreateParameters params = new StorageAccountCreateParameters();
			params.setLocation(this.region());
			params.setAccountType(this.accountType());
			params.setTags(this.inner().getTags());

			azure.storageManagementClient().getStorageAccountsOperations().create(this.groupName, this.name(), params);
			return get(this.groupName, this.name());
		}


		@Override
		public StorageAccountImpl refresh() throws Exception {
			this.setInner(getNativeEntity(
				ResourcesImpl.groupFromResourceId(this.id()), 
				ResourcesImpl.nameFromResourceId(this.id())));
			return this;
		}


		@Override
		public void delete() throws Exception {
			azure.storageAccounts().delete(this.id());
		}
	}
}
