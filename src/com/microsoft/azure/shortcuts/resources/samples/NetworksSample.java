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

package com.microsoft.azure.shortcuts.resources.samples;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.microsoft.azure.shortcuts.resources.Network;
import com.microsoft.azure.shortcuts.resources.Network.Subnet;
import com.microsoft.azure.shortcuts.resources.implementation.Azure;

// Tests resources
public class NetworksSample {
    public static void main(String[] args) {
        try {
            Azure azure = Azure.authenticate("my.azureauth", null);
            test(azure);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    

    public static void test(Azure azure) throws Exception {
    	// Listing all networks
    	Map<String, Network> networks = azure.networks().list();
    	System.out.println(String.format("Network ids: \n\t%s", StringUtils.join(networks.keySet(), ",\n\t")));
    	
    	// Listing networks in a specific resource group
    	String groupName = "group1444089227523";
    	networks = azure.networks().list(groupName);
    	System.out.println(String.format("Network ids in group '%s': \n\t%s", groupName, StringUtils.join(networks.keySet(), ",\n\t")));
    	
    	// Get info about a specific network using its resource ID
    	Network network = azure.networks().get("/subscriptions/9657ab5d-4a4a-4fd2-ae7a-4cd9fbd030ef/resourceGroups/javasampleresourcegroup1/providers/Microsoft.Network/virtualNetworks/javasampleresourcegroup1vnetqwsks");
    	printNetwork(network);
	}
    
    
    private static void printNetwork(Network network) throws Exception {
    	StringBuilder output = new StringBuilder();
    	output
    		.append(String.format("Network name: %s\n", network.name()))
    		.append(String.format("Provisioning state: %s\n", network.provisioningState()))
    		.append(String.format("Address prefixes: %s\n", StringUtils.join(network.addressPrefixes(), ", ")))
    		.append(String.format("DNS servers: %s\n", StringUtils.join(network.dnsServers(), ", ")));
    	
    	for(Subnet subnet : network.subnets().values()) {
    		output
    			.append(String.format("Subnet: %s\n", subnet.name()))
    			.append(String.format("\tAddress prefix: %s\n", subnet.addressPrefix()))
    			.append(String.format("\tNetwork security group: %s\n", subnet.networkSecurityGroup()));
    	}
    	
    	System.out.println(output.toString());
    }
}
