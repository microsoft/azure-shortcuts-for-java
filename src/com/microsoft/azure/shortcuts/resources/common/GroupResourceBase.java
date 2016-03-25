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


import com.microsoft.azure.management.resources.models.ResourceGroupExtended;
import com.microsoft.azure.shortcuts.resources.ResourceGroup;

public interface GroupResourceBase extends 
	ResourceBase {
	
	public String resourceGroup();

	/**
	 * A resource definition allowing a resource group to be selected
	 */
	interface DefinitionWithResourceGroup<T> {
		/**
		 * Associates the resources with an existing resource group.
		 * @param groupName The name of an existing resource group to put this resource in.
		 * @return The next stage of the resource definition
		 */
		T withExistingResourceGroup(String groupName);
		
		/**
		 * Associates the resources with an existing resource group.
		 * @param group An existing resource group to put the resource in
		 * @return The next stage of the resource definition
		 */
		T withExistingResourceGroup(ResourceGroup group);
		
		/**
		 * Associates the resources with an existing resource group.
		 * @param group An existing resource group object as returned by the Azure SDK for Java to put the resource in
		 * @return The next stage of the resource definition
		 */
		T withExistingResourceGroup(ResourceGroupExtended group);

		/**
		 * Creates a new resource group to put the resource in. The group will be created in the same region as the resource. 
		 * @param name The name of the new group
		 * @return The next stage of the resource definition
		 */
		T withNewResourceGroup(String name);
		
		/**
		 * Creates a new resource group to put the resource in. The group will be created in the same region as the resource. 
		 * The group's name is automatically derived from the resource's name.
		 * @return The next stage of the resource definition
		 */
		T withNewResourceGroup();
		
		/**
		 * Creates a new resource group to put the resource in based on the provisionable definition specified.
		 * @param groupDefinition A provisionable definition for a new resource group
		 * @return The next stage of the resource definition
		 */
		T withNewResourceGroup(ResourceGroup.DefinitionProvisionable groupDefinition) throws Exception;
	}
}
