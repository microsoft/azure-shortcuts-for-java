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

import com.microsoft.azure.management.resources.models.ResourceGroup;
import com.microsoft.azure.management.resources.models.ResourceGroupExtended;
import com.microsoft.azure.shortcuts.common.implementation.EntitiesImpl;
import com.microsoft.azure.shortcuts.common.implementation.IndexableRefreshableWrapperImpl;
import com.microsoft.azure.shortcuts.resources.Group;
import com.microsoft.azure.shortcuts.resources.Groups;
import com.microsoft.azure.shortcuts.resources.Region;

public class GroupsImpl 
	extends EntitiesImpl<Azure>
	implements Groups {
	
	GroupsImpl(Azure azure) {
		super(azure);
	}
	
	
	@Override
	public Map<String, Group> list() throws Exception {
		HashMap<String, Group> wrappers = new HashMap<>();
		for(ResourceGroupExtended nativeItem : getNativeEntities()) {
			GroupImpl wrapper = new GroupImpl(nativeItem, this.azure);
			wrappers.put(nativeItem.getName(), wrapper);
		}
		
		return Collections.unmodifiableMap(wrappers);
	}

		
	@Override
	// Gets a specific resource group
	public GroupImpl get(String name) throws Exception {
		ResourceGroupExtended azureGroup = azure.resourceManagementClient().getResourceGroupsOperations().get(name).getResourceGroup();
		return new GroupImpl(azureGroup, this.azure);
	}
	
	
	@Override
	public void delete(String name) throws Exception {
		azure.resourceManagementClient().getResourceGroupsOperations().delete(name);
		//TODO: Apparently the effect of the deletion is not immediate - Azure SDK misleadingly returns from this synch call even though listing resource groups will still include this
	}
	

	@Override
	public GroupImpl update(String name) {
		return createWrapper(name);
	}


	@Override
	public GroupImpl define(String name) {
		return createWrapper(name);
	}

	
	/***************************************************
	 * Helpers
	 ***************************************************/
	
	// Wraps native Azure group
	private GroupImpl createWrapper(String name) {
		ResourceGroupExtended azureGroup = new ResourceGroupExtended();
		azureGroup.setName(name);
		return new GroupImpl(azureGroup, this.azure);
		
	}
	
	// Helper to get the resource groups from Azure
	private ArrayList<ResourceGroupExtended> getNativeEntities() throws Exception {
		return this.azure.resourceManagementClient().getResourceGroupsOperations().list(null).getResourceGroups();		
	}
	
	
	/***************************************************************
	 * Implements logic for individual resource group
	 ***************************************************************/
	private class GroupImpl 
		extends 
			IndexableRefreshableWrapperImpl<Group, ResourceGroupExtended>
		implements
			Group.Update,
			Group.DefinitionProvisionable,
			Group.DefinitionBlank,
			Group {
		
		private final Azure azure;
		
		private GroupImpl(ResourceGroupExtended azureGroup, Azure azure) {
			super(azureGroup.getName(), azureGroup);
			this.azure = azure;
		}


		/***********************************************************
		 * Getters
		 ***********************************************************/
		
		@Override
		public String region() throws Exception {
			return this.inner().getLocation();
		}

		@Override
		public Map<String, String> tags() throws Exception {
			return Collections.unmodifiableMap(this.inner().getTags());
		}

		@Override
		public String provisioningState() throws Exception {
			return this.inner().getProvisioningState();
		}

		@Override
		public String name() {
			return this.inner().getName();
		}
		
		
		/**************************************************************
		 * Setters (fluent interface)
		 **************************************************************/
		
		@Override
		public GroupImpl withTags(Map<String, String> tags) {
			this.inner().setTags(new HashMap<>(tags));
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
		public GroupImpl withRegion(String regionName) {
			this.inner().setLocation(regionName);
			return this;
		}
		
		@Override
		public GroupImpl withRegion(Region region) {
			return this.withRegion(region.toString());
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
			} else if(null == (group = azure.groups().get(this.id))) {
				throw new Exception("Resource group not found");
			} else {
				params.setLocation(group.region());
			}

			azure.resourceManagementClient().getResourceGroupsOperations().createOrUpdate(this.id, params);
			return this;
		}

		
		@Override
		public void delete() throws Exception {
			azure.groups().delete(this.id);
		}

		
		@Override
		public GroupImpl provision() throws Exception {
			ResourceGroup params = new ResourceGroup();
			params.setLocation(this.inner().getLocation());
			params.setTags(this.inner().getTags());
			azure.resourceManagementClient().getResourceGroupsOperations().createOrUpdate(this.id, params);
			return this;
		}

		
		@Override
		public GroupImpl refresh() throws Exception {
			this.setInner(azure.resourceManagementClient().getResourceGroupsOperations().get(this.id).getResourceGroup());
			return this;
		}
	}
}
