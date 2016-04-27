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

import com.microsoft.azure.management.network.models.NetworkInterfaceIpConfiguration;
import com.microsoft.azure.shortcuts.resources.Network;
import com.microsoft.azure.shortcuts.resources.NetworkInterface;
import com.microsoft.azure.shortcuts.resources.NetworkSecurityGroup;
import com.microsoft.azure.shortcuts.resources.Protocol;
import com.microsoft.azure.shortcuts.resources.PublicIpAddress;
import com.microsoft.azure.shortcuts.resources.Region;
import com.microsoft.azure.shortcuts.resources.implementation.Subscription;

public class NetworkInterfacesSample {
    public static void main(String[] args) {
        try {
            Subscription subscription = Subscription.authenticate("my.azureauth", null);
            test(subscription);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    

    public static void test(Subscription subscription) throws Exception {
    	String suffix = String.valueOf(System.currentTimeMillis());
    	String newNetworkInterfaceName = "nic" + suffix;
    	String newNetworkName = "vnet" + suffix;
    	String newGroupName = "rg" + suffix;
    	String newNSGName = "nsg" + suffix;
    	
    	// Listing all network interfaces
    	Map<String, NetworkInterface> nics = subscription.networkInterfaces().asMap();
    	System.out.println("Network interfaces:");
    	for(NetworkInterface nic : nics.values()) {
    		printNetworkInterface(nic);
    	}
    	
    	// Create a new network interface in a new default resource group
    	NetworkInterface nicMinimal = subscription.networkInterfaces().define(newNetworkInterfaceName)
    		.withRegion(Region.US_WEST)
    		.withNewResourceGroup(newGroupName)
    		.withNewNetwork("10.0.0.0/28")
    		.withPrivateIpAddressDynamic()
    		.withoutPublicIpAddress()
    		.provision();
    	
    	// Get info about a specific network interface using its group and name
    	nicMinimal = subscription.networkInterfaces(nicMinimal.id());
    	nicMinimal = subscription.networkInterfaces().get(nicMinimal.id());
    	printNetworkInterface(nicMinimal);

    	// Listing network interfaces in a specific resource group
    	nics = subscription.networkInterfaces().asMap(newGroupName);
    	System.out.println(String.format("Network interface ids in group '%s': \n\t%s", newGroupName, StringUtils.join(nics.keySet(), ",\n\t")));

    	// Create a virtual network to test the network interface with
    	Network network = subscription.networks().define(newNetworkName)
    		.withRegion(Region.US_WEST)
    		.withExistingResourceGroup(newGroupName)
    		.withAddressSpace("10.0.0.0/28")
    		.withSubnet("subnet1", "10.0.0.0/29") 
    		.withSubnet("subnet2", "10.0.0.8/29")
    		.provision();
    	
    	// Create a NSG to test the NIC with
    	NetworkSecurityGroup nsg = subscription.networkSecurityGroups().define(newNSGName)
    		.withRegion(Region.US_WEST)
    		.withExistingResourceGroup(newGroupName)
    		.defineRule("httpIn")
    			.allowOutbound()
    			.fromAnyAddress()
    			.fromPort(80)
    			.toAnyAddress()
    			.toAnyPort()
    			.withProtocol(Protocol.TCP)
    			.attach()
    		.provision();
    	
    	// More detailed NIC definition
    	NetworkInterface nic = subscription.networkInterfaces().define(newNetworkInterfaceName + "2")
    		.withRegion(Region.US_WEST)
    		.withExistingResourceGroup(newGroupName)
    		.withExistingNetwork(network)
    		.withSubnet("subnet1")
    		.withPrivateIpAddressStatic("10.0.0.5")
    		.withNewPublicIpAddress()
    		.withExistingNetworkSecurityGroup(nsg)
    		.withTag("hello", "world")
    		.provision();
    		
    	// Get info about a specific NIC using its resource ID
    	nic = subscription.networkInterfaces(nic.resourceGroup(), nic.name());
    	printNetworkInterface(nic);
    	
    	// Delete the NIC
    	nicMinimal.delete();
    	nic.delete();
    	
    	// Delete the virtual network
    	network.delete();
    	
    	// Delete the auto-created group
    	subscription.resourceGroups(newGroupName).delete();
    }
    
    
    private static void printNetworkInterface(NetworkInterface nic) throws Exception {
    	StringBuilder output = new StringBuilder();
    	NetworkInterfaceIpConfiguration ipConfig = nic.inner().getIpConfigurations().get(0);
    	Map<String, PublicIpAddress> pips = nic.publicIpAddresses();
    	output
    		.append(String.format("Network interface ID: %s\n", nic.id()))
    		.append(String.format("\tName: %s\n", nic.name()))
    		.append(String.format("\tGroup: %s\n", nic.resourceGroup()))
    		.append(String.format("\tRegion: %s\n", nic.region()))
    		.append(String.format("\tPrimary subnet ID: %s\n", ipConfig.getSubnet().getId()))
    		.append(String.format("\tPrimary private IP: %s\n", ipConfig.getPrivateIpAddress()))
    		.append(String.format("\tPublic IPs:\n"));
    	for(PublicIpAddress pip : pips.values()) {
    		output
    			.append(String.format("\t\tName:%s\n", pip.name()))
    			.append(String.format("\t\tLeaf domain label:%s\n", pip.leafDomainLabel()))
    			.append(String.format("\t\tIP address:%s\n", pip.ipAddress()))
    			;
    	} 
    	
    	System.out.println(output.toString());
    }
}
