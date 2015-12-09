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
package com.microsoft.azure.shortcuts.resources.common;

import java.util.Map;

import com.microsoft.azure.management.resources.models.ResourceGroupExtended;
import com.microsoft.azure.shortcuts.common.Indexable;
import com.microsoft.azure.shortcuts.resources.Group;

public interface ResourceBase extends 
	Indexable {
	
	public String type();
	public String name();
	public String group();
	public String region();
	public Map<String, String> tags();

	/**
	 * A resource definition allowing an existing resource group to be selected
	 */
	interface DefinitionWithGroupExisting<T> {
		/**
		 * @param groupName The name of an existing resource group to put this resource in
		 * @return A resource definition with sufficient required inputs to be provisioned in the cloud
		 */
		T withGroupExisting(String groupName);
		
		/**
		 * @param group An existing resource group to put the resource in
		 * @return A resource definition with sufficient required inputs to be provisioned in the cloud
		 */
		T withGroupExisting(Group group);
		
		/**
		 * @param group An existing resource group object as returns by the Azure SDK for Java to put the resource in
		 * @return A resource definition with sufficient required inputs to be provisioned in the cloud
		 */
		T withGroupExisting(ResourceGroupExtended group);
	}
	
	
	/**
	 * A resource definition allowing a region be selected for the resource
	 */	
	interface DefinitionWithRegion<T> {
		/**
		 * @param region The name of the location for the resource
		 * @return The next stage of the resource definition
		 */
	    T withRegion(String region);
	}
	
	
	/**
	 * A resource definition allowing tags to be specified
	 */
	interface DefinitionWithTags<T> {
		T withTags(Map<String, String> tags);
		T withTag(String key, String value);
	}
}
