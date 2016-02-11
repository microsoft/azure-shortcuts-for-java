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

import com.microsoft.azure.management.compute.models.VirtualMachineImageListSkusParameters;
import com.microsoft.azure.management.compute.models.VirtualMachineImageResource;
import com.microsoft.azure.shortcuts.common.implementation.IndexableWrapperImpl;
import com.microsoft.azure.shortcuts.resources.Offer;
import com.microsoft.azure.shortcuts.resources.Publisher;
import com.microsoft.azure.shortcuts.resources.SKU;

class OfferImpl 
	extends IndexableWrapperImpl<com.microsoft.azure.management.compute.models.VirtualMachineImageResource>
	implements Offer {

	private final Publisher publisher;
	private final Azure azure;
	
	OfferImpl(String name, VirtualMachineImageResource innerObject, Publisher publisher, Azure azure) {
		super(name, innerObject);
		this.publisher = publisher;
		this.azure = azure;
	}
	
	@Override
	public String name() {
		return this.inner().getName();
	}
	
	@Override
	public Publisher publisher() {
		return this.publisher;
	}

	@Override
	public Map<String, SKU> skus() throws Exception {
		VirtualMachineImageListSkusParameters params = new VirtualMachineImageListSkusParameters();
		params.setLocation(this.publisher().region().toString());
		params.setPublisherName(this.publisher().name());
		params.setOffer(this.name());
		ArrayList<VirtualMachineImageResource> nativeItems = this.azure.computeManagementClient().getVirtualMachineImagesOperations().listSkus(params).getResources();
		TreeMap<String, SKU> skus = new TreeMap<>();
		for(VirtualMachineImageResource nativeItem : nativeItems) {
			skus.put(nativeItem.getId(), new SKUImpl(nativeItem.getName(), nativeItem, this));
		}
		
		return Collections.unmodifiableMap(skus);
	}	
}
