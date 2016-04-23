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
package com.microsoft.azure.shortcuts.resources;

import java.util.Map;

import com.microsoft.azure.management.resources.models.ResourceGroupExtended;
import com.microsoft.azure.shortcuts.common.Deletable;
import com.microsoft.azure.shortcuts.common.Indexable;
import com.microsoft.azure.shortcuts.common.Provisionable;
import com.microsoft.azure.shortcuts.common.Refreshable;
import com.microsoft.azure.shortcuts.common.Updatable;
import com.microsoft.azure.shortcuts.common.Wrapper;
import com.microsoft.azure.shortcuts.resources.common.Taggable;

public interface ResourceGroup extends 
	Indexable,
	Refreshable<ResourceGroup>,
	Wrapper<ResourceGroupExtended>,
	Deletable {
	
	String region() throws Exception;
	Map<String, String> tags() throws Exception;
	String provisioningState() throws Exception;
	String name();

	
	public interface Definition extends
		DefinitionBlank,
		DefinitionProvisionable {}
	
	/**
	 * A new blank resource group definition
	 */
	public interface DefinitionBlank {
		DefinitionProvisionable withRegion(String regionName);
		DefinitionProvisionable withRegion(Region region);
	}
	
	
	/**
	 * A new resource group definition with sufficient input parameters specified to be provisioned in the cloud
	 */
	public interface DefinitionProvisionable extends 
		Provisionable<ResourceGroup> {
		
		DefinitionProvisionable withTags(Map<String, String> tags);
		DefinitionProvisionable withTag(String key, String value);
	}
	
	
	/**
	 * An existing resource group modification request ready to be applied in the cloud
	 */
	public interface Update extends 
		UpdateBlank, 
		Updatable<Update> {
	}
	

	/**
	 * A blank modification request for an existing resource group
	 */
	public interface UpdateBlank extends 
		Deletable, 
		Taggable<Update>  {
	}
}
