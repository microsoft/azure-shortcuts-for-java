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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import com.microsoft.azure.management.resources.models.ProviderResourceType;
import com.microsoft.azure.shortcuts.common.implementation.NamedImpl;
import com.microsoft.azure.shortcuts.common.implementation.NamedRefreshableImpl;
import com.microsoft.azure.shortcuts.common.implementation.SupportsListing;
import com.microsoft.azure.shortcuts.common.implementation.SupportsReading;
import com.microsoft.azure.shortcuts.resources.reading.Provider;

public class Providers implements
	SupportsListing,
	SupportsReading<Provider>{
	
	final Azure azure;
	
	Providers(Azure azure) {
		this.azure = azure;
	}
	
	@Override
	// Returns list of resource names in the subscription
	public String[] list() throws Exception {
		ArrayList<com.microsoft.azure.management.resources.models.Provider> providers = azure.resourceManagementClient().getProvidersOperations().list(null).getProviders();
		String[] names = new String[providers.size()];
		int i = 0;
		for(com.microsoft.azure.management.resources.models.Provider provider: providers) {
			names[i++]= provider.getNamespace();
		}
		return names;
	}
	
	
	@Override
	public Provider get(String namespace) throws Exception {
		ProviderImpl provider = new ProviderImpl(namespace, false);
		return provider.refresh();
	}

	
	// Implements logic for individual provider
	private class ProviderImpl
		extends
			NamedRefreshableImpl<Provider>
		implements 
			Provider {
		
		private String registrationState;
		private HashMap<String, ResourceType> resourceTypes = new HashMap<>();		
		private ProviderImpl(String id, boolean initialized) {
			super(id, initialized);
		}


		/***********************************************************
		 * Getters
		 * @throws Exception 
		 ***********************************************************/

		@Override
		public String registrationState() throws Exception {
			return this.registrationState;
		}
		

		@Override
		public HashMap<String, ResourceType> resourceTypes() throws Exception {
			return this.resourceTypes;
		}
		
		
		@Override
		public ResourceType resourceTypes(String name) throws Exception {
			return this.resourceTypes().get(name);
		}
		
		
		// Implementation of resource type
		private class ResourceTypeImpl
			extends NamedImpl<ResourceType>
			implements Provider.ResourceType {

			private ArrayList<String> apiVersions = new ArrayList<>();
			private ResourceTypeImpl(String name) {
				super(name);
			}

			@Override
			public String[] apiVersions() {
				return this.apiVersions.toArray(new String[0]);
			}

			@Override
			public String latestApiVersion() {
				// Assume the collection is sorted in ascending order
				if(this.apiVersions.size() > 0) {
					return this.apiVersions.get(this.apiVersions.size() -1);
				} else {
					return null;
				}
			}
		}
		

		/************************************************************
		 * Verbs
		 ************************************************************/

		@Override
		public ProviderImpl refresh() throws Exception {
			com.microsoft.azure.management.resources.models.Provider response = 
					azure.resourceManagementClient().getProvidersOperations().get(this.name).getProvider();
			this.setName(response.getNamespace());
			this.registrationState = response.getRegistrationState();
			
			ArrayList<ProviderResourceType> resourceTypes = response.getResourceTypes();
			for(ProviderResourceType r : resourceTypes) {
				ProviderImpl.ResourceTypeImpl resourceType = this.new ResourceTypeImpl(r.getName());
				this.resourceTypes.put(r.getName(), resourceType);
				resourceType.apiVersions = r.getApiVersions();
				Collections.sort(resourceType.apiVersions);
			}
			
			this.initialized = true;
			return this;
		}
	}
}
