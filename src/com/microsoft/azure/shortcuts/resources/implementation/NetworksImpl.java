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

import java.util.ArrayList;
import java.util.List;

import com.microsoft.azure.management.network.models.AddressSpace;
import com.microsoft.azure.management.network.models.DhcpOptions;
import com.microsoft.azure.management.network.models.VirtualNetwork;
import com.microsoft.azure.shortcuts.resources.Network;
import com.microsoft.azure.shortcuts.resources.Networks;


public class NetworksImpl 
	extends GroupableResourcesBaseImpl<
		Network, 
		VirtualNetwork,
		NetworkImpl>
	implements Networks {
	
	NetworksImpl(Subscription subscription) {
		super(subscription);
	}
	
	@Override
	public NetworkImpl define(String name) throws Exception {
		VirtualNetwork azureNetwork = new VirtualNetwork();
		azureNetwork.setName(name);
		azureNetwork.setSubnets(new ArrayList<com.microsoft.azure.management.network.models.Subnet>());
		
		// Ensure address space
		ArrayList<String> cidrs = new ArrayList<>();
		AddressSpace addressSpace = new AddressSpace();
		addressSpace.setAddressPrefixes(cidrs);
		azureNetwork.setAddressSpace(addressSpace);

		// Ensure DHCP options
		ArrayList<String> dnsServers = new ArrayList<String>(); 
		DhcpOptions dhcpOptions = new DhcpOptions(); 
		dhcpOptions.setDnsServers(dnsServers);
		azureNetwork.setDhcpOptions(dhcpOptions);

		return wrap(azureNetwork);
	}

	@Override
	public void delete(String groupName, String name) throws Exception {
		subscription.networkManagementClient().getVirtualNetworksOperations().delete(groupName, name);
	}


	/***************************************************
	 * Helpers
	 ***************************************************/
	
	@Override
	protected List<VirtualNetwork> getNativeEntities(String resourceGroupName) throws Exception {
		if(resourceGroupName == null) {
			return this.subscription.networkManagementClient().getVirtualNetworksOperations().listAll().getVirtualNetworks();
		} else {
			return this.subscription.networkManagementClient().getVirtualNetworksOperations().list(resourceGroupName).getVirtualNetworks();
		}
	}
	
	@Override
	protected VirtualNetwork getNativeEntity(String groupName, String name) throws Exception {
		return subscription.networkManagementClient().getVirtualNetworksOperations().get(groupName, name).getVirtualNetwork();
	}
	
	@Override 
	protected NetworkImpl wrap(VirtualNetwork nativeItem) {
		return new NetworkImpl(nativeItem, this);
	}
}
