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
package com.microsoft.azure.shortcuts.services;

import java.net.URI;
import java.util.Calendar;
import java.util.List;

import com.microsoft.azure.shortcuts.common.Deletable;
import com.microsoft.azure.shortcuts.common.Named;
import com.microsoft.azure.shortcuts.common.Provisionable;
import com.microsoft.azure.shortcuts.common.Refreshable;
import com.microsoft.azure.shortcuts.common.Updatable;
import com.microsoft.azure.shortcuts.common.Wrapper;
import com.microsoft.windowsazure.management.storage.models.GeoRegionStatus;
import com.microsoft.windowsazure.management.storage.models.StorageAccountStatus;


public interface StorageAccount extends 
	Named,
	Refreshable<StorageAccount>,
	Wrapper<com.microsoft.windowsazure.management.storage.models.StorageAccount> {
	
	String affinityGroup() throws Exception;
	String description() throws Exception;
	String label() throws Exception;
	String geoPrimaryRegion() throws Exception;
	GeoRegionStatus geoPrimaryRegionStatus() throws Exception;
	String geoSecondaryRegion() throws Exception;
	GeoRegionStatus geoSecondaryRegionStatus() throws Exception;
	String region() throws Exception;
	StorageAccountStatus status() throws Exception;
	Calendar lastGeoFailoverTime() throws Exception;
	List<URI> endpoints() throws Exception;
	String type() throws Exception;
	

	/**
	 * A storage account definition requiring the region (location) to be specified
	 */
	public interface WithRegion<T> {
		T withRegion(String region);
		T withRegion(Region region);		
	}
	
	/**
	 * A new blank storage account definition
	 */
	public interface DefinitionBlank extends 
		WithRegion<DefinitionProvisionable> {
	}
	
	/**
	 * A storage account definition requring the label to be specified
	 */
	public interface WithLabel<T> {
		T withLabel(String label);
	}
	
	/**
	 * A storage account definition requiring the storage type to be specified
	 */
	public interface WithType<T> {
		T withType(String type);
	}

	/**
	 * A storage account definition requiring the description to be specified
	 */
	public interface WithDescription<T> {
		T withDescription(String description);
	}
	
	/**
	 * A storage account definition with sufficient input parameters specified to be provisioned in the cloud
	 */
	public interface DefinitionProvisionable extends 
		WithType<DefinitionProvisionable>,
		WithLabel<DefinitionProvisionable>,
		WithDescription<DefinitionProvisionable>,
		Provisionable<UpdateBlank> {
	}
	

	/**
	 * An existing storage account update ready to be applied in the cloud
	 */
	public interface Update extends UpdateBlank, Updatable<Update> {
	}

	
	/**
	 * A blank update request for an existing storage account
	 */
	public interface UpdateBlank extends 
		Deletable,
		WithType<Update>,
		WithDescription<Update>,
		WithLabel<Update> {
	}
}
