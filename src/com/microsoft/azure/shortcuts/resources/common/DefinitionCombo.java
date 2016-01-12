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

import com.microsoft.azure.management.network.models.VirtualNetwork;
import com.microsoft.azure.shortcuts.resources.Network;
import com.microsoft.azure.shortcuts.resources.StorageAccount;

public interface DefinitionCombo {
	
	/**
	 * A resource definition allowing to associate a virtual network with this resource
	 */
	public interface WithNetwork<T> {
		/**
		 * Associates an existing virtual network with this resource
		 * @param id The resource ID of the virtual network to associate with the resource
		 * @return The next stage of the resource definition
		 */
		T withNetworkExisting(String id);
		
		/**
		 * Associates an existing virtual network with this resource
		 * @param network The virtual network to associate with the resource
		 * @return The next stage of the resource definition
		 */
		T withNetworkExisting(Network network);
		
		/**
		 * Associates an existing virtual network with this resource
		 * @param network The Azure SDK VirtualNetwork to associate with the resource
		 * @return The next stage of the resource definition
		 */
		T withNetworkExisting(VirtualNetwork network);
		
		/**
		 * Creates a new virtual network to associate with this resource, based on the provided definition
		 * @param networkDefinition A provisionable definition of a virtual network
		 * @return The next stage of the resource definition
		 */
		T withNetworkNew(Network.DefinitionProvisionable networkDefinition) throws Exception;
		
		/**
		 * Creates a new virtual network to associate with this resource, in the same resource group and region, 
		 * and with one default subnet covering the entirety of the network's IP address space
		 * @param name The name of the new virtual network
		 * @return The next stage of the resource definition
		 */
		T withNetworkNew(String name);
		
		/**
		 * Creates a new virtual network to associate with this resource, with a name derived from the name of this resource, 
		 * in the same resource group and region, and with one default subnet covering the entirety of the network's IP address space
		 * @return The next stage of the resource definition
		 */
		T withNetworkNew();
	}

	
	/**
	 * A resource definition allowing to associate a storage account with this resource
	 */
	interface WithStorageAccount<T> {
		/**
		 * Associates an existing storage account with this resource
		 * @param name The name of an existing storage account to associate with this resource
		 * @return The next stage of the resource definition
		 */
		T withStorageAccountExisting(String name);
		
		/**
		 * Associates an existing storage account with this resource
		 * @param An existing storage account to associate with this resource
		 * @return The next stage of the resource definition
		 */
		T withStorageAccountExisting(StorageAccount storageAccount);

		/**
		 * Associates an existing storage account with this resource
		 * @param storageAccount An existing Azure SDK StorageAccount to associate with this resource
		 * @return The next stage of the resource definition
		 */
		T withStorageAccountExisting(com.microsoft.azure.management.storage.models.StorageAccount storageAccount);

		/**
		 * Creates a new storage account to associate with this resource, in the same region and resource group as this resource
		 * @param name The name of the storage account to create and associate with this resource
		 * @return The next stage of the resource definition
		 */
		T withStorageAccountNew(String name);
		
		/**
		 * Creates a new storage account to associate with this resource, in the same region and resource group as this resource, 
		 * and with a name derived from the name of this resource
		 * @return The next stage of the resource definition
		 */
		T withStorageAccountNew();
		

		/**
		 * Creates a new storage account to associate with this resource, based on the provided definition
		 * @param definition A provisionable definition of a storage account
		 * @return The next stage of the resource definition
		 */
		T withStorageAccountNew(StorageAccount.DefinitionProvisionable definition) throws Exception;
	}


	
}
