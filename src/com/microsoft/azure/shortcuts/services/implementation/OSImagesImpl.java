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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.microsoft.azure.shortcuts.common.implementation.EntitiesImpl;
import com.microsoft.azure.shortcuts.common.implementation.NamedRefreshableWrapperImpl;
import com.microsoft.azure.shortcuts.services.OSImage;
import com.microsoft.azure.shortcuts.services.OsImages;
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
		ArrayList<VirtualMachineOSImage> osImages = getOSImages();
		Collections.sort(osImages, new Comparator<VirtualMachineOSImage>() {
			@Override
			public int compare(VirtualMachineOSImage o1, VirtualMachineOSImage o2) {
				return o1.getName().compareToIgnoreCase(o2.getName());
			}
		});
		
		TreeMap<String, OSImage> wrappers = new TreeMap<>();
		for(VirtualMachineOSImage nativeItem : osImages) {
			OSImageImpl wrapper = new OSImageImpl(nativeItem);
			wrappers.put(nativeItem.getName(), wrapper);
		}
		
		return Collections.unmodifiableMap(wrappers);
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
		extends NamedRefreshableWrapperImpl<OSImage, VirtualMachineOSImage> 
		implements OSImage {
		
		private OSImageImpl(VirtualMachineOSImage osImage) {
			super(osImage.getName(), osImage);
		}

		
		/***********************************************************
		 * Getters
		 ***********************************************************/
		
		@Override
		public String category() throws Exception {
			return this.inner().getCategory();
		}

		@Override
		public String description() throws Exception {
			return this.inner().getDescription();
		}

		@Override
		public String eula() throws Exception {
			return this.inner().getEula();
		}

		@Override
		public URI iconUri() throws Exception {
			return new URI(this.inner().getIconUri());
		}

		@Override
		public String family() throws Exception {
			return this.inner().getImageFamily();
		}

		@Override
		public String ioType() throws Exception {
			return this.inner().getIOType();
		}
		
		@Override
		public String label() throws Exception {
			return this.inner().getLabel();
		}

		@Override
		public String language() throws Exception {
			return this.inner().getLanguage();
		}

		@Override
		public List<String> regions() throws Exception {
			return  Arrays.asList(this.inner().getLocation().split(";"));
		}

		@Override
		public double logicalSizeInGB() throws Exception {
			return this.inner().getLogicalSizeInGB();
		}

		@Override
		public URI mediaLink() throws Exception {
			return this.inner().getMediaLinkUri();
		}

		@Override
		public String operatingSystemType() throws Exception {
			return this.inner().getOperatingSystemType();
		}

		@Override
		public URI privacyUri() throws Exception {
			return this.inner().getPrivacyUri();
		}

		@Override
		public Calendar publishedDate() throws Exception {
			return this.inner().getPublishedDate();
		}

		@Override
		public String publisher() throws Exception {
			return this.inner().getPublisherName();
		}

		@Override
		public String recommendedVMSize() throws Exception {
			return this.inner().getRecommendedVMSize();
		}

		@Override
		public URI smallIconUri() throws Exception {
			return new URI(this.inner().getSmallIconUri());
		}

		@Override
		public boolean isPremium() throws Exception {
			return this.inner().isPremium();
		}

		@Override
		public boolean isShownInGui() throws Exception {
			return this.inner().isShowInGui();
		}

		
		/************************************************************
		 * Verbs
		 ************************************************************/

		@Override
		public OSImage refresh() throws Exception {
			VirtualMachineOSImageGetResponse response = azure.computeManagementClient().getVirtualMachineOSImagesOperations().get(this.inner().getName());
			this.inner().setCategory(response.getCategory());
			this.inner().setDescription(response.getDescription());
			this.inner().setEula(response.getEula());
			this.inner().setIconUri(response.getIconUri());
			this.inner().setImageFamily(response.getImageFamily());
			this.inner().setIOType(response.getIOType());
			this.inner().setLabel(response.getLabel());
			this.inner().setLanguage(response.getLanguage());
			this.inner().setLocation(response.getLocation());
			this.inner().setLogicalSizeInGB(response.getLogicalSizeInGB());
			this.inner().setMediaLinkUri(response.getMediaLinkUri());
			this.inner().setOperatingSystemType(response.getOperatingSystemType());
			this.inner().setPrivacyUri(response.getPrivacyUri());
			this.inner().setPublishedDate(response.getPublishedDate());
			this.inner().setPublisherName(response.getPublisherName());
			this.inner().setRecommendedVMSize(response.getRecommendedVMSize());
			this.inner().setSmallIconUri(response.getSmallIconUri());
			this.inner().setIsPremium(response.isPremium());
			this.inner().setShowInGui(response.isShowInGui());
			return this;
		}
	}
}
