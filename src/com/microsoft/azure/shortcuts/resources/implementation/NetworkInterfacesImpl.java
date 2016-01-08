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
import java.util.Map;

import com.microsoft.azure.management.network.models.IpAllocationMethod;
import com.microsoft.azure.management.network.models.NetworkInterfaceIpConfiguration;
import com.microsoft.azure.management.resources.models.ResourceGroupExtended;
import com.microsoft.azure.shortcuts.common.implementation.EntitiesImpl;
import com.microsoft.azure.shortcuts.resources.Group;
import com.microsoft.azure.shortcuts.resources.Network;
import com.microsoft.azure.shortcuts.resources.NetworkInterface;
import com.microsoft.azure.shortcuts.resources.NetworkInterfaces;
import com.microsoft.azure.shortcuts.resources.Region;
import com.microsoft.azure.shortcuts.resources.common.implementation.GroupResourceBaseImpl;


public class NetworkInterfacesImpl 
	extends EntitiesImpl<Azure>
	implements NetworkInterfaces {
		
	NetworkInterfacesImpl(Azure azure) {
		super(azure);
	}
	
	
	@Override
	public Map<String, NetworkInterface> list() throws Exception {
		return this.list(null);
	}

	
	@Override
	public Map<String, NetworkInterface> list(String groupName) throws Exception {
		HashMap<String, NetworkInterface> wrappers = new HashMap<>();
		for(com.microsoft.azure.management.network.models.NetworkInterface nativeItem : getNativeEntities(groupName)) {
			NetworkInterfaceImpl wrapper = new NetworkInterfaceImpl(nativeItem);
			wrappers.put(nativeItem.getId(), wrapper);
		}
		
		return Collections.unmodifiableMap(wrappers);
	}

	
	@Override
	public NetworkInterfaceImpl get(String resourceId) throws Exception {
		return this.get(
			ResourcesImpl.groupFromResourceId(resourceId), 
			ResourcesImpl.nameFromResourceId(resourceId));
	}
	

	@Override
	public NetworkInterfaceImpl get(String groupName, String name) throws Exception {
		return new NetworkInterfaceImpl(this.getNativeEntity(groupName, name));
	}
	
	
	@Override
	public NetworkInterfaceImpl define(String name) throws Exception {
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
		azure.networkManagementClient().getNetworkInterfacesOperations().delete(groupName, name);
	}


	/***************************************************
	 * Helpers
	 ***************************************************/
	
	// Helper to get the networks from Azure
	private ArrayList<com.microsoft.azure.management.network.models.NetworkInterface> getNativeEntities(String resourceGroupName) throws Exception {
		if(resourceGroupName == null) {
			return this.azure.networkManagementClient().getNetworkInterfacesOperations().listAll().getNetworkInterfaces();
		} else {
			return this.azure.networkManagementClient().getNetworkInterfacesOperations().list(resourceGroupName).getNetworkInterfaces();
		}
	}
	
	
	// Helper to get a network from Azure
	private com.microsoft.azure.management.network.models.NetworkInterface getNativeEntity(String groupName, String name) throws Exception {
		return azure.networkManagementClient().getNetworkInterfacesOperations().get(groupName, name).getNetworkInterface();
	}
	
	
	// Helper to create a wrapper
	private NetworkInterfaceImpl createWrapper(String name) {
		com.microsoft.azure.management.network.models.NetworkInterface azureNetworkInterface = new com.microsoft.azure.management.network.models.NetworkInterface();
		azureNetworkInterface.setName(name);
		
		// Default IP configs
		azureNetworkInterface.setIpConfigurations(new ArrayList<NetworkInterfaceIpConfiguration>());
		
		// TODO Min settings
		
		return new NetworkInterfaceImpl(azureNetworkInterface);
	}
	
	
	/***************************************************************
	 * Implements logic for individual resource group
	 ***************************************************************/
	private class NetworkInterfaceImpl 
		extends 
			GroupResourceBaseImpl<NetworkInterface, com.microsoft.azure.management.network.models.NetworkInterface>
		implements
			NetworkInterface,
			NetworkInterface.DefinitionWithSubnetPrimary,
			NetworkInterface.DefinitionBlank,
			NetworkInterface.DefinitionProvisionable {
		
		private NetworkInterfaceImpl(com.microsoft.azure.management.network.models.NetworkInterface azureNetworkInterface) {
			super(azureNetworkInterface.getName(), azureNetworkInterface);
		}


		/***********************************************************
		 * Getters
		 ***********************************************************/

		
		/**************************************************************
		 * Setters (fluent interface)
		 **************************************************************/

		@Override
		public NetworkInterfaceImpl withSubnetPrimary(Network.Subnet subnet) {
			return this.withSubnetPrimary(subnet, null);
		}
		
		
		@Override
		public NetworkInterfaceImpl withSubnetPrimary(Network.Subnet subnet, String staticPrivateIpAddress) {
			if(subnet == null) {
				return null;
			}
			
			NetworkInterfaceIpConfiguration ipConfig = new NetworkInterfaceIpConfiguration();
			this.inner().getIpConfigurations().add(ipConfig);
			ipConfig.setName(subnet.id()); // TODO Allow to customize?
			ipConfig.setSubnet(subnet.inner());
			ipConfig.setPrivateIpAllocationMethod((staticPrivateIpAddress != null) ? IpAllocationMethod.STATIC : IpAllocationMethod.DYNAMIC);
			ipConfig.setPrivateIpAddress(staticPrivateIpAddress);
			return this;
			
			//TODO: public IP address
		}

		
		@Override
		public NetworkInterfaceImpl withRegion(String regionName) {
			super.withRegion(regionName);
			return this;
		}
		
		@Override
		public NetworkInterfaceImpl withRegion(Region region) {
			return this.withRegion(region.toString());
		}

		@Override
		public NetworkInterfaceImpl withGroupExisting(String groupName) {
			super.withGroupExisting(groupName);
			return this;
		}

		@Override
		public NetworkInterfaceImpl withGroupNew(String name) {
			super.withGroupNew(name);
			return this;
		}

		@Override
		public NetworkInterfaceImpl withGroupExisting(Group group) {
			return this.withGroupExisting(group.name());
		}

		@Override
		public NetworkInterfaceImpl withGroupExisting(ResourceGroupExtended group) {
			return this.withGroupExisting(group.getName());
		}
		
		@Override
		public NetworkInterfaceImpl withTags(Map<String, String> tags) {
			super.withTags(tags);
			return this;
		}


		@Override
		public NetworkInterfaceImpl withTag(String name, String value) {
			super.withTag(name, value);
			return this;
		}
		
		
		@Override
		public NetworkInterfaceImpl withoutTag(String name) {
			super.withoutTag(name);
			return this;
		}
		
		

		/************************************************************
		 * Verbs
		 ************************************************************/

		@Override
		public void delete() throws Exception {
			azure.networkInterfaces().delete(this.id());
		}

		@Override
		public NetworkInterfaceImpl provision() throws Exception {
			// Create a group as needed
			ensureGroup(azure);
		
			// TODO
			
			azure.networkManagementClient().getNetworkInterfacesOperations().createOrUpdate(this.groupName, this.name(), this.inner());
			return get(this.groupName, this.name());
		}
		
		@Override
		public NetworkInterfaceImpl refresh() throws Exception {
			this.setInner(getNativeEntity(
					ResourcesImpl.groupFromResourceId(this.id()), 
					ResourcesImpl.nameFromResourceId(this.id())));
			return this;
		}
	}
}
