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

import com.microsoft.azure.management.resources.models.ResourceGroupExtended;
import com.microsoft.azure.shortcuts.common.implementation.NamedImpl;
import com.microsoft.azure.shortcuts.common.implementation.SupportsDeleting;
import com.microsoft.azure.shortcuts.common.implementation.SupportsListing;
import com.microsoft.azure.shortcuts.common.implementation.SupportsReading;
import com.microsoft.azure.shortcuts.resources.reading.Group;
import com.microsoft.windowsazure.exception.ServiceException;

public class Groups 
	implements 
		SupportsListing,
		SupportsReading<Group>,
		SupportsDeleting {
	
	final Azure azure;
	Groups(Azure azure) {
		this.azure = azure;
	}
	
	@Override
	// Returns list of resource names in the subscription
	public String[] list() {
		try {
			ArrayList<ResourceGroupExtended> groups = 
					azure.resourceManagementClient().getResourceGroupsOperations().list(null).getResourceGroups();
			
			String[] names = new String[groups.size()];
			int i = 0;
			for(ResourceGroupExtended group: groups) {
				names[i++]= group.getName();
			}
			return names;

		} catch (IOException | ServiceException | URISyntaxException e) {
			return new String[0];
		}
	}
	
	
	@Override
	// Gets a specific resource group
	public Group get(String name) throws Exception {
		GroupImpl group = new GroupImpl(name);
		ResourceGroupExtended response = azure.resourceManagementClient().getResourceGroupsOperations().get(name).getResourceGroup();
		group.region = response.getLocation();
		group.id = response.getId();
		group.tags = response.getTags();
		
		return group;
	}
	
	
	@Override
	public void delete(String name) throws Exception {
		azure.resourceManagementClient().getResourceGroupsOperations().delete(name);
		//TODO: Apparently the effect of the deletion is not immediate - Azure SDK misleadingly returns from this synch call even though listing resource groups will still include this
	}
	
			
	// Implements logic for individual resource group
	private class GroupImpl 
		extends 
			NamedImpl
		implements
			Group {
		
		public HashMap<String, String> tags;
		public String region, id;

		private GroupImpl(String name) {
			super(name.toLowerCase());
		}

		@Override
		public String region() {
			return this.region;
		}

		@Override
		public String id() {
			return this.id;
		}

		@Override
		public HashMap<String, String> tags() {
			return this.tags;
		}

		@Override
		public String getProvisioningState() {
			// This property is not cached because it is useful to read it in real time
			try {
				return azure.resourceManagementClient().getResourceGroupsOperations().get(this.name).getResourceGroup().getProvisioningState();
			} catch (IOException | ServiceException | URISyntaxException e) {
				return null;
			}
		}
	}
}
