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

import com.microsoft.azure.shortcuts.resources.NetworkSecurityGroup;
import com.microsoft.azure.shortcuts.resources.Region;
import com.microsoft.azure.shortcuts.resources.implementation.Azure;

public class NetworkSecurityGroupsSample {
    public static void main(String[] args) {
        try {
            Azure azure = Azure.authenticate("my.azureauth", null);
            test(azure);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    

    public static void test(Azure azure) throws Exception {
    	String newNetworkSecurityGroupName = "testNSG";
    	
    	// Listing all network security groups
    	Map<String, NetworkSecurityGroup> nsgs = azure.networkSecurityGroups().list();
    	System.out.println("Network security groups:");
    	for(NetworkSecurityGroup nsg : nsgs.values()) {
    		printNSG(nsg);
    	}
    	
    	// Create a NSG in a default new group
    	NetworkSecurityGroup nsgMinimal = azure.networkSecurityGroups().define(newNetworkSecurityGroupName)
    		.withRegion(Region.US_WEST)
    		.withNewGroup()
    		.provision();
    	
    	// Get info about a specific NSG using its group and name
    	nsgMinimal = azure.networkSecurityGroups(nsgMinimal.id());
    	nsgMinimal = azure.networkSecurityGroups().get(nsgMinimal.id());
    	String groupNameCreated = nsgMinimal.group(); 
    	printNSG(nsgMinimal);

    	// More detailed NSG definition
    	NetworkSecurityGroup nsg = azure.networkSecurityGroups().define(newNetworkSecurityGroupName + "2")
    		.withRegion(Region.US_WEST)
    		.withExistingGroup(groupNameCreated)
    		.withTag("hello", "world")
    		.provision();
    	    	
    	// Listing NSGs in a specific resource group
    	nsgs = azure.networkSecurityGroups().list(groupNameCreated);
    	System.out.println(String.format("NSG ids in group '%s': \n\t%s", groupNameCreated, StringUtils.join(nsgs.keySet(), ",\n\t")));
    	
    	// Get info about a specific PIP using its resource ID
    	nsg = azure.networkSecurityGroups(nsg.group(), nsg.name());
    	printNSG(nsg);
    	
    	// Delete the NSG
    	nsgMinimal.delete();
    	nsg.delete();
    	
    	// Delete the auto-created group
    	azure.resourceGroups(groupNameCreated).delete();
    }
    
    
    private static void printNSG(NetworkSecurityGroup nsg) throws Exception {
    	StringBuilder output = new StringBuilder();
    	output
    		.append(String.format("Network Security Group ID: %s\n", nsg.id()))
    		.append(String.format("\tName: %s\n", nsg.name()))
    		.append(String.format("\tGroup: %s\n", nsg.group()))
    		.append(String.format("\tRegion: %s\n", nsg.region()))
    		;
    	
    	System.out.println(output.toString());
    }
}
