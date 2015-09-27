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
import com.microsoft.azure.shortcuts.Utils;
import com.microsoft.windowsazure.Configuration;
import com.microsoft.windowsazure.exception.ServiceException;
import com.microsoft.windowsazure.management.configuration.PublishSettingsLoader;

import java.io.IOException;
import java.net.URISyntaxException;


public class AzureResources {
    public static String MANAGEMENT_URI = "https://management.core.windows.net/";
    public static String ARM_URL = "https://management.azure.com/";
    public static String ARM_AAD_URL = "https://login.windows.net/";


    private Configuration configuration;
    private ResourceManagementClient resourceManagementClient;
    private StorageManagementClient storageManagementClient;
    private ComputeManagementClient computeManagementClient;
    private NetworkResourceProviderClient networkResourceProviderClient;

    public final StorageAccounts storageAccounts;

    public AzureResources(String subscriptionId, String tenantId, String clientId, String clientKey) throws Exception {
        this.configuration = Utils.createConfiguration(subscriptionId, tenantId, clientId, clientKey);

        resourceManagementClient = ResourceManagementService.create(configuration);
        storageManagementClient = StorageManagementService.create(configuration);
        computeManagementClient = ComputeManagementService.create(configuration);
        networkResourceProviderClient = NetworkResourceProviderService.create(configuration);

        storageAccounts = new StorageAccounts(storageManagementClient, resourceManagementClient);
    }

    public AzureResources(String publishSettingsPath, String subscriptionId) throws IOException, ServiceException, URISyntaxException {
        this.configuration = PublishSettingsLoader.createManagementConfiguration(publishSettingsPath, subscriptionId);
        resourceManagementClient = ResourceManagementService.create(configuration);
        storageManagementClient = StorageManagementService.create(configuration);
        computeManagementClient = ComputeManagementService.create(configuration);
        networkResourceProviderClient = NetworkResourceProviderService.create(configuration);

        storageAccounts = new StorageAccounts(storageManagementClient, resourceManagementClient);
    }

}
