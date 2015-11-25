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
package com.microsoft.azure.shortcuts.services.samples;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.microsoft.azure.shortcuts.services.Network;
import com.microsoft.azure.shortcuts.services.Network.Subnet;
import com.microsoft.azure.shortcuts.services.implementation.Azure;

//Tests Virtual Networks
public class Networks {
	public static void main(String[] args) {
		String publishSettingsPath = "my.publishsettings";
		String subscriptionId = "9657ab5d-4a4a-4fd2-ae7a-4cd9fbd030ef";

		try {
			// Instantiate Azure management class
			final Azure azure = Azure.authenticate(publishSettingsPath, subscriptionId);

			test(azure);
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}

	public static void test(Azure azure) throws Exception {
		String networkName;
		Network network;
		
		// Create a network with multiple subnets
		networkName = "net" + String.valueOf(System.currentTimeMillis());
		System.out.println(String.format("Creating virtual network named '%s'...", networkName));
		azure.networks().define(networkName)
			.withRegion("West US")
			.withCidr("10.0.0.0/28")
			.withSubnet("Foo", "10.0.0.0/29")
			.withSubnet("Bar", "10.0.0.8/29")
			.provision();

		// List the virtual networks
		List<String> virtualNetworkNames = azure.networks().names();
		System.out.println("Available virtual networks: " + StringUtils.join(virtualNetworkNames, ", "));

		// Get created virtual network
		network = azure.networks().get(networkName);
		printNetwork(network);

		// Delete the newly created virtual network
		System.out.println(String.format("Deleting virtual network named '%s'...", network.name()));
		azure.networks().delete(network.name());

		// Create network with default subnet
		networkName = "net" + String.valueOf(System.currentTimeMillis());
		System.out.println(String.format("Creating virtual network named '%s'...", networkName));
		azure.networks().define(networkName)
			.withRegion("West US")
			.withCidr("10.0.0.0/29")
			.provision();

		// List the virtual networks
		virtualNetworkNames = azure.networks().names();
		System.out.println("Available virtual networks: " + StringUtils.join(virtualNetworkNames, ", "));

		// Get created virtual network
		network = azure.networks().get(networkName);
		printNetwork(network);

		// Delete the newly created virtual network
		System.out.println(String.format("Deleting virtual network named '%s'...", network.name()));
		azure.networks().delete(network.name());
	}
	
	
	private static void printNetwork(Network network) throws Exception {
		StringBuilder subnets = new StringBuilder();
		for(Subnet subnet : network.subnets().values()) {
			subnets
				.append("\tSubnet: ").append(subnet.name())
				.append("\n\t\tCIDR: ").append(subnet.addressPrefix())
				.append("\n\t\tNetwork security group: ").append(subnet.networkSecurityGroup())
				.append("\n");
			
		}
		
		System.out.println(String.format("Network: %s\n"
				+ "\tRegion: %s\n"
				+ "\tCIDR: %s\n"
				+ "\tAffinity group: %s\n"
				+ "\tState: %s"
				+ "\tID: %s"
				+ "%s",
				network.name(),
				network.region(),
				network.addressPrefixes(),
				network.affinityGroup(),
				network.state(),
				network.id(),
				subnets
				));
	}
}
