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
package com.microsoft.azure.shortcuts.services.samples;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.microsoft.azure.shortcuts.services.Region;
import com.microsoft.azure.shortcuts.services.implementation.Azure;
import com.microsoft.windowsazure.management.models.LocationAvailableServiceNames;

// Tests Regions
public class RegionsSample {
	public static void main(String[] args) {
		String publishSettingsPath = "my.publishsettings";
		String subscriptionId = "9657ab5d-4a4a-4fd2-ae7a-4cd9fbd030ef";

		try {
			// Instantiate Azure management class
			final Azure azure = Azure.authenticate(publishSettingsPath, subscriptionId);

			test(azure);
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}

	
	public static void test(Azure azure) throws Exception {
		// List regions supporting VM
		List<String> regionNames = azure.regions().names(LocationAvailableServiceNames.PERSISTENTVMROLE);
		System.out.println("Available regions supporting VMs: " + StringUtils.join(regionNames, ", "));
		
		// Get info about a specific region
		Region region = azure.regions("West US");
		printRegion(region);
	}
	
	
	private static void printRegion(Region region) throws Exception {
		System.out.println(String.format("Region: %s\n"
				+ "\tDisplay name: %s\n"
				+ "\tAvailable VM sizes: %s\n"
				+ "\tAvailable web/worker role sizes: %s\n"
				+ "\tAvailable services: %s\n"
				+ "\tAvailable storage account types: %s\n",
				region.name(),
				region.displayName(),
				StringUtils.join(region.availableVirtualMachineSizes(), ", "),
				StringUtils.join(region.availableWebWorkerRoleSizes(), ", "),
				StringUtils.join(region.availableServices(), ", "),
				StringUtils.join(region.availableStorageAccountTypes(), ", ")
				));
		}
}
