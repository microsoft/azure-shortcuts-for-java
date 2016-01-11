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
import java.util.Map.Entry;

import java.util.TreeMap;

import com.microsoft.azure.management.network.models.AddressSpace;
import com.microsoft.azure.management.network.models.DhcpOptions;
import com.microsoft.azure.management.network.models.VirtualNetwork;
import com.microsoft.azure.management.resources.models.ResourceGroupExtended;
import com.microsoft.azure.shortcuts.common.implementation.EntitiesImpl;
import com.microsoft.azure.shortcuts.common.implementation.IndexableWrapperImpl;
import com.microsoft.azure.shortcuts.resources.Group;
import com.microsoft.azure.shortcuts.resources.Network;
import com.microsoft.azure.shortcuts.resources.Networks;
import com.microsoft.azure.shortcuts.resources.Region;
import com.microsoft.azure.shortcuts.resources.common.implementation.GroupResourceBaseImpl;


public class NetworksImpl 
	extends EntitiesImpl<Azure>
	implements Networks {
	
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
		for(VirtualNetwork nativeItem : getNativeEntities(groupName)) {
			NetworkImpl wrapper = new NetworkImpl(nativeItem);
			wrappers.put(nativeItem.getId(), wrapper);
		}
		
		return Collections.unmodifiableMap(wrappers);
	}

	
	@Override
	public NetworkImpl get(String resourceId) throws Exception {
		return this.get(
			ResourcesImpl.groupFromResourceId(resourceId), 
			ResourcesImpl.nameFromResourceId(resourceId));
	}
	

	@Override
	public NetworkImpl get(String groupName, String name) throws Exception {
		return new NetworkImpl(this.getNativeEntity(groupName, name));
	}
	
	@Override
	public NetworkImpl define(String name) throws Exception {
		return createWrapper(name);
	}

	
	@Override
	public void delete(String id) throws Exception {
		this.delete(
			ResourcesImpl.groupFromResourceId(id), 
			ResourcesImpl.nameFromResourceId(id));
	}
	
	
	@Override
	public void delete(String groupName, String name) throws Exception {
		azure.networkManagementClient().getVirtualNetworksOperations().delete(groupName, name);
	}


	/***************************************************
	 * Helpers
	 ***************************************************/
	
	// Helper to get the networks from Azure
	private ArrayList<VirtualNetwork> getNativeEntities(String resourceGroupName) throws Exception {
		if(resourceGroupName == null) {
			return this.azure.networkManagementClient().getVirtualNetworksOperations().listAll().getVirtualNetworks();
		} else {
			return this.azure.networkManagementClient().getVirtualNetworksOperations().list(resourceGroupName).getVirtualNetworks();
		}
	}
	
	
	// Helper to get a network from Azure
	private VirtualNetwork getNativeEntity(String groupName, String name) throws Exception {
		return azure.networkManagementClient().getVirtualNetworksOperations().get(groupName, name).getVirtualNetwork();
	}
	
	
	private NetworkImpl createWrapper(String name) {
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

		return new NetworkImpl(azureNetwork);
		
	}
	
	
	/***************************************************************
	 * Implements logic for individual resource group
	 ***************************************************************/
	private class NetworkImpl 
		extends 
			GroupResourceBaseImpl<Network, VirtualNetwork>
		implements
			Network,
			Network.DefinitionBlank,
			Network.DefinitionProvisionable,
			Network.DefinitionWithAddressSpace,
			Network.DefinitionProvisionableWithSubnet, 
			Network.DefinitionWithSubnet {
		
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
		public List<String> addressSpaces() {
			return Collections.unmodifiableList(this.inner().getAddressSpace().getAddressPrefixes());
		}
		
		@Override
		public List<String> dnsServerIPs() {
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
		
		@Override
		public Subnet subnets(String name) {
			return this.subnets().get(name);
		}

		
		/**************************************************************
		 * Setters (fluent interface)
		 **************************************************************/

		@Override
		public NetworkImpl withDnsServer(String ipAddress) {
			this.inner().getDhcpOptions().getDnsServers().add(ipAddress);
			return this;
		}

		@Override
		public NetworkImpl withSubnet(String name, String cidr) {
			com.microsoft.azure.management.network.models.Subnet azureSubnet = new com.microsoft.azure.management.network.models.Subnet(cidr);
			azureSubnet.setName(name);
			this.inner().getSubnets().add(azureSubnet);
			return this;
		}


		@Override
		public NetworkImpl withSubnets(Map<String, String> nameCidrPairs) {
			ArrayList<com.microsoft.azure.management.network.models.Subnet> azureSubnets = 
				new ArrayList<com.microsoft.azure.management.network.models.Subnet>();
			this.inner().setSubnets(azureSubnets);
			for(Entry<String, String> pair : nameCidrPairs.entrySet()) {
				this.withSubnet(pair.getKey(), pair.getValue());
			}
			return this;
		}

		
		@Override
		public NetworkImpl withRegion(String regionName) {
			super.withRegion(regionName);
			return this;
		}
		
		@Override
		public NetworkImpl withRegion(Region region) {
			return this.withRegion(region.toString());
		}

		@Override
		public NetworkImpl withGroupExisting(String groupName) {
			super.withGroupExisting(groupName);
			return this;
		}

		@Override
		public NetworkImpl withGroupNew(String name) {
			super.withGroupNew(name);
			return this;
		}

		@Override
		public NetworkImpl withGroupExisting(Group group) {
			return this.withGroupExisting(group.name());
		}

		@Override
		public NetworkImpl withGroupExisting(ResourceGroupExtended group) {
			return this.withGroupExisting(group.getName());
		}
		
		@Override
		public NetworkImpl withTags(Map<String, String> tags) {
			super.withTags(tags);
			return this;
		}


		@Override
		public NetworkImpl withTag(String name, String value) {
			super.withTag(name, value);
			return this;
		}
		
		
		@Override
		public NetworkImpl withoutTag(String name) {
			super.withoutTag(name);
			return this;
		}
		
		
		@Override
		public NetworkImpl withAddressSpace(String cidr) {
			this.inner().getAddressSpace().getAddressPrefixes().add(cidr);
			return this;
		}


		/************************************************************
		 * Verbs
		 ************************************************************/

		@Override
		public void delete() throws Exception {
			azure.networks().delete(this.id());
		}

		@Override
		public NetworkImpl provision() throws Exception {
			// Create a group as needed
			ensureGroup(azure);
		
			// Ensure address spaces
			if(this.addressSpaces().size() == 0) {
				this.withAddressSpace("10.0.0.0/16");
			}
			
			// Create a subnet as needed, covering the entire first address space
			if(this.subnets().size() == 0) {
				this.withSubnet("subnet1", this.addressSpaces().get(0));
			}
			
			azure.networkManagementClient().getVirtualNetworksOperations().createOrUpdate(this.groupName, this.name(), this.inner());
			return get(this.groupName, this.name());
		}
		
		@Override
		public NetworkImpl refresh() throws Exception {
			this.setInner(getNativeEntity(
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
