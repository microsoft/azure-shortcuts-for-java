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

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import com.microsoft.azure.management.compute.models.VirtualMachineSize;
import com.microsoft.azure.shortcuts.resources.listing.Sizes;
import com.microsoft.windowsazure.exception.ServiceException;

public class SizesImpl 
	extends EntitiesImpl
	implements Sizes {

	SizesImpl(Azure azure) {
		super(azure);
	}
	
	@Override
	public List<String> list(String region) {
		try {
			ArrayList<VirtualMachineSize> items = azure.computeManagementClient().getVirtualMachineSizesOperations().list(region).getVirtualMachineSizes();
			ArrayList<String> names = new ArrayList<>();
			for(VirtualMachineSize item : items) {
				names.add(item.getName());
			}
			return names;
		} catch (IOException | ServiceException | URISyntaxException e) {
			// Not very actionable, so just return an empty array
			return new ArrayList<>();
		}
	}

	@Override
	public List<String> list() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
}
