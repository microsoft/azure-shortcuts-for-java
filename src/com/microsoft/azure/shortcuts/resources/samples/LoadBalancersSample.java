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

import com.microsoft.azure.shortcuts.resources.LoadBalancer;
import com.microsoft.azure.shortcuts.resources.Region;
import com.microsoft.azure.shortcuts.resources.implementation.Azure;

public class LoadBalancersSample {
    public static void main(String[] args) {
        try {
            Azure azure = Azure.authenticate("my.azureauth", null);
            test(azure);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    

    public static void test(Azure azure) throws Exception {
    	LoadBalancer lb;
    	String groupName = "lbtestgroup";
    	String lbName = "marcinslb";
    	
    	// Create a new LB in a new resource group
    	lb = azure.loadBalancers().define(lbName)
    		.withRegion(Region.US_WEST)
    		.withNewResourceGroup(groupName)
    		.withNewPublicIpAddress("marcinstest2")
    		.provision();
    	
    	// Get info about a specific lb using its group and name
    	lb = azure.loadBalancers(lb.id());
    	printLB(lb);

    	// Listing all lbs
    	Map<String, LoadBalancer> lbs = azure.loadBalancers().list();
    	System.out.println(String.format("Load Balancer ids: \n\t%s", StringUtils.join(lbs.keySet(), ",\n\t")));
    	
    	// Listing lbs in a specific resource group
    	lbs = azure.loadBalancers().list(groupName);
    	System.out.println(String.format("Load balancer ids in group '%s': \n\t%s", groupName, StringUtils.join(lbs.keySet(), ",\n\t")));
    	
    	// Get info about a specific lb using its resource ID
    	lb = azure.loadBalancers(lb.resourceGroup(), lb.name());
    	printLB(lb);
    	
    	// Delete the load balancer
    	lb.delete();
    	
    	// Create a new lb in an existing resource group
    	lb = azure.loadBalancers().define(lbName + "2")
    		.withRegion(Region.US_WEST)
    		.withExistingResourceGroup(groupName)
    		.withNewPublicIpAddress("marcinstest3")
    		.provision();
    	
    	printLB(lb);

    	// Delete the lb
    	lb.delete();
    	
    	// Delete the group
    	azure.resourceGroups(groupName).delete();
    }
    
    
    private static void printLB(LoadBalancer lb) throws Exception {
    	StringBuilder output = new StringBuilder();
    	output
    		.append(String.format("Load Balancer ID: %s\n", lb.id()))
    		.append(String.format("\tName: %s\n", lb.name()))
    		.append(String.format("\tGroup: %s\n", lb.resourceGroup()))
    		;
    	
    	System.out.println(output.toString());
    }
}
