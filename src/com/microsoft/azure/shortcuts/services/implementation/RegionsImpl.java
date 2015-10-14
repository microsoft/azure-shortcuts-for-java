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
import java.util.List;

import com.microsoft.azure.shortcuts.services.listing.Regions;
import com.microsoft.windowsazure.management.models.LocationsListResponse.Location;

// Class encapsulating the API related to locations
public class RegionsImpl implements Regions {
	
	final Azure azure;
	RegionsImpl(Azure azure) {
		this.azure = azure;
	}
	
	@Override
	public List<String> list(String serviceType) {
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
	// Lists all regions
	public List<String> list() {
		return list(null);
	}
}
