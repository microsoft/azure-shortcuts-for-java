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
import com.microsoft.azure.management.resources.models.ResourceGroup;
import com.microsoft.azure.management.storage.StorageManagementClient;
import com.microsoft.azure.management.storage.StorageManagementService;
import com.microsoft.azure.shortcuts.services.Utils;
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

    public Azure(String subscriptionId, String tenantId, String clientId, String clientKey) throws Exception {
        this.configuration = Utils.createConfiguration(subscriptionId, tenantId, clientId, clientKey);

        resourceManagementClient = ResourceManagementService.create(configuration);
        storageManagementClient = StorageManagementService.create(configuration);
        computeManagementClient = ComputeManagementService.create(configuration);
        networkResourceProviderClient = NetworkResourceProviderService.create(configuration);

        storageAccounts = new StorageAccounts(storageManagementClient, resourceManagementClient);
    }

    public Azure(String publishSettingsPath, String subscriptionId) throws IOException, ServiceException, URISyntaxException {
        this.configuration = PublishSettingsLoader.createManagementConfiguration(publishSettingsPath, subscriptionId);
        resourceManagementClient = ResourceManagementService.create(configuration);
        storageManagementClient = StorageManagementService.create(configuration);
        computeManagementClient = ComputeManagementService.create(configuration);
        networkResourceProviderClient = NetworkResourceProviderService.create(configuration);

        storageAccounts = new StorageAccounts(storageManagementClient, resourceManagementClient);
    }

}
