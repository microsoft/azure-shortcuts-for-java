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
import java.util.List;

import com.microsoft.azure.shortcuts.common.Utils;
import com.microsoft.azure.shortcuts.common.implementation.NamedImpl;
import com.microsoft.azure.shortcuts.common.implementation.NamedRefreshableImpl;
import com.microsoft.azure.shortcuts.services.creation.NetworkDefinitionBlank;
import com.microsoft.azure.shortcuts.services.creation.NetworkDefinitionProvisionable;
import com.microsoft.azure.shortcuts.services.creation.NetworkDefinitionWithCidr;
import com.microsoft.azure.shortcuts.services.listing.Networks;
import com.microsoft.azure.shortcuts.services.reading.Network;
import com.microsoft.windowsazure.management.network.models.NetworkSetConfigurationParameters;
import com.microsoft.windowsazure.management.network.models.NetworkListResponse.VirtualNetworkSite;

// Class encapsulating the API related to virtual networks
public class NetworksImpl 
	extends EntitiesImpl
	implements Networks {
	
	NetworksImpl(Azure azure) {
		super(azure);
	}

	@Override
	// Returns information about existing network
	public NetworkImpl get(String name) throws Exception {
		NetworkImpl network  = new NetworkImpl(name, false);
		return network.refresh();
	}
	
	@Override
	// Starts a new network definition
	public NetworkDefinitionBlank define(String name) {
		return new NetworkImpl(name, true);
	}
	
	
	// Requests a network configuration update based on the XML netconfig representation
	private void updateNetworkConfig(String xml) throws Exception {
		NetworkSetConfigurationParameters params = new NetworkSetConfigurationParameters();
		params.setConfiguration(xml);
		azure.networkManagementClient().getNetworksOperations().setConfiguration(params);
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
	public List<String> list() {
		try {
			final ArrayList<VirtualNetworkSite> items = azure.networkManagementClient().getNetworksOperations()
					.list().getVirtualNetworkSites();
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
	
	
	// Encapsulates the required and optional parameters for a network
	private class NetworkImpl 
		extends NamedRefreshableImpl<Network>
		implements 
			NetworkDefinitionBlank, 
			NetworkDefinitionWithCidr, 
			NetworkDefinitionProvisionable,
			Network {

		private String region, cidr, affinityGroup, label;
		private ArrayList<NetworkImpl.SubnetImpl> subnets = new ArrayList<NetworkImpl.SubnetImpl>();
		
		private NetworkImpl(String name, boolean initialized) {
			super(name, initialized);
		}
		
		
		/***********************************************************
		 * Getters
		 * @throws Exception 
		 ***********************************************************/

		public String cidr() throws Exception {
			ensureInitialized();
			return this.cidr;
		}
		
		public String region() throws Exception {
			ensureInitialized();
			return this.region;
		}

		public String affinityGroup() throws Exception {
			ensureInitialized(); 
			return this.affinityGroup;
		}

		public String label() throws Exception {
			ensureInitialized();
			return this.label;
		}

		@Override
		public Subnet[] subnets() {
			return subnets.toArray(new Subnet[0]);
		}
		
		// Implementation of Subnet
		private class SubnetImpl 
			extends NamedImpl<Subnet>
			implements Network.Subnet {
			
			private String cidr;

			private SubnetImpl(String name) {
				super(name);
			}
			
			@Override
			public String cidr() {
				return this.cidr;
			}
		}


		/**************************************************************
		 * Setters (fluent interface)
		 **************************************************************/

		@Override
		public NetworkImpl withRegion(String region) {
			this.region = region;
			return this;
		}
		
		@Override
		public NetworkImpl withCidr(String cidr) {
			this.cidr = cidr;
			return this;
		}
		
		@Override
		public NetworkDefinitionProvisionable withSubnet(String name, String cidr) {
			SubnetImpl subnet = new SubnetImpl(name);
			subnet.cidr = cidr;
			this.subnets.add(subnet);
			return this;
		}
	

		/************************************************************
		 * Verbs
		 ************************************************************/

		@Override
		public void delete() throws Exception {
			azure.networks().delete(this.name);
		}


		@Override
		public NetworkImpl provision() throws Exception {
			// If no subnets specified, create a default subnet containing the whole network
			if(this.subnets.size() == 0) {
				SubnetImpl subnet = new SubnetImpl("Subnet-1");
				subnet.cidr = this.cidr;
				this.subnets.add(subnet);
			}

			// Declare the subnets
			final String subnetTemplate = "<Subnet name=\"${subnetName}\"><AddressPrefix>${subnetCidr}</AddressPrefix></Subnet>";				
			StringBuilder subnetsSection = new StringBuilder();
			for(SubnetImpl subnet : this.subnets) {
				subnetsSection.append(subnetTemplate
					.replace("${subnetName}", subnet.name())
					.replace("${subnetCidr}", subnet.cidr()));
			}
			
			// Network site XML template
			final String networkTemplate = 
				"<VirtualNetworkSite name=\"${name}\" Location=\"${location}\">"
				+ "<AddressSpace><AddressPrefix>${cidr}</AddressPrefix></AddressSpace>"
				+ "<Subnets>${subnets}</Subnets>"
				+ "</VirtualNetworkSite>";
			
			// Create network site description based on the inputs and the template
			final String networkDescription = networkTemplate
				.replace("${name}", this.name)
				.replace("${location}", this.region)
				.replace("${cidr}", this.cidr)
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
			String networkConfig = azure.networkManagementClient().getNetworksOperations().getConfiguration().getConfiguration();

			// Correct for garbage prefix in XML returned by Azure
			networkConfig = networkConfig.substring(networkConfig.indexOf('<'));

			final String siteXpath = "/*[local-name()='NetworkConfiguration']"
					+ "/*[local-name()='VirtualNetworkConfiguration']"
					+ "/*[local-name()='VirtualNetworkSites']"
					+ "/*[local-name()='VirtualNetworkSite' and @name='" + name + "']";

			final String cidrXpath = siteXpath + "/*[local-name()='AddressSpace']/*[local-name()='AddressPrefix']";
			this.cidr = Utils.findXMLNode(networkConfig, cidrXpath).getTextContent();

			// Determine affinity group
			for(VirtualNetworkSite site : azure.networkManagementClient().getNetworksOperations().list().getVirtualNetworkSites()) {
				if(site.getName().equalsIgnoreCase(name)) {
					this.affinityGroup = site.getAffinityGroup();
					this.label = site.getLabel();
					this.region = site.getLocation();
					// Read subnets
					for(com.microsoft.windowsazure.management.network.models.NetworkListResponse.Subnet s : site.getSubnets()) {
						// TODO: Other subnet properties
						
						NetworkImpl.SubnetImpl subnet = this.new SubnetImpl(s.getName());
						this.subnets.add(subnet);
						subnet.cidr = s.getAddressPrefix();
					}
					// TODO Other data
					break;
				}
			}
			
			this.initialized = true;
			return this;
		}
	}
}

