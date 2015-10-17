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
package com.microsoft.azure.shortcuts.resources.implementation;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.microsoft.azure.management.resources.models.GenericResourceExtended;
import com.microsoft.azure.management.resources.models.ResourceListParameters;
import com.microsoft.azure.shortcuts.common.implementation.EntitiesImpl;
import com.microsoft.azure.shortcuts.common.implementation.NamedRefreshableImpl;
import com.microsoft.azure.shortcuts.common.updating.Deletable;
import com.microsoft.azure.shortcuts.resources.listing.Resources;
import com.microsoft.azure.shortcuts.resources.reading.Provider;
import com.microsoft.azure.shortcuts.resources.reading.Resource;
import com.microsoft.windowsazure.core.ResourceIdentity;
import com.microsoft.windowsazure.exception.ServiceException;


public class ResourcesImpl
	extends EntitiesImpl<Azure>
	implements Resources {
	
	ResourcesImpl(Azure azure) {
		super(azure);
	}
	
	@Override
	// Returns list of resource names in the subscription
	public List<String> names() {
		return names(null);
	}
	
	
	// Returns list of resource names in the specified resource group
	public List<String> names(String group) {
		try {
			ResourceListParameters params = new ResourceListParameters();
			params.setResourceGroupName(group);
			ArrayList<GenericResourceExtended> items = 
					azure.resourceManagementClient().getResourcesOperations().list(params).getResources();
			
			ArrayList<String> names = new ArrayList<>();
			for(GenericResourceExtended item : items) {
				names.add(item.getId());
			}

			return names;

		} catch (IOException | ServiceException | URISyntaxException e) {
			return new ArrayList<String>();
		}
	}

 
	// Indexes to the parts in the resource id
	private enum RESOURCE_ID {
		// Assumes this format: /subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/{resourceProviderNamespace}/{resourceType}/{resourceName}
		SUBSCRIPTION(2),
		GROUP(4),
		PROVIDER(6),
		TYPE(7),
		NAME(8);
		
	    public final int val;
	    RESOURCE_ID(int val) {
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
 	
	
    // Instantiates a ResourceIdentity from the ID
    public ResourceIdentity createResourceIdentity(String resourceId) throws Exception {
    	return createResourceIdentity(
    		RESOURCE_ID.NAME.from(resourceId),
    		RESOURCE_ID.TYPE.from(resourceId),
    		RESOURCE_ID.PROVIDER.from(resourceId));
    }
    
	
    // Instantiates a ResourceIdentity from name, type and provider
    private ResourceIdentity createResourceIdentity(String name, String type, String provider) throws Exception {
    	ResourceIdentity identity = new ResourceIdentity();
		identity.setResourceName(name);
		identity.setResourceProviderNamespace(provider);
		identity.setResourceType(type);
		
		// Find latest API version
		final Provider p = azure.providers().get(provider);
		final String latestApiVersion = p.resourceTypes(type).latestApiVersion();
		identity.setResourceProviderApiVersion(latestApiVersion);
    	return identity;
    }


	@Override
	public Resource get(String id) throws Exception {
		return this.get(
			RESOURCE_ID.GROUP.from(id), 
			createResourceIdentity(id));
	}
	
	
	// Gets a resource based on its name, type, provider and group
	public Resource get(String name, String type, String provider, String group) throws Exception {
		ResourceIdentity resourceIdentity = createResourceIdentity(name, type, provider);
		return this.get(group, resourceIdentity);
	}

	
	// Returns a resource based on its group and identity object
	private Resource get(String group, ResourceIdentity identity) throws Exception {
		GenericResourceExtended azureResource= azure.resourceManagementClient().getResourcesOperations().get(group, identity).getResource();
		ResourceImpl resource = new ResourceImpl(azureResource);
		return resource;
	}
	
	
	@Override
	public void delete(String id) throws Exception {
		azure.resourceManagementClient().getResourcesOperations().delete(
				RESOURCE_ID.GROUP.from(id), 
				createResourceIdentity(id));
	}
	
	
	// Deletes a resource based on its name, type, provider and group
	public void delete(String name, String type, String provider, String group) throws Exception {
		azure.resourceManagementClient().getResourcesOperations().delete(
			group, 
			createResourceIdentity(name, type, provider));
	}
	

	// Implements the individual resource logic
	private class ResourceImpl 
		extends
			NamedRefreshableImpl<Resource>
		implements 
			Resource, Deletable {
		
		GenericResourceExtended azureResource;
		
		private ResourceImpl(GenericResourceExtended azureResource) {
			super(azureResource.getId(), true);
			this.azureResource = azureResource;
		}

		
		/***********************************************************
		 * Getters
		 ***********************************************************/

		@Override
		public String group() throws Exception {
			return RESOURCE_ID.GROUP.from(this.azureResource.getId());
		}

		@Override
		public String region() throws Exception {
			return this.azureResource.getLocation();
		}

		@Override
		public String shortName() throws Exception {
			return RESOURCE_ID.NAME.from(azureResource.getId());
		}

		@Override
		public String provider() throws Exception {
			return RESOURCE_ID.PROVIDER.from(this.azureResource.getId());
		}

		@Override
		public String type() throws Exception {
			return RESOURCE_ID.TYPE.from(this.azureResource.getId());
		}

		@Override
		public Map<String, String> tags() throws Exception {
			return this.azureResource.getTags();
		}

		@Override
		public String properties() throws Exception {
			return this.azureResource.getProperties();
		}

		@Override
		public String provisioningState() throws Exception {
			return this.azureResource.getProvisioningState();
		}
		
		
		/************************************************************
		 * Verbs
		 ************************************************************/

		@Override
		public void delete() throws Exception {
			azure.resources().delete(this.name);
		}
		

		@Override
		public ResourceImpl refresh() throws Exception {
			return refresh(
				RESOURCE_ID.GROUP.from(this.name),
				createResourceIdentity(this.name));
		}
		
		
		// Refreshes the resource based on the group and identity information
		private ResourceImpl refresh(String group, ResourceIdentity identity) throws Exception {
			this.azureResource = azure.resourceManagementClient().getResourcesOperations().get(group, identity).getResource();
			this.initialized = true;
			return this;
		}
	}
}
