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

package com.microsoft.azure.shortcuts.resources.samples;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.microsoft.azure.shortcuts.resources.StorageAccount;
import com.microsoft.azure.shortcuts.resources.implementation.Azure;

public class StorageAccountsSample {
    public static void main(String[] args) {
        try {
            Azure azure = Azure.authenticate("my.azureauth", null);
            test(azure);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    

    public static void test(Azure azure) throws Exception {
    	// Listing all storage accounts
    	Map<String, StorageAccount> storageAccounts = azure.storageAccounts().list();
    	System.out.println(String.format("Storage accounts ids: \n\t%s", StringUtils.join(storageAccounts.keySet(), ",\n\t")));
    	
    	// Listing storage accounts in a specific resource group
    	String groupName = "lenatest";
    	storageAccounts = azure.storageAccounts().list(groupName);
    	System.out.println(String.format("Storage account ids in group '%s': \n\t%s", groupName, StringUtils.join(storageAccounts.keySet(), ",\n\t")));
    	
    	// Get info about a specific storage account using its resource ID
    	StorageAccount storageAccount = azure.storageAccounts("/subscriptions/9657ab5d-4a4a-4fd2-ae7a-4cd9fbd030ef/resourceGroups/lenatest/providers/Microsoft.Storage/storageAccounts/lenatest1");
    	printStorageAccount(storageAccount);

    	// Get info about a specific storage account using its group and name
    	storageAccount = azure.storageAccounts("lenatest", "lenatest1");
    	printStorageAccount(storageAccount);

    }
    
    
    private static void printStorageAccount(StorageAccount storageAccount) throws Exception {
    	StringBuilder output = new StringBuilder();
    	output
    		.append(String.format("Storage account id: %s\n", storageAccount.id()))
    		.append(String.format("\tName: %s\n", storageAccount.name()))
    		.append(String.format("\tGroup: %s\n", storageAccount.group()))
    		;
    	
    	System.out.println(output.toString());
    }
}
