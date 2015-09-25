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
