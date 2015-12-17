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
package com.microsoft.azure.shortcuts.resources.implementation;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.microsoft.azure.management.compute.models.AvailabilitySetReference;
import com.microsoft.azure.management.compute.models.BootDiagnostics;
import com.microsoft.azure.management.compute.models.CachingTypes;
import com.microsoft.azure.management.compute.models.DataDisk;
import com.microsoft.azure.management.compute.models.DiagnosticsProfile;
import com.microsoft.azure.management.compute.models.DiskCreateOptionTypes;
import com.microsoft.azure.management.compute.models.HardwareProfile;
import com.microsoft.azure.management.compute.models.ImageReference;
import com.microsoft.azure.management.compute.models.NetworkInterfaceReference;
import com.microsoft.azure.management.compute.models.OSDisk;
import com.microsoft.azure.management.compute.models.OSProfile;
import com.microsoft.azure.management.compute.models.StorageProfile;
import com.microsoft.azure.management.compute.models.VirtualHardDisk;
import com.microsoft.azure.management.compute.models.VirtualMachineExtension;
import com.microsoft.azure.management.resources.models.ResourceGroupExtended;
import com.microsoft.azure.shortcuts.common.implementation.EntitiesImpl;
import com.microsoft.azure.shortcuts.resources.Group;
import com.microsoft.azure.shortcuts.resources.Region;
import com.microsoft.azure.shortcuts.resources.Size;
import com.microsoft.azure.shortcuts.resources.StorageAccount;
import com.microsoft.azure.shortcuts.resources.VirtualMachine;
import com.microsoft.azure.shortcuts.resources.VirtualMachine.DefinitionBlank;
import com.microsoft.azure.shortcuts.resources.common.implementation.ResourceBaseImpl;
import com.microsoft.azure.shortcuts.resources.VirtualMachines;

public class VirtualMachinesImpl
	extends EntitiesImpl<Azure>
	implements VirtualMachines {
	
	VirtualMachinesImpl(Azure azure) {
		super(azure);
	}

	
	@Override
	public Map<String, VirtualMachine> list() throws Exception {
		return this.list(null);
	}

	
	@Override
	public Map<String, VirtualMachine> list(String groupName) throws Exception {
		ArrayList<com.microsoft.azure.management.compute.models.VirtualMachine> nativeItems;
		HashMap<String, VirtualMachine> wrappers = new HashMap<>();
		if(groupName != null) {
			nativeItems = azure.computeManagementClient().getVirtualMachinesOperations().list(groupName).getVirtualMachines();
		} else {
			nativeItems = azure.computeManagementClient().getVirtualMachinesOperations().listAll(null).getVirtualMachines();
		}
		
		for(com.microsoft.azure.management.compute.models.VirtualMachine nativeItem : nativeItems) {
			VirtualMachineImpl wrapper = new VirtualMachineImpl(nativeItem);
			wrappers.put(nativeItem.getId(), wrapper);
		}
		
		return Collections.unmodifiableMap(wrappers);
	}


	@Override
	public VirtualMachine get(String resourceId) throws Exception {
		return this.get(
				ResourcesImpl.groupFromResourceId(resourceId), 
				ResourcesImpl.nameFromResourceId(resourceId));
	}

	
	@Override
	public VirtualMachine get(String resourceGroup, String name) throws Exception {
		com.microsoft.azure.management.compute.models.VirtualMachine azureVM = 
			azure.computeManagementClient().getVirtualMachinesOperations().get(
				resourceGroup, name).getVirtualMachine();
		return new VirtualMachineImpl(azureVM);
	}

	
	@Override
	public DefinitionBlank define(String name) throws Exception {
		return createVirtualMachineWrapper(name);
	}

	
	/***************************************************
	 * Helpers
	 ***************************************************/
	
	VirtualMachineImpl createVirtualMachineWrapper(String name) {
		com.microsoft.azure.management.compute.models.VirtualMachine azureVM = new com.microsoft.azure.management.compute.models.VirtualMachine();
		azureVM.setName(name);
		azureVM.setType("Microsoft.Compute/virtualMachines");
		azureVM.setId(name);
		azureVM.setOSProfile(new OSProfile());
		azureVM.setAvailabilitySetReference(new AvailabilitySetReference());
		azureVM.setHardwareProfile(new HardwareProfile());
		
		StorageProfile storageProfile = new StorageProfile();
		azureVM.setStorageProfile(storageProfile);
		storageProfile.setImageReference(new ImageReference());
		
		OSDisk osDisk = new OSDisk("osdisk", new VirtualHardDisk(), DiskCreateOptionTypes.FROMIMAGE);
		storageProfile.setOSDisk(osDisk);
		osDisk.setCaching(CachingTypes.NONE);
		
		//TODO prepare the rest
		
		return new VirtualMachineImpl(azureVM);
	}
	
	
	/***************************************************
	 * Implements logic for individual Virtual Machine
	 ***************************************************/
	private class VirtualMachineImpl
		extends 
			ResourceBaseImpl<VirtualMachine, com.microsoft.azure.management.compute.models.VirtualMachine>
		implements 
			VirtualMachine,
			VirtualMachine.DefinitionBlank,
			VirtualMachine.DefinitionWithAdminUsername,
			VirtualMachine.DefinitionWithAdminPassword,
			VirtualMachine.DefinitionWithImagePublishedBy,
			VirtualMachine.DefinitionWithImageOffer,
			VirtualMachine.DefinitionWithImageSKU,
			VirtualMachine.DefinitionWithImageVersion,
			VirtualMachine.DefinitionProvisionable {

		private boolean isExistingStorageAccount;
		private String storageAccountId;

		private VirtualMachineImpl(com.microsoft.azure.management.compute.models.VirtualMachine azureVM) {
			super(azureVM.getId(), azureVM);
		}


		/***************************************************
		 * Getters
		 ***************************************************/
		
		@Override
		public String size() {
			return this.inner().getHardwareProfile().getVirtualMachineSize();
		}

		@Override
		public URI bootDiagnosticsStorage() {
			DiagnosticsProfile p = this.inner().getDiagnosticsProfile();
			if(p == null) {
				return null;
			}
			
			BootDiagnostics d = p.getBootDiagnostics();
			if(d == null) {
				return null;
			}
			
			return d.getStorageUri();
		}

		@Override
		public boolean isBootDiagnosticsEnabled() {
			DiagnosticsProfile p  = this.inner().getDiagnosticsProfile();
			if(p == null) {
				return false;
			}
			
			BootDiagnostics d = p.getBootDiagnostics();
			if(d == null) {
				return false;
			}
			
			return d.isEnabled();
		}
		
		@Override
		public URI availabilitySet()  {
			try {
				AvailabilitySetReference s = this.inner().getAvailabilitySetReference();
				if(s == null) {
					return null;
				} else {
					return new URI(s.getReferenceUri());
				}
			} catch (URISyntaxException e) {
				return null;
			}
		}

		@Override
		public ArrayList<VirtualMachineExtension> extensions() {
			return this.inner().getExtensions();
		}

		@Override
		public Integer platformFaultDomain() {
			return this.inner().getInstanceView().getPlatformFaultDomain();
		}

		@Override
		public Integer platformUpdateDomain() {
			return this.inner().getInstanceView().getPlatformUpdateDomain();
		}

		@Override
		public String remoteDesktopThumbprint() {
			return this.inner().getInstanceView().getRemoteDesktopThumbprint();
		}

		@Override
		public String vmAgentVersion() {
			return this.inner().getInstanceView().getVMAgent().getVMAgentVersion();
		}

		@Override
		public ArrayList<NetworkInterfaceReference> networkInterfaces() {
			return this.inner().getNetworkProfile().getNetworkInterfaces();
		}

		@Override
		public String adminUserName() {
			return this.inner().getOSProfile().getAdminUsername();
		}

		@Override
		public String computerName() {
			OSProfile p = this.inner().getOSProfile();
			if(p == null) {
				return null;
			} else {
				return p.getComputerName();
			}
		}
		
		@Override
		public String customData() {
			OSProfile p = this.inner().getOSProfile();
			if(p == null) {
				return null;
			} else {
				return p.getCustomData();
			}
		}
		
		@Override
		public boolean isLinux() {
			return this.inner().getOSProfile().getLinuxConfiguration() != null;
		}
		
		@Override
		public boolean isWindows() {
			return this.inner().getOSProfile().getWindowsConfiguration() != null;
		}
				
		@Override
		public ImageReference image() {
			return this.inner().getStorageProfile().getImageReference();
		}
		
		@Override
		public List<DataDisk> dataDisks() {
			StorageProfile p = this.inner().getStorageProfile();
			if(p == null) {
				return null;
			}
			
			return Collections.unmodifiableList(p.getDataDisks());
		}
		
		
		/*******************************************************
		 * Setters (fluent interface)
		 *******************************************************/
		
		@Override
		public VirtualMachineImpl withRegion(String region) {
			this.inner().setLocation(region);
			return this;
		}
		
		@Override
		public VirtualMachineImpl withRegion(Region region) {
			return this.withRegion(region.toString());
		}

		@Override
		public DefinitionWithImagePublishedBy withAdminPassword(String password) {
			this.inner().getOSProfile().setAdminPassword(password);
			return this;
		}


		@Override
		public VirtualMachineImpl withAdminUsername(String username) {
			this.inner().getOSProfile().setAdminUsername(username);
			return this;
		}

		@Override
		public VirtualMachineImpl withImagePublishedBy(String publisher) {
			this.inner().getStorageProfile().getImageReference().setPublisher(publisher);
			return this;
		}

		
		@Override
		public VirtualMachineImpl withImageOffer(String offer) {
			this.inner().getStorageProfile().getImageReference().setOffer(offer);
			return this;
		}

		
		@Override
		public VirtualMachineImpl withImageSKU(String sku) {
			this.inner().getStorageProfile().getImageReference().setSku(sku);
			return this;
		}

		
		@Override
		public VirtualMachineImpl withImageVersion(String version) {
			this.inner().getStorageProfile().getImageReference().setVersion(version);
			return this;
		}


		@Override
		public VirtualMachineImpl withLatestImageVersion() {
			this.inner().getStorageProfile().getImageReference().setVersion("latest");
			return this;
		}

		
		@Override
		public VirtualMachineImpl withAvailabilitySet(URI availabilitySetURI) {
			this.inner().getAvailabilitySetReference().setReferenceUri(availabilitySetURI.toString());
			return this;
		}

		@Override
		public VirtualMachineImpl withSize(String sizeName) {
			this.inner().getHardwareProfile().setVirtualMachineSize(sizeName);
			return this;
		}

		@Override
		public VirtualMachineImpl withSize(Size.Type size) {
			return this.withSize(size.toString());
		}

		@Override
		public VirtualMachineImpl withSize(Size size) {
			return this.withSize(size.id());
		}
		
		@Override
		public VirtualMachineImpl withStorageAccountExisting(String name) {
			this.storageAccountId = name;
			this.isExistingStorageAccount = true;
			return this;
		}

		@Override
		public VirtualMachineImpl withStorageAccountExisting(StorageAccount storageAccount) {
			return this.withStorageAccountExisting(storageAccount.name());
		}

		@Override
		public VirtualMachineImpl withStorageAccountExisting(
				com.microsoft.azure.management.storage.models.StorageAccount storageAccount) {
			return this.withStorageAccountExisting(storageAccount.getName());
		}

		@Override
		public VirtualMachineImpl withStorageAccountNew(String name) {
			this.storageAccountId = name;
			this.isExistingStorageAccount = false;
			return this;
		}
		
		@Override
		public VirtualMachineImpl withGroupExisting(String name) {
			this.groupName = name;
			this.isExistingGroup = true;
			return this;
		}

		@Override
		public VirtualMachineImpl withGroupExisting(Group group) {
			return this.withGroupExisting(group.name());
		}

		@Override
		public VirtualMachineImpl withGroupExisting(ResourceGroupExtended group) {
			return this.withGroupExisting(group.getName());
		}

		@Override
		public VirtualMachineImpl withGroupNew(String name) {
			this.groupName = name;
			this.isExistingGroup = false;
			return this;
		}

		@Override
		public DefinitionProvisionable withTags(Map<String, String> tags) {
			this.inner().setTags(new HashMap<>(tags));
			return this;
		}

		@Override
		public DefinitionProvisionable withTag(String key, String value) {
			this.inner().getTags().put(key, value);
			return this;
		}
		
		
		/*******************************************************
		 * Verbs
		 *******************************************************/
		
		@Override
		public UpdateBlank provision() throws Exception {
			// Ensure group
			Group group = this.ensureGroup(azure);
			
			// Ensure storage account
			StorageAccount storageAccount = ensureStorageAccount(group.name());

			// TODO VirtualNetwork
			
			URL diskBlob = new URL(new URL(storageAccount.primaryBlobEndpoint(), "vhd" + this.name() + "/"), "vhd" + this.name() + ".vhd");
			this.inner().getStorageProfile().getOSDisk().getVirtualHardDisk().setUri(diskBlob.toString());
			
			//throw new UnsupportedOperationException("Not yet implemented.");
			// TODO 
			return null;
		}
		
		
		@Override
		public VirtualMachineImpl refresh() throws Exception {
			this.setInner(azure.computeManagementClient().getVirtualMachinesOperations().get(
				ResourcesImpl.groupFromResourceId(this.id()),
				ResourcesImpl.nameFromResourceId(this.id())).getVirtualMachine());
			return this;
		}
		
		
		/**************************************************
		 * Helpers
		 **************************************************/
				
		// Gets or creates if needed the specified storage account
		private StorageAccount ensureStorageAccount(String groupName) throws Exception {
			if(!this.isExistingStorageAccount) {
				if(this.storageAccountId == null) {
					this.storageAccountId = "store" + this.name();
				}
				
				StorageAccount storageAccount = azure.storageAccounts().define(this.storageAccountId)
					.withRegion(this.region())
					.withGroupExisting(groupName)
					.provision();
				this.isExistingStorageAccount = true;
				return storageAccount;
				
			} else {
				return azure.storageAccounts(this.storageAccountId);
			}
		}
	}
}
