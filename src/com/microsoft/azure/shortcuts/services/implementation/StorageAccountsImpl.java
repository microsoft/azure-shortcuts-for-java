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
package com.microsoft.azure.shortcuts.services.implementation;

import java.net.URI;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.microsoft.azure.shortcuts.common.implementation.EntitiesImpl;
import com.microsoft.azure.shortcuts.common.implementation.IndexableRefreshableWrapperImpl;
import com.microsoft.azure.shortcuts.services.Region;
import com.microsoft.azure.shortcuts.services.StorageAccount;
import com.microsoft.azure.shortcuts.services.StorageAccounts;
import com.microsoft.windowsazure.management.storage.models.GeoRegionStatus;
import com.microsoft.windowsazure.management.storage.models.StorageAccountCreateParameters;
import com.microsoft.windowsazure.management.storage.models.StorageAccountGetResponse;
import com.microsoft.windowsazure.management.storage.models.StorageAccountProperties;
import com.microsoft.windowsazure.management.storage.models.StorageAccountStatus;
import com.microsoft.windowsazure.management.storage.models.StorageAccountTypes;
import com.microsoft.windowsazure.management.storage.models.StorageAccountUpdateParameters;

// Class encapsulating the API related to storage accounts
public class StorageAccountsImpl 
	extends EntitiesImpl<Azure>
	implements StorageAccounts {
	
	StorageAccountsImpl(Azure azure) {
		super(azure);
	}
	
	
	@Override
	// Starts a new storage account update
	public StorageAccountImpl update(String name) {
		return createStorageAccountWrapper(name);
	}

	
	@Override
	// Starts a new storage account definition
	public StorageAccountImpl define(String name) {
		return createStorageAccountWrapper(name);
	}
	
	
	@Override
	public void delete(String accountName) throws Exception {
		azure.storageManagementClient().getStorageAccountsOperations().delete(accountName);
	}
	
	
	@Override
	public StorageAccountImpl get(String name) throws Exception {
		return createStorageAccountWrapper(name).refresh();
	}


	@Override
	public Map<String, StorageAccount> list() throws Exception {
		HashMap<String, StorageAccount> wrappers = new HashMap<>();
		for(com.microsoft.windowsazure.management.storage.models.StorageAccount nativeItem : getAzureStorageAccounts()) {
			StorageAccountImpl wrapper = new StorageAccountImpl(nativeItem);
			wrappers.put(nativeItem.getName(), wrapper);
		}
		
		return Collections.unmodifiableMap(wrappers);
	}		

	
	/****************************************************
	 * Helpers
	 ****************************************************/

	// Create a storage account wrapper
	private StorageAccountImpl createStorageAccountWrapper(String name) {
		com.microsoft.windowsazure.management.storage.models.StorageAccount azureStorageAccount = 
			new com.microsoft.windowsazure.management.storage.models.StorageAccount();
		azureStorageAccount.setName(name);
		azureStorageAccount.setProperties(new StorageAccountProperties());
		return new StorageAccountImpl(azureStorageAccount);
	}

	
	// List native storage accounts
	private ArrayList<com.microsoft.windowsazure.management.storage.models.StorageAccount> getAzureStorageAccounts() throws Exception {
		return azure.storageManagementClient().getStorageAccountsOperations().list().getStorageAccounts();
	}
	


	// Nested class encapsulating the API related to creating new storage accounts
	private class StorageAccountImpl 
		extends IndexableRefreshableWrapperImpl<StorageAccount, com.microsoft.windowsazure.management.storage.models.StorageAccount>
		implements 
			StorageAccount.DefinitionBlank, 
			StorageAccount.DefinitionProvisionable,
			StorageAccount.Update,
			StorageAccount {
		
		public StorageAccountImpl(com.microsoft.windowsazure.management.storage.models.StorageAccount azureStorageAccount) {
			super(azureStorageAccount.getName(), azureStorageAccount);
		}

		/***********************************************************
		 * Getters
		 * @throws Exception 
		 ***********************************************************/

		@Override
		public String description() throws Exception {
			return this.inner().getProperties().getDescription();
		}

		@Override
		public String label() throws Exception {
			return this.inner().getProperties().getLabel();
		}

		@Override
		public String geoPrimaryRegion() throws Exception {
			return this.inner().getProperties().getGeoPrimaryRegion();
		}

		@Override
		public GeoRegionStatus geoPrimaryRegionStatus() throws Exception {
			return this.inner().getProperties().getStatusOfGeoPrimaryRegion();
		}

		@Override
		public String geoSecondaryRegion() throws Exception {
			return this.inner().getProperties().getGeoSecondaryRegion();
		}

		@Override
		public GeoRegionStatus geoSecondaryRegionStatus() throws Exception {
			return this.inner().getProperties().getStatusOfGeoSecondaryRegion();
		}

		@Override
		public String region() throws Exception {
			return this.inner().getProperties().getLocation();
		}

		@Override
		public StorageAccountStatus status() throws Exception {
			return this.inner().getProperties().getStatus();
		}

		@Override
		public Calendar lastGeoFailoverTime() throws Exception {
			return this.inner().getProperties().getLastGeoFailoverTime();
		}

		@Override
		public List<URI> endpoints() throws Exception {
			return Collections.unmodifiableList(this.inner().getProperties().getEndpoints());
		}

		@Override
		public String type() throws Exception {
			return this.inner().getProperties().getAccountType();
		}


		@Override
		public String affinityGroup() throws Exception {
			return this.inner().getProperties().getAffinityGroup();
		}


		/**************************************************************
		 * Setters (fluent interface)
		 **************************************************************/

		@Override
		public StorageAccountImpl withRegion(String region) {
			this.inner().getProperties().setLocation(region);
			return this;
		}
		
		@Override
		public StorageAccountImpl withRegion(Region region) {
			return this.withRegion(region.name());
		}

		@Override
		public StorageAccountImpl withType(String type) {
			this.inner().getProperties().setAccountType(type);
			return this;
		}
		
		@Override
		public StorageAccountImpl withLabel(String label) {
			this.inner().getProperties().setLabel(label);
			return this;
		}
		
		@Override
		public StorageAccountImpl withDescription(String description) {
			this.inner().getProperties().setDescription(description);
			return this;
		}

		
		/************************************************************
		 * Verbs
		 ************************************************************/

		@Override
		public StorageAccountImpl provision() throws Exception {
			final StorageAccountCreateParameters params = new StorageAccountCreateParameters();
			params.setName(this.inner().getName().toLowerCase());
			params.setLocation(this.region());
			params.setAffinityGroup(this.affinityGroup());
			params.setDescription(this.description());
			params.setLabel((this.label() == null) ? this.name() : this.label());
			params.setAccountType((this.type()== null) ? StorageAccountTypes.STANDARD_LRS : this.type());
			azure.storageManagementClient().getStorageAccountsOperations().create(params);
			return this;
		}
		
	
		@Override
		public StorageAccountImpl apply() throws Exception {
			StorageAccountUpdateParameters params = new StorageAccountUpdateParameters();
			params.setAccountType(this.type());
			params.setDescription(this.description());
			params.setLabel(this.label());
			azure.storageManagementClient().getStorageAccountsOperations().update(this.name().toLowerCase(), params);
			return this;
		}

		
		@Override
		public void delete() throws Exception {
			azure.storageAccounts().delete(this.id);
		}

		
		@Override
		public StorageAccountImpl refresh() throws Exception {
			StorageAccountGetResponse response = azure.storageManagementClient().getStorageAccountsOperations().get(this.inner().getName());
			StorageAccountProperties newProperties =  response.getStorageAccount().getProperties();
			this.inner().setProperties(newProperties);
			return this;
		}
	}
}
