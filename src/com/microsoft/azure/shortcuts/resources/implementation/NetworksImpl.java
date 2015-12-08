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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.microsoft.azure.management.network.models.VirtualNetwork;
import com.microsoft.azure.shortcuts.common.implementation.EntitiesImpl;
import com.microsoft.azure.shortcuts.common.implementation.IndexableWrapperImpl;
import com.microsoft.azure.shortcuts.resources.Network;
import com.microsoft.azure.shortcuts.resources.Networks;


public class NetworksImpl 
	extends EntitiesImpl<Azure>
	implements Networks {
	
	List<Network> groups = null;
	
	NetworksImpl(Azure azure) {
		super(azure);
	}
	
	
	@Override
	public Map<String, Network> list() throws Exception {
		return this.list(null);
	}

	
	@Override
	public Map<String, Network> list(String groupName) throws Exception {
		HashMap<String, Network> wrappers = new HashMap<>();
		for(VirtualNetwork nativeItem : getAzureVirtualNetworks(groupName)) {
			NetworkImpl wrapper = new NetworkImpl(nativeItem);
			wrappers.put(nativeItem.getId(), wrapper);
		}
		
		return Collections.unmodifiableMap(wrappers);
	}

	
	@Override
	public Network get(String resourceId) throws Exception {
		return this.get(
			ResourcesImpl.groupFromResourceId(resourceId), 
			ResourcesImpl.nameFromResourceId(resourceId));
	}
	

	@Override
	public Network get(String groupName, String name) throws Exception {
		return new NetworkImpl(this.getAzureVirtualNetwork(groupName, name));
	}

	
	/***************************************************
	 * Helpers
	 ***************************************************/
	
	// Helper to get the networks from Azure
	private ArrayList<com.microsoft.azure.management.network.models.VirtualNetwork> getAzureVirtualNetworks(String resourceGroupName) throws Exception {
		if(resourceGroupName == null) {
			return this.azure.networkManagementClient().getVirtualNetworksOperations().listAll().getVirtualNetworks();
		} else {
			return this.azure.networkManagementClient().getVirtualNetworksOperations().list(resourceGroupName).getVirtualNetworks();
		}
	}
	
	
	// Helper to get a network from Azure
	private com.microsoft.azure.management.network.models.VirtualNetwork getAzureVirtualNetwork(String groupName, String name) throws Exception {
		return azure.networkManagementClient().getVirtualNetworksOperations().get(groupName, name).getVirtualNetwork();
	}
	
	
	/***************************************************************
	 * Implements logic for individual resource group
	 ***************************************************************/
	private class NetworkImpl 
		extends 
			ResourceBaseExtendedImpl<Network, VirtualNetwork>
		implements
			Network {
		
		private NetworkImpl(VirtualNetwork azureVirtualNetwork) {
			super(azureVirtualNetwork.getName(), azureVirtualNetwork);
		}


		/***********************************************************
		 * Getters
		 ***********************************************************/
		@Override
		public String provisioningState() {
			return this.inner().getProvisioningState();
		}
		
		@Override
		public List<String> addressPrefixes() {
			return Collections.unmodifiableList(this.inner().getAddressSpace().getAddressPrefixes());
		}
		
		@Override
		public List<String> dnsServers() {
			return Collections.unmodifiableList(this.inner().getDhcpOptions().getDnsServers());
		}

		@Override
		public Map<String, Subnet> subnets() {
			TreeMap<String, Subnet> wrappers = new TreeMap<>();
			for(com.microsoft.azure.management.network.models.Subnet nativeObject : this.inner().getSubnets()) {
				SubnetImpl wrapper = new SubnetImpl(nativeObject.getName(), nativeObject);
				wrappers.put(wrapper.id(), new SubnetImpl(nativeObject.getName(), nativeObject));
			}
			return Collections.unmodifiableMap(wrappers);
		}

		
		/**************************************************************
		 * Setters (fluent interface)
		 **************************************************************/
		

		/************************************************************
		 * Verbs
		 ************************************************************/
		
		@Override
		public NetworkImpl refresh() throws Exception {
			this.setInner(getAzureVirtualNetwork(
					ResourcesImpl.groupFromResourceId(this.id()), 
					ResourcesImpl.nameFromResourceId(this.id())));
			return this;
		}
		
		
		/*****************************************************
		 * Implements Subnet wrapper
		 *****************************************************/
		private class SubnetImpl
			extends IndexableWrapperImpl<com.microsoft.azure.management.network.models.Subnet>
			implements Subnet {

			protected SubnetImpl(String name, com.microsoft.azure.management.network.models.Subnet innerObject) {
				super(name, innerObject);
			}

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
		}
	}
}
