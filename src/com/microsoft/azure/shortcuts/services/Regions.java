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
package com.microsoft.azure.shortcuts.services;

import java.util.ArrayList;

import com.microsoft.azure.shortcuts.services.implementation.SupportsListing;
import com.microsoft.windowsazure.management.models.LocationsListResponse.Location;

// Class encapsulating the API related to locations
public class Regions implements 
	SupportsListing {
	
	final Azure azure;
	Regions(Azure azure) {
		this.azure = azure;
	}
	
	// Return the list of available region names supporting the specified service type, 
	// which must be one of the constants from the LocationAvailableServiceNames class, or all if null
	public String[] list(String serviceType) {
		try {
			ArrayList<Location> locations = azure.managementClient().getLocationsOperations().list().getLocations();
			String[] names = new String[locations.size()];
			int i=0;
			for(Location location : locations) {
				if(null == serviceType || location.getAvailableServices().contains(serviceType)) {
					names[i++] = location.getName();
				}
			}
			return names;
		} catch (Exception e) {
			// Not very actionable, so just return an empty array
			return new String[0];
		}
	}

	// Lists all regions
	public String[] list() {
		return list(null);
	}
}
