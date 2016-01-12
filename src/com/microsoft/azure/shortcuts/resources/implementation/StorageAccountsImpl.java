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
import java.util.List;
import java.util.Map;

import com.microsoft.azure.management.storage.models.AccountType;
import com.microsoft.azure.management.storage.models.StorageAccountCreateParameters;
import com.microsoft.azure.shortcuts.resources.Region;
import com.microsoft.azure.shortcuts.resources.StorageAccount;
import com.microsoft.azure.shortcuts.resources.StorageAccounts;
import com.microsoft.azure.shortcuts.resources.common.implementation.GroupableResourceBaseImpl;
import com.microsoft.azure.shortcuts.resources.common.implementation.GroupableResourcesBaseImpl;


public class StorageAccountsImpl 
	extends GroupableResourcesBaseImpl<Azure, StorageAccount, com.microsoft.azure.management.storage.models.StorageAccount>
	implements StorageAccounts {
	
	StorageAccountsImpl(Azure azure) {
		super(azure);
	}
	
	@Override
	public StorageAccountImpl define(String name) throws Exception {
		com.microsoft.azure.management.storage.models.StorageAccount nativeItem = new com.microsoft.azure.management.storage.models.StorageAccount();
		nativeItem.setName(name);
		return createWrapper(nativeItem);
	}
	
	@Override
	public void delete(String groupName, String name) throws Exception {
		azure.storageManagementClient().getStorageAccountsOperations().delete(groupName, name);
	}


	
	/***************************************************
	 * Helpers
	 ***************************************************/
	
	@Override
	protected List<com.microsoft.azure.management.storage.models.StorageAccount> getNativeEntities(String resourceGroupName) throws Exception {
		if(resourceGroupName == null) {
			return this.azure.storageManagementClient().getStorageAccountsOperations().list().getStorageAccounts();
		} else {
			return this.azure.storageManagementClient().getStorageAccountsOperations().listByResourceGroup(resourceGroupName).getStorageAccounts();
		}
	}
	
	@Override
	protected com.microsoft.azure.management.storage.models.StorageAccount getNativeEntity(String groupName, String name) throws Exception {
		return azure.storageManagementClient().getStorageAccountsOperations().getProperties(groupName, name).getStorageAccount();		
	}
	
	@Override
	protected StorageAccountImpl createWrapper(com.microsoft.azure.management.storage.models.StorageAccount nativeItem) {
		return new StorageAccountImpl(nativeItem);
	}
	
	
	/***************************************************************
	 * Implements logic for individual resource group
	 ***************************************************************/
	private class StorageAccountImpl 
		extends 
			GroupableResourceBaseImpl<
				StorageAccount, 
				com.microsoft.azure.management.storage.models.StorageAccount,
				StorageAccountImpl>
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
		public StorageAccount provision() throws Exception {
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
