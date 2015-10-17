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
import java.util.Map;

import com.microsoft.azure.management.compute.models.VirtualMachineSize;
import com.microsoft.azure.shortcuts.common.implementation.EntitiesImpl;
import com.microsoft.azure.shortcuts.common.implementation.NamedImpl;
import com.microsoft.azure.shortcuts.resources.listing.Sizes;
import com.microsoft.azure.shortcuts.resources.reading.Size;

public class SizesImpl 
	extends EntitiesImpl<Azure>
	implements Sizes {

	SizesImpl(Azure azure) {
		super(azure);
	}
	
	@Override
	public Map<String, Size> list() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	
	@Override
	public Map<String, Size> list(String region) throws Exception {
		ArrayList<VirtualMachineSize> items = azure.computeManagementClient().getVirtualMachineSizesOperations().list(region).getVirtualMachineSizes();
		return super.list(
			items, 
			a -> new SizeImpl(a),
			o -> o.getName());
	}


	// Implementation of a Size
	private class SizeImpl
		extends NamedImpl
		implements Size {

		final VirtualMachineSize azureSize;
		
		private SizeImpl(VirtualMachineSize azureSize) {
			super(azureSize.getName());
			this.azureSize = azureSize;
		}

		
		/*************************************
		 * Getters
		 *************************************/
		
		@Override
		public int maxDataDiskCount() {
			return this.azureSize.getMaxDataDiskCount();
		}


		@Override
		public int memoryInMB() {
			return this.azureSize.getMemoryInMB();
		}


		@Override
		public int numberOfCores() {
			return this.azureSize.getNumberOfCores();
		}


		@Override
		public int osDiskSizeInMB() {
			return this.azureSize.getOSDiskSizeInMB();
		}


		@Override
		public int resourceDiskSizeInMB() {
			return this.azureSize.getResourceDiskSizeInMB();
		}
	}
}
