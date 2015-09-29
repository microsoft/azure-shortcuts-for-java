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

import org.apache.commons.lang3.NotImplementedException;

import com.microsoft.azure.shortcuts.services.creation.CloudServiceDefinitionBlank;
import com.microsoft.azure.shortcuts.services.creation.CloudServiceDefinitionProvisionable;
import com.microsoft.azure.shortcuts.services.implementation.NamedImpl;
import com.microsoft.azure.shortcuts.services.implementation.SupportsCreating;
import com.microsoft.azure.shortcuts.services.implementation.SupportsDeleting;
import com.microsoft.azure.shortcuts.services.implementation.SupportsListing;
import com.microsoft.azure.shortcuts.services.implementation.SupportsReading;
import com.microsoft.azure.shortcuts.services.implementation.SupportsUpdating;
import com.microsoft.azure.shortcuts.services.reading.CloudService;
import com.microsoft.azure.shortcuts.services.updating.CloudServiceUpdatable;
import com.microsoft.windowsazure.management.compute.models.HostedServiceCreateParameters;
import com.microsoft.windowsazure.management.compute.models.HostedServiceGetResponse;
import com.microsoft.windowsazure.management.compute.models.HostedServiceUpdateParameters;
import com.microsoft.windowsazure.management.compute.models.HostedServiceListResponse.HostedService;

// Class encapsulating the API related to cloud services
public class CloudServices implements 
	SupportsListing,
	SupportsReading<CloudService>,
	SupportsCreating<CloudServiceDefinitionBlank>,
	SupportsDeleting,
	SupportsUpdating<CloudServiceUpdatable> {
	
	final Azure azure;
	
	CloudServices(Azure azure) {
		this.azure = azure;
	}
	
	
	private class CloudServiceImpl 
		extends NamedImpl 
		implements 
			CloudServiceDefinitionBlank, 
			CloudServiceDefinitionProvisionable,
			CloudService,
			CloudServiceUpdatable {
		
		private String region, description, affinityGroup, label, reverseDnsFqdn;
		Calendar created, lastModified;
		
		private CloudServiceImpl(String name) {
			super(name.toLowerCase());
		}

		// Delete this cloud service
		public void delete() throws Exception {
			azure.cloudServices.delete(this.name);
		}
		
		
		// Provision a new cloud service
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

		
		// Apply updates to the cloud service
		public CloudServiceImpl apply() throws Exception {
			HostedServiceUpdateParameters params = new HostedServiceUpdateParameters();
			params.setDescription(this.description);
			params.setLabel(this.label);
			params.setReverseDnsFqdn(this.reverseDnsFqdn);
			azure.computeManagementClient().getHostedServicesOperations().update(this.name, params);
			return this;
		}

		
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
		
		public String region() {
			return this.region;
		}

		public String description() {
			return this.description;
		}

		public String label() {
			return this.label;
		}

		public String reverseDnsFqdn() {
			return this.reverseDnsFqdn;
		}

		public Calendar created() {
			return this.created;
		}

		public Calendar modified() {
			return this.lastModified;
		}

		public String affinityGroup() {
			return this.affinityGroup;
		}
	}
	
	
	// Starts a new cloud service definition
	public CloudServiceDefinitionBlank define(String name) {
		return new CloudServiceImpl(name);
	}
	
	
	// Deletes the specified cloud service
	public void delete(String name) throws Exception {
		azure.computeManagementClient().getHostedServicesOperations().delete(name);
	}
	
	
	// Starts a cloud service update
	public CloudServiceUpdatable update(String name) {
		return new CloudServiceImpl(name);
	}
	
	
	// Return the list of cloud services
	public String[] list() {
		try {
			final ArrayList<HostedService> services = azure.computeManagementClient().getHostedServicesOperations()
					.list().getHostedServices();
			String[] names = new String[services.size()];
			int i = 0;
			for(HostedService cloudService: services) {
				names[i++]= cloudService.getServiceName();
			}
			return names;
		} catch (Exception e) {
			// Not very actionable, so just return an empty array
			return new String[0];
		}
	}
	
	
	// Return the specified cloud service information
	public CloudService get(String name) throws Exception {
		CloudServiceImpl cloudService = new CloudServiceImpl(name);
		HostedServiceGetResponse response = azure.computeManagementClient().getHostedServicesOperations().get(name);
		cloudService.description = response.getProperties().getDescription();
		cloudService.label = response.getProperties().getLabel();
		cloudService.region = response.getProperties().getLocation();
		cloudService.reverseDnsFqdn = response.getProperties().getReverseDnsFqdn();
		cloudService.created = response.getProperties().getDateCreated();
		cloudService.lastModified = response.getProperties().getDateLastModified();
		cloudService.affinityGroup = response.getProperties().getAffinityGroup();

		return cloudService;
	}
}



