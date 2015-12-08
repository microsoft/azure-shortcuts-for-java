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

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import com.microsoft.azure.management.compute.models.DataDisk;
import com.microsoft.azure.management.compute.models.ImageReference;
import com.microsoft.azure.management.compute.models.NetworkInterfaceReference;
import com.microsoft.azure.management.compute.models.VirtualMachineExtension;
import com.microsoft.azure.management.resources.models.ResourceGroupExtended;
import com.microsoft.azure.shortcuts.common.Provisionable;
import com.microsoft.azure.shortcuts.common.Refreshable;
import com.microsoft.azure.shortcuts.common.Wrapper;
import com.microsoft.azure.shortcuts.resources.common.ResourceBaseExtended;

public interface VirtualMachine extends 
	ResourceBaseExtended,
	Refreshable<VirtualMachine>,
	Wrapper<com.microsoft.azure.management.compute.models.VirtualMachine> {
	
	String size();
	URI bootDiagnosticsStorage();
	boolean isBootDiagnosticsEnabled();
	URI availabilitySet();
	ArrayList<VirtualMachineExtension> extensions();
	Integer platformFaultDomain();
	Integer platformUpdateDomain();
	String remoteDesktopThumbprint();
	String vmAgentVersion();
	ArrayList<NetworkInterfaceReference> networkInterfaces();
	String adminUserName();
	String computerName();
	String customData();
	boolean isLinux();
	boolean isWindows();
	ImageReference image();
	List<DataDisk> dataDisks();
	
	
	/**
	 * A new blank virtual machine definition requiring the first set of input parameters to be specified
	 */
	interface DefinitionBlank extends 
		DefinitionWithRegion {
		/*TODO 
		 * Defaulted: groupName, endpoints, vnetname, storageAccountName
		 * Optional:  tags
		 */
	}
	
	
	/**
	 * A virtual machine definition requiring the region to be specified
	 */
	interface DefinitionWithRegion {
		/**
		 * @param region The name of the location (Azure region) where the VM is to be created
		 * @return The next stage of the VM definition
		 */
		DefinitionWithAdminUsername withRegion(String region);
	}

	
	/**
	 * A virtual machine definition requiring the admin username to be specified
	 */
	interface DefinitionWithAdminUsername {
		/**
		 * @param The desired admin username for the virtual machine
		 * @return The next stage of the VM definition
		 */
		DefinitionWithAdminPassword withAdminUsername(String username);
	}
	

	/** 
	 * A virtual machine definition requiring the admin password to be specified
	 */
	interface DefinitionWithAdminPassword {
		/**
		 * @param password The desired admin password for the virtual machine
		 * @return The next stage of the VM definition
		 */
		DefinitionWithImagePublishedBy withAdminPassword(String password);
	}

	
	/**
	 * A virtual machine definition requiring the publisher of the base image to be specified
	 */
	interface DefinitionWithImagePublishedBy {
		/** 
		 * Specifies the publisher of the image to base the virtual machine on
		 * @param publisher The identifier of the image publisher
		 * @return The next stage of the VM definition
		 */
		DefinitionWithImageOffer withImagePublishedBy(String publisher);
	}
	
	
	/**
	 * A virtual machine definition requiring the offer name of the base image to be specified
	 */
	interface DefinitionWithImageOffer {
		/**
		 * @param offer The offer name from the specified publisher of the image to base the virtual machine on
		 * @return The next stage of the VM definition
		 */
		DefinitionWithImageSKU withImageOffer(String offer);
	}
	
	
	/**
	 * A virtual machine definition requiring the SKU of the base image offer to be specified
	 */
	interface DefinitionWithImageSKU {
		/**
		 * @param sku The SKU name of from the selected offer of the image to base the virtual machine on
		 * @return The next stage of the VM definition
		 */
		DefinitionWithImageVersion withImageSKU(String sku);
	}
	
	
	/**
	 * A virtual machine definition requiring the version of the base image offer SKU to be specified
	 */
	interface DefinitionWithImageVersion {
		/**
		 * @param version The version of the selected SKU of the image to base the virtual machine on
		 * @return The next stage of the VM definition
		 */
		DefinitionProvisionable withImageVersion(String version);

		/**
		 * Automatically selects the latest version of the selected image SKU available
		 * @return The next stage of the VM definition
		 */
		DefinitionProvisionable withLatestImageVersion();
	}

	
	/**
	 * A virtual machine definition allowing to select an existing storage account
	 */
	interface DefinitionWithStorageAccountExisting<T> {
		/**
		 * @param name The name of an existing storage account to use for the OS disk of the virtual machine
		 * @return A definition of the virtual machine with sufficient inputs to be provisioned
		 */
		T withStorageAccountExisting(String name);
		
		/**
		 * @param The existing storage account to use for the OS disk of the virtual machine
		 * @return A definition of the virtual machine with sufficient inputs to be provisioned
		 */
		T withStorageAccountExisting(StorageAccount storageAccount);

		/**
		 * @param storageAccount The existing storage account to use for the OS disk of the virtual machine
		 * @return A definition of the virtual machine with sufficient inputs to be provisioned
		 */
		T withStorageAccountExisting(com.microsoft.azure.management.storage.models.StorageAccount storageAccount);		
	}
	
	
	/**
	 * A virtual machine definition allowing to specify the name of a new storage account to create for the new virtual machine
	 */
	interface DefinitionWithStorageAccountNew<T> {
		T withStorageAccountNew(String name);
	}
	
	
	/**
	 * A virtual machine definition allowing to specify the size of the new virtual machine
	 */
	interface DefinitionWithSize<T> {
		/**
		 * @param sizeName The name of the size for the virtual machine as text
		 * @return A definition of the virtual machine with sufficient inputs to be provisioned
		 */
		T withSize(String sizeName);
		
		/**
		 * @param size The size for the virtual machine
		 * @return A definition of the virtual machine with sufficient inputs to be provisioned
		 */
		T withSize(Size.Type size);
		
		/**
		 * @param size The size object returned dynamically by Azure's list of available sizes for the virtual machine
		 * @return A definition of the virtual machine with sufficient inputs to be provisioned
		 */
		T withSize(Size size);		
	}
	
	
	/**
	 * A virtual machine definition allowing to select an existing resource group to associate the virtual machine with
	 */
	interface DefinitionWithGroupExisting<T> {
		/**
		 * @param name The name of an existing resource group to associate the virtual machine with
		 * @return A definition of the virtual machine with sufficient inputs to be provisioned
		 */
		T withGroupExisting(String name);

		/**
		 * @param group The Group object representing an existing group to associate the virtual machine with
		 * @return A definition of the virtual machine with sufficient inputs to be provisioned
		 */
		T withGroupExisting(Group group);

		/**
		 * @param group The ResourceGroup object (from the Azure SDK for Java) representing an existing resource group to associate the virtual machine with
		 * @return A definition of the virtual machine with sufficient inputs to be provisioned
		 */
		T withGroupExisting(ResourceGroupExtended group);
	}
	
	
	/**
	 * A virtual machine definition allowing to specify a new resource group to create for the virtual machine
	 */
	interface DefinitionWithGroupNew<T> {
		T withGroupNew(String name);
		// TODO T withGroupNew(Group.DefinitionProvisionable groupDefinition);
	}
	
	
	/**
	 * A virtual machine definition with sufficient inputs to provision a new virtual machine in the cloud, 
	 * but exposing additional optional inputs to specify
	 */
	interface DefinitionProvisionable extends
		DefinitionWithStorageAccountExisting<DefinitionProvisionable>,
		DefinitionWithStorageAccountNew<DefinitionProvisionable>,
		DefinitionWithSize<DefinitionProvisionable>,
		DefinitionWithGroupExisting<DefinitionProvisionable>,
		DefinitionWithGroupNew<DefinitionProvisionable>,
		Provisionable<UpdateBlank> {
		
		/**
		 * @param availabilitySetId The ID of the availability set to associate the virtual machine with
		 * @return A definition of the virtual machine with sufficient inputs to be provisioned
		 */
		DefinitionProvisionable withAvailabilitySet(URI availabilitySetURI);
		
	}
	
	interface UpdateBlank {
		
	}
}
