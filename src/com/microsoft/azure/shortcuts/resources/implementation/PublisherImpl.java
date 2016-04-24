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
import java.util.Map;
import java.util.TreeMap;

import com.microsoft.azure.management.compute.models.VirtualMachineImageListOffersParameters;
import com.microsoft.azure.management.compute.models.VirtualMachineImageResource;
import com.microsoft.azure.shortcuts.common.implementation.IndexableWrapperImpl;
import com.microsoft.azure.shortcuts.resources.Offer;
import com.microsoft.azure.shortcuts.resources.Publisher;
import com.microsoft.azure.shortcuts.resources.Region;

// Implements logic for individual provider
class PublisherImpl
	extends
		IndexableWrapperImpl<VirtualMachineImageResource>
	implements 
		Publisher {
	
	private final ArmEntitiesImpl collection;
	
	private PublisherImpl(VirtualMachineImageResource publisher, ArmEntitiesImpl parentCollection) {
		super(publisher.getId(), publisher);
		this.collection = parentCollection;
	}
	
	static PublisherImpl wrap(VirtualMachineImageResource publisher, ArmEntitiesImpl parentCollection) {
		return new PublisherImpl(publisher, parentCollection);
	}

	/***********************************************************
	 * Getters
	 ***********************************************************/		

	@Override
	public String name() {
		return this.inner().getName();
	}

	@Override
	public Region region() {
		return Region.fromName(this.inner().getLocation());
	}

	@Override
	public Map<String, Offer> offers() throws Exception {
		VirtualMachineImageListOffersParameters params = new VirtualMachineImageListOffersParameters();
		params.setLocation(this.region().toString());
		params.setPublisherName(this.name());
		ArrayList<VirtualMachineImageResource> nativeItems = collection.subscription().computeManagementClient().getVirtualMachineImagesOperations().listOffers(params).getResources();
		TreeMap<String, Offer> offers = new TreeMap<>();
		for(VirtualMachineImageResource nativeItem : nativeItems) {
			offers.put(nativeItem.getId(), new OfferImpl(nativeItem.getName(), nativeItem, this, collection.subscription()));
		}
		
		return Collections.unmodifiableMap(offers);
	}


	/************************************************************
	 * Verbs
	 ************************************************************/
}

