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
import java.util.HashMap;
import java.util.List;

import com.microsoft.azure.management.resources.models.ResourceGroup;
import com.microsoft.azure.management.resources.models.ResourceGroupExtended;
import com.microsoft.azure.shortcuts.common.implementation.EntitiesImpl;
import com.microsoft.azure.shortcuts.common.implementation.NamedRefreshableImpl;
import com.microsoft.azure.shortcuts.resources.creation.GroupDefinitionBlank;
import com.microsoft.azure.shortcuts.resources.creation.GroupDefinitionProvisionable;
import com.microsoft.azure.shortcuts.resources.listing.Groups;
import com.microsoft.azure.shortcuts.resources.reading.Group;
import com.microsoft.azure.shortcuts.resources.updating.GroupUpdatable;
import com.microsoft.azure.shortcuts.resources.updating.GroupUpdatableBlank;

public class GroupsImpl 
	extends EntitiesImpl<Azure>
	implements Groups {
	
	List<Group> groups = null;
	
	GroupsImpl(Azure azure) {
		super(azure);
	}
	
	@Override
	public List<String> names() throws Exception {
		ArrayList<ResourceGroupExtended> groups = 
			azure.resourceManagementClient().getResourceGroupsOperations().list(null).getResourceGroups();
		
		ArrayList<String> names = new ArrayList<>();
		for(ResourceGroupExtended group : groups) {
			names.add(group.getName());
		}
		
		return names;		
	}
	
		
	@Override
	// Gets a specific resource group
	public Group get(String name) throws Exception {
		GroupImpl group = new GroupImpl(name, false);
		return group.refresh();
	}
	
	
	@Override
	public void delete(String name) throws Exception {
		azure.resourceManagementClient().getResourceGroupsOperations().delete(name);
		//TODO: Apparently the effect of the deletion is not immediate - Azure SDK misleadingly returns from this synch call even though listing resource groups will still include this
	}
	

	@Override
	public GroupUpdatableBlank update(String name) {
		return new GroupImpl(name, false);
	}


	@Override
	public GroupDefinitionBlank define(String name) {
		return new GroupImpl(name, true);
	}

	
	// Implements logic for individual resource group
	private class GroupImpl 
		extends 
			NamedRefreshableImpl<Group>
		implements
			GroupUpdatable,
			GroupDefinitionProvisionable,
			GroupDefinitionBlank,
			Group {
		
		private HashMap<String, String> tags = new HashMap<String, String>();
		private String region, id, provisioningState;

		private GroupImpl(String name, boolean initialized) {
			super(name.toLowerCase(), initialized);
		}


		/***********************************************************
		 * Getters
		 ***********************************************************/
		
		@Override
		public String region() throws Exception {
			ensureInitialized();
			return this.region;
		}

		@Override
		public String id() throws Exception {
			ensureInitialized();
			return this.id;
		}

		@Override
		public HashMap<String, String> tags() throws Exception {
			ensureInitialized();
			return this.tags;
		}

		@Override
		public String getProvisioningState() throws Exception {
			// This property is not cached because it is useful to read it in real time
			this.refresh();
			return this.provisioningState;
		}

		
		/**************************************************************
		 * Setters (fluent interface)
		 **************************************************************/
		
		@Override
		public GroupImpl withTags(HashMap<String, String> tags) {
			this.tags = tags;
			return this;
		}

		@Override
		public GroupImpl withTag(String key, String value) {
			this.tags.put(key, value);
			return this;
		}

		@Override
		public GroupImpl withoutTag(String key) {
			this.tags.remove(key);
			return this;
		}

		@Override
		public GroupImpl withRegion(String region) {
			this.region = region;
			return this;
		}


		/************************************************************
		 * Verbs
		 ************************************************************/
		
		@Override
		public GroupImpl apply() throws Exception {
			ResourceGroup params = new ResourceGroup();
			Group group;
			
			params.setTags(this.tags);
			
			// Figure out the region, since the SDK requires on the params explicitly even though it cannot be changed
			if(this.region != null) {
				params.setLocation(this.region);
			} else if(null == (group = azure.groups().get(this.name))) {
				throw new Exception("Resource group not found");
			} else {
				params.setLocation(group.region());
			}

			azure.resourceManagementClient().getResourceGroupsOperations().createOrUpdate(this.name, params);
			return this;
		}

		
		@Override
		public void delete() throws Exception {
			azure.groups().delete(this.name);
		}

		
		@Override
		public GroupImpl provision() throws Exception {
			ResourceGroup params = new ResourceGroup();
			params.setLocation(this.region);
			params.setTags(this.tags);
			azure.resourceManagementClient().getResourceGroupsOperations().createOrUpdate(this.name, params);
			return this;
		}

		
		@Override
		public GroupImpl refresh() throws Exception {
			ResourceGroupExtended response = azure.resourceManagementClient().getResourceGroupsOperations().get(this.name).getResourceGroup();
			this.region = response.getLocation();
			this.id = response.getId();
			this.tags = response.getTags();
			this.provisioningState = response.getProvisioningState();
			this.initialized = true;
			return this;
		}
	}
}
