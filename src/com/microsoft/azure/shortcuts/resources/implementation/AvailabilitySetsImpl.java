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

import com.microsoft.azure.management.resources.models.ResourceGroupExtended;
import com.microsoft.azure.shortcuts.common.implementation.EntitiesImpl;
import com.microsoft.azure.shortcuts.resources.AvailabilitySet;
import com.microsoft.azure.shortcuts.resources.AvailabilitySets;
import com.microsoft.azure.shortcuts.resources.Group;
import com.microsoft.azure.shortcuts.resources.Region;
import com.microsoft.azure.shortcuts.resources.common.implementation.ResourceBaseImpl;


public class AvailabilitySetsImpl 
	extends EntitiesImpl<Azure>
	implements AvailabilitySets {
	
	List<AvailabilitySets> availabilitySets = null;
	
	AvailabilitySetsImpl(Azure azure) {
		super(azure);
	}
	
		
	@Override
	public Map<String, AvailabilitySet> list(String groupName) throws Exception {
		HashMap<String, AvailabilitySet> wrappers = new HashMap<>();
		for(com.microsoft.azure.management.compute.models.AvailabilitySet nativeItem : getAzureAvailabilitySets(groupName)) {
			wrappers.put(nativeItem.getId(), new AvailabilitySetImpl(nativeItem));
		}
		
		return Collections.unmodifiableMap(wrappers);
	}

	@Override
	public AvailabilitySetImpl get(String resourceId) throws Exception {
		return this.get(
			ResourcesImpl.groupFromResourceId(resourceId), 
			ResourcesImpl.nameFromResourceId(resourceId));
	}	

	@Override
	public AvailabilitySetImpl get(String groupName, String name) throws Exception {
		return new AvailabilitySetImpl(this.getAzureAvailabilitySet(groupName, name));
	}

	@Override
	public void delete(String groupName, String name) throws Exception {
		azure.computeManagementClient().getAvailabilitySetsOperations().delete(groupName, name);
	}
	
	@Override
	public void delete(String id) throws Exception {
		this.delete(
				ResourcesImpl.groupFromResourceId(id), 
				ResourcesImpl.nameFromResourceId(id));
	}

	@Override
	public AvailabilitySetImpl define(String name) throws Exception {
		com.microsoft.azure.management.compute.models.AvailabilitySet nativeItem = new com.microsoft.azure.management.compute.models.AvailabilitySet();
		nativeItem.setName(name);
		return new AvailabilitySetImpl(nativeItem);
	}

	
	/***************************************************
	 * Helpers
	 ***************************************************/
	
	// Helper to get the availability sets from Azure
	private ArrayList<com.microsoft.azure.management.compute.models.AvailabilitySet> getAzureAvailabilitySets(String resourceGroupName) throws Exception {
		return this.azure.computeManagementClient().getAvailabilitySetsOperations().list(resourceGroupName).getAvailabilitySets();
	}
	
	// Helper to get an availability set from Azure
	private com.microsoft.azure.management.compute.models.AvailabilitySet getAzureAvailabilitySet(String groupName, String name) throws Exception {
		return azure.computeManagementClient().getAvailabilitySetsOperations().get(groupName, name).getAvailabilitySet();
	}
	
	
	/***************************************************************
	 * Implements logic for individual resource group
	 ***************************************************************/
	private class AvailabilitySetImpl 
		extends 
			ResourceBaseImpl<AvailabilitySet, com.microsoft.azure.management.compute.models.AvailabilitySet>
		implements
			AvailabilitySet,
			AvailabilitySet.DefinitionBlank,
			AvailabilitySet.DefinitionProvisionable {
		
		private AvailabilitySetImpl(com.microsoft.azure.management.compute.models.AvailabilitySet azureAvailabilitySet) {
			super(azureAvailabilitySet.getId(), azureAvailabilitySet);
		}


		/***********************************************************
		 * Getters
		 ***********************************************************/
		
		
		
		/**************************************************************
		 * Setters (fluent interface)
		 **************************************************************/

		@Override
		public AvailabilitySetImpl withRegion(String regionName) {
			this.inner().setLocation(regionName);
			return this;
		}


		@Override
		public AvailabilitySetImpl withRegion(Region region) {
			return this.withRegion(region.toString());
		}

		
		@Override
		public AvailabilitySetImpl withGroupExisting(String groupName) {
			super.withGroupExisting(groupName);
			return this;
		}


		@Override
		public AvailabilitySetImpl withGroupExisting(Group group) {
			return this.withGroupExisting(group.name());
		}


		@Override
		public AvailabilitySetImpl withGroupExisting(ResourceGroupExtended group) {
			return this.withGroupExisting(group.getName());
		}


		@Override
		public AvailabilitySetImpl withGroupNew(String name) {
			super.withGroupNew(name);
			return this;
		}

		@Override
		public AvailabilitySetImpl withTags(Map<String, String> tags) {
			super.withTags(tags);
			return this;
		}


		@Override
		public AvailabilitySetImpl withTag(String name, String value) {
			super.withTag(name, value);
			return this;
		}


		/************************************************************
		 * Verbs
		 ************************************************************/

		@Override
		public void delete() throws Exception {
			azure.availabilitySets().delete(this.id());
		}

		@Override
		public AvailabilitySetImpl refresh() throws Exception {
			this.setInner(getAzureAvailabilitySet(
				ResourcesImpl.groupFromResourceId(this.id()), 
				ResourcesImpl.nameFromResourceId(this.id())));
			return this;
		}


		@Override
		public AvailabilitySet provision() throws Exception {
			ensureGroup(azure); // Create group if needed
			azure.computeManagementClient().getAvailabilitySetsOperations().createOrUpdate(this.groupName, this.inner());
			return get(this.groupName, this.name());
		}
	}
}
