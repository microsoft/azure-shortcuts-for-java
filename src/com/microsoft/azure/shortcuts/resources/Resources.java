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
package com.microsoft.azure.shortcuts.resources;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;

import com.microsoft.azure.management.resources.models.GenericResourceExtended;
import com.microsoft.azure.management.resources.models.ResourceListParameters;
import com.microsoft.azure.shortcuts.common.implementation.NamedImpl;
import com.microsoft.azure.shortcuts.common.implementation.SupportsListing;
import com.microsoft.azure.shortcuts.common.implementation.SupportsReading;
import com.microsoft.azure.shortcuts.resources.reading.Resource;
import com.microsoft.windowsazure.core.ResourceIdentity;
import com.microsoft.windowsazure.exception.ServiceException;

public class Resources implements
	SupportsListing,
	SupportsReading<Resource> {
	
	final Azure azure;
	
	Resources(Azure azure) {
		this.azure = azure;
	}
	
	@Override
	// Returns list of resource names in the subscription
	public String[] list() {
		return list(null);
	}
	
	
	// Returns list of resource names in the specified resource group
	public String[] list(String group) {
		try {
			ResourceListParameters params = new ResourceListParameters();
			params.setResourceGroupName(group);
			ArrayList<GenericResourceExtended> resources = 
					azure.resourceManagementClient().getResourcesOperations().list(params).getResources();
			
			String[] names = new String[resources.size()];
			int i = 0;
			for(GenericResourceExtended resource: resources) {
				names[i++]= resource.getId();
			}
						
			return names;

		} catch (IOException | ServiceException | URISyntaxException e) {
			return new String[0];
		}		
	}

 
	// Indexes to the parts in the resource id
	private enum RESOURCE_ID_PART {
		// Assumes this format: /subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/{resourceProviderNamespace}/{resourceType}/{resourceName}
		SUBSCRIPTION(2),
		GROUP(4),
		PROVIDER(6),
		TYPE(7),
		NAME(8);
		
	    public final int val;
	
	    RESOURCE_ID_PART(int val) {
	        this.val = val;
	    }
	    
	    // Returns the requested part of the resource id
	    public String from(String resourceId) {
	    	String[] parts = resourceId.split("/");
	    	if(parts.length <= this.val) {
	    		return null;
	    	} else {
	    		return parts[this.val];
	    	}
	    }
	}
 	

	@Override
	public Resource get(String id) throws Exception {
		ResourceImpl resource = new ResourceImpl(id);
		
		ResourceIdentity resourceIdentity = new ResourceIdentity();
		resourceIdentity.setResourceName(RESOURCE_ID_PART.NAME.from(id));
		resourceIdentity.setResourceProviderNamespace(RESOURCE_ID_PART.PROVIDER.from(id));
		resourceIdentity.setResourceType(RESOURCE_ID_PART.TYPE.from(id));
		resourceIdentity.setResourceProviderApiVersion("2015-06-01"); //TODO: This should probably not be fixed...
		
		GenericResourceExtended response = azure.resourceManagementClient().getResourcesOperations().get(RESOURCE_ID_PART.GROUP.from(id), resourceIdentity).getResource();
		if(response != null) {
			resource.region = response.getLocation();
			resource.tags = response.getTags();
			return resource;
		} else {
			throw new Exception("Resource not found");
		}
	}

	
	// Implements the individual resource logic
	private class ResourceImpl 
		extends
			NamedImpl
		implements 
			Resource {
		
		private HashMap<String, String> tags = new HashMap<>();
		private String region;
		
		private ResourceImpl(String id) {
			super(id);
		}

		@Override
		public String group() {
			return RESOURCE_ID_PART.GROUP.from(this.name);
		}

		@Override
		public String region() {
			return this.region;
		}

		@Override
		public String shortName() {
			return RESOURCE_ID_PART.NAME.from(this.name);
		}

		@Override
		public String provider() {
			return RESOURCE_ID_PART.PROVIDER.from(this.name);
		}

		@Override
		public String type() {
			return RESOURCE_ID_PART.TYPE.from(this.name);
		}

		@Override
		public HashMap<String, String> tags() {
			return this.tags;
		}
	}
}
