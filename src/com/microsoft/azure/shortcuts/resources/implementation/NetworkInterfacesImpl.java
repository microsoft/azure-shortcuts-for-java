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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.microsoft.azure.management.network.models.IpAllocationMethod;
import com.microsoft.azure.management.network.models.NetworkInterfaceIpConfiguration;
import com.microsoft.azure.management.network.models.ResourceId;
import com.microsoft.azure.management.network.models.VirtualNetwork;
import com.microsoft.azure.shortcuts.resources.Network;
import com.microsoft.azure.shortcuts.resources.NetworkInterface;
import com.microsoft.azure.shortcuts.resources.NetworkInterfaces;
import com.microsoft.azure.shortcuts.resources.PublicIpAddress;
import com.microsoft.azure.shortcuts.resources.common.implementation.GroupableResourceBaseImpl;
import com.microsoft.azure.shortcuts.resources.common.implementation.GroupableResourcesBaseImpl;


public class NetworkInterfacesImpl 
	extends GroupableResourcesBaseImpl<
		Azure, 
		NetworkInterface, 
		com.microsoft.azure.management.network.models.NetworkInterface,
		NetworkInterfacesImpl.NetworkInterfaceImpl>
	implements NetworkInterfaces {
		
	NetworkInterfacesImpl(Azure azure) {
		super(azure);
	}
	
	@Override
	public NetworkInterfaceImpl define(String name) throws Exception {
		com.microsoft.azure.management.network.models.NetworkInterface azureNetworkInterface = new com.microsoft.azure.management.network.models.NetworkInterface();
		azureNetworkInterface.setName(name);
		
		// Default IP configs, creating Primary by default
		azureNetworkInterface.setIpConfigurations(new ArrayList<NetworkInterfaceIpConfiguration>(Arrays.asList(new NetworkInterfaceIpConfiguration())));
		
		// TODO Min settings
		
		return wrap(azureNetworkInterface);
	}

	@Override
	public void delete(String groupName, String name) throws Exception {
		azure.networkManagementClient().getNetworkInterfacesOperations().delete(groupName, name);
	}


	/***************************************************
	 * Helpers
	 ***************************************************/
	
	@Override
	protected List<com.microsoft.azure.management.network.models.NetworkInterface> getNativeEntities(String groupName) throws Exception {
		if(groupName == null) {
			return this.azure.networkManagementClient().getNetworkInterfacesOperations().listAll().getNetworkInterfaces();
		} else {
			return this.azure.networkManagementClient().getNetworkInterfacesOperations().list(groupName).getNetworkInterfaces();
		}
	}
	
	@Override
	protected com.microsoft.azure.management.network.models.NetworkInterface getNativeEntity(String groupName, String name) throws Exception {
		return azure.networkManagementClient().getNetworkInterfacesOperations().get(groupName, name).getNetworkInterface();
	}
	
	@Override
	protected NetworkInterfaceImpl wrap(com.microsoft.azure.management.network.models.NetworkInterface nativeItem) {
		return new NetworkInterfaceImpl(nativeItem);
	}
	

	/***************************************************************
	 * Implements logic for individual resource group
	 ***************************************************************/
	class NetworkInterfaceImpl 
		extends 
			GroupableResourceBaseImpl<
				NetworkInterface, 
				com.microsoft.azure.management.network.models.NetworkInterface,
				NetworkInterfaceImpl>
		implements
			NetworkInterface,
			NetworkInterface.DefinitionBlank,
			NetworkInterface.DefinitionWithGroup,
			NetworkInterface.DefinitionWithNetwork,
			NetworkInterface.DefinitionWithSubnet,
			NetworkInterface.DefinitionWithPrivateIp,
			NetworkInterface.DefinitionWithPublicIpAddress,
			NetworkInterface.DefinitionProvisionable {
		
		private NetworkInterfaceImpl(com.microsoft.azure.management.network.models.NetworkInterface azureNetworkInterface) {
			super(azureNetworkInterface.getName(), azureNetworkInterface);
		}

		private boolean isPublicIpAddressExisting;
		private String publicIpAddressDns;
		
		private boolean isNetworkExisting;
		private String networkId;
		private String addressSpace;
		
		private String subnetId;

		/***********************************************************
		 * Helpers
		 ***********************************************************/
		private NetworkInterfaceIpConfiguration getPrimaryIpConfiguration() {
			// TODO: in the future, Azure will support multiple ipConfigs on a NIC, but currently it doesn't, so the first one can be assumed to be the primary
			return this.inner().getIpConfigurations().get(0); 
		}
		
		
		private void ensurePublicIpAddress() throws Exception {
			if(!this.isPublicIpAddressExisting) {
				// Create a new public IP
				if(this.publicIpAddressDns == null) {
					// Generate a public leaf domain name if needed
					this.publicIpAddressDns = this.name().toLowerCase();
				}
				
				PublicIpAddress pip = azure.publicIpAddresses().define(this.publicIpAddressDns)
					.withRegion(this.region())
					.withGroupExisting(this.groupName)
					.withLeafDomainLabel(this.publicIpAddressDns)
					.provision();
				this.isPublicIpAddressExisting = true;
				this.withPublicIpAddressExisting(pip.id());
			}
		}
		
		
		private Network.Subnet ensureSubnet(Network network) throws Exception {
			if(network == null) {
				return null;
			} else if(this.subnetId != null) {
				return network.subnets(this.subnetId);
			} else {
				// If no subnet specified, return the first one
				return network.subnets().values().iterator().next();
			}
		}
		
		
		//TODO Remove duplication with VirtualMachineImpl's verion
		private Network ensureNetwork() throws Exception {
			if(!this.isNetworkExisting) {
				// Create a new virtual network
				if(this.networkId == null) {
					// Generate a name if needed
					this.networkId = this.name() + "net";
				}
		
				Network network = azure.networks().define(this.networkId)
					.withRegion(this.region())
					.withGroupExisting(groupName)
					.withAddressSpace(this.addressSpace)
					.provision();
				this.isNetworkExisting = true;
				return network;
			} else {
				return azure.networks(this.networkId);
			}
		}
		
		
		/***********************************************************
		 * Getters
		 ***********************************************************/

		@Override
		public Map<String, PublicIpAddress> publicIpAddresses() {
			TreeMap<String, PublicIpAddress> pips = new TreeMap<>();
			try {
				for(NetworkInterfaceIpConfiguration ipConfig : this.inner().getIpConfigurations()) {
					ResourceId pipId = ipConfig.getPublicIpAddress();
					if(pipId == null) {
						continue;
					} else {
						PublicIpAddress pip = azure.publicIpAddresses(pipId.getId());
						pips.put(pip.id(), pip);						
					}
				}
			} catch (Exception e) {
			}
			return pips;
		}

		
		/**************************************************************
		 * Setters (fluent interface)
		 **************************************************************/

		@Override
		public NetworkInterfaceImpl withPrivateIpAddressDynamic() {
			return this.withPrivateIpAddressStatic(null);
		}
		
		
		@Override
		public NetworkInterfaceImpl withPrivateIpAddressStatic(String staticPrivateIpAddress) {
			NetworkInterfaceIpConfiguration ipConfig = getPrimaryIpConfiguration(); 
			ipConfig.setPrivateIpAllocationMethod((staticPrivateIpAddress != null) ? IpAllocationMethod.STATIC : IpAllocationMethod.DYNAMIC);
			ipConfig.setPrivateIpAddress(staticPrivateIpAddress);
			return this;
		}

		@Override
		public NetworkInterfaceImpl withPublicIpAddressExisting(com.microsoft.azure.management.network.models.PublicIpAddress publicIpAddress) {
			return this.withPublicIpAddressExisting((publicIpAddress != null) ? publicIpAddress.getId() : null);
		}

		@Override
		public NetworkInterfaceImpl withPublicIpAddressExisting(PublicIpAddress publicIpAddress) {
			return this.withPublicIpAddressExisting((publicIpAddress != null) ? publicIpAddress.id() : null);
		}

		// Helper to associate with an existing public IP address using its resource ID
		private NetworkInterfaceImpl withPublicIpAddressExisting(String resourceId) {
			this.isPublicIpAddressExisting = true;
			NetworkInterfaceIpConfiguration ipConfig = getPrimaryIpConfiguration();
			if(resourceId == null) {
				ipConfig.setPublicIpAddress(null);
			} else {
				ResourceId r = new ResourceId();
				r.setId(resourceId);
				ipConfig.setPublicIpAddress(r);
			}
			return this;
		}
		
		@Override
		public NetworkInterfaceImpl withPublicIpAddressNew() {
			return this.withPublicIpAddressNew(null);
		}

		@Override
		public NetworkInterfaceImpl withPublicIpAddressNew(String leafDnsLabel) {
			this.isPublicIpAddressExisting = false;
			this.publicIpAddressDns = (leafDnsLabel == null) ? null : leafDnsLabel.toLowerCase();
			return this;
		}

		@Override
		public NetworkInterfaceImpl withoutPublicIpAddress() {
			return this.withPublicIpAddressExisting((PublicIpAddress)null);
		}
		
		@Override
		public NetworkInterfaceImpl withNetworkNew(Network.DefinitionProvisionable networkDefinition) throws Exception {
			return this.withNetworkExisting(networkDefinition.provision());
		}

		@Override
		public NetworkInterfaceImpl withNetworkNew(String name, String addressSpace) {
			this.isNetworkExisting = false;
			this.networkId = name;
			this.addressSpace = addressSpace;
			return this;
		}

		@Override
		public NetworkInterfaceImpl withNetworkNew(String addressSpace) {
			return this.withNetworkNew(null, addressSpace);
		}

		@Override
		public NetworkInterfaceImpl withNetworkExisting(String id) {
			this.isNetworkExisting = true;
			this.networkId = id;
			return this;
		}

		@Override
		public NetworkInterfaceImpl withNetworkExisting(Network network) {
			if(network != null) {
				return this.withNetworkExisting(network.id());
			} else {
				return null;
			}
		}


		@Override
		public NetworkInterfaceImpl withNetworkExisting(VirtualNetwork network) {
			if(network != null) {
				return this.withNetworkExisting(network.getId());
			} else {
				return null;
			}
		}

		@Override
		public NetworkInterfaceImpl withSubnet(String subnetId) {
			this.subnetId = subnetId;
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
		public NetworkInterface provision() throws Exception {
			// Create a group as needed
			ensureGroup(azure);
		
			// Ensure virtual network
			Network network = ensureNetwork();
			
			// Ensure subnet
			Network.Subnet subnet = ensureSubnet(network);
			
			// Set the subnet on the primary (first) IP configuration
			NetworkInterfaceIpConfiguration ipConfig = this.inner().getIpConfigurations().get(0);
			ipConfig.setName(subnet.inner().getName());
			ipConfig.setSubnet(subnet.inner());

			// Ensure public IP as needed
			ensurePublicIpAddress();
			
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
