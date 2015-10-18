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
import java.util.Map;

import com.microsoft.azure.shortcuts.common.implementation.EntitiesImpl;
import com.microsoft.azure.shortcuts.common.implementation.NamedRefreshableImpl;
import com.microsoft.azure.shortcuts.services.creation.CloudServiceDefinitionBlank;
import com.microsoft.azure.shortcuts.services.creation.CloudServiceDefinitionProvisionable;
import com.microsoft.azure.shortcuts.services.listing.CloudServices;
import com.microsoft.azure.shortcuts.services.reading.CloudService;
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
		return super.list(
			getHostedService(this.azure),
			a -> new CloudServiceImpl(a),
			o -> o.getServiceName());
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
	private static ArrayList<HostedService> getHostedService(Azure azure) throws Exception {
		return azure.computeManagementClient().getHostedServicesOperations().list().getHostedServices();
	}
	
	
	/************************************************************
	 * Nested class implementing a cloud service
	 ************************************************************/
	private class CloudServiceImpl 
		extends 
			NamedRefreshableImpl<CloudService>
		implements 
			CloudServiceDefinitionBlank, 
			CloudServiceDefinitionProvisionable,
			CloudService,
			CloudServiceUpdatable {
		
		private HostedService azureService;
		
		private CloudServiceImpl(HostedService azureService) {
			super(azureService.getServiceName().toLowerCase(), true);
			this.azureService = azureService;
		}


		/***********************************************************
		 * Getters
		 * @throws Exception 
		 ***********************************************************/

		public String region() throws Exception {
			return this.azureService.getProperties().getLocation();
		}

		public String description() throws Exception {
			return this.azureService.getProperties().getDescription();
		}

		public String label() throws Exception {
			return this.azureService.getProperties().getLabel();
		}

		public String reverseDnsFqdn() throws Exception {
			return this.azureService.getProperties().getReverseDnsFqdn();
		}

		public Calendar created() throws Exception {
			return this.azureService.getProperties().getDateCreated();
		}

		public Calendar modified() throws Exception {
			return this.azureService.getProperties().getDateLastModified();
		}

		public String affinityGroup() throws Exception {
			return this.azureService.getProperties().getAffinityGroup();
		}


		/**************************************************************
		 * Setters (fluent interface)
		 **************************************************************/

		public CloudServiceImpl withRegion(String region) {
			this.azureService.getProperties().setLocation(region);
			return this;
		}
		
		public CloudServiceImpl withAffinityGroup(String affinityGroup) {
			this.azureService.getProperties().setAffinityGroup(affinityGroup);
			return this;
		}
		
		//TODO withNetwork
		
		public CloudServiceImpl withDescription(String description) {
			this.azureService.getProperties().setDescription(description);
			return this;
		}
		
		public CloudServiceImpl withLabel(String label) {
			this.azureService.getProperties().setLabel(label);
			return this;
		}
		
		public CloudServiceImpl withReverseDnsFqdn(String fqdn) {
			this.azureService.getProperties().setReverseDnsFqdn(fqdn);
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
			this.azureService.getProperties().setDateCreated(props.getDateCreated());
			this.azureService.getProperties().setDateLastModified(props.getDateLastModified());
			return this
				.withAffinityGroup(props.getAffinityGroup())
				.withDescription(props.getDescription())
				.withLabel(props.getLabel())
				.withRegion(props.getLocation())
				.withReverseDnsFqdn(props.getReverseDnsFqdn());
			}
	}
}



