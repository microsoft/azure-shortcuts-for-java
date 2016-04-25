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

import com.microsoft.azure.shortcuts.resources.AvailabilitySet;
import com.microsoft.azure.shortcuts.resources.AvailabilitySets;


public class AvailabilitySetsImpl 
	extends GroupableResourcesBaseImpl<
		AvailabilitySet,
		com.microsoft.azure.management.compute.models.AvailabilitySet,
		AvailabilitySetImpl>
	implements AvailabilitySets {
	
	AvailabilitySetsImpl(Subscription subscription) {
		super(subscription);
	}
	
	@Override
	public void delete(String groupName, String name) throws Exception {
		subscription.computeManagementClient().getAvailabilitySetsOperations().delete(groupName, name);
	}
	
	@Override
	public AvailabilitySetImpl define(String name) {
		com.microsoft.azure.management.compute.models.AvailabilitySet nativeItem = new com.microsoft.azure.management.compute.models.AvailabilitySet();
		nativeItem.setName(name);
		return wrap(nativeItem);
	}

	
	/***************************************************
	 * Helpers
	 ***************************************************/
	
	@Override
	protected ArrayList<com.microsoft.azure.management.compute.models.AvailabilitySet> getNativeEntities(String resourceGroupName) throws Exception {
		// TODO What if null?
		return this.subscription.computeManagementClient().getAvailabilitySetsOperations().list(resourceGroupName).getAvailabilitySets();
	}
	
	@Override
	com.microsoft.azure.management.compute.models.AvailabilitySet getNativeEntity(String groupName, String name) throws Exception {
		return subscription.computeManagementClient().getAvailabilitySetsOperations().get(groupName, name).getAvailabilitySet();
	}
	
	@Override
	protected AvailabilitySetImpl wrap(com.microsoft.azure.management.compute.models.AvailabilitySet nativeItem) {
		return new AvailabilitySetImpl(nativeItem, this);
	}	
}
