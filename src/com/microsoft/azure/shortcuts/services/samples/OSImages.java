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

import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.microsoft.azure.shortcuts.services.OSImage;
import com.microsoft.azure.shortcuts.services.implementation.Azure;

//Tests OS images
public class OSImages {
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
		final String imageName = "b39f27a8b8c64d52b05eac6a62ebad85__Ubuntu-12_04_5_LTS-amd64-server-20150413-en-us-30GB";

		// List the OS images
		Map<String, OSImage> osImages = azure.osImages().list();
		Set<String> osImageNames = osImages.keySet();
		
		System.out.println("Available OS images: \n\t" + StringUtils.join(osImageNames, ",\n\t"));
		
		// Get information about a specific OS image
		OSImage osImage = azure.osImages().get(imageName);
		printOsImage(osImage);
	}
	
	
	private static void printOsImage(OSImage osImage) throws Exception {
		System.out.println(String.format("Found image: %s\n"
				+ "\tCategory: %s\n"
				+ "\tDescription: %s\n"
				+ "\tEULA: %s\n"
				+ "\tFamily: %s\n"
				+ "\tIcon URI: %s\n"
				+ "\tIO type: %s\n"
				+ "\tPremium? %s\n"
				+ "\tShown in GUI? %s\n"
				+ "\tLabel: %s\n"
				+ "\tLanguage: %s\n"
				+ "\tLogical size (GB): %f\n"
				+ "\tMedia link: %s\n"
				+ "\tOperating system type: %s\n"
				+ "\tPrivacy URI: %s\n"
				+ "\tPublished date: %s\n"
				+ "\tPublisher: %s\n"
				+ "\tRecommended VM size: %s\n"
				+ "\tRegions: %s\n"
				+ "\tSmall icon URI %s\n",
				osImage.name(),
				osImage.category(),
				osImage.description(),
				osImage.eula(),
				osImage.family(),
				osImage.iconUri(),
				osImage.ioType(),
				osImage.isPremium(),
				osImage.isShownInGui(),
				osImage.label(),
				osImage.language(),
				osImage.logicalSizeInGB(),
				osImage.mediaLink(),
				osImage.operatingSystemType(),
				osImage.privacyUri(),
				osImage.publishedDate().getTime(),
				osImage.publisher(),
				osImage.recommendedVMSize(),
				StringUtils.join(osImage.regions(), ", "),
				osImage.smallIconUri()
				));		
	}
}
