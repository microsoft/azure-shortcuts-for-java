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

import java.util.Map;

import com.microsoft.azure.shortcuts.common.Deletable;
import com.microsoft.azure.shortcuts.common.Indexable;
import com.microsoft.azure.shortcuts.common.Provisionable;
import com.microsoft.azure.shortcuts.common.Refreshable;
import com.microsoft.azure.shortcuts.common.Wrapper;
import com.microsoft.azure.shortcuts.resources.common.DefinitionCombos;
import com.microsoft.azure.shortcuts.resources.common.GroupResourceBase;

public interface NetworkInterface extends 
	Indexable,
	GroupResourceBase,
	Refreshable<NetworkInterface>,
	Wrapper<com.microsoft.azure.management.network.models.NetworkInterface>,
	Deletable {

	Map<String, PublicIpAddress> publicIpAddresses();
	
	/**
	 * A new blank network interface definition
	 */
	public interface DefinitionBlank extends 
		GroupResourceBase.DefinitionWithRegion<DefinitionWithGroup> {
	}
	
	/**
	 * A network interface definition allowing to specify a group to associate with it
	 */
	public interface DefinitionWithGroup extends
		GroupResourceBase.DefinitionWithGroup<DefinitionWithNetwork> {}
	
	/**
	 * A network interface definition allowing to associate a virtual network with the network interface
	 */
	public interface DefinitionWithNetwork extends 
		DefinitionCombos.WithNetworkExisting<DefinitionWithSubnet>,
		DefinitionCombos.WithNetworkNew<DefinitionWithPrivateIp> {}
	
	/**
	 * A network interface definition allowing to specify a subnet from the selected network to associate the network interface with
	 */
	public interface DefinitionWithSubnet extends 
		DefinitionCombos.WithSubnet<DefinitionWithPrivateIp> {}
	
	/**
	 * A network interface definition allowing to assign a private IP address within an existing virtual network subnet
	 */
	public interface DefinitionWithPrivateIp extends 
		DefinitionCombos.WithPrivateIpAddress<DefinitionWithPublicIpAddress> {}	
	
	/**
	 * A network interface definition allowing to associate it with a public IP address
	 */
	public interface DefinitionWithPublicIpAddress extends 
		DefinitionCombos.WithPublicIpAddress<DefinitionProvisionable> { }	
	
	/**
	 * A network interface definition with sufficient input parameters specified to be provisioned in the cloud
	 */
	public interface DefinitionProvisionable extends 
		Provisionable<NetworkInterface>,
		GroupResourceBase.DefinitionWithTags<DefinitionProvisionable> {
	}
}
