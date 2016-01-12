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

import java.util.List;
import java.util.Map;

import com.microsoft.azure.management.network.models.IpAllocationMethod;
import com.microsoft.azure.management.resources.models.ResourceGroupExtended;
import com.microsoft.azure.shortcuts.resources.Group;
import com.microsoft.azure.shortcuts.resources.PublicIpAddress;
import com.microsoft.azure.shortcuts.resources.PublicIpAddresses;
import com.microsoft.azure.shortcuts.resources.Region;
import com.microsoft.azure.shortcuts.resources.common.implementation.GroupableResourceBaseImpl;
import com.microsoft.azure.shortcuts.resources.common.implementation.GroupableResourcesBaseImpl;


public class PublicIpAddressesImpl 
	extends GroupableResourcesBaseImpl<Azure, PublicIpAddress, com.microsoft.azure.management.network.models.PublicIpAddress>
	implements PublicIpAddresses {
		
	PublicIpAddressesImpl(Azure azure) {
		super(azure);
	}
	
	
	@Override
	public Map<String, PublicIpAddress> list() throws Exception {
		return this.list(null);
	}

	
	@Override
	public PublicIpAddressImpl get(String resourceId) throws Exception {
		return this.get(
			ResourcesImpl.groupFromResourceId(resourceId), 
			ResourcesImpl.nameFromResourceId(resourceId));
	}
	

	@Override
	public PublicIpAddressImpl get(String groupName, String name) throws Exception {
		return new PublicIpAddressImpl(this.getNativeEntity(groupName, name));
	}
	
	
	@Override
	public PublicIpAddressImpl define(String name) throws Exception {
		return createWrapper(name);
	}

	
	@Override
	public void delete(String id) throws Exception {
		this.delete(
			ResourcesImpl.groupFromResourceId(id), 
			ResourcesImpl.nameFromResourceId(id));
	}
	
	
	@Override
	public void delete(String groupName, String name) throws Exception {
		azure.networkManagementClient().getPublicIpAddressesOperations().delete(groupName, name);
	}


	/***************************************************
	 * Helpers
	 ***************************************************/
	
	@Override
	protected List<com.microsoft.azure.management.network.models.PublicIpAddress> getNativeEntities(String resourceGroupName) throws Exception {
		if(resourceGroupName == null) {
			return this.azure.networkManagementClient().getPublicIpAddressesOperations().listAll().getPublicIpAddresses();
		} else {
			return this.azure.networkManagementClient().getPublicIpAddressesOperations().list(resourceGroupName).getPublicIpAddresses();
		}
	}
	
	@Override
	protected com.microsoft.azure.management.network.models.PublicIpAddress getNativeEntity(String groupName, String name) throws Exception {
		return azure.networkManagementClient().getPublicIpAddressesOperations().get(groupName, name).getPublicIpAddress();
	}
	
	@Override
	protected PublicIpAddress createWrapper(com.microsoft.azure.management.network.models.PublicIpAddress nativeItem) {
		return new PublicIpAddressImpl(nativeItem);
	}
	
	// Helper to create a wrapper
	private PublicIpAddressImpl createWrapper(String name) {
		com.microsoft.azure.management.network.models.PublicIpAddress nativeItem = new com.microsoft.azure.management.network.models.PublicIpAddress();
		nativeItem.setName(name);
		nativeItem.setPublicIpAllocationMethod(IpAllocationMethod.DYNAMIC);
		
		return new PublicIpAddressImpl(nativeItem);
	}
	
	
	/***************************************************************
	 * Implements logic for individual resource group
	 ***************************************************************/
	private class PublicIpAddressImpl 
		extends 
			GroupableResourceBaseImpl<PublicIpAddress, com.microsoft.azure.management.network.models.PublicIpAddress>
		implements
			PublicIpAddress,
			PublicIpAddress.DefinitionBlank,
			PublicIpAddress.DefinitionWithIpAddress,
			PublicIpAddress.DefinitionProvisionable {
		
		private PublicIpAddressImpl(com.microsoft.azure.management.network.models.PublicIpAddress azurePublicIpAddress) {
			super(azurePublicIpAddress.getName(), azurePublicIpAddress);
		}


		/***********************************************************
		 * Getters
		 ***********************************************************/

		@Override
		public String ipAddress() {
			return this.inner().getIpAddress();
		}
		
		
		/**************************************************************
		 * Setters (fluent interface)
		 **************************************************************/

		@Override
		public DefinitionProvisionable withStaticIp(String ipAddress) {
			this.inner().setIpAddress(ipAddress);
			this.inner().setPublicIpAllocationMethod(IpAllocationMethod.STATIC);
			return this;
		}


		@Override
		public DefinitionProvisionable withDynamicIp() {
			this.inner().setPublicIpAllocationMethod(IpAllocationMethod.DYNAMIC);
			return this;
		}

		
		@Override
		public PublicIpAddressImpl withRegion(String regionName) {
			super.withRegion(regionName);
			return this;
		}
		
		@Override
		public PublicIpAddressImpl withRegion(Region region) {
			return this.withRegion(region.toString());
		}

		@Override
		public PublicIpAddressImpl withGroupExisting(String groupName) {
			super.withGroupExisting(groupName);
			return this;
		}

		@Override
		public PublicIpAddressImpl withGroupNew(String name) {
			super.withGroupNew(name);
			return this;
		}

		@Override
		public PublicIpAddressImpl withGroupExisting(Group group) {
			return this.withGroupExisting(group.name());
		}

		@Override
		public PublicIpAddressImpl withGroupExisting(ResourceGroupExtended group) {
			return this.withGroupExisting(group.getName());
		}
		
		@Override
		public PublicIpAddressImpl withTags(Map<String, String> tags) {
			super.withTags(tags);
			return this;
		}


		@Override
		public PublicIpAddressImpl withTag(String name, String value) {
			super.withTag(name, value);
			return this;
		}
		
		
		@Override
		public PublicIpAddressImpl withoutTag(String name) {
			super.withoutTag(name);
			return this;
		}
		

		/************************************************************
		 * Verbs
		 ************************************************************/

		@Override
		public void delete() throws Exception {
			azure.publicIpAddresses().delete(this.id());
		}

		@Override
		public PublicIpAddressImpl provision() throws Exception {
			// Create a group as needed
			ensureGroup(azure);
		
			azure.networkManagementClient().getPublicIpAddressesOperations().createOrUpdate(this.groupName, this.name(), this.inner());
			return get(this.groupName, this.name());
		}
		
		@Override
		public PublicIpAddressImpl refresh() throws Exception {
			this.setInner(getNativeEntity(
					ResourcesImpl.groupFromResourceId(this.id()), 
					ResourcesImpl.nameFromResourceId(this.id())));
			return this;
		}
	}
}
