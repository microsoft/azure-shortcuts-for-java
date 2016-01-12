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
import com.microsoft.azure.shortcuts.common.Provisionable;
import com.microsoft.azure.shortcuts.common.Refreshable;
import com.microsoft.azure.shortcuts.common.Wrapper;
import com.microsoft.azure.shortcuts.resources.common.DefinitionCombo;
import com.microsoft.azure.shortcuts.resources.common.GroupResourceBase;

public interface VirtualMachine extends 
	GroupResourceBase,
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
		GroupResourceBase.DefinitionWithRegion<DefinitionWithGroup> {
		/*TODO 
		 * Defaulted: endpoints
		 * Optional:  tags
		 */
	}
	
	/**
	 * A virtual machine definition requiring the resource group to be specified
	 */
	interface DefinitionWithGroup extends
		GroupResourceBase.DefinitionWithGroup<DefinitionWithNetwork> {}
	
	/**
	 * A virtual machine definition requiring the virtual network to be specified
	 */
	interface DefinitionWithNetwork extends 
		DefinitionCombo.WithNetwork<DefinitionWithAdminUsername> {}
	
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
	 * A virtual machine definition with sufficient inputs to provision a new virtual machine in the cloud, 
	 * but exposing additional optional inputs to specify
	 */
	interface DefinitionProvisionable extends
		DefinitionCombo.WithStorageAccount<DefinitionProvisionable>,
		DefinitionWithSize<DefinitionProvisionable>,
		GroupResourceBase.DefinitionWithTags<DefinitionProvisionable>,
		DefinitionCombo.WithAvailabilitySet<DefinitionProvisionable>,
		DefinitionCombo.WithNetworkInterface<DefinitionProvisionable>,
		Provisionable<VirtualMachine> {
		
		/**
		 * @param computerName The computer name for the virtual machine
		 * @return A definition of the virtual machine with sufficient inputs to be provisioned
		 */
		DefinitionProvisionable withComputerName(String computerName);
	}
	
	interface UpdateBlank {
		
	}
}
