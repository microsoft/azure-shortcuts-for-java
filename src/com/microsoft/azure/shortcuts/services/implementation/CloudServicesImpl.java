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
package com.microsoft.azure.shortcuts.services.implementation;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.microsoft.azure.shortcuts.common.implementation.EntitiesImpl;
import com.microsoft.azure.shortcuts.common.implementation.NamedRefreshableWrapperImpl;
import com.microsoft.azure.shortcuts.services.creation.CloudServiceDefinitionBlank;
import com.microsoft.azure.shortcuts.services.creation.CloudServiceDefinitionProvisionable;
import com.microsoft.azure.shortcuts.services.listing.CloudServices;
import com.microsoft.azure.shortcuts.services.reading.CloudService;
import com.microsoft.azure.shortcuts.services.reading.Region;
import com.microsoft.azure.shortcuts.services.updating.CloudServiceUpdatable;
import com.microsoft.windowsazure.management.compute.models.ComputeCapabilities;
import com.microsoft.windowsazure.management.compute.models.HostedServiceCreateParameters;
import com.microsoft.windowsazure.management.compute.models.HostedServiceUpdateParameters;
import com.microsoft.windowsazure.management.compute.models.HostedServiceListResponse.HostedService;
import com.microsoft.windowsazure.management.compute.models.HostedServiceProperties;

// Class encapsulating the API related to cloud services
public class CloudServicesImpl 
	extends EntitiesImpl<Azure>
	implements CloudServices {
	
	CloudServicesImpl(Azure azure) {
		super(azure);
	}
	

	@Override
	public CloudServiceImpl define(String name) {
		return createCloudService(name);
	}
	
	
	@Override
	public void delete(String name) throws Exception {
		azure.computeManagementClient().getHostedServicesOperations().delete(name);
	}
	
	
	@Override
	public CloudServiceImpl update(String name) {
		return createCloudService(name);
	}
	
	
	@Override
	public CloudService get(String name) throws Exception {
		return createCloudService(name).refresh();
	}

	
	@Override
	public Map<String, CloudService> list() throws Exception {
		HashMap<String, CloudService> wrappers = new HashMap<>();
		for(HostedService nativeItem : getHostedServices()) {
			CloudServiceImpl wrapper = new CloudServiceImpl(nativeItem);
			wrappers.put(nativeItem.getServiceName(), wrapper);
		}
		
		return Collections.unmodifiableMap(wrappers);
	}	

	
	// Helper to create a blank hosted service
	private CloudServiceImpl createCloudService(String name) {
		HostedService azureService = new HostedService();
		azureService.setServiceName(name);
		azureService.setProperties(new HostedServiceProperties());
		azureService.setComputeCapabilities(new ComputeCapabilities());
		return new CloudServiceImpl(azureService);
	}
	
	
	// Helper to return list of hosted services
	private ArrayList<HostedService> getHostedServices() throws Exception {
		return this.azure.computeManagementClient().getHostedServicesOperations().list().getHostedServices();
	}
	
	
	/************************************************************
	 * Nested class implementing a cloud service
	 ************************************************************/
	private class CloudServiceImpl 
		extends 
			NamedRefreshableWrapperImpl<CloudService, HostedService>
		implements 
			CloudServiceDefinitionBlank, 
			CloudServiceDefinitionProvisionable,
			CloudService,
			CloudServiceUpdatable {
		
		private CloudServiceImpl(HostedService azureService) {
			super(azureService.getServiceName().toLowerCase(), azureService);
		}


		/***********************************************************
		 * Getters
		 * @throws Exception 
		 ***********************************************************/

		@Override
		public String region() throws Exception {
			return this.inner().getProperties().getLocation();
		}

		@Override
		public String description() throws Exception {
			return this.inner().getProperties().getDescription();
		}

		@Override
		public String label() throws Exception {
			return this.inner().getProperties().getLabel();
		}

		@Override
		public String reverseDnsFqdn() throws Exception {
			return this.inner().getProperties().getReverseDnsFqdn();
		}

		@Override
		public Calendar created() throws Exception {
			return this.inner().getProperties().getDateCreated();
		}

		@Override
		public Calendar modified() throws Exception {
			return this.inner().getProperties().getDateLastModified();
		}

		@Override
		public String affinityGroup() throws Exception {
			return this.inner().getProperties().getAffinityGroup();
		}


		/**************************************************************
		 * Setters (fluent interface)
		 **************************************************************/

		@Override
		public CloudServiceImpl withRegion(String region) {
			this.inner().getProperties().setLocation(region);
			return this;
		}
		
		@Override
		public CloudServiceImpl withRegion(Region region) {
			return this.withRegion(region.name());
		}

		@Override
		public CloudServiceImpl withAffinityGroup(String affinityGroup) {
			this.inner().getProperties().setAffinityGroup(affinityGroup);
			return this;
		}
		
		//TODO withNetwork
		
		@Override
		public CloudServiceImpl withDescription(String description) {
			this.inner().getProperties().setDescription(description);
			return this;
		}
		
		@Override
		public CloudServiceImpl withLabel(String label) {
			this.inner().getProperties().setLabel(label);
			return this;
		}
		
		@Override
		public CloudServiceImpl withReverseDnsFqdn(String fqdn) {
			this.inner().getProperties().setReverseDnsFqdn(fqdn);
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
			params.setAffinityGroup(this.affinityGroup());
			params.setDescription(this.description());
			params.setLabel((this.label() == null) ? this.name() : this.label());
			params.setLocation(this.region());
			params.setServiceName(this.name());
			params.setReverseDnsFqdn(this.reverseDnsFqdn());

			azure.computeManagementClient().getHostedServicesOperations().create(params);			
			return this;
		}

		
		@Override
		public CloudServiceImpl apply() throws Exception {
			HostedServiceUpdateParameters params = new HostedServiceUpdateParameters();
			params.setDescription(this.description());
			params.setLabel(this.label());
			params.setReverseDnsFqdn(this.reverseDnsFqdn());
			azure.computeManagementClient().getHostedServicesOperations().update(this.name, params);
			return this;
		}


		@Override
		public CloudServiceImpl refresh() throws Exception {
			HostedServiceProperties props = azure.computeManagementClient().getHostedServicesOperations().get(this.name()).getProperties();
			this.inner().getProperties().setDateCreated(props.getDateCreated());
			this.inner().getProperties().setDateLastModified(props.getDateLastModified());
			return this
				.withAffinityGroup(props.getAffinityGroup())
				.withDescription(props.getDescription())
				.withLabel(props.getLabel())
				.withRegion(props.getLocation())
				.withReverseDnsFqdn(props.getReverseDnsFqdn());
			}
	}
}



