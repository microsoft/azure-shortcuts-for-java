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
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

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
	public OSImage get(String name) throws Exception {
		return createOsImage(name).refresh();
	}

	
	@Override
	public Map<String, OSImage> list() throws Exception {
		return super.list(
			getOSImages(), 
			(a) -> new OSImageImpl(a),
			(o) -> o.getName());
	}	

	
	/*********************************************************
	 * Helpers
	 *********************************************************/
	
	// Helper to list OSImages in Azure
	private ArrayList<VirtualMachineOSImage> getOSImages() throws Exception {
		return azure.computeManagementClient().getVirtualMachineOSImagesOperations().list().getImages();			
	}
	
	// Helper to create an instance of Azure' native OS image class
	private OSImageImpl createOsImage(String name) {
		VirtualMachineOSImage image = new VirtualMachineOSImage();
		image.setName(name);
		return new OSImageImpl(image);
	}
	
	
	// Encapsulated information about an image
	private class OSImageImpl 
		extends NamedRefreshableImpl<OSImage> 
		implements OSImage {
		
		private VirtualMachineOSImage azureOsImage;
		
		private OSImageImpl(VirtualMachineOSImage osImage) {
			super(osImage.getName(), true);
			this.azureOsImage = osImage;
		}

		
		/***********************************************************
		 * Getters
		 * @throws Exception 
		 ***********************************************************/
		@Override
		public String category() throws Exception {
			return this.azureOsImage.getCategory();
		}

		@Override
		public String description() throws Exception {
			return this.azureOsImage.getDescription();
		}

		@Override
		public String eula() throws Exception {
			return this.azureOsImage.getEula();
		}

		@Override
		public URI iconUri() throws Exception {
			return new URI(this.azureOsImage.getIconUri());
		}

		@Override
		public String family() throws Exception {
			return this.azureOsImage.getImageFamily();
		}

		@Override
		public String ioType() throws Exception {
			return this.azureOsImage.getIOType();
		}
		
		@Override
		public String label() throws Exception {
			return this.azureOsImage.getLabel();
		}

		@Override
		public String language() throws Exception {
			return this.azureOsImage.getLanguage();
		}

		@Override
		public List<String> regions() throws Exception {
			return  Arrays.asList(this.azureOsImage.getLocation().split(";"));
		}

		@Override
		public double logicalSizeInGB() throws Exception {
			return this.azureOsImage.getLogicalSizeInGB();
		}

		@Override
		public URI mediaLink() throws Exception {
			return this.azureOsImage.getMediaLinkUri();
		}

		@Override
		public String operatingSystemType() throws Exception {
			return this.azureOsImage.getOperatingSystemType();
		}

		@Override
		public URI privacyUri() throws Exception {
			return this.azureOsImage.getPrivacyUri();
		}

		@Override
		public Calendar publishedDate() throws Exception {
			return this.azureOsImage.getPublishedDate();
		}

		@Override
		public String publisher() throws Exception {
			return this.azureOsImage.getPublisherName();
		}

		@Override
		public String recommendedVMSize() throws Exception {
			return this.azureOsImage.getRecommendedVMSize();
		}

		@Override
		public URI smallIconUri() throws Exception {
			return new URI(this.azureOsImage.getSmallIconUri());
		}

		@Override
		public boolean isPremium() throws Exception {
			return this.azureOsImage.isPremium();
		}

		@Override
		public boolean isShownInGui() throws Exception {
			return this.azureOsImage.isShowInGui();
		}

		
		/************************************************************
		 * Verbs
		 ************************************************************/

		@Override
		public OSImage refresh() throws Exception {
			VirtualMachineOSImageGetResponse response = azure.computeManagementClient().getVirtualMachineOSImagesOperations().get(this.azureOsImage.getName());
			this.azureOsImage.setCategory(response.getCategory());
			this.azureOsImage.setDescription(response.getDescription());
			this.azureOsImage.setEula(response.getEula());
			this.azureOsImage.setIconUri(response.getIconUri());
			this.azureOsImage.setImageFamily(response.getImageFamily());
			this.azureOsImage.setIOType(response.getIOType());
			this.azureOsImage.setLabel(response.getLabel());
			this.azureOsImage.setLanguage(response.getLanguage());
			this.azureOsImage.setLocation(response.getLocation());
			this.azureOsImage.setLogicalSizeInGB(response.getLogicalSizeInGB());
			this.azureOsImage.setMediaLinkUri(response.getMediaLinkUri());
			this.azureOsImage.setOperatingSystemType(response.getOperatingSystemType());
			this.azureOsImage.setPrivacyUri(response.getPrivacyUri());
			this.azureOsImage.setPublishedDate(response.getPublishedDate());
			this.azureOsImage.setPublisherName(response.getPublisherName());
			this.azureOsImage.setRecommendedVMSize(response.getRecommendedVMSize());
			this.azureOsImage.setSmallIconUri(response.getSmallIconUri());
			this.azureOsImage.setIsPremium(response.isPremium());
			this.azureOsImage.setShowInGui(response.isShowInGui());
			return this;
		}
	}
}
