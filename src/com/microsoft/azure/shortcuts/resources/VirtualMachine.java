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
import com.microsoft.azure.shortcuts.common.Deletable;
import com.microsoft.azure.shortcuts.common.Provisionable;
import com.microsoft.azure.shortcuts.common.Refreshable;
import com.microsoft.azure.shortcuts.common.Wrapper;
import com.microsoft.azure.shortcuts.resources.common.DefinitionCombos;
import com.microsoft.azure.shortcuts.resources.common.GroupResourceBase;

public interface VirtualMachine extends 
	GroupResourceBase,
	Refreshable<VirtualMachine>,
	Wrapper<com.microsoft.azure.management.compute.models.VirtualMachine>,
	Deletable {
	
	public String size();
	public URI bootDiagnosticsStorage();
	public boolean isBootDiagnosticsEnabled();
	public URI availabilitySet();
	public ArrayList<VirtualMachineExtension> extensions();
	public Integer platformFaultDomain();
	public Integer platformUpdateDomain();
	public String remoteDesktopThumbprint();
	public String vmAgentVersion();
	public ArrayList<NetworkInterfaceReference> networkInterfaces();
	public String adminUserName();
	public String computerName();
	public String customData();
	public boolean isLinux();
	public boolean isWindows();
	public ImageReference image();
	public List<DataDisk> dataDisks();
	
	/**
	 * Stops (powers off) the virtual machine without deallocating it. Charges keep accruing.
	 * @throws Exception 
	 */
	public void stop() throws Exception;

	/** 
	 * Restarts a virtual machine
	 * @throws Exception
	 */
	public void restart() throws Exception;

	/** 
	 * Deallocates a virtual machine. Charges no longer accrue.
	 * @throws Exception
	 */
	public void deallocate() throws Exception;

	/**
	 * Starts a stopped virtual machine.
	 * @throws Exception
	 */
	public void start() throws Exception;
	
	/**
	 * Captures a virtual machine image based on this virtual machine
	 * @param containerName The name of the container where to save the image
	 * @param diskNamePrefix The prefix to use for the name of the virtual hard disk for the image
	 * @param overwrite Determines whether to overwrite an existing image VHD, if any
	 * @throws Exception
	 */
	public void capture(String containerName, String diskNamePrefix, boolean overwrite) throws Exception;

	/**
	 * Sets the state of the virtual machine as generalized, which is required for capturing an image
	 * @throws Exception
	 */
	public void generalize() throws Exception;


	/**
	 * A new blank virtual machine definition requiring the first set of input parameters to be specified
	 */
	public interface DefinitionBlank extends 
		GroupResourceBase.DefinitionWithRegion<DefinitionWithGroup> {
		//TODO load balancers
		//TODO network security groups
		//TODO custom images
		//TODO image capture
	}
	
	/**
	 * A virtual machine definition requiring the resource group to be specified
	 */
	public interface DefinitionWithGroup extends
		GroupResourceBase.DefinitionWithGroup<DefinitionWithNetworking> {}
	
	/**
	 * A virtual machine definition allowing the networking to be specified
	 */
	public interface DefinitionWithNetworking extends 
		DefinitionCombos.WithNewNetwork<DefinitionWithPrivateIp>,
		DefinitionCombos.WithExistingNetwork<DefinitionWithSubnet>,
		DefinitionCombos.WithExistingNetworkInterface<DefinitionWithAdminUsername> {}
	

	/**
	 * A virtual machine definition allowing a subnet with the selected virtual network to be associated with it
	 */
	public interface DefinitionWithSubnet extends 
		DefinitionCombos.WithSubnet<DefinitionWithPrivateIp> {
	}
	
	/**
	 * A virtual machine definition allowing the primary private IP address to be specified
	 */
	public interface DefinitionWithPrivateIp extends 
		DefinitionCombos.WithPrivateIpAddress<DefinitionWithPublicIp> {}
	
	/**
	 * A virtual machine definition allowing the primary public IP address to be specified
	 */
	public interface DefinitionWithPublicIp extends
		DefinitionCombos.WithPublicIpAddress<DefinitionWithAdminUsername> {}
	
	/**
	 * A virtual machine definition requiring the admin username to be specified
	 */
	public interface DefinitionWithAdminUsername {
		/**
		 * @param The desired admin username for the virtual machine
		 * @return The next stage of the VM definition
		 */
		DefinitionWithAdminPassword withAdminUsername(String username);
	}
	

	/** 
	 * A virtual machine definition requiring the admin password to be specified
	 */
	public interface DefinitionWithAdminPassword {
		/**
		 * @param password The desired admin password for the virtual machine
		 * @return The next stage of the VM definition
		 */
		DefinitionWithImage withAdminPassword(String password);
	}

	
	/**
	 * A virtual machine definition allowing the selection of a base image for the virtual machine
	 */
	public interface DefinitionWithImage {
		DefinitionProvisionable withLatestImage(String publisher, String offer, String sku);
		DefinitionProvisionable withImage(String publisher, String offer, String sku, String version);
	}
	
	
	/**
	 * A virtual machine definition allowing to specify the size of the new virtual machine
	 */
	public interface DefinitionWithSize<T> {
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
	 * A virtual machine definition allowing to specify a data disk to attach to the VM
	 */
	public interface DefinitionWithDataDisk<R> {
		R withNewDataDisk(int diskSizeGB);
		R withExistingDataDisk(URI vhdUri);
		R withExistingDataDisk(String vhdUri);
	}
	
	
	/**
	 * A virtual machine definition with sufficient inputs to provision a new virtual machine in the cloud, 
	 * but exposing additional optional inputs to specify
	 */
	public interface DefinitionProvisionable extends
		DefinitionCombos.WithStorageAccount<DefinitionProvisionable>,
		DefinitionWithSize<DefinitionProvisionable>,
		GroupResourceBase.DefinitionWithTags<DefinitionProvisionable>,
		DefinitionCombos.WithAvailabilitySet<DefinitionProvisionable>,
		DefinitionWithDataDisk<DefinitionProvisionable>,
		Provisionable<VirtualMachine> {
		
		/**
		 * @param computerName The computer name for the virtual machine
		 * @return A definition of the virtual machine with sufficient inputs to be provisioned
		 */
		DefinitionProvisionable withComputerName(String computerName);
	}
	
	public interface UpdateBlank {
		
	}
}
