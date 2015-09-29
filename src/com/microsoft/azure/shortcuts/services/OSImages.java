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
import java.util.ArrayList;
import java.util.Calendar;

import com.microsoft.azure.shortcuts.common.implementation.SupportsListing;
import com.microsoft.azure.shortcuts.services.implementation.NamedImpl;
import com.microsoft.azure.shortcuts.services.implementation.SupportsReading;
import com.microsoft.azure.shortcuts.services.reading.OSImage;
import com.microsoft.windowsazure.management.compute.models.VirtualMachineOSImageGetResponse;
import com.microsoft.windowsazure.management.compute.models.VirtualMachineOSImageListResponse.VirtualMachineOSImage;

// Class encapsulating the API related to OS images
public class OSImages implements 
	SupportsListing,
	SupportsReading<OSImage> {
	
	final Azure azure;
	
	OSImages(Azure azure) {
		this.azure = azure;
	}

	// Returns the list of available OS image names
	public String[] list() {
		try {
			ArrayList<VirtualMachineOSImage> images = azure.computeManagementClient().getVirtualMachineOSImagesOperations().list().getImages();
			String[] names = new String[images.size()];
			int i=0;
			for(VirtualMachineOSImage image : images) {
				names[i++] = image.getName();
				//image.
			}
			return names;
		} catch (Exception e) {
			// Not very actionable, so just return an empty array
			return new String[0];
		}
	}
	
	
	// Returns OS image information
	public OSImage get(String name) throws Exception {
		OSImageImpl osImage = new OSImageImpl(name);
		VirtualMachineOSImageGetResponse response = azure.computeManagementClient().getVirtualMachineOSImagesOperations().get(name);
		osImage.category = response.getCategory();
		osImage.description = response.getDescription();
		osImage.eula = response.getEula();
		osImage.iconUri = new URI(response.getIconUri());
		osImage.family = response.getImageFamily();
		osImage.ioType = response.getIOType();
		osImage.label = response.getLabel();
		osImage.language = response.getLanguage();
		osImage.regions = response.getLocation().split(";");
		osImage.logicalSizeInGB = response.getLogicalSizeInGB();
		osImage.mediaLink = response.getMediaLinkUri();
		osImage.operatingSystemType = response.getOperatingSystemType();
		osImage.privacyUri = response.getPrivacyUri();
		osImage.publishedDate = response.getPublishedDate();
		osImage.publisher = response.getPublisherName();
		osImage.recommendedVMSize = response.getRecommendedVMSize();
		osImage.smallIconUri = new URI(response.getSmallIconUri());
		osImage.isPremium = response.isPremium();
		osImage.isShownInGui = response.isShowInGui();
		
		return osImage;
	}

	
	// Encapsulated information about an image
	private class OSImageImpl 
		extends NamedImpl 
		implements OSImage {
		
		private String category, description, eula, family, ioType, label, language, operatingSystemType, publisher, recommendedVMSize;
		private String[] regions;
		private URI privacyUri, mediaLink, iconUri, smallIconUri;
		private Calendar publishedDate;
		private boolean isPremium, isShownInGui;
		private double logicalSizeInGB;
		
		private OSImageImpl(String name) {
			super(name);
		}

		public String category() {
			return this.category;
		}

		public String description() {
			return this.description;
		}

		public String eula() {
			return this.eula;
		}

		public URI iconUri() {
			return this.iconUri;
		}

		public String family() {
			return this.family;
		}

		public String ioType() {
			return this.ioType;
		}

		public String label() {
			return this.label;
		}

		public String language() {
			return this.language;
		}

		public String[] regions() {
			return this.regions;
		}

		public double logicalSizeInGB() {
			return this.logicalSizeInGB;
		}

		public URI mediaLink() {
			return this.mediaLink;
		}

		public String operatingSystemType() {
			return this.operatingSystemType;
		}

		public URI privacyUri() {
			return this.privacyUri;
		}

		public Calendar publishedDate() {
			return this.publishedDate;
		}

		public String publisher() {
			return this.publisher;
		}

		public String recommendedVMSize() {
			return this.recommendedVMSize;
		}

		public URI smallIconUri() {
			return this.smallIconUri;
		}

		public boolean isPremium() {
			return this.isPremium;
		}

		public boolean isShownInGui() {
			return this.isShownInGui;
		}
	}	
}
