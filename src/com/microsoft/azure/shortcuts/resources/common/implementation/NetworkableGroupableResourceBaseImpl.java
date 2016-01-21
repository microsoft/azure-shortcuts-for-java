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
package com.microsoft.azure.shortcuts.resources.common.implementation;

import com.microsoft.azure.management.network.models.VirtualNetwork;
import com.microsoft.azure.shortcuts.resources.Network;
import com.microsoft.azure.shortcuts.resources.implementation.Azure;

public abstract class NetworkableGroupableResourceBaseImpl<
		T, 
		I extends com.microsoft.windowsazure.core.ResourceBaseExtended, 
		TI extends NetworkableGroupableResourceBaseImpl<T, I, TI>>
	extends
		GroupableResourceBaseImpl<T, I, TI> {

	protected NetworkableGroupableResourceBaseImpl(String id, I innerObject) {
		super(id, innerObject);
	}
	
	private boolean isNetworkExisting;
	private String networkId;
	private String networkCidr;
	protected String subnetId;
	
	final protected Network ensureNetwork(Azure azure) throws Exception {
		if(!this.isNetworkExisting) {
			// Create a new virtual network
			if(this.networkId == null) {
				// Generate a name if needed
				this.networkId = this.name() + "net";
			}
	
			Network network = azure.networks().define(this.networkId)
				.withRegion(this.region())
				.withGroupExisting(groupName)
				.withAddressSpace(this.networkCidr)
				.provision();
			this.isNetworkExisting = true;
			return network;
		} else {
			return azure.networks(this.networkId);
		}
	}

	
	final protected Network.Subnet ensureSubnet(Network network) throws Exception {
		if(network == null) {
			return null;
		} else if(this.subnetId != null) {
			return network.subnets(this.subnetId);
		} else {
			// If no subnet specified, return the first one
			return network.subnets().values().iterator().next();
		}
	}
		

	/***********************************************************
	 * WithNetwork* Implementation
	 ***********************************************************/
	@SuppressWarnings("unchecked")
	public TI withNetworkExisting(String id) {
		this.isNetworkExisting = true;
		this.networkId = id;
		return (TI)this;
	}

	public TI withNetworkExisting(Network network) {
		return this.withNetworkExisting(network.id());
	}

	public TI withNetworkExisting(VirtualNetwork network) {
		return this.withNetworkExisting(network.getId());
	}

	@SuppressWarnings("unchecked")
	public TI withNetworkNew(String name, String addressSpace) {
		this.isNetworkExisting = false;
		this.networkId = name;
		this.networkCidr = addressSpace;
		return (TI) this;
	}

	public TI withNetworkNew(Network.DefinitionProvisionable networkDefinition) throws Exception {
		return this.withNetworkExisting(networkDefinition.provision());
	}

	public TI withNetworkNew(String addressSpace) {
		return this.withNetworkNew((String)null, addressSpace);
	}
	
	
	/********************************************************
	 * WithSubnet implementation
	 ********************************************************/
	@SuppressWarnings("unchecked")
	public TI withSubnet(String subnetId) {
		this.subnetId = subnetId;
		return (TI)this;
	}
}