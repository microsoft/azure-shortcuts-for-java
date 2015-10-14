package com.microsoft.azure.shortcuts.resources.listing;

import java.util.List;

import com.microsoft.azure.shortcuts.common.implementation.SupportsDeleting;
import com.microsoft.azure.shortcuts.common.implementation.SupportsListingNames;
import com.microsoft.azure.shortcuts.common.implementation.SupportsReading;
import com.microsoft.azure.shortcuts.resources.reading.Resource;

public interface Resources extends
	SupportsListingNames,
	SupportsReading<Resource>,
	SupportsDeleting {

	/**
	 * Lists the names of resources in a specific group
	 * @param groupName
	 * @return
	 */
	List<String> list(String groupName);

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
