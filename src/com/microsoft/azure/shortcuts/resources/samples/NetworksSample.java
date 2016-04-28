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

import com.microsoft.azure.shortcuts.resources.ResourceGroup;
import com.microsoft.azure.shortcuts.resources.Subnet;
import com.microsoft.azure.shortcuts.resources.Network;
import com.microsoft.azure.shortcuts.resources.Region;
import com.microsoft.azure.shortcuts.resources.implementation.Subscription;

// Tests resources
public class NetworksSample {
    public static void main(String[] args) {
        try {
            Subscription subscription = Subscription.authenticate("my.azureauth", null);
            test(subscription);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void test(Subscription subscription) throws Exception {
    	testProvisionMinimal(subscription);
    	testProvisionWithSubnets(subscription);
    	testProvisionWithNSG(subscription);
    }
    
    
    private static void testProvisionWithNSG(Subscription subscription) throws Exception {
    	String suffix = String.valueOf(System.currentTimeMillis());
    	String newNetworkName = "net" + suffix;
    	String existingNsgName = "nsg" + suffix;
    	String existingGroupName = "rg" + suffix;
    	
    	// Create an NSG
    	subscription.networkSecurityGroups().define(existingNsgName)
    		.withRegion(Region.US_WEST)
    		.withNewResourceGroup(existingGroupName)
    		.provision();
    			
    	// Create a network with 2 subnets created the child-resource way, assigning an NSG
    	Network network = subscription.networks().define(newNetworkName)
    		.withRegion(Region.US_WEST)
    		.withExistingResourceGroup(existingGroupName)
    		.withAddressSpace("10.0.0.0/28")
    		.defineSubnet("subnetA")
    			.withAddressPrefix("10.0.0.0/29")
    			.withExistingNetworkSecurityGroup(existingNsgName)
    			.attach()
    		.defineSubnet("subnetB")
    			.withAddressPrefix("10.0.0.8/29")
    			.attach()
    		.withTag("hello", "world")
    		.provision();
    	
    	printNetwork(network);
    	
    	// Delete the group
    	subscription.resourceGroups().delete(existingGroupName);
    	
    }
    
	// Create a new network with a default subnet in a new default resource group
    private static void testProvisionMinimal(Subscription subscription) throws Exception {
    	String suffix = String.valueOf(System.currentTimeMillis());
    	String newNetworkName = "net" + suffix;
    	
    	Network network = subscription.networks().define(newNetworkName)
    		.withRegion(Region.US_WEST)
    		.withNewResourceGroup()
    		.withAddressSpace("10.0.0.0/28")
    		.provision();
    	
    	// Get info about a specific network using its id
    	network = subscription.networks(network.id());
    	printNetwork(network);

    	// Listing all networks
    	Map<String, Network> networks = subscription.networks().asMap();
    	System.out.println(String.format("Network ids: \n\t%s", StringUtils.join(networks.keySet(), ",\n\t")));
    	
    	// Clean up
    	String groupName = network.resourceGroup();
    	subscription.networks().delete(network.id());
    	subscription.resourceGroups().delete(groupName);
    }

    
	// Create a new network with two subnets, in an existing resource group
    private static void testProvisionWithSubnets(Subscription subscription) throws Exception {
    	String suffix = String.valueOf(System.currentTimeMillis());
    	String existingGroupName = "rg" + suffix;
    	String newNetworkName = "net" + suffix;
    	
    	// Create a test group
    	ResourceGroup group = subscription.resourceGroups().define(existingGroupName)
    		.withRegion(Region.US_WEST)
    		.provision();
    	
    	Network network = subscription.networks().define(newNetworkName)
    		.withRegion(Region.US_WEST)
    		.withExistingResourceGroup(existingGroupName)
    		.withAddressSpace("10.0.0.0/28")
    		.withSubnet("Foo", "10.0.0.0/29")
    		.withSubnet("Bar", "10.0.0.8/29")
    		.provision();
    	
    	printNetwork(network);

    	// Listing networks in a specific resource group
    	Map<String, Network> networks = subscription.networks().asMap(existingGroupName);
    	System.out.println(String.format("Network ids in group '%s': \n\t%s", existingGroupName, StringUtils.join(networks.keySet(), ",\n\t")));
    	
    	// Clean up
    	network.delete();
    	group.delete();
    }
    
    
    private static void printNetwork(Network network) throws Exception {
    	StringBuilder output = new StringBuilder();
    	output
    		.append(String.format("Network ID: %s\n", network.id()))
    		.append(String.format("\tName: %s\n", network.name()))
    		.append(String.format("\tGroup: %s\n", network.resourceGroup()))
    		.append(String.format("\tProvisioning state: %s\n", network.provisioningState()))
    		.append(String.format("\tAddress spaces: %s\n", StringUtils.join(network.addressSpaces(), ", ")))
    		.append(String.format("\tDNS servers: %s\n", StringUtils.join(network.dnsServerIPs(), ", ")));
    	
    	for(Subnet subnet : network.subnets().values()) {
    		output
    			.append(String.format("\tSubnet: %s\n", subnet.id()))
    			.append(String.format("\t\tAddress prefix: %s\n", subnet.addressPrefix()))
    			.append(String.format("\t\tNetwork security group: %s\n", subnet.networkSecurityGroup()));
    	}
    	
    	System.out.println(output.toString());
    }
}
