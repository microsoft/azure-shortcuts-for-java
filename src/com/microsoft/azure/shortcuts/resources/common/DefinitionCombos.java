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
import com.microsoft.azure.shortcuts.resources.PublicIpAddress;
import com.microsoft.azure.shortcuts.resources.StorageAccount;

public interface DefinitionCombos {
	
	public interface WithNetworkExisting<R> {
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
	}
	
	/**
	 * A resource definition allowing to associate a virtual network with it
	 */
	public interface WithNetworkNew<R> {
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
	 * A resource definition allowing to associate a subnet with it
	 */
	public interface WithSubnet<R> {
		R withSubnet(String subnetId);
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
	 * A resource definition allowing to create a primary network interface associated with this resource
	 */
	public interface WithNetworkInterfaceExisting<R> {
		/**
		 * Selects an existing network interface as the primary NIC for this resource
		 * @param resourceId The resource ID of an existing network interface
		 * @return The next stage of the resource definition
		 */
		R withNetworkInterfaceExisting(String resourceId);
		
		/**
		 * Selects an existing network interface as the primary NIC for this resource
		 * @param networkInterface An existing network interface
		 * @return The next stage of the resource definition
		 */
		R withNetworkInterfaceExisting(NetworkInterface networkInterface);
		
		/**
		 * Selects an existing network interface as the primary NIC for this resource
		 * @param networkInterface An existing Azure SDK network interface object
		 * @return The next stage of the resource definition
		 */
		R withNetworkInterfaceExisting(com.microsoft.azure.management.network.models.NetworkInterface networkInterface);		
	}
	
	/**
	 * A resource definition allowing to associate a primary network interface with this resource
	 */
	public interface WithNetworkInterfaceNew<R> {
		/**
		 * Creates a new network interface to associate with this resource as its primary NIC, in the same region and group, 
		 * using the provided name, within the provided existing subnet, with dynamic private IP allocation enabled
		 * @param name The name for the network interface
		 * @param subnet An existing subnet
		 * @return The next stage of the resource definition
		 */
		R withNetworkInterfaceNew(String name, Network.Subnet subnet);
		
		/**
		 * Creates a new network interface to associate with this resource as its primary NIC, in the same region and group, 
		 * using a name derived from the name of this resource, within the provided existing subnet, with dynamic private IP allocation enabled
		 * @param subnet
		 * @return The next stage of the resource definition
		 */
		R withNetworkInterfaceNew(Network.Subnet subnet);
		
		/**
		 * Creates a network interface to associate with this resource as its primary NIC, in the same region and group, 
		 * using a name derived from the name of this resource, within the first subnet of the associated virtual network, 
		 * with dynamic private IP allocation enabled
		 * @return The next stage of the resource definition
		 */
		R withNetworkInterfaceNew();
	}

	
	/**
	 * A resource definition allowing to associate it with a public IP address
	 */
	public interface WithPublicIpAddress<R> {
		/**
		 * Associates a public IP address that exists in the subscription with this resource
		 * @param publicIpAddress An existing public IP address
		 * @return The next stage of the definition
		 */
		R withPublicIpAddressExisting(PublicIpAddress publicIpAddress);
		
		/**
		 * Associates a public IP address that exists in the subscription with this resource
		 * @param publicIpAddress An existing public IP address represented by an Azure SDK object
		 * @return The next stage of the definition
		 */
		R withPublicIpAddressExisting(com.microsoft.azure.management.network.models.PublicIpAddress publicIpAddress);
		
		
		/**
		 * Specifies that no public IP address should be associated with this resource
		 * @return The next stage of the definition
		 */
		R withoutPublicIpAddress();
		
		/**
		 * Creates a new public IP address for this resource, in the same region and group as the resource, with an internal name and DNS label derived from the resource
		 * @return The next stage of the definition
		 */
		R withPublicIpAddressNew();
		
		/**
		 * Creates a new public IP address for this resource, in the same region and group as the resource, with the specified leaf domain label and an internal name derived from it
		 * @param leafDnsLabel The leaf domain label
		 * @return The next stage of the definition
		 */
		R withPublicIpAddressNew(String leafDnsLabel);
	}
	
	
	/**
	 * A resource definition allowing to associate it with a private IP address within a virtual network subnet
	 */
	public interface WithPrivateIpAddress<R> {
		/**
		 * Enables dynamic private IP address allocation within the specified existing virtual network subnet as the primary subnet
		 * @param subnet The Subnet to associate with the resource
		 * @return The next stage of the definition
		 */
		R withPrivateIpAddressDynamic();

		/**
		 * Assigns the specified static IP address within the specified existing virtual network subnet as the primary subnet
		 * @param subnet The Subnet to associate with this resource
		 * @param staticPrivateIpAddress The static private IP address within the specified subnet to assign to this resource
		 * @return The next stage of the definition
		 */
		R withPrivateIpAddressStatic(String staticPrivateIpAddress);
	}

}
