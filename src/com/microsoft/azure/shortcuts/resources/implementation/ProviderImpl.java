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
import java.util.List;
import java.util.Map;

import com.microsoft.azure.management.resources.models.ProviderResourceType;
import com.microsoft.azure.shortcuts.common.implementation.IndexableImpl;
import com.microsoft.azure.shortcuts.common.implementation.IndexableRefreshableWrapperImpl;
import com.microsoft.azure.shortcuts.resources.Provider;

// Implements logic for individual provider
class ProviderImpl
	extends
		IndexableRefreshableWrapperImpl<Provider, com.microsoft.azure.management.resources.models.Provider>
	implements 
		Provider {
	
	private final ArmEntitiesImpl collection;
	
	private ProviderImpl(
			com.microsoft.azure.management.resources.models.Provider azureProvider, 
			ArmEntitiesImpl parentCollection) {
		super(azureProvider.getNamespace(), azureProvider);
		this.collection = parentCollection;
	}

	static ProviderImpl wrap(
			com.microsoft.azure.management.resources.models.Provider nativeEntity, 
			ArmEntitiesImpl collection) {
		return new ProviderImpl(nativeEntity, collection);
	}
	
	
	/***********************************************************
	 * Getters
	 * @throws Exception 
	 ***********************************************************/		

	@Override
	public String registrationState() throws Exception {
		return this.inner().getRegistrationState();
	}
	

	@Override
	public Map<String, ResourceType> resourceTypes() throws Exception {
		HashMap<String, ResourceType> resourceTypes = new HashMap<>();
		for(ProviderResourceType item : this.inner().getResourceTypes()) {
			ResourceTypeImpl resourceType = new ResourceTypeImpl(item);
			resourceTypes.put(item.getName(), resourceType);
		}
		return Collections.unmodifiableMap(resourceTypes);
	}
	
	
	@Override
	public ResourceType resourceTypes(String name) throws Exception {
		return this.resourceTypes().get(name);
	}
	
	
	// Implementation of resource type
	private class ResourceTypeImpl
		extends IndexableImpl
		implements Provider.ResourceType {

		final private ProviderResourceType azureResourceType;
		
		private ResourceTypeImpl(ProviderResourceType azureResourceType) {
			super(azureResourceType.getName());
			this.azureResourceType = azureResourceType;
		}

		@Override
		public List<String> apiVersions() {
			return Collections.unmodifiableList(this.azureResourceType.getApiVersions());
		}

		@Override
		public String latestApiVersion() {
			// Assume the collection is sorted in ascending order
			ArrayList<String> versions = azureResourceType.getApiVersions();
			if(versions == null || versions.isEmpty()) {
				return null;
			} else if(versions.size() == 1) {
				return versions.get(0);
			} else {
				Collections.sort(versions);
				return versions.get(versions.size() -1);
			}
		}
	}
	

	/************************************************************
	 * Verbs
	 ************************************************************/

	@Override
	public ProviderImpl refresh() throws Exception {
		com.microsoft.azure.management.resources.models.Provider azureProvider = 
			this.collection.subscription().resourceManagementClient().getProvidersOperations().get(this.id).getProvider();
		this.setInner(azureProvider);
		return this;
	}

}

