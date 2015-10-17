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

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.microsoft.azure.shortcuts.resources.implementation.Azure;
import com.microsoft.azure.shortcuts.resources.reading.Resource;

// Tests resources
public class Resources {
    public static void main(String[] args) {
        try {
            Azure azure = Azure.authenticate("my.azureauth", null);
            test(azure);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    

    public static void test(Azure azure) throws Exception {
    	// Listing all resources 
    	List<String> resourceIds = azure.resources().names();
    	System.out.println("Resources: \n\t" + StringUtils.join(resourceIds, ",\n\t"));

    	// Listing resources in a specific group
    	String groupName = "azchat";
    	List<String> resourceIds2 = azure.resources().names(groupName);
    	System.out.println("Resources inside group '" + groupName + "': \n\t" + StringUtils.join(resourceIds2, ",\n\t"));
    	
        // Getting information about a specific resource based on ID
    	Resource resource = azure.resources().get("/subscriptions/9657ab5d-4a4a-4fd2-ae7a-4cd9fbd030ef/resourceGroups/group1444089227523/providers/Microsoft.Storage/storageAccounts/store1444089227523");
    	printResource(resource);
    		
    	// Getting information about a specific resource based on name, type, provider and group
        resource =  azure.resources().get(
        	resource.shortName(),
        	resource.type(),
        	resource.provider(),
        	resource.group());
        printResource(resource);	
    	    	
    	// Delete a resource based on its metadata
        System.out.println(String.format("Deleting resource '%s' of type '%s' by provider '%s' in group '%s'",
        	resource.shortName(),
        	resource.type(),
        	resource.provider(),
        	resource.group()));
        		
        azure.resources().delete(
    		resource.shortName(),
    		resource.type(),
    		resource.provider(),
    		resource.group());
    	
    	// Delete a resource based on its ID
    	String resourceToDelete = "ThisMustFail";
    	System.out.println("Deleting resource " + resourceToDelete);
    	azure.resources().delete(resourceToDelete);
    	
	}
    
    
    private static void printResource(Resource resource) throws Exception {
    	
		System.out.println(String.format("Found resource ID: %s\n"
			+ "\tGroup: %s\n"
			+ "\tProvider: %s\n"
			+ "\tRegion: %s\n"
			+ "\tShort name: %s\n"
			+ "\tTags: %s\n"
			+ "\tType: %s\n"
			+ "\tProvisioning state %s\n",
			
			resource.name(),
			resource.group(),
			resource.provider(),
			resource.region(),
			resource.shortName(),
			resource.tags(),
			resource.type(),
			resource.provisioningState()
			));    	
    }
}
