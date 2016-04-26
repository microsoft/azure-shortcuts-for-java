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
package com.microsoft.azure.shortcuts.resources.implementation;

import com.microsoft.azure.shortcuts.resources.Network;
import com.microsoft.azure.shortcuts.resources.Subnet;

/***************************************************************
 * Implements logic for individual NSG
 ***************************************************************/
class SubnetImpl 
	extends
		ChildResourceImpl<com.microsoft.azure.management.network.models.Subnet, NetworkImpl>
	implements
		Subnet,
		Subnet.Definition<Network.DefinitionProvisionableWithSubnet> {
	SubnetImpl(
			com.microsoft.azure.management.network.models.Subnet nativeItem,
			NetworkImpl network) {
		super(nativeItem.getName(), nativeItem, network);
	}

	/***********************************************************
	 * Getters
	 ***********************************************************/
	@Override
	public String addressPrefix() {
		return this.inner().getAddressPrefix();
	}
	
	@Override
	public String networkSecurityGroup() {
		if(this.inner().getNetworkSecurityGroup() != null) {
			return this.inner().getNetworkSecurityGroup().getId();
		} else {
			return null;
		}
	}
	
	
	/**************************************************************
	 * Setters (fluent interface)
	 **************************************************************/

	@Override
	public SubnetImpl withAddressPrefix(String cidr) {
		this.inner().setAddressPrefix(cidr);
		return this;
	}


	/************************************************************
	 * Verbs
	 ************************************************************/

	@Override
	public NetworkImpl attach() throws Exception {
		this.parent().inner().getSubnets().add(this.inner());
		return this.parent();
	}

	/*********************************************************
	 * Helpers
	 *********************************************************/
}
