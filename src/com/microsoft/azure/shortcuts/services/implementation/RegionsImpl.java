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
package com.microsoft.azure.shortcuts.services.implementation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import com.microsoft.azure.shortcuts.common.implementation.EntitiesImpl;
import com.microsoft.azure.shortcuts.common.implementation.IndexableRefreshableWrapperImpl;
import com.microsoft.azure.shortcuts.services.Region;
import com.microsoft.azure.shortcuts.services.Regions;
import com.microsoft.windowsazure.management.models.LocationsListResponse.Location;

// Class encapsulating the API related to locations
public class RegionsImpl 
	extends EntitiesImpl<Azure>
	implements Regions {
	
	RegionsImpl(Azure azure) {
		super(azure);
	}
	
	@Override
	public Map<String, Region> list() throws Exception {
		return this.list(null);
	}
	
	@Override
	public Map<String, Region> list(String serviceType) {
		HashMap<String, Region> wrappers = new HashMap<>();
		try {
			for(Location nativeItem : getAzureLocations()) {
				if(serviceType == null || nativeItem.getAvailableServices().contains(serviceType)) {
					wrappers.put(nativeItem.getName(), new RegionImpl(nativeItem));
				}
			}
			
			return Collections.unmodifiableMap(wrappers);
		} catch (Exception e) {
			return Collections.unmodifiableMap(new HashMap<String, Region>());
		}
		
	}

	@Override
	public Region get(String name) throws Exception {
		return createRegion(name).refresh();
	}

	
	/***************************************************
	 * Helpers
	 ***************************************************/
	
	private List<Location> getAzureLocations() throws Exception {
		return azure.managementClient().getLocationsOperations().list().getLocations();
	}
	
	private RegionImpl createRegion(String name) {
		Location azureLocation = new Location();
		azureLocation.setName(name);
		return new RegionImpl(azureLocation);
	}
	
	
	/***************************************************
	 * Implementation of an individual region
	 ***************************************************/
	
	private class RegionImpl
		extends IndexableRefreshableWrapperImpl<Region, Location>
		implements Region {
		
		private RegionImpl(Location azureLocation) {
			super(azureLocation.getName(), azureLocation);
		}

		
		/**************************************************
		 * Getters
		 **************************************************/
		
		@Override
		public String displayName() throws Exception {
			return this.inner().getDisplayName();
		}
		
		@Override
		public List<String> availableVirtualMachineSizes() throws Exception {
			return Collections.unmodifiableList(this.inner().getComputeCapabilities().getVirtualMachinesRoleSizes());
		}

		@Override
		public List<String> availableWebWorkerRoleSizes() throws Exception {
			return Collections.unmodifiableList(this.inner().getComputeCapabilities().getWebWorkerRoleSizes());
		}
		
		@Override
		public List<String> availableServices() throws Exception {
			return Collections.unmodifiableList(this.inner().getAvailableServices());
		}
		
		@Override
		public List<String> availableStorageAccountTypes() throws Exception {
			return Collections.unmodifiableList(this.inner().getStorageCapabilities().getStorageAccountTypes());
		}

		
		/***************************************************
		 * Verbs
		 ***************************************************/
		
		@Override
		public Region refresh() throws Exception {
			ArrayList<Location> azureLocations = azure.managementClient().getLocationsOperations().list().getLocations();
			for(Location azureLocation : azureLocations) {
				if(azureLocation.getName().equals(this.inner().getName())) {
					this.setInner(azureLocation);
					return this;
				}
			}
			throw new NoSuchElementException(String.format("Region '%s' not found.", this.inner().getName()));
		}
	}
}
