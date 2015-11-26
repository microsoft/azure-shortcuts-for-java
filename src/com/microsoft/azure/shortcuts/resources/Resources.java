package com.microsoft.azure.shortcuts.resources;

import java.util.Map;

import com.microsoft.azure.shortcuts.common.SupportsDeleting;
import com.microsoft.azure.shortcuts.common.SupportsListingEntities;
import com.microsoft.azure.shortcuts.common.SupportsReading;

public interface Resources extends
	SupportsListingEntities<Resource>,
	SupportsReading<Resource>,
	SupportsDeleting {

	/**
	 * Lists the names of resources in a specific group
	 * @param groupName
	 * @return
	 * @throws Exception 
	 */
	Map<String, Resource> list(String groupName) throws Exception;

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
