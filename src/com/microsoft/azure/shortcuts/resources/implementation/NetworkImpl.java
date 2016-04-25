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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import java.util.TreeMap;

import com.microsoft.azure.management.network.models.VirtualNetwork;
import com.microsoft.azure.shortcuts.common.implementation.IndexableWrapperImpl;
import com.microsoft.azure.shortcuts.resources.Network;

class NetworkImpl 
	extends 
		GroupableResourceBaseImpl<
			Network, 
			VirtualNetwork,
			NetworkImpl,
			NetworksImpl>
	implements
		Network,
		Network.Definition {
	
	NetworkImpl(VirtualNetwork azureVirtualNetwork, NetworksImpl collection) {
		super(azureVirtualNetwork.getName(), azureVirtualNetwork, collection);
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
	public Subnet subnets(String id) {
		return this.subnets().get(id);
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
	public NetworkImpl withAddressSpace(String cidr) {
		this.inner().getAddressSpace().getAddressPrefixes().add(cidr);
		return this;
	}
	
	
	/************************************************************
	 * Verbs
	 ************************************************************/
	
	@Override
	public void delete() throws Exception {
		this.collection.subscription().networks().delete(this.id());
	}
	
	@Override
	public Network provision() throws Exception {
		// Create a group as needed
		ensureGroup();
	
		// Ensure address spaces
		if(this.addressSpaces().size() == 0) {
			this.withAddressSpace("10.0.0.0/16");
		}
		
		// Create a subnet as needed, covering the entire first address space
		if(this.subnets().size() == 0) {
			this.withSubnet("subnet1", this.addressSpaces().get(0));
		}
		
		Subscription subscription = this.collection.subscription();
		subscription.networkManagementClient().getVirtualNetworksOperations().createOrUpdate(this.groupName, this.name(), this.inner());
		return subscription.networks().get(this.groupName, this.name());
		
	}
	
	@Override
	public NetworkImpl refresh() throws Exception {
		this.setInner(this.collection.getNativeEntity(
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
