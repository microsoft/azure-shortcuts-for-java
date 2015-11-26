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

import com.microsoft.azure.management.resources.models.ResourceGroup;
import com.microsoft.azure.management.resources.models.ResourceGroupExtended;
import com.microsoft.azure.shortcuts.common.implementation.EntitiesImpl;
import com.microsoft.azure.shortcuts.common.implementation.NamedRefreshableWrapperImpl;
import com.microsoft.azure.shortcuts.resources.Group;
import com.microsoft.azure.shortcuts.resources.Groups;


public class GroupsImpl 
	extends EntitiesImpl<Azure>
	implements Groups {
	
	List<Group> groups = null;
	
	GroupsImpl(Azure azure) {
		super(azure);
	}
	
	
	@Override
	public Map<String, Group> list() throws Exception {
		HashMap<String, Group> wrappers = new HashMap<>();
		for(ResourceGroupExtended nativeItem : getAzureGroups()) {
			GroupImpl wrapper = new GroupImpl(nativeItem);
			wrappers.put(nativeItem.getName(), wrapper);
		}
		
		return Collections.unmodifiableMap(wrappers);
	}

		
	@Override
	// Gets a specific resource group
	public GroupImpl get(String name) throws Exception {
		ResourceGroupExtended azureGroup = azure.resourceManagementClient().getResourceGroupsOperations().get(name).getResourceGroup();
		return new GroupImpl(azureGroup);
	}
	
	
	@Override
	public void delete(String name) throws Exception {
		azure.resourceManagementClient().getResourceGroupsOperations().delete(name);
		//TODO: Apparently the effect of the deletion is not immediate - Azure SDK misleadingly returns from this synch call even though listing resource groups will still include this
	}
	

	@Override
	public GroupImpl update(String name) {
		return createStorageAccountWrapper(name);
	}


	@Override
	public GroupImpl define(String name) {
		return createStorageAccountWrapper(name);
	}

	
	/***************************************************
	 * Helpers
	 ***************************************************/
	
	// Wraps native Azure group
	private GroupImpl createStorageAccountWrapper(String name) {
		ResourceGroupExtended azureGroup = new ResourceGroupExtended();
		azureGroup.setName(name);
		return new GroupImpl(azureGroup);
		
	}
	
	// Helper to get the resource groups from Azure
	private ArrayList<ResourceGroupExtended> getAzureGroups() throws Exception {
		return this.azure.resourceManagementClient().getResourceGroupsOperations().list(null).getResourceGroups();		
	}
	
	
	/***************************************************************
	 * Implements logic for individual resource group
	 ***************************************************************/
	private class GroupImpl 
		extends 
			NamedRefreshableWrapperImpl<Group, ResourceGroupExtended>
		implements
			Group.Update,
			Group.DefinitionProvisionable,
			Group.DefinitionBlank,
			Group {
		
		private GroupImpl(ResourceGroupExtended azureGroup) {
			super(azureGroup.getName(), azureGroup);
		}


		/***********************************************************
		 * Getters
		 ***********************************************************/
		
		@Override
		public String region() throws Exception {
			return this.inner().getLocation();
		}

		@Override
		public String id() throws Exception {
			return this.inner().getId();
		}

		@Override
		public Map<String, String> tags() throws Exception {
			return Collections.unmodifiableMap(this.inner().getTags());
		}

		@Override
		public String provisioningState() throws Exception {
			return this.inner().getProvisioningState();
		}

		
		/**************************************************************
		 * Setters (fluent interface)
		 **************************************************************/
		
		@Override
		public GroupImpl withTags(HashMap<String, String> tags) {
			this.inner().setTags(tags);
			return this;
		}

		@Override
		public GroupImpl withTag(String key, String value) {
			if(this.inner().getTags() == null) {
				this.inner().setTags(new HashMap<String, String>());
			}
			this.inner().getTags().put(key, value);
			return this;
		}

		@Override
		public GroupImpl withoutTag(String key) {
			this.inner().getTags().remove(key);
			return this;
		}

		@Override
		public GroupImpl withRegion(String region) {
			this.inner().setLocation(region);
			return this;
		}


		/************************************************************
		 * Verbs
		 ************************************************************/
		
		@Override
		public GroupImpl apply() throws Exception {
			ResourceGroup params = new ResourceGroup();
			Group group;
			
			params.setTags(this.inner().getTags());
			
			// Figure out the region, since the SDK requires on the params explicitly even though it cannot be changed
			if(this.inner().getLocation() != null) {
				params.setLocation(this.inner().getLocation());
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
			params.setLocation(this.inner().getLocation());
			params.setTags(this.inner().getTags());
			azure.resourceManagementClient().getResourceGroupsOperations().createOrUpdate(this.name, params);
			return this;
		}

		
		@Override
		public GroupImpl refresh() throws Exception {
			this.innerObject =  azure.resourceManagementClient().getResourceGroupsOperations().get(this.name).getResourceGroup();
			return this;
		}
	}
}
