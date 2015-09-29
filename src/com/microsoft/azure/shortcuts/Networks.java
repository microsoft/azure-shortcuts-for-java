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
package com.microsoft.azure.shortcuts;

import java.util.ArrayList;

import org.apache.commons.lang3.NotImplementedException;

import com.microsoft.azure.shortcuts.creation.NetworkDefinitionBlank;
import com.microsoft.azure.shortcuts.creation.NetworkDefinitionProvisionable;
import com.microsoft.azure.shortcuts.creation.NetworkDefinitionWithCidr;
import com.microsoft.azure.shortcuts.implementation.NamedImpl;
import com.microsoft.azure.shortcuts.implementation.SupportsCreating;
import com.microsoft.azure.shortcuts.implementation.SupportsDeleting;
import com.microsoft.azure.shortcuts.implementation.SupportsListing;
import com.microsoft.azure.shortcuts.reading.Network;
import com.microsoft.azure.shortcuts.updating.NetworkUpdatable;
import com.microsoft.windowsazure.management.network.models.NetworkSetConfigurationParameters;
import com.microsoft.windowsazure.management.network.models.NetworkListResponse.VirtualNetworkSite;

// Class encapsulating the API related to virtual networks
public class Networks implements 
	SupportsCreating<NetworkDefinitionBlank>,
	SupportsDeleting,
	SupportsListing {
	
	final Azure azure;
	
	Networks(Azure azure) {
		this.azure = azure;
	}

	// Returns information about existing network
	public Network get(String name) throws Exception {
		String networkConfig = azure.networking.getNetworksOperations().getConfiguration().getConfiguration();

		// Correct for garbage prefix in XML returned by Azure
		networkConfig = networkConfig.substring(networkConfig.indexOf('<'));

		NetworkImpl network  = new NetworkImpl(name);
		final String siteXpath = "/*[local-name()='NetworkConfiguration']"
				+ "/*[local-name()='VirtualNetworkConfiguration']"
				+ "/*[local-name()='VirtualNetworkSites']"
				+ "/*[local-name()='VirtualNetworkSite' and @name='" + name + "']";

		final String cidrXpath = siteXpath + "/*[local-name()='AddressSpace']/*[local-name()='AddressPrefix']";
		network.cidr = Utils.findXMLNode(networkConfig, cidrXpath).getTextContent();

		// Determine affinity group
		for(VirtualNetworkSite site : azure.networking.getNetworksOperations().list().getVirtualNetworkSites()) {
			if(site.getName().equalsIgnoreCase(name)) {
				network.affinityGroup = site.getAffinityGroup();
				network.label = site.getLabel();
				network.region = site.getLocation();
				// Read subnets
				for(com.microsoft.windowsazure.management.network.models.NetworkListResponse.Subnet s : site.getSubnets()) {
					// TODO: Other subnet properties
					
					NetworkImpl.SubnetImpl subnet = network.new SubnetImpl(s.getName());
					network.subnets.add(subnet);
					subnet.cidr = s.getAddressPrefix();
				}
				// TODO Other data
				break;
			}
		}
		
		return network;
	}
	
	// Starts a new network definition
	public NetworkDefinitionBlank define(String name) {
		return new NetworkImpl(name);
	}
	
	
	// Requests a network configuration update based on the XML netconfig representation
	private void updateNetworkConfig(String xml) throws Exception {
		NetworkSetConfigurationParameters params = new NetworkSetConfigurationParameters();
		params.setConfiguration(xml);
		azure.networking.getNetworksOperations().setConfiguration(params);
	}
	
	
	// Deletes the specified network
	public void delete(String name) throws Exception {
		//  XPath to the network XML to delete
		final String xpath = String.format(
				"/*[local-name()='NetworkConfiguration']"
				+ "/*[local-name()='VirtualNetworkConfiguration']"
				+ "/*[local-name()='VirtualNetworkSites']"
				+ "/*[local-name()='VirtualNetworkSite' and @name='%s']", name);
		
		// Get current network configuration
		String networkConfig = azure.networking.getNetworksOperations().getConfiguration().getConfiguration();
		
		// Correct for garbage prefix in XML returned by Azure
		networkConfig = networkConfig.substring(networkConfig.indexOf('<'));

		// Delete the network from the config using the xpath
		final String newConfig = Utils.deleteXMLElement(networkConfig, xpath);
		
		// Update the network configuration
		updateNetworkConfig(newConfig);
	}
	
	
	// Lists existing virtual networks
	public String[] list() {
		try {
			final ArrayList<VirtualNetworkSite> networks = azure.networking.getNetworksOperations()
					.list().getVirtualNetworkSites();
			String[] names = new String[networks.size()];
			int i=0;
			for(VirtualNetworkSite network : networks) {
				names[i++] = network.getName();
			}
			return names;
		} catch (Exception e) {
			// Not very actionable, so just return an empty array
			return new String[0];
		}	
	}
	
	
	// Encapsulates the required and optional parameters for a network
	private class NetworkImpl 
		extends NamedImpl 
		implements 
			NetworkDefinitionBlank, 
			NetworkDefinitionWithCidr, 
			NetworkDefinitionProvisionable,
			NetworkUpdatable,
			Network {

		private String region, cidr, affinityGroup, label;
		private ArrayList<NetworkImpl.SubnetImpl> subnets = new ArrayList<NetworkImpl.SubnetImpl>();
		
		private NetworkImpl(String name) {
			super(name);
		}
					
		public NetworkImpl apply() throws Exception {
			// TODO Auto-generated method stub
			throw new NotImplementedException("Not yet implemented.");
		}
		
		public void delete() throws Exception {
			azure.networks.delete(this.name);
		}

		public NetworkImpl withRegion(String region) {
			this.region = region;
			return this;
		}
		
		public NetworkImpl withCidr(String cidr) {
			this.cidr = cidr;
			return this;
		}
		
		
		// Provisions a new network
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
			String networkConfig = azure.networking.getNetworksOperations().getConfiguration().getConfiguration();
			
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
		
		public String cidr() {
			return this.cidr;
		}
		
		public String region() {
			return this.region;
		}

		public String affinityGroup() {
			return this.affinityGroup;
		}

		public String label() {
			return this.label;
		}

		@Override
		public Subnet[] subnets() {
			return subnets.toArray(new Subnet[0]);
		}
		
		// Implementation of Subnet
		private class SubnetImpl 
			extends NamedImpl 
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

		@Override
		public NetworkDefinitionProvisionable withSubnet(String name, String cidr) {
			SubnetImpl subnet = new SubnetImpl(name);
			subnet.cidr = cidr;
			this.subnets.add(subnet);
			return this;
		}
	}
}

