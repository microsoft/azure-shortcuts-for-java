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
package com.microsoft.azure.shortcuts.resources;

import com.microsoft.azure.shortcuts.common.Deletable;
import com.microsoft.azure.shortcuts.common.Indexable;
import com.microsoft.azure.shortcuts.common.Provisionable;
import com.microsoft.azure.shortcuts.common.Refreshable;
import com.microsoft.azure.shortcuts.common.Wrapper;
import com.microsoft.azure.shortcuts.resources.common.GroupResourceBase;

public interface PublicIpAddress extends 
	Indexable,
	GroupResourceBase,
	Refreshable<PublicIpAddress>,
	Wrapper<com.microsoft.azure.management.network.models.PublicIpAddress>,
	Deletable {
	
	/**
	 * @return The assigned public IP address
	 */
	String ipAddress();
	String leafDomainLabel();

	/**
	 * A new blank public IP address definition
	 */
	public interface DefinitionBlank extends 
		GroupResourceBase.DefinitionWithRegion<DefinitionWithGroup> {}
	
	/**
	 * A public IP address definition allowing to specify the resource group to include it in.
	 */
	public interface DefinitionWithGroup extends
		GroupResourceBase.DefinitionWithGroup<DefinitionProvisionable> {}
	
	/**
	 * A public IP address definition allowing to specify the IP address allocation method and a static IP address, if needed
	 */
	public interface DefinitionWithIpAddress {
		/**
		 * Enables static IP address allocation. The actual IP address allocated for this resource by Azure can be obtained 
		 * after the provisioning process is complete from ipAddress().
		 * @return The next stage of the public IP address definition
		 */
		DefinitionProvisionable withStaticIp();
		
		/**
		 * Enables dynamic IP address allocation.
		 * @return The next stage of the public IP address definition
		 */
		DefinitionProvisionable withDynamicIp();
	}

	/**
	 * A public IP address definition allowing to specify the leaf domain label, if any
	 */
	public interface DefinitionWithLeafDomainLabel {
		/**
		 * Specifies the leaf domain label to associate with this public IP address. The fully qualified domain name (FQDN) 
		 * will be constructed automatically by appending the rest of the domain to this label.
		 * @param dnsName The leaf domain label to use. This must follow the required naming convention for leaf domain names.
		 * @return The next stage of the public IP address definition
		 */
		DefinitionProvisionable withLeafDomainLabel(String dnsName);
		
		/**
		 * Ensures that no leaf domain label will be used. This means that this public IP address will not be associated with a domain name.
		 * @return The next stage of the public IP address definition
		 */
		DefinitionProvisionable withoutLeafDomainLabel();
	}
	
	/**
	 * A public IP address definition with sufficient input parameters specified to be provisioned in the cloud
	 */
	public interface DefinitionProvisionable extends 
		Provisionable<PublicIpAddress>,
		GroupResourceBase.DefinitionWithTags<DefinitionProvisionable>,
		DefinitionWithLeafDomainLabel,
		DefinitionWithIpAddress {
	}

}
