package com.microsoft.azure.shortcuts.resources;

import java.util.List;

import com.microsoft.azure.shortcuts.common.SupportsListingNames;

public interface VirtualMachines extends
	SupportsListingNames {
	
	/**
	 * Lists the names of resources in a specific group
	 * @param groupName
	 * @return
	 * @throws Exception 
	 */
	List<String> names(String groupName) throws Exception;
}
