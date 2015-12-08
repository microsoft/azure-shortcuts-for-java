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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.microsoft.azure.shortcuts.common.implementation.EntitiesImpl;
import com.microsoft.azure.shortcuts.resources.StorageAccount;
import com.microsoft.azure.shortcuts.resources.StorageAccounts;


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
		for(com.microsoft.azure.management.storage.models.StorageAccount nativeItem : getAzureStorageAccounts(groupName)) {
			wrappers.put(nativeItem.getId(), new StorageAccountImpl(nativeItem));
		}
		
		return Collections.unmodifiableMap(wrappers);
	}

	
	@Override
	public StorageAccount get(String resourceId) throws Exception {
		return this.get(
			ResourcesImpl.groupFromResourceId(resourceId), 
			ResourcesImpl.nameFromResourceId(resourceId));
	}
	

	@Override
	public StorageAccount get(String groupName, String name) throws Exception {
		return new StorageAccountImpl(this.getAzureStorageAccount(groupName, name));
	}

	
	/***************************************************
	 * Helpers
	 ***************************************************/
	
	// Helper to get the storage accounts from Azure
	private ArrayList<com.microsoft.azure.management.storage.models.StorageAccount> getAzureStorageAccounts(String resourceGroupName) throws Exception {
		if(resourceGroupName == null) {
			return this.azure.storageManagementClient().getStorageAccountsOperations().list().getStorageAccounts();
		} else {
			return this.azure.storageManagementClient().getStorageAccountsOperations().listByResourceGroup(resourceGroupName).getStorageAccounts();
		}
	}
	
	// Helper to get a storage account from Azure
	private com.microsoft.azure.management.storage.models.StorageAccount getAzureStorageAccount(String groupName, String name) throws Exception {
		return azure.storageManagementClient().getStorageAccountsOperations().getProperties(groupName, name).getStorageAccount();		
	}
	
	
	/***************************************************************
	 * Implements logic for individual resource group
	 ***************************************************************/
	private class StorageAccountImpl 
		extends 
			ResourceBaseExtendedImpl<StorageAccount, com.microsoft.azure.management.storage.models.StorageAccount>
		implements
			StorageAccount {
		
		private StorageAccountImpl(com.microsoft.azure.management.storage.models.StorageAccount azureStorageAccount) {
			super(azureStorageAccount.getId(), azureStorageAccount);
		}


		/***********************************************************
		 * Getters
		 ***********************************************************/
		
		/**************************************************************
		 * Setters (fluent interface)
		 **************************************************************/
		

		/************************************************************
		 * Verbs
		 ************************************************************/
		
		@Override
		public StorageAccountImpl refresh() throws Exception {
			this.setInner(getAzureStorageAccount(
					ResourcesImpl.groupFromResourceId(this.id()), 
					ResourcesImpl.nameFromResourceId(this.id())));
			return this;
		}		
	}
}
