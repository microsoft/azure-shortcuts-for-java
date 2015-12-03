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
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.microsoft.azure.shortcuts.resources.Size;
import com.microsoft.azure.shortcuts.resources.implementation.Azure;

// Tests sizes
public class SizesSample {
    public static void main(String[] args) {
        try {
            Azure azure = Azure.authenticate("my.azureauth", null);
            test(azure);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void test(Azure azure) throws Exception {
		// List size names in a specific region
    	Set<String> sizeNames = azure.sizes().list("westus").keySet();
    	System.out.println("VM sizes: \n\t" + StringUtils.join(sizeNames, ",\n\t"));
    	
    	// List sizes in a specific region
    	Map<String, Size> sizes = azure.sizes().list("westus");
    	for(Size size : sizes.values()) {
        	System.out.println(String.format("VM size: %s\n"
        		+ "\tMax data disk count: %d\n"
        		+ "\tMemory in MB: %d\n"
        		+ "\tNumber of cores: %d\n"
        		+ "\tOS disk size in MB: %d\n"
        		+ "\tResource disk size in MB: %d\n",
        		size.id(),
        		size.maxDataDiskCount(),
        		size.memoryInMB(),
        		size.numberOfCores(),
        		size.osDiskSizeInMB(),
        		size.resourceDiskSizeInMB()
        		));
    	}
    }
}
