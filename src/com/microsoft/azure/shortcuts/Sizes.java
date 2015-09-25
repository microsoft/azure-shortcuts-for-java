package com.microsoft.azure.shortcuts;

import java.util.ArrayList;

import com.microsoft.azure.shortcuts.implementation.SupportsListing;
import com.microsoft.windowsazure.management.models.RoleSizeListResponse.RoleSize;

// Encapsulates the API related to VM sizes
class Sizes implements 
	SupportsListing {
	
	final Azure azure;
	Sizes(Azure azure) {
		this.azure = azure;
	}
	
	// Return the list of available size names supporting the specified type of compute service
	public String[] list(boolean supportingVM, boolean supportingCloudServices) {
		try {
			ArrayList<RoleSize> sizes = azure.management.getRoleSizesOperations().list().getRoleSizes();
			String[] names = new String[sizes.size()];
			int i=0;
			for(RoleSize size : sizes) {
				if(supportingVM && size.isSupportedByVirtualMachines() 
				|| supportingCloudServices && size.isSupportedByWebWorkerRoles()) {
					names[i++] = size.getName();
				}
			}
			return names;
		} catch (Exception e) {
			// Not very actionable, so just return an empty array
			return new String[0];
		}			
	}

	// Returns all available size names
	public String[] list() {
		return list(true, true);
	}
}
