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

import com.microsoft.azure.management.compute.ComputeManagementClient;
import com.microsoft.azure.management.compute.ComputeManagementService;
import com.microsoft.azure.management.network.NetworkResourceProviderClient;
import com.microsoft.azure.management.network.NetworkResourceProviderService;
import com.microsoft.azure.management.resources.ResourceManagementClient;
import com.microsoft.azure.management.resources.ResourceManagementService;
import com.microsoft.azure.management.storage.StorageManagementClient;
import com.microsoft.azure.management.storage.StorageManagementService;
import com.microsoft.azure.shortcuts.common.Utils;
import com.microsoft.windowsazure.Configuration;
import com.microsoft.windowsazure.exception.ServiceException;
import com.microsoft.windowsazure.management.configuration.PublishSettingsLoader;

import java.io.IOException;
import java.net.URISyntaxException;


public class Azure {
    public static String MANAGEMENT_URI = "https://management.core.windows.net/";
    public static String ARM_URL = "https://management.azure.com/";
    public static String ARM_AAD_URL = "https://login.windows.net/";


    private Configuration configuration;
    private ResourceManagementClient resourceManagementClient;
    private StorageManagementClient storageManagementClient;
    private ComputeManagementClient computeManagementClient;
    private NetworkResourceProviderClient networkResourceProviderClient;

    public final StorageAccounts storageAccounts;
    public final Resources resources;

    public Azure(String subscriptionId, String tenantId, String clientId, String clientKey) throws Exception {
    	this(Utils.createConfiguration(subscriptionId, tenantId, clientId, clientKey));
    }

    public Azure(String publishSettingsPath, String subscriptionId) throws IOException, ServiceException, URISyntaxException {
    	this(PublishSettingsLoader.createManagementConfiguration(publishSettingsPath, subscriptionId));
    }
    
    private Azure(Configuration configuration) {
    	this.configuration = configuration;
        this.storageAccounts = new StorageAccounts(this);
        this.resources = new Resources(this);
    }
    
    
    // Returns the compute management client, creating if needed
    ComputeManagementClient computeManagementClient() {
    	if(this.computeManagementClient == null) {
    		this.computeManagementClient = ComputeManagementService.create(configuration);
    	}
    	
    	return this.computeManagementClient;
    }
    
    
    // Returns the network management client, creating if needed
    NetworkResourceProviderClient getNetworkManagementClient() {
    	if(this.networkResourceProviderClient == null) {
    		this.networkResourceProviderClient = NetworkResourceProviderService.create(configuration);
    	}
    	
    	return this.networkResourceProviderClient;
    }
    
    
    // Returns the resource management client, creating if needed
    ResourceManagementClient resourceManagementClient() {
    	if(this.resourceManagementClient == null) {
    		this.resourceManagementClient = ResourceManagementService.create(configuration);
    	}
    	
    	return this.resourceManagementClient;

    }

    
    // Returns the storage management client
    StorageManagementClient storageManagementClient() {
    	if(this.storageManagementClient == null) {
    		this.storageManagementClient = StorageManagementService.create(configuration);
    	}
    	
    	return this.storageManagementClient;
    }
}
