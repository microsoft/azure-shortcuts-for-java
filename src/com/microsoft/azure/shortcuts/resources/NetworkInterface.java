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

import com.microsoft.azure.shortcuts.common.Deletable;
import com.microsoft.azure.shortcuts.common.Indexable;
import com.microsoft.azure.shortcuts.common.Provisionable;
import com.microsoft.azure.shortcuts.common.Refreshable;
import com.microsoft.azure.shortcuts.common.Wrapper;
import com.microsoft.azure.shortcuts.resources.common.GroupResourceBase;

public interface NetworkInterface extends 
	Indexable,
	GroupResourceBase,
	Refreshable<NetworkInterface>,
	Wrapper<com.microsoft.azure.management.network.models.NetworkInterface>,
	Deletable {
	
	/**
	 * A new blank network interface definition
	 */
	public interface DefinitionBlank extends 
		GroupResourceBase.DefinitionWithRegion<DefinitionWithGroup> {
	}
	
	public interface DefinitionWithGroup extends
		GroupResourceBase.DefinitionWithGroup<DefinitionWithSubnetPrimary> {}
	
	/**
	 * A network interface definition expecting an existing virtual network subnet to associate the NIC with
	 */
	public interface DefinitionWithSubnetPrimary {
		/**
		 * Associates an existing virtual network subnet as the primary subnet for this network interface and enables dynamic private IP address allocation
		 * @param subnet The Subnet to associate with the network interface
		 * @return The next stage of the network interface definition
		 */
		DefinitionProvisionable withSubnetPrimary(Network.Subnet subnet);

		/**
		 * Associates an existing virtual network subnet as the primary subnet for this network interface and specifies a static private IP address from that subnet
		 * @param subnet The Subnet to associate with the network interface
		 * @param staticPrivateIpAddress The static private IP address within the specified subnet to assign to this NIC
		 * @return The next stage of the network interface definition
		 */
		DefinitionProvisionable withSubnetPrimary(Network.Subnet subnet, String staticPrivateIpAddress);
	}
	
	/**
	 * A network interface definition with sufficient input parameters specified to be provisioned in the cloud
	 */
	public interface DefinitionProvisionable extends 
		Provisionable<NetworkInterface>,
		GroupResourceBase.DefinitionWithTags<DefinitionProvisionable> {
	}
}
