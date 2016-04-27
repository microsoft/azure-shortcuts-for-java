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

import com.microsoft.azure.shortcuts.resources.AvailabilitySet;
import com.microsoft.azure.shortcuts.resources.ResourceGroup;
import com.microsoft.azure.shortcuts.resources.Region;
import com.microsoft.azure.shortcuts.resources.implementation.Subscription;

public class AvailabilitySetSample {
    public static void main(String[] args) {
        try {
            Subscription subscription = Subscription.authenticate("my.azureauth", null);
            test(subscription);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    

    public static void test(Subscription subscription) throws Exception {
    	String newAvailabilitySetName = "marcinsas";
    	AvailabilitySet availabilitySet;
    	
    	// Create a new availability set in a new default group
    	availabilitySet = subscription.availabilitySets().define(newAvailabilitySetName)
    		.withRegion(Region.US_WEST)
    		.withNewResourceGroup()
    		.provision();
    	
    	// Get info about a specific availability set using its group and name
    	availabilitySet = subscription.availabilitySets(availabilitySet.id());
    	printAvailabilitySet(availabilitySet);

    	// Listing availability sets in a specific resource group
    	Map<String, AvailabilitySet> availabilitySets = subscription.availabilitySets().asMap(availabilitySet.resourceGroup());
    	System.out.println(String.format("Availability set ids in group '%s': \n\t%s", 
    			availabilitySet.resourceGroup(), StringUtils.join(availabilitySets.keySet(), ",\n\t")));
    		
    	// Delete availability set
    	availabilitySet.delete();
    	
    	// Delete its group
    	subscription.resourceGroups().delete(availabilitySet.resourceGroup());
    	
    	// Create a new group
    	ResourceGroup group = subscription.resourceGroups().define("marcinstestgroup")
    			.withRegion(Region.US_WEST)
    			.provision();
    	
    	// Create an availability set in an existing group
    	availabilitySet = subscription.availabilitySets().define(newAvailabilitySetName + "2")
    		.withRegion(Region.US_WEST)
    		.withExistingResourceGroup(group)
    		.withTag("hello", "world")
    		.withoutTag("hello")
    		.provision();
    	
    	// Get an existing availability set based onb group and name
    	availabilitySet = subscription.availabilitySets(availabilitySet.resourceGroup(), availabilitySet.name());
    	printAvailabilitySet(availabilitySet);
    	
    	// Delete the entire group
    	subscription.resourceGroups().delete(group.id());
    }
    
    
    private static void printAvailabilitySet(AvailabilitySet availabilitySet) throws Exception {
    	StringBuilder output = new StringBuilder();
    	output
    		.append(String.format("Availability set id: %s\n", availabilitySet.id()))
    		.append(String.format("\tName: %s\n", availabilitySet.name()))
    		.append(String.format("\tGroup: %s\n", availabilitySet.resourceGroup()))
    		.append(String.format("\tVirtual machine ids: \n\t%s", StringUtils.join(availabilitySet.virtualMachineIds(), "\n\t")))
    		;
    	
    	System.out.println(output.toString());
    }
}
