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
package com.microsoft.azure.shortcuts.services.implementation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import com.microsoft.azure.shortcuts.common.implementation.EntitiesImpl;
import com.microsoft.azure.shortcuts.common.implementation.IndexableImpl;
import com.microsoft.azure.shortcuts.common.implementation.IndexableRefreshableWrapperImpl;
import com.microsoft.azure.shortcuts.common.implementation.Utils;
import com.microsoft.azure.shortcuts.services.Network;
import com.microsoft.azure.shortcuts.services.Networks;
import com.microsoft.azure.shortcuts.services.Region;
import com.microsoft.windowsazure.management.network.models.NetworkSetConfigurationParameters;
import com.microsoft.windowsazure.management.network.models.NetworkListResponse.AddressSpace;
import com.microsoft.windowsazure.management.network.models.NetworkListResponse.VirtualNetworkSite;

// Class encapsulating the API related to virtual networks
public class NetworksImpl 
	extends EntitiesImpl<Azure>
	implements Networks {
	
	NetworksImpl(Azure azure) {
		super(azure);
	}

	
	@Override
	// Returns information about existing network
	public NetworkImpl get(String name) throws Exception {
		return createVirtualNetworkWrapper(name).refresh();
	}
	
	
	@Override
	// Starts a new network definition
	public Network.DefinitionBlank define(String name) {
		return createVirtualNetworkWrapper(name);
	}
	
	
	@Override
	// Deletes the specified network
	public void delete(String name) throws Exception {
		//  XPath to the network XML to delete
		final String xpath = String.format(
				"/*[local-name()='NetworkConfiguration']"
				+ "/*[local-name()='VirtualNetworkConfiguration']"
				+ "/*[local-name()='VirtualNetworkSites']"
				+ "/*[local-name()='VirtualNetworkSite' and @name='%s']", name);
		
		// Get current network configuration
		String networkConfig = azure.networkManagementClient().getNetworksOperations().getConfiguration().getConfiguration();
		
		// Correct for garbage prefix in XML returned by Azure
		networkConfig = networkConfig.substring(networkConfig.indexOf('<'));

		// Delete the network from the config using the xpath
		final String newConfig = Utils.deleteXMLElement(networkConfig, xpath);
		
		// Update the network configuration
		updateNetworkConfig(newConfig);
	}
	
	
	@Override
	// Lists existing virtual networks
	public List<String> names() {
		try {
			final ArrayList<VirtualNetworkSite> items = 
				azure.networkManagementClient().getNetworksOperations().list().getVirtualNetworkSites();
			ArrayList<String> names = new ArrayList<>();
			for(VirtualNetworkSite item : items) {
				names.add(item.getName());
			}
			return names;
		} catch (Exception e) {
			// Not very actionable, so just return an empty array
			return new ArrayList<>();
		}	
	}
	
	
	/*******************************************************
	 * Helpers
	 *******************************************************/
	
	// Requests a network configuration update based on the XML netconfig representation
	private void updateNetworkConfig(String xml) throws Exception {
		NetworkSetConfigurationParameters params = new NetworkSetConfigurationParameters();
		params.setConfiguration(xml);
		azure.networkManagementClient().getNetworksOperations().setConfiguration(params);
	}
	
	
	// Wraps a native VirtualNetworkSite
	private NetworkImpl createVirtualNetworkWrapper(String name)
	{
		VirtualNetworkSite site = new VirtualNetworkSite();
		site.setName(name);
		AddressSpace azureAddressSpace = new AddressSpace();
		azureAddressSpace.setAddressPrefixes(new ArrayList<String>());
		site.setAddressSpace(azureAddressSpace);
		site.setSubnets(new ArrayList<com.microsoft.windowsazure.management.network.models.NetworkListResponse.Subnet>());
		
		return new NetworkImpl(site);
	}
	
	
	// Encapsulates the required and optional parameters for a network
	private class NetworkImpl 
		extends IndexableRefreshableWrapperImpl<Network, VirtualNetworkSite>
		implements 
			Network.DefinitionBlank, 
			Network.DefinitionWithCidr, 
			Network.DefinitionProvisionable,
			Network.UpdateBlank,
			Network {

		public NetworkImpl(VirtualNetworkSite site) {
			super(site.getName(), site);
		}


		/***********************************************************
		 * Getters
		 ***********************************************************/

		@Override
		public List<String> addressPrefixes() throws Exception {
			return Collections.unmodifiableList(this.inner().getAddressSpace().getAddressPrefixes());
		}
		
		@Override
		public String region() throws Exception {
			return this.inner().getLocation();
		}

		@Override
		public String affinityGroup() throws Exception {
			return this.inner().getAffinityGroup();
		}

		@Override
		public String label() throws Exception {
			return this.inner().getLabel();
		}
		
		@Override
		public String state() throws Exception {
			return this.inner().getState();
		}
		
		@Override
		public String id() throws Exception {
			return this.inner().getId();
		}
		
		@Override
		public Map<String, Subnet> subnets() {
			HashMap<String, Subnet> subnets = new HashMap<>();
			for(com.microsoft.windowsazure.management.network.models.NetworkListResponse.Subnet azureSubnet : this.inner().getSubnets()) {
				SubnetImpl subnet = new SubnetImpl(azureSubnet);
				subnets.put(azureSubnet.getName(), subnet);
			}
			return Collections.unmodifiableMap(subnets);
		}
		
		// Implementation of Subnet
		private class SubnetImpl 
			extends IndexableImpl
			implements Network.Subnet {
			
			private com.microsoft.windowsazure.management.network.models.NetworkListResponse.Subnet azureSubnet;

			public SubnetImpl(com.microsoft.windowsazure.management.network.models.NetworkListResponse.Subnet azureSubnet) {
				super(azureSubnet.getName());
				this.azureSubnet = azureSubnet;
			}

			@Override
			public String addressPrefix() {
				return this.azureSubnet.getAddressPrefix();
			}

			@Override
			public String networkSecurityGroup() {
				return this.azureSubnet.getNetworkSecurityGroup();
			}
		}


		/**************************************************************
		 * Setters (fluent interface)
		 **************************************************************/

		@Override
		public NetworkImpl withRegion(String region) {
			this.inner().setLocation(region);
			return this;
		}
		
		@Override
		public NetworkImpl withRegion(Region region) {
			return this.withRegion(region.name());
		}
		
		@Override
		public NetworkImpl withCidr(String cidr) {
			this.inner().getAddressSpace().getAddressPrefixes().add(cidr);
			return this;
		}
		
		private NetworkImpl withSubnet(String name, String cidr, String securityGroup) {
			com.microsoft.windowsazure.management.network.models.NetworkListResponse.Subnet azureSubnet = 
					new com.microsoft.windowsazure.management.network.models.NetworkListResponse.Subnet();
			azureSubnet.setAddressPrefix(cidr);
			azureSubnet.setName(name);
			azureSubnet.setNetworkSecurityGroup(securityGroup);
			this.inner().getSubnets().add(azureSubnet);
			return this;
		}
	

		@Override
		public NetworkImpl withSubnet(String name, String cidr) {
			return this.withSubnet(name,  cidr, null);
		}
		

		/************************************************************
		 * Verbs
		 ************************************************************/

		@Override
		public void delete() throws Exception {
			azure.networks().delete(this.id);
		}


		@Override
		public NetworkImpl provision() throws Exception {
			// If no subnets specified, create a default subnet containing the first CIDR of the network
			if(this.inner().getSubnets().size() == 0) {
				com.microsoft.windowsazure.management.network.models.NetworkListResponse.Subnet azureSubnet = 
						new com.microsoft.windowsazure.management.network.models.NetworkListResponse.Subnet();
				azureSubnet.setAddressPrefix(this.addressPrefixes().get(0));
				azureSubnet.setName("Subnet-1");
				this.inner().getSubnets().add(azureSubnet);
			}

			// Declare the subnets
			final String subnetTemplate = "<Subnet name=\"${subnetName}\"><AddressPrefix>${subnetCidr}</AddressPrefix></Subnet>";				
			StringBuilder subnetsSection = new StringBuilder();
			for(Subnet subnet : this.subnets().values()) {
				subnetsSection.append(subnetTemplate
					.replace("${subnetName}", subnet.name())
					.replace("${subnetCidr}", subnet.addressPrefix()));
				// TODO: securityGroup
			}
			
			// Address space XML template
			final String addressPrefixTemplate = "<AddressPrefix>${cidr}</AddressPrefix>";
			StringBuilder addressSpaceSection = new StringBuilder();
			for(String addressPrefix : this.addressPrefixes()) {
				addressSpaceSection.append(addressPrefixTemplate.replace("${cidr}", addressPrefix));
			}
				
			// Network site XML template
			final String networkTemplate = 
				"<VirtualNetworkSite name=\"${name}\" Location=\"${location}\">"
				+ "<AddressSpace>${addressSpace}</AddressSpace>"
				+ "<Subnets>${subnets}</Subnets>"
				+ "</VirtualNetworkSite>";
			
			// Create network site description based on the inputs and the template
			final String networkDescription = networkTemplate
				.replace("${name}", this.inner().getName())
				.replace("${location}", this.region())
				.replace("${addressSpace}", addressSpaceSection.toString())
				.replace("${subnets}", subnetsSection.toString());
			
			// Get current network configuration
			String networkConfig = azure.networkManagementClient().getNetworksOperations().getConfiguration().getConfiguration();
			
			// Correct for garbage prefix in XML returned by Azure
			networkConfig = networkConfig.substring(networkConfig.indexOf('<'));
			
			// XPath to the parent of virtual networks in network configuration XML
			final String parentXPath = "/*[local-name()='NetworkConfiguration']"
				+ "/*[local-name()='VirtualNetworkConfiguration']"
				+ "/*[local-name()='VirtualNetworkSites']";				

			// Add the new network to the configuration
			String newConfig = Utils.insertXMLElement(networkConfig, networkDescription, parentXPath);
			
			// Submit the new network config to Azure
			updateNetworkConfig(newConfig);
			
			return this;
		}


		@Override
		public NetworkImpl refresh() throws Exception {
			ArrayList<VirtualNetworkSite> azureSites = azure.networkManagementClient().getNetworksOperations().list().getVirtualNetworkSites();
			
			for(VirtualNetworkSite s : azureSites) {
				if(s.getName().equals(this.inner().getName())) {
					this.setInner(s);
					return this;
				}
			}
			
			throw new NoSuchElementException(String.format("Virtual network '%s' not found.", this.inner().getName()));
		}
	}
}

