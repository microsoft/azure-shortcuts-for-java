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

import java.util.List;
import java.util.Map;

import com.microsoft.azure.management.network.models.VirtualNetwork;
import com.microsoft.azure.shortcuts.common.Deletable;
import com.microsoft.azure.shortcuts.common.Indexable;
import com.microsoft.azure.shortcuts.common.Provisionable;
import com.microsoft.azure.shortcuts.common.Refreshable;
import com.microsoft.azure.shortcuts.common.Wrapper;
import com.microsoft.azure.shortcuts.resources.common.GroupResourceBase;

public interface Network extends 
	Indexable,
	GroupResourceBase,
	Refreshable<Network>,
	Wrapper<VirtualNetwork>,
	Deletable {
	
	String provisioningState();
	List<String> addressSpaces();
	List<String> dnsServerIPs();
	Map<String, Subnet> subnets();
	Subnet subnets(String id);
	
	/**
	 * A new blank virtual network definition
	 */
	public interface DefinitionBlank extends 
		GroupResourceBase.DefinitionWithRegion<DefinitionWithGroup> { }
	
	public interface DefinitionWithGroup extends
		GroupResourceBase.DefinitionWithGroup<DefinitionProvisionable> {}
		
	
	/**
	 * A virtual network definition expecting at least one subnet to be specified
	 */
	public interface DefinitionWithSubnet {
		DefinitionProvisionableWithSubnet withSubnet(String name, String cidr);
		DefinitionProvisionableWithSubnet withSubnets(Map<String, String> nameCidrPairs);
	}

	/**
	 * A virtual network definition expecting the network's address space to be specified
	 */
	public interface DefinitionWithAddressSpace {
		DefinitionProvisionableWithSubnet withAddressSpace(String cidr);
	}
	
	/**
	 * A virtual network definition expecting the IP address of an existing DNS server to be associated with the network 
	 */
	public interface DefinitionWithDNSServer {
		DefinitionProvisionable withDnsServer(String ipAddress);
	}
	
	/**
	 * A new virtual network definition with sufficient input parameters specified to be provisioned in the cloud
	 */
	public interface DefinitionProvisionable extends 
		Provisionable<Network>,
		DefinitionWithAddressSpace,
		DefinitionWithDNSServer,
		GroupResourceBase.DefinitionWithTags<DefinitionProvisionable> {
	}
	
	public interface DefinitionProvisionableWithSubnet extends 
		DefinitionProvisionable,
		DefinitionWithSubnet { 
	}
	
	
	public interface Subnet extends Indexable, Wrapper<com.microsoft.azure.management.network.models.Subnet> {
		String addressPrefix();
		String networkSecurityGroup();
	}
}
