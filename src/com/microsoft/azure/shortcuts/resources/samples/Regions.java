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

import java.util.Arrays;

import com.microsoft.azure.shortcuts.resources.Azure;
import com.microsoft.windowsazure.management.models.LocationAvailableServiceNames;

// Tests resources
public class Regions {
    public static void main(String[] args) {
        try {
            Azure azure = new Azure("my.azureauth", null);
            test(azure);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void test(Azure azure) throws Exception {
		// List all regions
    	System.out.println("All regions: \n\t" + Arrays.toString(
			azure.regions.list()).replaceAll(", ", ",\n\t"));

    	// List regions supporting high memory
    	System.out.println("Regions supporting high memory: \n\t" + Arrays.toString(
			azure.regions.list(LocationAvailableServiceNames.HIGHMEMORY)).replaceAll(", ", ",\n\t"));    	
    }
}
