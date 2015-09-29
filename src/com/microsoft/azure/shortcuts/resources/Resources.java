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

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;

import com.microsoft.azure.management.resources.models.GenericResourceExtended;
import com.microsoft.azure.shortcuts.common.implementation.SupportsListing;
import com.microsoft.windowsazure.exception.ServiceException;

public class Resources implements
	SupportsListing {
	
	final Azure azure;
	
	Resources(Azure azure) {
		this.azure = azure;
	}
	
	@Override
	// Returns list of resource names in the subscription
	public String[] list() {
		try {
			ArrayList<GenericResourceExtended> resources = azure.resourceManagementClient().getResourcesOperations().list(null).getResources();
			String[] names = new String[resources.size()];
			int i = 0;
			for(GenericResourceExtended resource: resources) {
				names[i++]= resource.getName();
			}
			return names;

		} catch (IOException | ServiceException | URISyntaxException e) {
			return new String[0];
		}
		
	}
	

}
