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
import com.microsoft.azure.shortcuts.resources.implementation.Azure;

public class AvailabilitySetSample {
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
    	String existingAvailabilitySetName = "javasampleresourcegroup1availabilitysetfjdrf";
    	AvailabilitySet availabilitySet;
    	
    	// Listing availability sets in a specific resource group
    	Map<String, AvailabilitySet> availabilitySets = azure.availabilitySets().list(existingGroupName);
    	System.out.println(String.format("Availability set ids in group '%s': \n\t%s", existingGroupName, StringUtils.join(availabilitySets.keySet(), ",\n\t")));
    	
    	// Get info about a specific availability set using its group and name
    	availabilitySet = azure.availabilitySets(existingGroupName, existingAvailabilitySetName);
    	printAvailabilitySet(availabilitySet);
    	
    	// Delete availability set
    	availabilitySet.delete();
    }
    
    
    private static void printAvailabilitySet(AvailabilitySet availabilitySet) throws Exception {
    	StringBuilder output = new StringBuilder();
    	output
    		.append(String.format("Availability set id: %s\n", availabilitySet.id()))
    		.append(String.format("\tName: %s\n", availabilitySet.name()))
    		.append(String.format("\tGroup: %s\n", availabilitySet.group()))
    		;
    	
    	System.out.println(output.toString());
    }
}
