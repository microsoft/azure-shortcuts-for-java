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
package com.microsoft.azure.shortcuts.resources.common;

import java.net.URI;

import com.microsoft.azure.management.network.models.VirtualNetwork;
import com.microsoft.azure.shortcuts.resources.AvailabilitySet;
import com.microsoft.azure.shortcuts.resources.Network;
import com.microsoft.azure.shortcuts.resources.NetworkInterface;
import com.microsoft.azure.shortcuts.resources.StorageAccount;

public interface DefinitionCombo {
	
	/**
	 * A resource definition allowing to associate a virtual network with this resource
	 */
	public interface WithNetwork<R> {
		/**
		 * Associates an existing virtual network with this resource
		 * @param id The resource ID of the virtual network to associate with the resource
		 * @return The next stage of the resource definition
		 */
		R withNetworkExisting(String id);
		
		/**
		 * Associates an existing virtual network with this resource
		 * @param network The virtual network to associate with the resource
		 * @return The next stage of the resource definition
		 */
		R withNetworkExisting(Network network);
		
		/**
		 * Associates an existing virtual network with this resource
		 * @param network The Azure SDK VirtualNetwork to associate with the resource
		 * @return The next stage of the resource definition
		 */
		R withNetworkExisting(VirtualNetwork network);
		
		/**
		 * Creates a new virtual network to associate with this resource, based on the provided definition
		 * @param networkDefinition A provisionable definition of a virtual network
		 * @return The next stage of the resource definition
		 */
		R withNetworkNew(Network.DefinitionProvisionable networkDefinition) throws Exception;
		
		/**
		 * Creates a new virtual network to associate with this resource, in the same resource group and region, 
		 * and with one default subnet covering the entirety of the network's IP address space
		 * @param name The name of the new virtual network
		 * @return The next stage of the resource definition
		 */
		R withNetworkNew(String name, String addressSpace);
		
		/**
		 * Creates a new virtual network to associate with this resource, with a name derived from the name of this resource, 
		 * in the same resource group and region, with the specified IP address space, and with one default subnet covering the 
		 * entirety of the address space
		 * @return The next stage of the resource definition
		 */
		R withNetworkNew(String addressSpace);
	}

	
	/**
	 * A resource definition allowing to associate a storage account with this resource
	 */
	public interface WithStorageAccount<R> {
		/**
		 * Associates an existing storage account with this resource
		 * @param name The name of an existing storage account to associate with this resource
		 * @return The next stage of the resource definition
		 */
		R withStorageAccountExisting(String name);
		
		/**
		 * Associates an existing storage account with this resource
		 * @param An existing storage account to associate with this resource
		 * @return The next stage of the resource definition
		 */
		R withStorageAccountExisting(StorageAccount storageAccount);

		/**
		 * Associates an existing storage account with this resource
		 * @param storageAccount An existing Azure SDK StorageAccount to associate with this resource
		 * @return The next stage of the resource definition
		 */
		R withStorageAccountExisting(com.microsoft.azure.management.storage.models.StorageAccount storageAccount);

		/**
		 * Creates a new storage account to associate with this resource, in the same region and resource group as this resource
		 * @param name The name of the storage account to create and associate with this resource
		 * @return The next stage of the resource definition
		 */
		R withStorageAccountNew(String name);
		
		/**
		 * Creates a new storage account to associate with this resource, in the same region and resource group as this resource, 
		 * and with a name derived from the name of this resource
		 * @return The next stage of the resource definition
		 */
		R withStorageAccountNew();
		

		/**
		 * Creates a new storage account to associate with this resource, based on the provided definition
		 * @param definition A provisionable definition of a storage account
		 * @return The next stage of the resource definition
		 */
		R withStorageAccountNew(StorageAccount.DefinitionProvisionable definition) throws Exception;
	}


	/**
	 * A resource definition allowing to associate an availability set with this resource
	 */
	public interface WithAvailabilitySet<R> {
		/**
		 * Associates an existing availability set with this resource
		 * @param id The resource ID of an existing availability set
		 * @return The next stage of the resource definition
		 */
		R withAvailabilitySetExisting(String id);

		/**
		 * Associates an existing availability set with this resource
		 * @param availabilitySet An existing availability set
		 * @return The next stage of the resource definition
		 */
		R withAvailabilitySetExisting(AvailabilitySet availabilitySet);
		
		/**
		 * Associates an existing availability set with this resource
		 * @param uri The URI of an existing availability set
		 * @return The next stage of the resource definition
		 */
		R withAvailabilitySetExisting(URI uri);
		
		/**
		 * Associates an existing availability set with this resource
		 * @param availabilitySet An existing AvailabilitySet from the Azure SDK API 
		 * @return The next stage of the resource definition
		 */
		R withAvailabilitySetExisting(com.microsoft.azure.management.compute.models.AvailabilitySet availabilitySet);
		
		/**
		 * Creates a new availability set to associate with this resource, in the same region and resource group
		 * @param name The name of the new availability set
		 * @return The next stage of the resource definition
		 */
		R withAvailabilitySetNew(String name);
		
		/**
		 * Creates a new availability set to associate with this resource, in the same region and resource group, 
		 * and with a name derived from the name of this resource
		 * @return The next stage of the resource definition
		 */
		R withAvailabilitySetNew();
		
		/**
		 * Creates a new availability set to associate with this resource, based on the provided definition
		 * @param definition A provisionable definition for a new availability set
		 * @return The next stage of the resource definition
		 */
		R withAvailabilitySetNew(AvailabilitySet.DefinitionProvisionable definition) throws Exception;
	}

	
	/**
	 * A resource definition allowing to associate a network interface with tis resource
	 */
	interface WithPrimaryNetworkInterface<R> {
		/**
		 * Associates an existing network interface with this resource
		 * @param resourceId The resource ID of an existing network interface
		 * @param asPrimary True if the network interface is to be set as the primary network interface in a set of network interfaces
		 * @return The next stage of the resource definition
		 */
		R withPrimaryNetworkInterfaceExisting(String resourceId);
		
		/**
		 * Associates an existing network interface with this resource
		 * @param networkInterface An existing network interface
		 * @param asPrimary True if the network interface is to be set as the primary network interface in a set of network interfaces
		 * @return The next stage of the resource definition
		 */
		R withPrimaryNetworkInterfaceExisting(NetworkInterface networkInterface);
		
		/**
		 * Associates an existing network interface with this resource
		 * @param networkInterface An existing Azure SDK network interface object
		 * @return The next stage of the resource definition
		 */
		R withPrimaryNetworkInterfaceExisting(com.microsoft.azure.management.network.models.NetworkInterface networkInterface);
		
		/**
		 * Creates a new network interface to associate with this resource, in the same region and group, 
		 * using the provided name, within the provided existing subnet, with dynamic private IP allocation enabled
		 * @param name The name for the network interface
		 * @param subnet An existing subnet
		 * @return The next stage of the resource definition
		 */
		R withPrimaryNetworkInterfaceNew(String name, Network.Subnet subnet);
		
		/**
		 * Creates a new network interface to associate with this resource, in the same region and group, 
		 * using a name derived from the name of this resource, within the provided existing subnet, with dynamic private IP allocation enabled
		 * @param subnet
		 * @return The next stage of the resource definition
		 */
		R withPrimaryNetworkInterfaceNew(Network.Subnet subnet);
	}
}
