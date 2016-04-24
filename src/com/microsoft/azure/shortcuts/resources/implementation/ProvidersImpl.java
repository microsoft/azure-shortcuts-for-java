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

import com.microsoft.azure.shortcuts.resources.Provider;
import com.microsoft.azure.shortcuts.resources.Providers;
import com.microsoft.azure.shortcuts.resources.common.implementation.ArmEntitiesImpl;

public class ProvidersImpl
	extends ArmEntitiesImpl
	implements Providers {
	
	ProvidersImpl(Subscription subscription) {
		super(subscription);
	}
	
	
	@Override
	public Map<String, Provider> asMap() throws Exception {
		HashMap<String, Provider> wrappers = new HashMap<>();
		for(com.microsoft.azure.management.resources.models.Provider nativeItem : getNativeEntities(azure)) {
			ProviderImpl wrapper = ProviderImpl.wrap(nativeItem, this);
			wrappers.put(nativeItem.getNamespace(), wrapper);
		}
			
		return Collections.unmodifiableMap(wrappers);
	}

	
	@Override
	public Provider get(String namespace) throws Exception {
		com.microsoft.azure.management.resources.models.Provider azureProvider = 
				azure.resourceManagementClient().getProvidersOperations().get(namespace).getProvider();

		return ProviderImpl.wrap(azureProvider, this);
	}


	// Get providers from Azure
	private static ArrayList<com.microsoft.azure.management.resources.models.Provider> getNativeEntities(Subscription azure) throws Exception {
		return azure.resourceManagementClient().getProvidersOperations().list(null).getProviders();		
	}
}
