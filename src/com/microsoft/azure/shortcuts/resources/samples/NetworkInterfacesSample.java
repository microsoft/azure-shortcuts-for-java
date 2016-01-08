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
import com.microsoft.azure.shortcuts.resources.NetworkInterface;
import com.microsoft.azure.shortcuts.resources.Region;
import com.microsoft.azure.shortcuts.resources.implementation.Azure;

public class NetworkInterfacesSample {
    public static void main(String[] args) {
        try {
            Azure azure = Azure.authenticate("my.azureauth", null);
            test(azure);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    

    public static void test(Azure azure) throws Exception {
    	String existingGroupName = "javasampleresourcegroup1";
    	String newNetworkInterfaceName = "testnic";
    	String newNetworkName = "myvnet";
    	
    	// Listing all network interfaces
    	Map<String, NetworkInterface> nics = azure.networkInterfaces().list();
    	System.out.println("Network interfaces:");
    	for(NetworkInterface nic : nics.values()) {
    		printNetworkInterface(nic);
    	}
    	
    	// Create a virtual network to test the network interface with
    	Network network = azure.networks().define(newNetworkName)
    		.withRegion(Region.US_WEST)
    		.withAddressSpace("10.0.0.0/28")
    		.withSubnet("subnet1", "10.0.0.0/29") 
    		.withSubnet("subnet2", "10.0.0.8/29")
    		.withGroupExisting(existingGroupName)
    		.provision();
    	
    	// Create a new network interface in a new default resource group
    	NetworkInterface nicMinimal = azure.networkInterfaces().define(newNetworkInterfaceName)
    		.withRegion(Region.US_WEST)
    		.withSubnetPrimary(network.subnets().get("subnet1"))
    		.provision();
    	
    	// Get info about a specific network interface using its group and name
    	nicMinimal = azure.networkInterfaces(nicMinimal.id());
    	nicMinimal = azure.networkInterfaces().get(nicMinimal.id());
    	String groupNameCreated = nicMinimal.group(); 
    	printNetworkInterface(nicMinimal);

    	// Listing network interfaces in a specific resource group
    	nics = azure.networkInterfaces().list(existingGroupName);
    	System.out.println(String.format("Network interface ids in group '%s': \n\t%s", existingGroupName, StringUtils.join(nics.keySet(), ",\n\t")));
    	
    	// More detailed NIC definition
    	NetworkInterface nic = azure.networkInterfaces().define(newNetworkInterfaceName + "2")
    		.withRegion(Region.US_WEST)
    		.withSubnetPrimary(network.subnets().get("subnet1"))
    		.withGroupExisting(existingGroupName)
    		.withTag("hello", "world")
    		.provision();
    		
    	// Get info about a specific NIC using its resource ID
    	nic = azure.networkInterfaces(nic.group(), nic.name());
    	printNetworkInterface(nic);
    	
    	// Delete the NIC
    	nicMinimal.delete();
    	nic.delete();
    	
    	// Delete the virtual network
    	network.delete();
    	
    	// Delete the auto-created group
    	azure.groups(groupNameCreated).delete();
    }
    
    
    private static void printNetworkInterface(NetworkInterface nic) throws Exception {
    	StringBuilder output = new StringBuilder();
    	output
    		.append(String.format("Network interface ID: %s\n", nic.id()))
    		.append(String.format("\tName: %s\n", nic.name()))
    		.append(String.format("\tGroup: %s\n", nic.group()));
    	
    	System.out.println(output.toString());
    }
}
