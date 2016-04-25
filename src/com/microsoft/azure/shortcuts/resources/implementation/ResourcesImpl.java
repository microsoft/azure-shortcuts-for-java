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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.microsoft.azure.management.resources.models.GenericResourceExtended;
import com.microsoft.azure.management.resources.models.ResourceListParameters;
import com.microsoft.azure.shortcuts.common.implementation.EntitiesImpl;
import com.microsoft.azure.shortcuts.resources.Provider;
import com.microsoft.azure.shortcuts.resources.Provider.ResourceType;
import com.microsoft.azure.shortcuts.resources.Resource;
import com.microsoft.azure.shortcuts.resources.Resources;
import com.microsoft.windowsazure.core.ResourceIdentity;


public class ResourcesImpl
	extends EntitiesImpl<Subscription>
	implements Resources {
	
	ResourcesImpl(Subscription subscription) {
		super(subscription);
	}
	

	// Indexes to the parts in the resource id
	enum RESOURCE_ID {
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
	    	if(parts.length == 1 && this.val == NAME.val) {
	    		return parts[0];
	    	} else if(parts.length <= this.val) {
	    		return null;
	    	} else {
	    		return parts[this.val];
	    	}
	    }
	    
	    // Returns a new ID based on the provided one, with the group set as specified
	    public String setInID(String groupName, String id) {
	    	String[] parts = id.split("/");
	    	if(parts.length <= this.val) {
	    		return null;
	    	} else {
	    		parts[this.val] = groupName;
	    		StringBuilder newId = new StringBuilder();
	    		for(int i = 0; i< parts.length; i++) {
	    			if(i>0) newId.append("/");
	    			newId.append(parts[i]);
	    		}
	    		return newId.toString();
	    	}
	    }
	}
 	

	public static String groupFromResourceId(String id) {
		return RESOURCE_ID.GROUP.from(id);
	}
	
	
	public static String nameFromResourceId(String id) {
		return RESOURCE_ID.NAME.from(id);
	}
	
	
	public static String resourceIdWithGroup(String group, String resourceId) {
		return RESOURCE_ID.GROUP.setInID(group, resourceId);
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
		final Provider p = subscription.providers().get(provider);
		ResourceType t = p.resourceTypes(type);
		if(t == null) {
			return identity;
		}
		final String latestApiVersion = t.latestApiVersion();
		identity.setResourceProviderApiVersion(latestApiVersion);
    	return identity;
    }


	@Override
	public Resource get(String id) throws Exception {
		return this.get(
			RESOURCE_ID.GROUP.from(id), 
			createResourceIdentity(id));
	}
	
	
	@Override
	public Resource get(String name, String type, String provider, String group) throws Exception {
		ResourceIdentity resourceIdentity = createResourceIdentity(name, type, provider);
		return this.get(group, resourceIdentity);
	}

	
	// Returns a resource based on its group and identity object
	private Resource get(String group, ResourceIdentity identity) throws Exception {
		GenericResourceExtended azureResource= subscription.resourceManagementClient().getResourcesOperations().get(group, identity).getResource();
		ResourceImpl resource = new ResourceImpl(azureResource, this);
		return resource;
	}
	
	
	@Override
	public void delete(String id) throws Exception {
		subscription.resourceManagementClient().getResourcesOperations().delete(
			RESOURCE_ID.GROUP.from(id), 
			createResourceIdentity(id));
	}
	
	
	@Override
	public void delete(String name, String type, String provider, String group) throws Exception {
		subscription.resourceManagementClient().getResourcesOperations().delete(
			group, 
			createResourceIdentity(name, type, provider));
	}
	
	
	@Override
	public Map<String, Resource> asMap() throws Exception {
		return this.asMap(null);
	}

	
	@Override
	public Map<String, Resource> asMap(String groupName) throws Exception {
		HashMap<String, Resource> wrappers = new HashMap<>();
		for(GenericResourceExtended nativeItem : getNativeEntities(groupName)) {
			ResourceImpl wrapper = new ResourceImpl(nativeItem, this);
			wrappers.put(nativeItem.getId(), wrapper);
		}
		
		return Collections.unmodifiableMap(wrappers);
	}

	
	/***********************************************************
	 * Helpers
	 ***********************************************************/
	
	private ArrayList<GenericResourceExtended> getNativeEntities(String groupName) throws Exception {
		ResourceListParameters params = new ResourceListParameters(); 
		params.setResourceGroupName(groupName);
		return subscription.resourceManagementClient().getResourcesOperations().list(params).getResources();
	}
}
