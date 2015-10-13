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
package com.microsoft.azure.shortcuts.services;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.microsoft.azure.shortcuts.common.implementation.NamedRefreshableImpl;
import com.microsoft.azure.shortcuts.common.implementation.SupportsCreating;
import com.microsoft.azure.shortcuts.common.implementation.SupportsDeleting;
import com.microsoft.azure.shortcuts.common.implementation.SupportsListing;
import com.microsoft.azure.shortcuts.common.implementation.SupportsReading;
import com.microsoft.azure.shortcuts.common.implementation.SupportsUpdating;
import com.microsoft.azure.shortcuts.services.creation.StorageAccountDefinitionBlank;
import com.microsoft.azure.shortcuts.services.creation.StorageAccountDefinitionProvisionable;
import com.microsoft.azure.shortcuts.services.reading.StorageAccount;
import com.microsoft.azure.shortcuts.services.updating.StorageAccountUpdatable;
import com.microsoft.azure.shortcuts.services.updating.StorageAccountUpdatableBlank;
import com.microsoft.windowsazure.exception.ServiceException;
import com.microsoft.windowsazure.management.storage.models.GeoRegionStatus;
import com.microsoft.windowsazure.management.storage.models.StorageAccountCreateParameters;
import com.microsoft.windowsazure.management.storage.models.StorageAccountGetResponse;
import com.microsoft.windowsazure.management.storage.models.StorageAccountProperties;
import com.microsoft.windowsazure.management.storage.models.StorageAccountStatus;
import com.microsoft.windowsazure.management.storage.models.StorageAccountTypes;
import com.microsoft.windowsazure.management.storage.models.StorageAccountUpdateParameters;

// Class encapsulating the API related to storage accounts
public class StorageAccounts implements 
	SupportsCreating<StorageAccountDefinitionBlank>, 
	SupportsUpdating<StorageAccountUpdatableBlank>,
	SupportsListing,
	SupportsReading<StorageAccount>,
	SupportsDeleting {
	
	final Azure azure;
	StorageAccounts(Azure azure) {
		this.azure = azure;
	}
	
	
	@Override
	// Starts a new storage account update
	public StorageAccountUpdatableBlank update(String name) {
		return new StorageAccountImpl(name, false);
	}

	
	@Override
	// Starts a new storage account definition
	public StorageAccountDefinitionBlank define(String name) {
		return new StorageAccountImpl(name, true);
	}
	
	
	@Override
	public void delete(String accountName) throws IOException, ServiceException {
		azure.storageManagementClient().getStorageAccountsOperations().delete(accountName);
	}
	
	
	@Override
	public List<String> list() {
		try {
			final ArrayList<com.microsoft.windowsazure.management.storage.models.StorageAccount> items = 
					azure.storageManagementClient().getStorageAccountsOperations().list().getStorageAccounts();
			ArrayList<String> names = new ArrayList<>();
			for(com.microsoft.windowsazure.management.storage.models.StorageAccount item : items) {
				names.add(item.getName());
			}
			return names;
		} catch (Exception e) {
			// Not very actionable, so just return an empty array
			return new ArrayList<>();
		}
	}
	
	
	@Override
	public StorageAccount get(String name) throws Exception {
		StorageAccountImpl storageAccount = new StorageAccountImpl(name, false);
		return storageAccount.refresh();
	}

	
	// Nested class encapsulating the API related to creating new storage accounts
	private class StorageAccountImpl 
		extends NamedRefreshableImpl<StorageAccount>
		implements 
			StorageAccountDefinitionBlank, 
			StorageAccountDefinitionProvisionable,
			StorageAccountUpdatable,
			StorageAccount {
		
		private String region, affinityGroup, type, label, description, geoPrimaryRegion, geoSecondaryRegion;
		private StorageAccountStatus status;
		private Calendar lastFailoverTime;
		private GeoRegionStatus geoPrimaryRegionStatus, geoSecondaryRegionStatus;
		public URI[] endpoints;
		
		private StorageAccountImpl(String name, boolean initialized) {
			super(name.toLowerCase(), initialized);
		}
		
		/***********************************************************
		 * Getters
		 * @throws Exception 
		 ***********************************************************/

		@Override
		public String description() throws Exception {
			ensureInitialized();
			return this.description;
		}

		@Override
		public String label() throws Exception {
			ensureInitialized();
			return this.label;
		}

		@Override
		public String geoPrimaryRegion() throws Exception {
			ensureInitialized();
			return this.geoPrimaryRegion;
		}

		@Override
		public GeoRegionStatus geoPrimaryRegionStatus() throws Exception {
			ensureInitialized();
			return this.geoPrimaryRegionStatus;
		}

		@Override
		public String geoSecondaryRegion() throws Exception {
			ensureInitialized();
			return this.geoSecondaryRegion;
		}

		@Override
		public GeoRegionStatus geoSecondaryRegionStatus() throws Exception {
			ensureInitialized();
			return this.geoSecondaryRegionStatus;
		}

		@Override
		public String region() throws Exception {
			ensureInitialized();
			return this.region;
		}

		@Override
		public StorageAccountStatus status() throws Exception {
			ensureInitialized();
			return this.status;
		}

		@Override
		public Calendar lastGeoFailoverTime() throws Exception {
			ensureInitialized();
			return this.lastFailoverTime;
		}

		@Override
		public URI[] endpoints() throws Exception {
			ensureInitialized();
			return this.endpoints;
		}

		@Override
		public String type() throws Exception {
			ensureInitialized();
			return this.type;
		}


		@Override
		public String affinityGroup() throws Exception {
			ensureInitialized();
			return this.affinityGroup;
		}


		/**************************************************************
		 * Setters (fluent interface)
		 **************************************************************/

		@Override
		public StorageAccountImpl withRegion(String region) {
			this.region =region;
			return this;
		}
					
		@Override
		public StorageAccountImpl withType(String type) {
			this.type = type;
			return this;
		}
		
		@Override
		public StorageAccountImpl withLabel(String label) {
			this.label= label;
			return this;
		}
		
		@Override
		public StorageAccountImpl withDescription(String description) {
			this.description = description;
			return this;
		}

		
		/************************************************************
		 * Verbs
		 ************************************************************/

		@Override
		public StorageAccountImpl provision() throws Exception {
			final StorageAccountCreateParameters params = new StorageAccountCreateParameters();
			params.setName(this.name.toLowerCase());
			params.setLocation(this.region);
			params.setAffinityGroup(this.affinityGroup);
			params.setDescription(this.description);
			params.setLabel((this.label == null) ? this.name : this.label);
			params.setAccountType((this.type == null) ? StorageAccountTypes.STANDARD_LRS : this.type);
			azure.storageManagementClient().getStorageAccountsOperations().create(params);
			return this;
		}
		
	
		@Override
		public StorageAccountImpl apply() throws Exception {
			StorageAccountUpdateParameters params = new StorageAccountUpdateParameters();
			params.setAccountType(this.type);
			params.setDescription(this.description);
			params.setLabel(this.label);
			azure.storageManagementClient().getStorageAccountsOperations().update(this.name, params);
			return this;
		}

		
		@Override
		public void delete() throws Exception {
			azure.storageAccounts.delete(this.name);
		}

		
		@Override
		public StorageAccount refresh() throws Exception {
			StorageAccountGetResponse response = azure.storageManagementClient().getStorageAccountsOperations().get(this.name);
			StorageAccountProperties properties =  response.getStorageAccount().getProperties();
			this.affinityGroup = properties.getAffinityGroup();
			this.description = properties.getDescription();
			this.label = properties.getLabel();
			this.geoPrimaryRegion = properties.getGeoPrimaryRegion();
			this.geoSecondaryRegion = properties.getGeoSecondaryRegion();
			this.region = properties.getLocation();
			this.status = properties.getStatus();
			this.lastFailoverTime = properties.getLastGeoFailoverTime();
			this.geoPrimaryRegionStatus = properties.getStatusOfGeoPrimaryRegion();
			this.geoSecondaryRegionStatus = properties.getStatusOfGeoSecondaryRegion();
			this.endpoints = properties.getEndpoints().toArray(new URI[0]);
			this.type = properties.getAccountType();
			this.initialized = true;
			return this;
		}
	}		
}
