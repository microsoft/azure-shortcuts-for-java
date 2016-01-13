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

import com.microsoft.azure.management.network.models.IpAllocationMethod;
import com.microsoft.azure.management.network.models.PublicIpAddressDnsSettings;
import com.microsoft.azure.shortcuts.resources.PublicIpAddress;
import com.microsoft.azure.shortcuts.resources.PublicIpAddresses;
import com.microsoft.azure.shortcuts.resources.common.implementation.GroupableResourceBaseImpl;
import com.microsoft.azure.shortcuts.resources.common.implementation.GroupableResourcesBaseImpl;


public class PublicIpAddressesImpl 
	extends GroupableResourcesBaseImpl<
		Azure, 
		PublicIpAddress, 
		com.microsoft.azure.management.network.models.PublicIpAddress,
		PublicIpAddressesImpl.PublicIpAddressImpl>
	implements PublicIpAddresses {
		
	PublicIpAddressesImpl(Azure azure) {
		super(azure);
	}
	
	@Override
	public PublicIpAddressImpl define(String name) throws Exception {
		com.microsoft.azure.management.network.models.PublicIpAddress nativeItem = new com.microsoft.azure.management.network.models.PublicIpAddress();
		nativeItem.setName(name);
		nativeItem.setPublicIpAllocationMethod(IpAllocationMethod.DYNAMIC);
		
		// Assume a public domain name is the same as the resource name by default
		PublicIpAddressDnsSettings dnsSettings = new PublicIpAddressDnsSettings();
		nativeItem.setDnsSettings(dnsSettings);
		dnsSettings.setDomainNameLabel(name.toLowerCase());

		return wrap(nativeItem);
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
	protected PublicIpAddressImpl wrap(com.microsoft.azure.management.network.models.PublicIpAddress nativeItem) {
		return new PublicIpAddressImpl(nativeItem);
	}
	
	
	/***************************************************************
	 * Implements logic for individual resource group
	 ***************************************************************/
	class PublicIpAddressImpl 
		extends 
			GroupableResourceBaseImpl<
				PublicIpAddress, 
				com.microsoft.azure.management.network.models.PublicIpAddress,
				PublicIpAddressImpl>
		implements
			PublicIpAddress,
			PublicIpAddress.DefinitionBlank,
			PublicIpAddress.DefinitionWithGroup,
			PublicIpAddress.DefinitionWithLeafDomainLabel,
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
		
		@Override
		public String leafDomainLabel() {
			if(this.inner().getDnsSettings() == null) {
				return null;
			} else {
				return this.inner().getDnsSettings().getDomainNameLabel();
			}
		}

		
		/**************************************************************
		 * Setters (fluent interface)
		 **************************************************************/

		@Override
		public PublicIpAddressImpl withStaticIp() {
			this.inner().setPublicIpAllocationMethod(IpAllocationMethod.STATIC);
			return this;
		}

		@Override
		public PublicIpAddressImpl withDynamicIp() {
			this.inner().setPublicIpAllocationMethod(IpAllocationMethod.DYNAMIC);
			return this;
		}

		@Override
		public PublicIpAddressImpl withLeafDomainLabel(String dnsName) {
			PublicIpAddressDnsSettings dnsSettings;
			if(dnsName == null) {
				this.inner().setDnsSettings(null);
				return this;
			} else if(null == (dnsSettings = this.inner().getDnsSettings())) {
				dnsSettings = new PublicIpAddressDnsSettings();
			}
			dnsSettings.setDomainNameLabel(dnsName);
			return this;
		}

		@Override
		public DefinitionProvisionable withoutLeafDomainLabel() {
			return this.withLeafDomainLabel(null);
		}
		
		
		/************************************************************
		 * Verbs
		 ************************************************************/

		@Override
		public void delete() throws Exception {
			azure.publicIpAddresses().delete(this.id());
		}

		@Override
		public PublicIpAddress provision() throws Exception {
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
