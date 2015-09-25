package com.microsoft.azure.shortcuts;

import java.util.ArrayList;

import com.microsoft.azure.shortcuts.implementation.SupportsListing;
import com.microsoft.windowsazure.management.models.LocationsListResponse.Location;

// Class encapsulating the API related to locations
class Regions implements 
	SupportsListing {
	
	final Azure azure;
	Regions(Azure azure) {
		this.azure = azure;
	}
	
	// Return the list of available region names supporting the specified service type, 
	// which must be one of the constants from the LocationAvailableServiceNames class, or all if null
	public String[] list(String serviceType) {
		try {
			ArrayList<Location> locations = azure.management.getLocationsOperations().list().getLocations();
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
