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

import java.io.File;
import java.util.Arrays;

import com.microsoft.azure.shortcuts.reading.CloudService;
import com.microsoft.azure.shortcuts.reading.Network;
import com.microsoft.azure.shortcuts.reading.OSImage;
import com.microsoft.azure.shortcuts.reading.StorageAccount;
import com.microsoft.azure.shortcuts.reading.VirtualMachine;
import com.microsoft.windowsazure.management.models.LocationAvailableServiceNames;

public class Samples {
	public static void main(String[] args) {
		String publishSettingsPath = "/Archive/deployFromStorage/interopdemos.publishsettings";
		String subscriptionId = "9657ab5d-4a4a-4fd2-ae7a-4cd9fbd030ef";

		try {
			// Instantiate Azure management class
			final Azure azure = new Azure(publishSettingsPath, subscriptionId);

			// Test virtual machines
			testVirtualMachines(azure);
			
			// Test virtual networks
			//testVirtualNetworks(azure);
			
			// Test cloud services
			//testCloudServices(azure);

			// Test Azure storage
			//testStorage(azure);
			
			// Test OS images
			//testOSImages(azure);
			
			// List the sizes
			System.out.println("Available VM sizes: " + Arrays.toString(
				azure.sizes.list(true, false)));

			// List the regions
			System.out.println("Available regions: " + Arrays.toString(
				azure.regions.list(LocationAvailableServiceNames.PERSISTENTVMROLE)));
			
			// Test cert creation
			File pfxFile = new File(new File(System.getProperty("user.home"), "Desktop"), "test.pfx");
			File jdkFilePath = new File(System.getenv("JAVA_HOME"));
			File cerFile = new File(new File(System.getProperty("user.home"), "Desktop"), "test.cer");
			String password = "Abcd.1234", alias = "test";
			
			Utils.createCertPkcs12(pfxFile, jdkFilePath, alias, password, alias, 3650);
			Utils.createCertPublicFromPkcs12(pfxFile, cerFile, jdkFilePath, alias, password);
			
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}		
	}

	// Tests OS images
	public static void testOSImages(Azure azure) throws Exception {
		final String imageName = "b39f27a8b8c64d52b05eac6a62ebad85__Ubuntu-12_04_5_LTS-amd64-server-20150413-en-us-30GB";

		// List the OS images
		System.out.println("Available OS images: \n\t" + Arrays.toString(
			azure.osImages.list()).replaceAll(", ", ",\n\t"));
		
		// Get information about a specific OS image
		OSImage osImage = azure.osImages.get(imageName);
		System.out.println(String.format("Found image: %s\n"
				+ "\tCategory: %s\n"
				+ "\tDescription: %s\n"
				+ "\tEULA: %s\n"
				+ "\tFamily: %s\n"
				+ "\tIcon URI: %s\n"
				+ "\tIO type: %s\n"
				+ "\tPremium? %s\n"
				+ "\tShown in GUI? %s\n"
				+ "\tLabel: %s\n"
				+ "\tLanguage: %s\n"
				+ "\tLogical size (GB): %f\n"
				+ "\tMedia link: %s\n"
				+ "\tOperating system type: %s\n"
				+ "\tPrivacy URI: %s\n"
				+ "\tPublished date: %s\n"
				+ "\tPublisher: %s\n"
				+ "\tRecommended VM size: %s\n"
				+ "\tRegions: %s\n"
				+ "\tSmall icon URI %s\n",
				osImage.name(),
				osImage.category(),
				osImage.description(),
				osImage.eula(),
				osImage.family(),
				osImage.iconUri(),
				osImage.ioType(),
				osImage.isPremium(),
				osImage.isShownInGui(),
				osImage.label(),
				osImage.language(),
				osImage.logicalSizeInGB(),
				osImage.mediaLink(),
				osImage.operatingSystemType(),
				osImage.privacyUri(),
				osImage.publishedDate().getTime(),
				osImage.publisher(),
				osImage.recommendedVMSize(),
				Arrays.toString(osImage.regions()),
				osImage.smallIconUri()
				));
	}
	
		
	// Tests storage account implementation
	public static void testStorage(Azure azure) throws Exception {
		final String accountName = "store" + String.valueOf(System.currentTimeMillis());
		System.out.println(String.format("Creating account named '%s'...", accountName));
		
		// Create a new storage account
		azure.storageAccounts.define(accountName)
			.withRegion("West US")
			.provision();

		// List storage accounts
		System.out.println("Available storage accounts: " + Arrays.toString(
			azure.storageAccounts.list()));

		// Get storage account information
		StorageAccount storageAccount = azure.storageAccounts.get(accountName);
		System.out.println(String.format("Found storage account: %s\n"
				+ "\tAffinity group: %s\n"
				+ "\tLabel: %s\n"
				+ "\tDescription: %s\n"
				+ "\tGeo primary region: %s\n"
				+ "\tGeo primary region status: %s\n"
				+ "\tGeo secondary region: %s\n"
				+ "\tGeo secondary region status: %s\n"
				+ "\tLast geo failover time: %s\n"
				+ "\tRegion: %s\n"
				+ "\tStatus: %s\n"
				+ "\tEndpoints: %s\n"
				+ "\tType: %s\n",
		
				storageAccount.name(),
				storageAccount.affinityGroup(),
				storageAccount.label(),
				storageAccount.description(),
				storageAccount.geoPrimaryRegion(),
				storageAccount.geoPrimaryRegionStatus(),
				storageAccount.geoSecondaryRegion(),
				storageAccount.geoSecondaryRegionStatus(),
				(storageAccount.lastGeoFailoverTime()!=null) ? storageAccount.lastGeoFailoverTime().getTime() : null,
				storageAccount.region(),
				storageAccount.status(),
				Arrays.toString(storageAccount.endpoints()),
				storageAccount.type()
				));
		
		
		// Update storage info
		System.out.println(String.format("Updating storage account named '%s'...", accountName));

		azure.storageAccounts.update(accountName)
			.withDescription("Updated")
			.withLabel("Updated")
			.apply();
		
		storageAccount = azure.storageAccounts.get(accountName);
		System.out.println(String.format("Updated storage account: %s\n"
				+ "\tLabel: %s\n"
				+ "\tDescription: %s\n",		
				storageAccount.name(),
				storageAccount.label(),
				storageAccount.description()));
		
		// Delete the newly created storage account
		System.out.println(String.format("Deleting storage account named '%s'...", accountName));
		azure.storageAccounts.delete(accountName);
	}
	
	
	// Tests virtual network implementation
	public static void testVirtualNetworks(Azure azure) throws Exception {
		String networkName;
		Network network;
		
		// Create a network with multiple subnets
		networkName = "net" + String.valueOf(System.currentTimeMillis());
		System.out.println(String.format("Creating virtual network named '%s'...", networkName));
		azure.networks.define(networkName)
			.withRegion("West US")
			.withCidr("10.0.0.0/28")
			.withSubnet("Foo", "10.0.0.0/29")
			.withSubnet("Bar", "10.0.0.8/29")
			.provision();

		// List the virtual networks
		System.out.println("Available virtual networks: " + Arrays.toString(
			azure.networks.list()));

		// Get created virtual network
		network = azure.networks.get(networkName);
		System.out.println(String.format("Network found: %s\n"
				+ "\tRegion: %s\n"
				+ "\tCIDR: %s\n"
				+ "\tAffinity group: %s\n"
				+ "\tSubnets: %s\n",
				network.name(),
				network.region(),
				network.cidr(),
				network.affinityGroup(),
				Arrays.toString(network.subnets())
				));

		// Delete the newly created virtual network
		System.out.println(String.format("Deleting virtual network named '%s'...", network.name()));
		azure.networks.delete(network.name());

		// Create network with default subnet
		networkName = "net" + String.valueOf(System.currentTimeMillis());
		System.out.println(String.format("Creating virtual network named '%s'...", networkName));
		azure.networks.define(networkName)
			.withRegion("West US")
			.withCidr("10.0.0.0/29")
			.provision();

		// List the virtual networks
		System.out.println("Available virtual networks: " + Arrays.toString(
			azure.networks.list()));

		// Get created virtual network
		network = azure.networks.get(networkName);
		System.out.println(String.format("Network found: %s\n"
				+ "\tRegion: %s\n"
				+ "\tCIDR: %s\n"
				+ "\tAffinity group: %s\n"
				+ "\tSubnets: %s\n",
				network.name(),
				network.region(),
				network.cidr(),
				network.affinityGroup(),
				Arrays.toString(network.subnets())
				));

		// Delete the newly created virtual network
		System.out.println(String.format("Deleting virtual network named '%s'...", network.name()));
		azure.networks.delete(network.name());
	}
	
	
	// Tests virtual machines
	public static void testVirtualMachines(Azure azure) throws Exception {
		final String timeStamp = String.valueOf(System.currentTimeMillis()).substring(5);
		
		// Create a new network
		final String network = "net" + timeStamp;
		System.out.println(String.format("Creating virtual network named '%s'...", network));
		azure.networks.define(network)
			.withRegion("West US")
			.withCidr("10.0.0.0/28")
			.withSubnet("Foo", "10.0.0.0/29")
			.withSubnet("Bar", "10.0.0.8/29")
			.provision();
		
		// Create a Linux VM in that network
		final String vmName2 = "vl" + timeStamp;
		System.out.println(String.format("Creating virtual machine named '%s'...", vmName2));
		final String cloudService2 = "cs" + timeStamp;
		azure.virtualMachines.define(vmName2)
			.withNetwork(network)
			.withSize("Small")
			.withAdminUsername("marcins")
			.withAdminPassword("Abcd.1234")
			.withLinuxImage("b39f27a8b8c64d52b05eac6a62ebad85__Ubuntu-12_04_5_LTS-amd64-server-20150413-en-us-30GB")
			.withTcpEndpoint(22)
			.withNewCloudService(cloudService2)
			.withSubnet("Foo")
			.provision();

		// Create a Linux VM in a new service
		final String vmName = "vm" + timeStamp;
		System.out.println(String.format("Creating virtual machine named '%s'...", vmName));
		azure.virtualMachines.define(vmName)
			.withRegion("West US")
			.withSize("Small")
			.withAdminUsername("marcins")
			.withAdminPassword("Abcd.1234")
			.withLinuxImage("b39f27a8b8c64d52b05eac6a62ebad85__Ubuntu-12_04_5_LTS-amd64-server-20150413-en-us-30GB")
			.withHostName(vmName)
			.withTcpEndpoint(22)
			.provision();

		// Add a Windows VM to the same service deployment
		final String vmNameWin = "wm" + timeStamp;
		azure.virtualMachines.define(vmNameWin)
			.withExistingCloudService(vmName)
			.withSize("Small")
			.withAdminUsername("marcins")
			.withAdminPassword("Abcd.1234")
			.withWindowsImage("a699494373c04fc0bc8f2bb1389d6106__Windows-Server-2012-R2-201504.01-en.us-127GB.vhd")
			.withTcpEndpoint(3389)
			.provision();		
		
		// List virtual machines
		System.out.println("Virtual machines: "+ Arrays.toString(
			azure.virtualMachines.list()));
		
		// Get information about the created Linux vm
		VirtualMachine vm = azure.virtualMachines.get(vmName);
		System.out.println(String.format("Reading information about vm: %s\n"
				+ "\tDeployment name: %s\n"
				+ "\tService name: %s\n"
				+ "\tSize: %s\n"
				+ "\tStatus: %s\n"
				+ "\tWindows? %s\n"
				+ "\tLinux? %s\n"
				+ "\tNetwork %s\n"
				+ "\tAffinity group %s\n",
				vm.name(),
				vm.deployment(),
				vm.cloudService(),
				vm.size(),
				vm.status().toString(),
				vm.isWindows(),
				vm.isLinux(),
				vm.network(),
				vm.affinityGroup()
				));
		
		// Get information about the created Windows vm
		vm = azure.virtualMachines.get(vmName + "." + vmNameWin);
		System.out.println(String.format("Reading information about vm: %s\n"
				+ "\tDeployment name: %s\n"
				+ "\tService name: %s\n"
				+ "\tSize: %s\n"
				+ "\tStatus: %s\n"
				+ "\tWindows? %s\n"
				+ "\tLinux? %s\n"
				+ "\tNetwork %s\n"
				+ "\tAffinity group %s\n",
				vm.name(),
				vm.deployment(),
				vm.cloudService(),
				vm.size(),
				vm.status().toString(),
				vm.isWindows(),
				vm.isLinux(),
				vm.network(),
				vm.affinityGroup()
				));
		
		// Get information about the second Linux VM
		vm = azure.virtualMachines.get(cloudService2 + "." + vmName2);
		System.out.println(String.format("Reading information about vm: %s\n"
				+ "\tDeployment name: %s\n"
				+ "\tService name: %s\n"
				+ "\tSize: %s\n"
				+ "\tStatus: %s\n"
				+ "\tWindows? %s\n"
				+ "\tLinux? %s\n"
				+ "\tNetwork %s\n"
				+ "\tAffinity group %s\n",
				vm.name(),
				vm.deployment(),
				vm.cloudService(),
				vm.size(),
				vm.status().toString(),
				vm.isWindows(),
				vm.isLinux(),
				vm.network(),
				vm.affinityGroup()
				));
		
	}

	
	// Tests cloud service implementation
	public static void testCloudServices(Azure azure) throws Exception {
		final String serviceName = "svc" + String.valueOf(System.currentTimeMillis());
		System.out.println(String.format("Creating cloud service named '%s'...", serviceName));

		// Create a new cloud service
		azure.cloudServices.define(serviceName)
			.withRegion("West US")
			.provision();

		// List cloud services
		System.out.println("Available cloud services: " + Arrays.toString(
			azure.cloudServices.list()));

		// Get cloud service info
		CloudService cloudService = azure.cloudServices.get(serviceName);
		System.out.println(String.format("Found cloud service: %s\n"
				+ "\tLabel: %s\n"
				+ "\tDescription: %s\n"
				+ "\tRegion: %s\n"
				+ "\tCreated: %s\n"
				+ "\tModified: %s\n"
				+ "\tAffinity group: %s\n"
				+ "\tReverse DNS FQDN: %s\n",
				cloudService.name(),
				cloudService.label(),
				cloudService.description(),
				cloudService.region(),
				cloudService.created().getTime(),
				cloudService.modified().getTime(),
				cloudService.affinityGroup(),
				cloudService.reverseDnsFqdn()));

		// Update cloud service
		System.out.println(String.format("Updating cloud service named '%s'...", serviceName));

		azure.cloudServices.update(serviceName)
			.withDescription("Updated")
			.withLabel("Updated")
			.apply();

		cloudService = azure.cloudServices.get(serviceName);
		System.out.println(String.format("Updated cloud service: %s\n"
				+ "\tLabel: %s\n"
				+ "\tDescription: %s\n",
				cloudService.name(),
				cloudService.label(),
				cloudService.description()
				));

		
		// Delete the newly created cloud service
		System.out.println(String.format("Deleting cloud service named '%s'...", serviceName));
		azure.cloudServices.delete(serviceName);
	}
	
	



	

}
