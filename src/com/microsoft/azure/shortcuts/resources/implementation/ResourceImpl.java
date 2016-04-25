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

import com.microsoft.azure.management.resources.models.GenericResourceExtended;
import com.microsoft.azure.shortcuts.resources.Resource;
import com.microsoft.windowsazure.core.ResourceIdentity;

class ResourceImpl 
	extends
		GroupableResourceBaseImpl<
			Resource, 
			GenericResourceExtended,
			ResourceImpl,
			ResourcesImpl>
	implements 
		Resource {
	
	ResourceImpl(GenericResourceExtended azureResource, ResourcesImpl collection) {
		super(azureResource.getId(), azureResource, collection);
	}
	
	
	/***********************************************************
	 * Getters
	 ***********************************************************/
	
	@Override
	public String provider() throws Exception {
		return ResourcesImpl.RESOURCE_ID.PROVIDER.from(this.inner().getId());
	}
	
	@Override
	public String properties() throws Exception {
		return this.inner().getProperties();
	}
	
	@Override
	public String provisioningState() throws Exception {
		return this.inner().getProvisioningState();
	}
	
	/************************************************************
	 * Verbs
	 ************************************************************/
	
	@Override
	public void delete() throws Exception {
		this.subscription().resources().delete(this.id);
	}
	
	
	@Override
	public ResourceImpl refresh() throws Exception {
		return refresh(
			ResourcesImpl.RESOURCE_ID.GROUP.from(this.id),
			this.collection.createResourceIdentity(this.id));
	}
	
	
	// Refreshes the resource based on the group and identity information
	private ResourceImpl refresh(String group, ResourceIdentity identity) throws Exception {
		this.setInner(this.subscription().resourceManagementClient().getResourcesOperations().get(group, identity).getResource());
		return this;
	}
}
