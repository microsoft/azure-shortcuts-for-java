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

import com.microsoft.azure.shortcuts.common.implementation.NamedImpl;
import com.microsoft.azure.shortcuts.common.implementation.SupportsListing;
import com.microsoft.azure.shortcuts.common.implementation.SupportsReading;
import com.microsoft.azure.shortcuts.services.creation.StorageAccountDefinitionBlank;
import com.microsoft.azure.shortcuts.services.creation.StorageAccountDefinitionProvisionable;
import com.microsoft.azure.shortcuts.services.implementation.SupportsCreating;
import com.microsoft.azure.shortcuts.services.implementation.SupportsDeleting;
import com.microsoft.azure.shortcuts.services.implementation.SupportsUpdating;
import com.microsoft.azure.shortcuts.services.reading.StorageAccount;
import com.microsoft.azure.shortcuts.services.updating.StorageAccountUpdatable;
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
	SupportsUpdating<StorageAccountUpdatable>,
	SupportsListing,
	SupportsReading<StorageAccount>,
	SupportsDeleting {
	
	final Azure azure;
	StorageAccounts(Azure azure) {
		this.azure = azure;
	}
	
	// Starts a new storage account update
	public StorageAccountUpdatable update(String name) {
		return new StorageAccountImpl(name);
	}

	
	// Starts a new storage account definition
	public StorageAccountDefinitionBlank define(String name) {
		return new StorageAccountImpl(name);
	}
	
	
	// Deletes the specified storage account
	public void delete(String accountName) throws IOException, ServiceException {
		azure.storageManagementClient().getStorageAccountsOperations().delete(accountName);
	}
	
	
	// Return the list of storage accounts
	public String[] list() {
		try {
			final ArrayList<com.microsoft.windowsazure.management.storage.models.StorageAccount> storageAccounts = 
					azure.storageManagementClient().getStorageAccountsOperations().list().getStorageAccounts();
			String[] names = new String[storageAccounts.size()];
			int i = 0;
			for(com.microsoft.windowsazure.management.storage.models.StorageAccount store: storageAccounts) {
				names[i++]= store.getName();
			}
			return names;
		} catch (Exception e) {
			// Not very actionable, so just return an empty array
			return new String[0];
		}
	}
	
	
	// Gets storage account information
	public StorageAccount get(String name) throws Exception {
		StorageAccountImpl storageAccount = new StorageAccountImpl(name);
		StorageAccountGetResponse response = azure.storageManagementClient().getStorageAccountsOperations().get(name);
		StorageAccountProperties properties =  response.getStorageAccount().getProperties();
		storageAccount.affinityGroup = properties.getAffinityGroup();
		storageAccount.description = properties.getDescription();
		storageAccount.label = properties.getLabel();
		storageAccount.geoPrimaryRegion = properties.getGeoPrimaryRegion();
		storageAccount.geoSecondaryRegion = properties.getGeoSecondaryRegion();
		storageAccount.region = properties.getLocation();
		storageAccount.status = properties.getStatus();
		storageAccount.lastFailoverTime = properties.getLastGeoFailoverTime();
		storageAccount.geoPrimaryRegionStatus = properties.getStatusOfGeoPrimaryRegion();
		storageAccount.geoSecondaryRegionStatus = properties.getStatusOfGeoSecondaryRegion();
		storageAccount.endpoints = properties.getEndpoints().toArray(new URI[0]);
		storageAccount.type = properties.getAccountType();
		return storageAccount;
	}

	
	// Nested class encapsulating the API related to creating new storage accounts
	private class StorageAccountImpl 
		extends NamedImpl
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
		
		private StorageAccountImpl(String name) {
			super(name.toLowerCase());
		}
		
		// Creates a new storage account
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
		
					
		// Apply changes to the storage account
		public StorageAccountImpl apply() throws Exception {
			StorageAccountUpdateParameters params = new StorageAccountUpdateParameters();
			params.setAccountType(this.type);
			params.setDescription(this.description);
			params.setLabel(this.label);
			azure.storageManagementClient().getStorageAccountsOperations().update(this.name, params);
			return this;
		}

		
		// Deletes this storage account
		public void delete() throws Exception {
			azure.storageAccounts.delete(this.name);
		}

		
		public StorageAccountImpl withRegion(String region) {
			this.region =region;
			return this;
		}
					
		public StorageAccountImpl withType(String type) {
			this.type = type;
			return this;
		}
		
		public StorageAccountImpl withLabel(String label) {
			this.label= label;
			return this;
		}
		
		public StorageAccountImpl withDescription(String description) {
			this.description = description;
			return this;
		}

		public String description() {
			return this.description;
		}

		public String label() {
			return this.label;
		}

		public String geoPrimaryRegion() {
			return this.geoPrimaryRegion;
		}

		public GeoRegionStatus geoPrimaryRegionStatus() {
			return this.geoPrimaryRegionStatus;
		}

		public String geoSecondaryRegion() {
			return this.geoSecondaryRegion;
		}

		public GeoRegionStatus geoSecondaryRegionStatus() {
			return this.geoSecondaryRegionStatus;
		}

		public String region() {
			return this.region;
		}

		public StorageAccountStatus status() {
			return this.status;
		}

		public Calendar lastGeoFailoverTime() {
			return this.lastFailoverTime;
		}

		public URI[] endpoints() {
			return this.endpoints;
		}

		public String type() {
			return this.type;
		}


		public String affinityGroup() {
			return this.affinityGroup;
		}

	}		
}
