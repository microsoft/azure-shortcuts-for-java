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

import com.microsoft.azure.shortcuts.services.listing.CloudServices;
import com.microsoft.azure.shortcuts.services.listing.Networks;
import com.microsoft.azure.shortcuts.services.listing.OsImages;
import com.microsoft.azure.shortcuts.services.listing.Regions;
import com.microsoft.azure.shortcuts.services.listing.Sizes;
import com.microsoft.azure.shortcuts.services.listing.StorageAccounts;
import com.microsoft.windowsazure.Configuration;
import com.microsoft.windowsazure.management.configuration.PublishSettingsLoader;
import com.microsoft.windowsazure.management.ManagementClient;
import com.microsoft.windowsazure.management.ManagementService;
import com.microsoft.windowsazure.management.network.NetworkManagementClient;
import com.microsoft.windowsazure.management.network.NetworkManagementService;
import com.microsoft.windowsazure.management.storage.StorageManagementClient;
import com.microsoft.windowsazure.management.storage.StorageManagementService;
import com.microsoft.windowsazure.management.compute.ComputeManagementClient;
import com.microsoft.windowsazure.management.compute.ComputeManagementService;

public class Azure {
	private Configuration configuration= null;
	private ManagementClient management = null;
	private ComputeManagementClient compute = null;
	private StorageManagementClient storage = null;
	private NetworkManagementClient networking = null;
	
	private final RegionsImpl regions = new RegionsImpl(this);
	private final SizesImpl sizes = new SizesImpl(this);
	private final OSImagesImpl osImages = new OSImagesImpl(this);
	private final StorageAccountsImpl storageAccounts = new StorageAccountsImpl(this);
	private final CloudServicesImpl cloudServices = new CloudServicesImpl(this);
	private final NetworksImpl networks = new NetworksImpl(this);
	public final VirtualMachines virtualMachines = new VirtualMachines(this);
	
	// Construct based on credentials from a publishsettings file for the selected subscription
	public Azure(String publishSettingsPath, String subscriptionId) throws IOException {
		this.configuration = PublishSettingsLoader.createManagementConfiguration(publishSettingsPath, subscriptionId);
	}
	
	
	/******************************************************
	 * Getters
	 ******************************************************/
	
	/**
	 * @return The interface exposing functionality related to cloud services
	 */
	public CloudServices cloudServices() {
		return this.cloudServices;
	}
	
	
	/**
	 * @return The interface exposing functionality related to virtual networks
	 */
	public Networks networks() {
		return this.networks;
	}
	
	
	/**
	 * @return The interface exposing functionality related to OS images
	 */
	public OsImages osImages() {
		return this.osImages;
	}
	
	
	/**
	 * @return The interface exposing functionality related to OS images
	 */
	public Regions regions() {
		return this.regions;
	}
	
	/**
	 * @return The interface exposing functionality related to virtual machine sizes
	 */
	public Sizes sizes() {
		return this.sizes;
	}
	
	
	/**
	 * @return The interface exposing functionality related to storage accounts
	 */
	public StorageAccounts storageAccounts() {
		return this.storageAccounts;
	}
	
	
	// Returns the management client, creating it as needed
	ManagementClient managementClient() {
		if(this.management == null) {
			this.management = ManagementService.create(configuration);
		}
		
		return this.management;
	}
	
	
	// Returns the compute management client, creating it as needed
	ComputeManagementClient computeManagementClient() {
		if(this.compute == null) {
			this.compute = ComputeManagementService.create(configuration);
		}
		
		return this.compute;
	}
	
	
	// Returns the storage management client, creating it as needed
	StorageManagementClient storageManagementClient() {
		if(this.storage == null) {
			this.storage = StorageManagementService.create(configuration);
		}
		
		return this.storage;
	}
	
	
	// Returns the network management client, creating as needed
	NetworkManagementClient networkManagementClient() {
		if(this.networking == null) {
			this.networking = NetworkManagementService.create(configuration);
		}
		
		return this.networking;
	}
}
