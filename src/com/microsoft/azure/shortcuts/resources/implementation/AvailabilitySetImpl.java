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
import java.util.Collections;
import java.util.List;

import com.microsoft.azure.management.compute.models.VirtualMachineReference;
import com.microsoft.azure.shortcuts.resources.AvailabilitySet;

class AvailabilitySetImpl 
	extends 
		GroupableResourceBaseImpl<
			AvailabilitySet, 
			com.microsoft.azure.management.compute.models.AvailabilitySet,
			AvailabilitySetImpl,
			AvailabilitySetsImpl>
	implements
		AvailabilitySet,
		AvailabilitySet.Definition {
	
	AvailabilitySetImpl(
			com.microsoft.azure.management.compute.models.AvailabilitySet azureAvailabilitySet, 
			AvailabilitySetsImpl collection) {
		super(azureAvailabilitySet.getId(), azureAvailabilitySet, collection);
	}
	
	
	/***********************************************************
	 * Getters
	 ***********************************************************/
	
	@Override
	public List<String> virtualMachineIds() {
		ArrayList<String> ids = new ArrayList<>();
		for(VirtualMachineReference vm : this.inner().getVirtualMachinesReferences()) {
			ids.add(vm.getReferenceUri());
		}
		
		return Collections.unmodifiableList(ids);
	}
	
	
	/**************************************************************
	 * Setters (fluent interface)
	 **************************************************************/
	
	
	/************************************************************
	 * Verbs
	 ************************************************************/
	
	@Override
	public void delete() throws Exception {
		this.subscription().availabilitySets().delete(this.id());
	}
	
	@Override
	public AvailabilitySetImpl refresh() throws Exception {
		this.setInner(this.collection.getNativeEntity(
			ResourcesImpl.groupFromResourceId(this.id()), 
			ResourcesImpl.nameFromResourceId(this.id())));
		return this;
	}
	
	
	@Override
	public AvailabilitySet create() throws Exception {
		ensureGroup(); // Create group if needed
		this.subscription().computeManagementClient().getAvailabilitySetsOperations().createOrUpdate(this.groupName, this.inner());
		return this.subscription().availabilitySets().get(this.groupName, this.name());
	}
}

