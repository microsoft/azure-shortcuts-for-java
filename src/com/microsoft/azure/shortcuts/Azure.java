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

package com.microsoft.azure.shortcuts;

import java.io.IOException;

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
	ManagementClient management = null;
	ComputeManagementClient compute = null;
	StorageManagementClient storage = null;
	NetworkManagementClient networking = null;
	
	public final Regions regions = new Regions(this);
	public final Sizes sizes = new Sizes(this);
	public final OSImages osImages = new OSImages(this);
	public final StorageAccounts storageAccounts = new StorageAccounts(this);
	public final CloudServices cloudServices = new CloudServices(this);
	public final Networks networks = new Networks(this);
	public final VirtualMachines virtualMachines = new VirtualMachines(this);
	
	// Construct based on credentials from a publishsettings file for the selected subscription
	public Azure(String publishSettingsPath, String subscriptionId) throws IOException {
		this.configuration = PublishSettingsLoader.createManagementConfiguration(publishSettingsPath, subscriptionId);
		this.management = ManagementService.create(configuration);
		this.compute = ComputeManagementService.create(configuration);
		this.storage = StorageManagementService.create(configuration);
		this.networking = NetworkManagementService.create(configuration);		
	}


}
