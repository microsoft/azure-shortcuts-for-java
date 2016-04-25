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

import com.microsoft.azure.management.resources.models.ResourceGroupExtended;
import com.microsoft.azure.shortcuts.common.implementation.EntitiesImpl;
import com.microsoft.azure.shortcuts.resources.ResourceGroup;
import com.microsoft.azure.shortcuts.resources.ResourceGroups;

public class ResourceGroupsImpl 
	extends EntitiesImpl<Subscription>
	implements ResourceGroups {
	
	ResourceGroupsImpl(Subscription subscription) {
		super(subscription);
	}
	
	
	@Override
	public Map<String, ResourceGroup> asMap() throws Exception {
		HashMap<String, ResourceGroup> wrappers = new HashMap<>();
		for(ResourceGroupExtended nativeItem : getNativeEntities()) {
			ResourceGroupImpl wrapper = new ResourceGroupImpl(nativeItem, this);
			wrappers.put(nativeItem.getName(), wrapper);
		}
		
		return Collections.unmodifiableMap(wrappers);
	}

		
	@Override
	// Gets a specific resource group
	public ResourceGroupImpl get(String name) throws Exception {
		ResourceGroupExtended azureGroup = subscription.resourceManagementClient().getResourceGroupsOperations().get(name).getResourceGroup();
		return new ResourceGroupImpl(azureGroup, this);
	}
	
	
	@Override
	public void delete(String name) throws Exception {
		subscription.resourceManagementClient().getResourceGroupsOperations().delete(name);
		//TODO: Apparently the effect of the deletion is not immediate - Azure SDK misleadingly returns from this synch call even though listing resource groups will still include this
	}
	

	@Override
	public ResourceGroupImpl update(String name) {
		return createWrapper(name);
	}


	@Override
	public ResourceGroupImpl define(String name) {
		return createWrapper(name);
	}

	
	/***************************************************
	 * Helpers
	 ***************************************************/
	
	// Wraps native Azure group
	private ResourceGroupImpl createWrapper(String name) {
		ResourceGroupExtended azureGroup = new ResourceGroupExtended();
		azureGroup.setName(name);
		return new ResourceGroupImpl(azureGroup, this);
		
	}
	
	// Helper to get the resource groups from Azure
	private ArrayList<ResourceGroupExtended> getNativeEntities() throws Exception {
		return this.subscription.resourceManagementClient().getResourceGroupsOperations().list(null).getResourceGroups();		
	}
}
