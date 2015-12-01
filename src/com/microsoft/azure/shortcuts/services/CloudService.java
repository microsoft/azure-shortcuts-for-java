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

import java.util.Calendar;

import com.microsoft.azure.shortcuts.common.Deletable;
import com.microsoft.azure.shortcuts.common.Named;
import com.microsoft.azure.shortcuts.common.Provisionable;
import com.microsoft.azure.shortcuts.common.Refreshable;
import com.microsoft.azure.shortcuts.common.Updatable;
import com.microsoft.azure.shortcuts.common.Wrapper;
import com.microsoft.windowsazure.management.compute.models.HostedServiceListResponse.HostedService;

// Encapsulates the readable properties of a cloud service
public interface CloudService extends 
	Named,
	Refreshable<CloudService>,
	Wrapper<HostedService> {
	
	String region() throws Exception;
	String description() throws Exception;
	String label() throws Exception;
	String reverseDnsFqdn() throws Exception;
	Calendar created() throws Exception;
	Calendar modified() throws Exception;
	String affinityGroup() throws Exception;
	
	
	/**
	 * A cloud service definition requiring a region to be specified
	 */
	public interface WithRegion<T> {
		T withRegion(String region);
		T withRegion(Region region);
	}
	
	/** 
	 * A cloud service definition requiring an affinity group to be specified
	 */
	public interface WithAffinityGroup<T> {
		T withAffinityGroup(String affinityGroup);
	}
	
	/**
	 * A new blank cloud service definition
	 */
	public interface DefinitionBlank extends
		WithRegion<DefinitionProvisionable>,
		WithAffinityGroup<DefinitionProvisionable> {
	}
	
	/** 
	 * A cloud service definition requiring a description to be specified
	 */
	public interface WithDescription<T> {
		public T withDescription(String description);
	}

	/** 
	 * A cloud service definition requiring a label to be specified
	 */
	public interface WithLabel<T> {
		public T withLabel(String label);
	}

	/** 
	 * A cloud service definition requiring a reverse DNS fully qualified domain name to be specified
	 */
	public interface WithReverseDnsFqdn<T> {
		public T withReverseDnsFqdn(String fqdn);
	}

	/**
	 * A new cloud service definition with sufficient settings to be provisioned in the cloud
	 */
	public interface DefinitionProvisionable extends 
		WithDescription<DefinitionProvisionable>,
		WithLabel<DefinitionProvisionable>,
		WithReverseDnsFqdn<DefinitionProvisionable>,
		Provisionable<UpdateBlank> {
	}

	/**
	 * An existing cloud service update request ready to applied in the cloud
	 */
	public interface Update extends UpdateBlank, Updatable<Update> {
	}
	
	
	/**
	 * A blank existing cloud service update request
	 */
	public interface UpdateBlank extends 
		WithDescription<Update>,
		WithLabel<Update>,
		WithReverseDnsFqdn<Update>,
		Deletable {
	}
}
