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
import java.util.Arrays;
import java.util.List;

import com.microsoft.azure.management.network.models.NetworkInterfaceIpConfiguration;
import com.microsoft.azure.shortcuts.resources.NetworkInterface;
import com.microsoft.azure.shortcuts.resources.NetworkInterfaces;

public class NetworkInterfacesImpl 
	extends GroupableResourcesBaseImpl<
		NetworkInterface, 
		com.microsoft.azure.management.network.models.NetworkInterface,
		NetworkInterfaceImpl>
	implements NetworkInterfaces {
		
	NetworkInterfacesImpl(Subscription subscription) {
		super(subscription);
	}
	
	@Override
	public NetworkInterfaceImpl define(String name) throws Exception {
		com.microsoft.azure.management.network.models.NetworkInterface azureNetworkInterface = new com.microsoft.azure.management.network.models.NetworkInterface();
		azureNetworkInterface.setName(name);
		
		// Default IP configs, creating Primary by default
		azureNetworkInterface.setIpConfigurations(new ArrayList<NetworkInterfaceIpConfiguration>(Arrays.asList(new NetworkInterfaceIpConfiguration())));
		
		// TODO Min settings
		
		return wrap(azureNetworkInterface);
	}

	@Override
	public void delete(String groupName, String name) throws Exception {
		subscription.networkManagementClient().getNetworkInterfacesOperations().delete(groupName, name);
	}


	/***************************************************
	 * Helpers
	 ***************************************************/
	
	@Override
	protected List<com.microsoft.azure.management.network.models.NetworkInterface> getNativeEntities(String groupName) throws Exception {
		if(groupName == null) {
			return this.subscription.networkManagementClient().getNetworkInterfacesOperations().listAll().getNetworkInterfaces();
		} else {
			return this.subscription.networkManagementClient().getNetworkInterfacesOperations().list(groupName).getNetworkInterfaces();
		}
	}
	
	@Override
	protected com.microsoft.azure.management.network.models.NetworkInterface getNativeEntity(String groupName, String name) throws Exception {
		return subscription.networkManagementClient().getNetworkInterfacesOperations().get(groupName, name).getNetworkInterface();
	}
	
	@Override
	protected NetworkInterfaceImpl wrap(com.microsoft.azure.management.network.models.NetworkInterface nativeItem) {
		return new NetworkInterfaceImpl(nativeItem, this);
	}
}
