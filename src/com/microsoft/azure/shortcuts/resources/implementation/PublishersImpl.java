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
import java.util.NoSuchElementException;
import java.util.TreeMap;

import com.microsoft.azure.management.compute.models.VirtualMachineImageListPublishersParameters;
import com.microsoft.azure.management.compute.models.VirtualMachineImageResource;
import com.microsoft.azure.shortcuts.common.implementation.IndexableWrapperImpl;
import com.microsoft.azure.shortcuts.resources.Publisher;
import com.microsoft.azure.shortcuts.resources.Publishers;
import com.microsoft.azure.shortcuts.resources.Region;
import com.microsoft.azure.shortcuts.resources.common.implementation.ArmEntitiesImpl;

public class PublishersImpl
	extends ArmEntitiesImpl
	implements Publishers {
	
	PublishersImpl(Azure azure) {
		super(azure);
	}
	
	
	// Get providers from Azure
	private ArrayList<VirtualMachineImageResource> getNativeEntities(String region) throws Exception {
		VirtualMachineImageListPublishersParameters params = new VirtualMachineImageListPublishersParameters();
		params.setLocation(region);
		return this.azure().computeManagementClient().getVirtualMachineImagesOperations().listPublishers(params).getResources();
	}
	
	// Get the location from the resource id
	private String locationFromId(String id) {
		String[] parts = id.split("/");
		for(int i = 0; i<parts.length; i++) {
			if(parts[i].equalsIgnoreCase("Locations")) {
				return parts[i+1];
			}
		}
		
		return null;
	}
	
	
	@Override
	public Map<String, Publisher> list(Region region) throws Exception {
		TreeMap<String, Publisher> wrappers = new TreeMap<>();
		for(VirtualMachineImageResource nativeItem : getNativeEntities(region.toString())) {
			PublisherImpl wrapper = new PublisherImpl(nativeItem, this);
			wrappers.put(nativeItem.getId(), wrapper);
		}

		return Collections.unmodifiableMap(wrappers);
	}

	
	
	@Override
	public Publisher get(String id) throws Exception {
		return get(Region.fromName(locationFromId(id)), id);
	}

	@Override
	public Publisher get(Region region, String name) throws Exception {
		VirtualMachineImageListPublishersParameters params = new VirtualMachineImageListPublishersParameters();
		params.setLocation(region.toString());
		for(VirtualMachineImageResource nativeItem : azure.computeManagementClient().getVirtualMachineImagesOperations().listPublishers(params).getResources()) {
			if(nativeItem.getId().equalsIgnoreCase(name)) {
				return new PublisherImpl(nativeItem, this);
			} else if(nativeItem.getName().equalsIgnoreCase(name)) {
				return new PublisherImpl(nativeItem, this);
			}
		}

		throw new NoSuchElementException("Publisher not found.");		
	}

	// Implements logic for individual provider
	private class PublisherImpl
		extends
			IndexableWrapperImpl<com.microsoft.azure.management.compute.models.VirtualMachineImageResource>
		implements 
			Publisher {
		
		private final ArmEntitiesImpl collection;
		
		private PublisherImpl(com.microsoft.azure.management.compute.models.VirtualMachineImageResource publisher, ArmEntitiesImpl collection) {
			super(publisher.getId(), publisher);
			this.collection = collection;
		}

		@Override
		public String name() {
			return this.inner().getName();
		}

		@Override
		public Region region() {
			return Region.fromName(this.inner().getLocation());
		}


		/***********************************************************
		 * Getters
		 ***********************************************************/		

		/************************************************************
		 * Verbs
		 ************************************************************/

	}
}
