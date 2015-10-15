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
package com.microsoft.azure.shortcuts.services.implementation;

import java.net.URI;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.microsoft.azure.shortcuts.common.implementation.EntitiesImpl;
import com.microsoft.azure.shortcuts.common.implementation.NamedRefreshableImpl;
import com.microsoft.azure.shortcuts.services.listing.OsImages;
import com.microsoft.azure.shortcuts.services.reading.OSImage;
import com.microsoft.windowsazure.management.compute.models.VirtualMachineOSImageGetResponse;
import com.microsoft.windowsazure.management.compute.models.VirtualMachineOSImageListResponse.VirtualMachineOSImage;

// Class encapsulating the API related to OS images
public class OSImagesImpl 
	extends EntitiesImpl<Azure>
	implements OsImages {
	
	OSImagesImpl(Azure azure) {
		super(azure);
	}


	@Override
	public List<String> names() {
		try {
			ArrayList<VirtualMachineOSImage> items = 
				azure.computeManagementClient().getVirtualMachineOSImagesOperations().list().getImages();			
			ArrayList<String> names = new ArrayList<>();
			for(VirtualMachineOSImage item : items) {
				names.add(item.getName());
			}

			return names;
		} catch (Exception e) {
			// Not very actionable, so just return an empty array
			return new ArrayList<>();
		}
	}
	

	@Override
	public OSImage get(String name) throws Exception {
		OSImageImpl osImage = new OSImageImpl(name, false);
		return osImage.refresh();
	}

	
	// Encapsulated information about an image
	private class OSImageImpl 
		extends NamedRefreshableImpl<OSImage> 
		implements OSImage {
		
		private String category, description, eula, family, ioType, label, language, operatingSystemType, publisher, recommendedVMSize;
		private String[] regions;
		private URI privacyUri, mediaLink, iconUri, smallIconUri;
		private Calendar publishedDate;
		private boolean isPremium, isShownInGui;
		private double logicalSizeInGB;
		
		private OSImageImpl(String name, boolean initialized) {
			super(name, initialized);
		}

		
		/***********************************************************
		 * Getters
		 * @throws Exception 
		 ***********************************************************/
		@Override
		public String category() throws Exception {
			ensureInitialized();
			return this.category;
		}

		@Override
		public String description() throws Exception {
			ensureInitialized();
			return this.description;
		}

		@Override
		public String eula() throws Exception {
			ensureInitialized();
			return this.eula;
		}

		@Override
		public URI iconUri() throws Exception {
			ensureInitialized();
			return this.iconUri;
		}

		@Override
		public String family() throws Exception {
			ensureInitialized();
			return this.family;
		}

		@Override
		public String ioType() throws Exception {
			ensureInitialized();
			return this.ioType;
		}
		
		@Override
		public String label() throws Exception {
			ensureInitialized();
			return this.label;
		}

		@Override
		public String language() throws Exception {
			ensureInitialized();
			return this.language;
		}

		@Override
		public String[] regions() throws Exception {
			ensureInitialized();
			return this.regions;
		}

		@Override
		public double logicalSizeInGB() throws Exception {
			ensureInitialized();
			return this.logicalSizeInGB;
		}

		@Override
		public URI mediaLink() throws Exception {
			ensureInitialized();
			return this.mediaLink;
		}

		@Override
		public String operatingSystemType() throws Exception {
			ensureInitialized();
			return this.operatingSystemType;
		}

		@Override
		public URI privacyUri() throws Exception {
			ensureInitialized();
			return this.privacyUri;
		}

		@Override
		public Calendar publishedDate() throws Exception {
			ensureInitialized();
			return this.publishedDate;
		}

		@Override
		public String publisher() throws Exception {
			ensureInitialized();
			return this.publisher;
		}

		@Override
		public String recommendedVMSize() throws Exception {
			ensureInitialized();
			return this.recommendedVMSize;
		}

		@Override
		public URI smallIconUri() throws Exception {
			ensureInitialized();
			return this.smallIconUri;
		}

		@Override
		public boolean isPremium() throws Exception {
			ensureInitialized();
			return this.isPremium;
		}

		@Override
		public boolean isShownInGui() throws Exception {
			ensureInitialized();
			return this.isShownInGui;
		}

		
		/************************************************************
		 * Verbs
		 ************************************************************/

		@Override
		public OSImage refresh() throws Exception {
			VirtualMachineOSImageGetResponse response = azure.computeManagementClient().getVirtualMachineOSImagesOperations().get(this.name);
			this.category = response.getCategory();
			this.description = response.getDescription();
			this.eula = response.getEula();
			this.iconUri = new URI(response.getIconUri());
			this.family = response.getImageFamily();
			this.ioType = response.getIOType();
			this.label = response.getLabel();
			this.language = response.getLanguage();
			this.regions = response.getLocation().split(";");
			this.logicalSizeInGB = response.getLogicalSizeInGB();
			this.mediaLink = response.getMediaLinkUri();
			this.operatingSystemType = response.getOperatingSystemType();
			this.privacyUri = response.getPrivacyUri();
			this.publishedDate = response.getPublishedDate();
			this.publisher = response.getPublisherName();
			this.recommendedVMSize = response.getRecommendedVMSize();
			this.smallIconUri = new URI(response.getSmallIconUri());
			this.isPremium = response.isPremium();
			this.isShownInGui = response.isShowInGui();
			this.initialized = true;
			return this;
		}
	}	
}
