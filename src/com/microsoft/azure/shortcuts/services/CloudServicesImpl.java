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
package com.microsoft.azure.shortcuts.services;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.lang3.NotImplementedException;

import com.microsoft.azure.shortcuts.common.implementation.NamedRefreshableImpl;
import com.microsoft.azure.shortcuts.services.creation.CloudServiceDefinitionBlank;
import com.microsoft.azure.shortcuts.services.creation.CloudServiceDefinitionProvisionable;
import com.microsoft.azure.shortcuts.services.listing.CloudServices;
import com.microsoft.azure.shortcuts.services.reading.CloudService;
import com.microsoft.azure.shortcuts.services.updating.CloudServiceUpdatable;
import com.microsoft.azure.shortcuts.services.updating.CloudServiceUpdatableBlank;
import com.microsoft.windowsazure.management.compute.models.HostedServiceCreateParameters;
import com.microsoft.windowsazure.management.compute.models.HostedServiceGetResponse;
import com.microsoft.windowsazure.management.compute.models.HostedServiceUpdateParameters;
import com.microsoft.windowsazure.management.compute.models.HostedServiceListResponse.HostedService;
import com.microsoft.windowsazure.management.compute.models.HostedServiceProperties;

// Class encapsulating the API related to cloud services
public class CloudServicesImpl implements CloudServices {
	
	final Azure azure;
	
	CloudServicesImpl(Azure azure) {
		this.azure = azure;
	}
	

	@Override
	public CloudServiceDefinitionBlank define(String name) {
		return new CloudServiceImpl(name, true);
	}
	
	
	@Override
	public void delete(String name) throws Exception {
		azure.computeManagementClient().getHostedServicesOperations().delete(name);
	}
	
	
	@Override
	public CloudServiceUpdatableBlank update(String name) {
		return new CloudServiceImpl(name, false);
	}
	
	
	@Override
	public List<String> list() {
		try {
			final ArrayList<HostedService> items = azure.computeManagementClient().getHostedServicesOperations()
					.list().getHostedServices();
			ArrayList<String> names = new ArrayList<>();
			for(HostedService item : items) {
				names.add(item.getServiceName());
			}

			return names;
		} catch (Exception e) {
			// Not very actionable, so just return an empty array
			return new ArrayList<String>();
		}
	}
	
	
	@Override
	public CloudService get(String name) throws Exception {
		CloudServiceImpl cloudService = new CloudServiceImpl(name, false);
		return cloudService.refresh();
	}

	
	private class CloudServiceImpl 
		extends NamedRefreshableImpl<CloudService>
		implements 
			CloudServiceDefinitionBlank, 
			CloudServiceDefinitionProvisionable,
			CloudService,
			CloudServiceUpdatable {
		
		private String region, description, affinityGroup, label, reverseDnsFqdn;
		Calendar created, lastModified;
		
		private CloudServiceImpl(String name, boolean initialized) {
			super(name.toLowerCase(), initialized);
		}


		/***********************************************************
		 * Getters
		 * @throws Exception 
		 ***********************************************************/

		public String region() throws Exception {
			ensureInitialized();
			return this.region;
		}

		public String description() throws Exception {
			ensureInitialized();
			return this.description;
		}

		public String label() throws Exception {
			ensureInitialized();
			return this.label;
		}

		public String reverseDnsFqdn() throws Exception {
			ensureInitialized();
			return this.reverseDnsFqdn;
		}

		public Calendar created() throws Exception {
			ensureInitialized();
			return this.created;
		}

		public Calendar modified() throws Exception {
			ensureInitialized();
			return this.lastModified;
		}

		public String affinityGroup() throws Exception {
			ensureInitialized();
			return this.affinityGroup;
		}


		/**************************************************************
		 * Setters (fluent interface)
		 **************************************************************/

		public CloudServiceImpl withRegion(String region) {
			this.region =region;
			return this;
		}
		
		public CloudServiceImpl withAffinityGroup(String affinityGroup) {
			this.affinityGroup = affinityGroup;
			throw new NotImplementedException("withNetwork() not yet implemented");
			//TODO: return this;
		}
		
		public CloudServiceImpl withDescription(String description) {
			this.description = description;
			return this;
		}
		
		public CloudServiceImpl withLabel(String label) {
			this.label = label;
			return this;
		}
		
		public CloudServiceImpl withReverseDnsFqdn(String fqdn) {
			this.reverseDnsFqdn= fqdn;
			return this;
		}
		

		/************************************************************
		 * Verbs
		 ************************************************************/

		@Override
		public void delete() throws Exception {
			azure.cloudServices().delete(this.name);
		}
		
		
		@Override
		public CloudServiceImpl provision() throws Exception {
			final HostedServiceCreateParameters params = new HostedServiceCreateParameters();
			params.setAffinityGroup(this.affinityGroup);
			params.setDescription(this.description);
			params.setLabel((this.label == null) ? this.name : this.label);
			params.setLocation(this.region);
			params.setServiceName(this.name);
			params.setReverseDnsFqdn(this.reverseDnsFqdn);

			azure.computeManagementClient().getHostedServicesOperations().create(params);
			return this;
		}

		
		@Override
		public CloudServiceImpl apply() throws Exception {
			HostedServiceUpdateParameters params = new HostedServiceUpdateParameters();
			params.setDescription(this.description);
			params.setLabel(this.label);
			params.setReverseDnsFqdn(this.reverseDnsFqdn);
			azure.computeManagementClient().getHostedServicesOperations().update(this.name, params);
			return this;
		}


		@Override
		public CloudServiceImpl refresh() throws Exception {
			HostedServiceGetResponse response = azure.computeManagementClient().getHostedServicesOperations().get(this.name);
			HostedServiceProperties props = response.getProperties();
			this.description = props.getDescription();
			this.label = props.getLabel();
			this.region = props.getLocation();
			this.reverseDnsFqdn = props.getReverseDnsFqdn();
			this.created = props.getDateCreated();
			this.lastModified = props.getDateLastModified();
			this.affinityGroup = props.getAffinityGroup();
			this.initialized = true;

			return this;
		}
	}	
}



