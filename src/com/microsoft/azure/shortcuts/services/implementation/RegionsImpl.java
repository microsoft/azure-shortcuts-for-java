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
import java.util.List;
import java.util.NoSuchElementException;

import com.microsoft.azure.shortcuts.common.implementation.EntitiesImpl;
import com.microsoft.azure.shortcuts.common.implementation.NamedRefreshableImpl;
import com.microsoft.azure.shortcuts.services.listing.Regions;
import com.microsoft.azure.shortcuts.services.reading.Region;
import com.microsoft.windowsazure.management.models.LocationsListResponse.Location;

// Class encapsulating the API related to locations
public class RegionsImpl 
	extends EntitiesImpl<Azure>
	implements Regions {
	
	RegionsImpl(Azure azure) {
		super(azure);
	}
	
	@Override
	public List<String> names(String serviceType) {
		try {
			ArrayList<Location> items = azure.managementClient().getLocationsOperations().list().getLocations();
			ArrayList<String> names = new ArrayList<>();
			for(Location item : items) {
				names.add(item.getName());
			}
			return names;
		} catch (Exception e) {
			// Not very actionable, so just return an empty array
			return new ArrayList<>();
		}
	}

	@Override
	public List<String> names() {
		return names(null);
	}
	
	
	@Override
	public Region get(String name) throws Exception {
		return createRegion(name).refresh();
	}

	
	/***************************************************
	 * Helpers
	 ***************************************************/
	
	private RegionImpl createRegion(String name) {
		Location azureLocation = new Location();
		azureLocation.setName(name);
		return new RegionImpl(azureLocation);
	}
	
	
	/***************************************************
	 * Implementation of an individual region
	 ***************************************************/
	
	private class RegionImpl
		extends NamedRefreshableImpl<Region>
		implements Region {
		
		private Location azureLocation;
		
		private RegionImpl(Location azureLocation) {
			super(azureLocation.getName());
			this.azureLocation =  azureLocation;
		}

		
		/**************************************************
		 * Getters
		 **************************************************/
		
		@Override
		public String displayName() throws Exception {
			return this.azureLocation.getDisplayName();
		}
		
		@Override
		public List<String> availableVirtualMachineSizes() throws Exception {
			return Collections.unmodifiableList(this.azureLocation.getComputeCapabilities().getVirtualMachinesRoleSizes());
		}

		@Override
		public List<String> availableWebWorkerRoleSizes() throws Exception {
			return Collections.unmodifiableList(this.azureLocation.getComputeCapabilities().getWebWorkerRoleSizes());
		}
		
		@Override
		public List<String> availableServices() throws Exception {
			return Collections.unmodifiableList(this.azureLocation.getAvailableServices());
		}
		
		@Override
		public List<String> availableStorageAccountTypes() throws Exception {
			return Collections.unmodifiableList(this.azureLocation.getStorageCapabilities().getStorageAccountTypes());
		}

		
		/***************************************************
		 * Verbs
		 ***************************************************/
		
		@Override
		public Region refresh() throws Exception {
			ArrayList<Location> azureLocations = azure.managementClient().getLocationsOperations().list().getLocations();
			for(Location azureLocation : azureLocations) {
				if(azureLocation.getName().equals(this.azureLocation.getName())) {
					this.azureLocation = azureLocation;
					return this;
				}
			}
			throw new NoSuchElementException(String.format("Region '%s' not found.", this.azureLocation.getName()));
		}
	}
}
