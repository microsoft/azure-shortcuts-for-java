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
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.microsoft.azure.shortcuts.services.implementation.Azure;
import com.microsoft.azure.shortcuts.services.reading.StorageAccount;

//Tests Storage Accounts
public class StorageAccounts {
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
		final String accountName = "store" + String.valueOf(System.currentTimeMillis());
		System.out.println(String.format("Creating account named '%s'...", accountName));
		
		// Create a new storage account
		azure.storageAccounts().define(accountName)
			.withRegion("West US")
			.provision();

		// List storage accounts
		List<String> storageAccountNames = azure.storageAccounts().names();
		System.out.println("Available storage accounts: " + StringUtils.join(storageAccountNames, ", "));

		// Get storage account information
		StorageAccount storageAccount = azure.storageAccounts().get(accountName);
		System.out.println(String.format("Found storage account: %s\n"
				+ "\tAffinity group: %s\n"
				+ "\tLabel: %s\n"
				+ "\tDescription: %s\n"
				+ "\tGeo primary region: %s\n"
				+ "\tGeo primary region status: %s\n"
				+ "\tGeo secondary region: %s\n"
				+ "\tGeo secondary region status: %s\n"
				+ "\tLast geo failover time: %s\n"
				+ "\tRegion: %s\n"
				+ "\tStatus: %s\n"
				+ "\tEndpoints: %s\n"
				+ "\tType: %s\n",
		
				storageAccount.name(),
				storageAccount.affinityGroup(),
				storageAccount.label(),
				storageAccount.description(),
				storageAccount.geoPrimaryRegion(),
				storageAccount.geoPrimaryRegionStatus(),
				storageAccount.geoSecondaryRegion(),
				storageAccount.geoSecondaryRegionStatus(),
				(storageAccount.lastGeoFailoverTime()!=null) ? storageAccount.lastGeoFailoverTime().getTime() : null,
				storageAccount.region(),
				storageAccount.status(),
				Arrays.toString(storageAccount.endpoints()),
				storageAccount.type()
				));
		
		
		// Update storage info
		System.out.println(String.format("Updating storage account named '%s'...", accountName));

		azure.storageAccounts().update(accountName)
			.withDescription("Updated")
			.withLabel("Updated")
			.apply();
		
		storageAccount = azure.storageAccounts().get(accountName);
		System.out.println(String.format("Updated storage account: %s\n"
				+ "\tLabel: %s\n"
				+ "\tDescription: %s\n",		
				storageAccount.name(),
				storageAccount.label(),
				storageAccount.description()));
		
		// Delete the newly created storage account
		System.out.println(String.format("Deleting storage account named '%s'...", accountName));
		azure.storageAccounts().delete(accountName);
	}
}
