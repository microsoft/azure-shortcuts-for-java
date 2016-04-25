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

import com.microsoft.azure.management.network.models.FrontendIpConfiguration;
import com.microsoft.azure.management.network.models.ResourceId;
import com.microsoft.azure.shortcuts.resources.LoadBalancer;
import com.microsoft.azure.shortcuts.resources.PublicIpAddress;

class LoadBalancerImpl 
	extends 
		PublicIpGroupableResourceBaseImpl<
			LoadBalancer, 
			com.microsoft.azure.management.network.models.LoadBalancer,
			LoadBalancerImpl,
			LoadBalancersImpl>
	implements
		LoadBalancer,
		LoadBalancer.Definition {
	
	LoadBalancerImpl(
			com.microsoft.azure.management.network.models.LoadBalancer nativeItem, 
			LoadBalancersImpl collection) {
		super(nativeItem.getName(), nativeItem, collection);
	}
	
	
	/***********************************************************
	 * Getters
	 ***********************************************************/
	
	
	/**************************************************************
	 * Setters (fluent interface)
	 **************************************************************/
	
	/************************************************************
	 * Verbs
	 ************************************************************/
	
	@Override
	public void delete() throws Exception {
		this.subscription().loadBalancers().delete(this.id());
	}
	
	@Override
	public LoadBalancer provision() throws Exception {
		// Create a group as needed
		ensureGroup();
		
		// Create public IP as needed and associate with the first IP config
		PublicIpAddress pip = ensurePublicIpAddress();
		ResourceId r  = new ResourceId();
		r.setId(pip.id());
		FrontendIpConfiguration ipConfig = new FrontendIpConfiguration();
		this.inner().getFrontendIpConfigurations().add(ipConfig);
		ipConfig.setPublicIpAddress(r);
		ipConfig.setName(this.name());
		
		this.subscription().networkManagementClient().getLoadBalancersOperations().createOrUpdate(this.groupName, this.name(), this.inner());
		return this.subscription().loadBalancers().get(this.groupName, this.name());
	}
	
	@Override
	public LoadBalancerImpl refresh() throws Exception {
		this.setInner(this.collection.getNativeEntity(
				ResourcesImpl.groupFromResourceId(this.id()), 
				ResourcesImpl.nameFromResourceId(this.id())));
		return this;
	}
}

