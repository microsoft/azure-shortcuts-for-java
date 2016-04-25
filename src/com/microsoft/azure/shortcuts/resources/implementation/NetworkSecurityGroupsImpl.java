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

import java.util.ArrayList;
import java.util.List;

import com.microsoft.azure.management.network.models.SecurityRule;
import com.microsoft.azure.shortcuts.resources.NetworkSecurityGroup;
import com.microsoft.azure.shortcuts.resources.NetworkSecurityGroups;

public class NetworkSecurityGroupsImpl 
	extends GroupableResourcesBaseImpl<
		NetworkSecurityGroup, 
		com.microsoft.azure.management.network.models.NetworkSecurityGroup,
		NetworkSecurityGroupImpl>
	implements NetworkSecurityGroups {
		
	NetworkSecurityGroupsImpl(Subscription subscription) {
		super(subscription);
	}
	
	@Override
	public NetworkSecurityGroupImpl define(String name) throws Exception {
		com.microsoft.azure.management.network.models.NetworkSecurityGroup nativeItem = 
				new com.microsoft.azure.management.network.models.NetworkSecurityGroup();
		nativeItem.setName(name);
		nativeItem.setSecurityRules(new ArrayList<SecurityRule>());
		return wrap(nativeItem);
	}

	@Override
	public void delete(String groupName, String name) throws Exception {
		subscription.networkManagementClient().getNetworkSecurityGroupsOperations().delete(groupName, name);
	}


	/***************************************************
	 * Helpers
	 ***************************************************/
	
	@Override
	protected List<com.microsoft.azure.management.network.models.NetworkSecurityGroup> getNativeEntities(String resourceGroupName) throws Exception {
		if(resourceGroupName == null) {
			return this.subscription.networkManagementClient().getNetworkSecurityGroupsOperations().listAll().getNetworkSecurityGroups();
		} else {
			return this.subscription.networkManagementClient().getNetworkSecurityGroupsOperations().list(resourceGroupName).getNetworkSecurityGroups();
		}
	}
	
	@Override
	protected com.microsoft.azure.management.network.models.NetworkSecurityGroup getNativeEntity(String groupName, String name) throws Exception {
		return subscription.networkManagementClient().getNetworkSecurityGroupsOperations().get(groupName, name).getNetworkSecurityGroup();
	}
	
	@Override
	protected NetworkSecurityGroupImpl wrap(
			com.microsoft.azure.management.network.models.NetworkSecurityGroup nativeItem) {
		return new NetworkSecurityGroupImpl(nativeItem, this);
	}
}
