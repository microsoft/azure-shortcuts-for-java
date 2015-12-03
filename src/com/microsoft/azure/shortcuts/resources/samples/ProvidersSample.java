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

import com.microsoft.azure.shortcuts.resources.Provider;
import com.microsoft.azure.shortcuts.resources.Provider.ResourceType;
import com.microsoft.azure.shortcuts.resources.implementation.Azure;

// Tests resources
public class ProvidersSample {
    public static void main(String[] args) {
        try {
            Azure azure = Azure.authenticate("my.azureauth", null);
            test(azure);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void test(Azure azure) throws Exception {
    	// List provider namespaces
    	Map<String, Provider> providers = azure.providers().list();
    	System.out.println(String.format("Provider namespaces: %s\t", StringUtils.join(providers.keySet(), "\n\t")));
    	
    	// List providers
    	for(Provider provider : providers.values()) {
    		System.out.println(provider.id() + " - " + provider.registrationState());
    	}
    	
    	if(providers.size() > 0) {
    		// Get information about a specific provider
    		Provider provider = azure.providers("microsoft.classicstorage");
    		
    		System.out.println(String.format("Found provider: %s\n" 
    				+ "\tRegistration State: %s\n"
    				+ "\tAPI versions for resource types:",
    				provider.id(),
    				provider.registrationState()));
    		
    		for(ResourceType t : provider.resourceTypes().values()) {
    			System.out.println(String.format("\t\t%s: %s", t.id(), StringUtils.join(t.apiVersions(), ", ")));
    		}
    		
    		// Get latest API version for a specific resource type
    		String resourceType = "storageAccounts";
    		System.out.println(String.format("\n\t\tLatest version for type %s: %s", resourceType, 
    			provider.resourceTypes().get(resourceType).latestApiVersion()));

    		// Get latest API version for a specific resource type - shortcut
    		System.out.println(String.format("\n\t\tLatest version for type %s: %s", resourceType, 
        		provider.resourceTypes(resourceType).latestApiVersion()));
    	}
    }
}
