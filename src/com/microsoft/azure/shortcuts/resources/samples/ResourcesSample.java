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

import com.microsoft.azure.shortcuts.resources.Resource;
import com.microsoft.azure.shortcuts.resources.implementation.Azure;

// Tests resources
public class ResourcesSample {
    public static void main(String[] args) {
        try {
            Azure azure = Azure.authenticate("my.azureauth", null);
            test(azure);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    

    public static void test(Azure azure) throws Exception {
    	// Listing all resource names
    	Map<String, Resource> resources = azure.resources().list();
    	System.out.println(String.format("Resource ids: %s\n\t", StringUtils.join(resources.keySet(), ",\n\t")));

    	// Listing resources in a specific group
    	String groupName = "azchat";
    	Map<String, Resource> resources2 = azure.resources().list(groupName);
    	System.out.println("Resources inside group '" + groupName + "':");
    	for(Resource resource : resources2.values()) {
    		printResource(resource);
    	}
    	
        // Getting information about a specific resource based on ID
    	Resource resource = azure.resources("/subscriptions/9657ab5d-4a4a-4fd2-ae7a-4cd9fbd030ef/resourceGroups/javasampleresourcegroup/providers/Microsoft.Storage/storageAccounts/javastojzgsg");
    	printResource(resource);
    		
    	// Getting information about a specific resource based on name, type, provider and group
        resource =  azure.resources().get(
        	resource.name(),
        	resource.type(),
        	resource.provider(),
        	resource.resourceGroup());
        printResource(resource);	
    	    	
    	// Delete a resource 
        System.out.println(String.format("Deleting resource '%s' of type '%s' by provider '%s' in group '%s'",
        	resource.name(),
        	resource.type(),
        	resource.provider(),
        	resource.resourceGroup()));

        resource.delete();
        
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
			
			resource.id(),
			resource.resourceGroup(),
			resource.provider(),
			resource.region(),
			resource.name(),
			resource.tags(),
			resource.type(),
			resource.provisioningState()
			));    	
    }
}
