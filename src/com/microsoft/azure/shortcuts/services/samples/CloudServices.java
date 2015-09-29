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

import java.util.Arrays;

import com.microsoft.azure.shortcuts.services.Azure;
import com.microsoft.azure.shortcuts.services.reading.CloudService;

//Tests Cloud Services
public class CloudServices {
	public static void main(String[] args) {
		String publishSettingsPath = "my.publishsettings";
		String subscriptionId = "9657ab5d-4a4a-4fd2-ae7a-4cd9fbd030ef";

		try {
			// Instantiate Azure management class
			final Azure azure = new Azure(publishSettingsPath, subscriptionId);

			test(azure);
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}

	
	// Tests cloud service implementation
	public static void test(Azure azure) throws Exception {
		final String serviceName = "svc" + String.valueOf(System.currentTimeMillis());
		System.out.println(String.format("Creating cloud service named '%s'...", serviceName));

		// Create a new cloud service
		azure.cloudServices.define(serviceName)
			.withRegion("West US")
			.provision();

		// List cloud services
		System.out.println("Available cloud services: " + Arrays.toString(
			azure.cloudServices.list()));

		// Get cloud service info
		CloudService cloudService = azure.cloudServices.get(serviceName);
		System.out.println(String.format("Found cloud service: %s\n"
				+ "\tLabel: %s\n"
				+ "\tDescription: %s\n"
				+ "\tRegion: %s\n"
				+ "\tCreated: %s\n"
				+ "\tModified: %s\n"
				+ "\tAffinity group: %s\n"
				+ "\tReverse DNS FQDN: %s\n",
				cloudService.name(),
				cloudService.label(),
				cloudService.description(),
				cloudService.region(),
				cloudService.created().getTime(),
				cloudService.modified().getTime(),
				cloudService.affinityGroup(),
				cloudService.reverseDnsFqdn()));

		// Update cloud service
		System.out.println(String.format("Updating cloud service named '%s'...", serviceName));

		azure.cloudServices.update(serviceName)
			.withDescription("Updated")
			.withLabel("Updated")
			.apply();

		cloudService = azure.cloudServices.get(serviceName);
		System.out.println(String.format("Updated cloud service: %s\n"
				+ "\tLabel: %s\n"
				+ "\tDescription: %s\n",
				cloudService.name(),
				cloudService.label(),
				cloudService.description()
				));

		
		// Delete the newly created cloud service
		System.out.println(String.format("Deleting cloud service named '%s'...", serviceName));
		azure.cloudServices.delete(serviceName);
	}
}
