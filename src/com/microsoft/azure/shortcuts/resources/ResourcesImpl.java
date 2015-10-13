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
import java.util.List;

import com.microsoft.azure.management.resources.models.GenericResourceExtended;
import com.microsoft.azure.management.resources.models.ResourceListParameters;
import com.microsoft.azure.shortcuts.common.implementation.NamedRefreshableImpl;
import com.microsoft.azure.shortcuts.common.updating.Deletable;
import com.microsoft.azure.shortcuts.resources.listing.Resources;
import com.microsoft.azure.shortcuts.resources.reading.Provider;
import com.microsoft.azure.shortcuts.resources.reading.Resource;
import com.microsoft.windowsazure.core.ResourceIdentity;
import com.microsoft.windowsazure.exception.ServiceException;

public class ResourcesImpl implements Resources {
	
	final Azure azure;
	
	ResourcesImpl(Azure azure) {
		this.azure = azure;
	}
	
	@Override
	// Returns list of resource names in the subscription
	public List<String> list() {
		return list(null);
	}
	
	
	// Returns list of resource names in the specified resource group
	public List<String> list(String group) {
		try {
			ResourceListParameters params = new ResourceListParameters();
			params.setResourceGroupName(group);
			ArrayList<GenericResourceExtended> items = 
					azure.resourceManagementClient().getResourcesOperations().list(params).getResources();
			
			ArrayList<String> names = new ArrayList<>();
			for(GenericResourceExtended item : items) {
				names.add(item.getName());
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
		ResourceImpl resource = new ResourceImpl(null, false);
		return resource.refresh(group, identity);
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
		
		private HashMap<String, String> tags = new HashMap<>();
		private String region, properties;
		
		private ResourceImpl(String id, boolean initialized) {
			super(id, initialized);
		}

		
		/***********************************************************
		 * Getters
		 ***********************************************************/

		@Override
		public String group() throws Exception {
			ensureInitialized();
			return RESOURCE_ID.GROUP.from(this.name);
		}

		@Override
		public String region() throws Exception {
			ensureInitialized();
			return this.region;
		}

		@Override
		public String shortName() throws Exception {
			ensureInitialized();
			return RESOURCE_ID.NAME.from(this.name);
		}

		@Override
		public String provider() throws Exception {
			ensureInitialized();
			return RESOURCE_ID.PROVIDER.from(this.name);
		}

		@Override
		public String type() throws Exception {
			ensureInitialized();
			return RESOURCE_ID.TYPE.from(this.name);
		}

		@Override
		public HashMap<String, String> tags() throws Exception {
			ensureInitialized();
			return this.tags;
		}

		@Override
		public String properties() throws Exception {
			ensureInitialized();
			return this.properties;
		}

		@Override
		public String getProvisioningState() throws Exception {
			if(this.name == null) {
				return null; // Temporary object, does not exist in the cloud
			} else {
				return azure.resourceManagementClient().getResourcesOperations().get(
						this.group(), this.toResourceIdentity()).getResource().getProvisioningState();
			}
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
			GenericResourceExtended response = azure.resourceManagementClient().getResourcesOperations().get(group, identity).getResource();
			this.setName(response.getId());
			this.region = response.getLocation();
			this.tags = response.getTags();
			this.properties = response.getProperties();
			this.initialized = true;
			return this;
		}
		
		
		/************************************************************
		 * Helpers
		 ************************************************************/

		// Returns ResourceIdentity instance based on this resource
		private ResourceIdentity toResourceIdentity() throws Exception {
			ResourceIdentity identity = createResourceIdentity(
				this.shortName(),
				this.type(),
				this.provider());
			return identity;
		}


	}
}
