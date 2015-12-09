package com.microsoft.azure.shortcuts.resources;

import com.microsoft.azure.shortcuts.common.SupportsDeleting;
import com.microsoft.azure.shortcuts.common.SupportsListing;
import com.microsoft.azure.shortcuts.common.SupportsGetting;
import com.microsoft.azure.shortcuts.resources.common.SupportsListingByGroup;

public interface Resources extends
	SupportsListing<Resource>,
	SupportsListingByGroup<Resource>,
	SupportsGetting<Resource>,
	SupportsDeleting {

	/**
	 * Gets a resource using its name, type, provider namespace and group name
	 * @param shortName
	 * @param type
	 * @param provider
	 * @param group
	 * @return
	 * @throws Exception 
	 */
	Resource get(String shortName, String type, String provider, String group) throws Exception;

	/**
	 * Deletes a resource found using its name, type, provider namespace and group name
	 * @param shortName
	 * @param type
	 * @param provider
	 * @param group
	 * @throws Exception 
	 */
	void delete(String shortName, String type, String provider, String group) throws Exception;
}
