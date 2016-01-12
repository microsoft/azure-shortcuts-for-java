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
import com.microsoft.azure.management.compute.models.NetworkProfile;
import com.microsoft.azure.management.compute.models.OSDisk;
import com.microsoft.azure.management.compute.models.OSProfile;
import com.microsoft.azure.management.compute.models.StorageProfile;
import com.microsoft.azure.management.compute.models.VirtualHardDisk;
import com.microsoft.azure.management.compute.models.VirtualMachineExtension;
import com.microsoft.azure.management.resources.models.ResourceGroupExtended;
import com.microsoft.azure.shortcuts.resources.AvailabilitySet;
import com.microsoft.azure.shortcuts.resources.Group;
import com.microsoft.azure.shortcuts.resources.Network;
import com.microsoft.azure.shortcuts.resources.NetworkInterface;
import com.microsoft.azure.shortcuts.resources.Region;
import com.microsoft.azure.shortcuts.resources.Size;
import com.microsoft.azure.shortcuts.resources.StorageAccount;
import com.microsoft.azure.shortcuts.resources.VirtualMachine;
import com.microsoft.azure.shortcuts.resources.VirtualMachine.DefinitionBlank;
import com.microsoft.azure.shortcuts.resources.common.implementation.GroupableResourceBaseImpl;
import com.microsoft.azure.shortcuts.resources.common.implementation.GroupableResourcesBaseImpl;
import com.microsoft.azure.shortcuts.resources.VirtualMachines;

public class VirtualMachinesImpl
	extends GroupableResourcesBaseImpl<Azure, VirtualMachine, com.microsoft.azure.management.compute.models.VirtualMachine>
	implements VirtualMachines {
	
	VirtualMachinesImpl(Azure azure) {
		super(azure);
	}

	
	@Override
	public Map<String, VirtualMachine> list() throws Exception {
		return this.list(null);
	}

	
	@Override
	public VirtualMachineImpl get(String resourceId) throws Exception {
		return this.get(
				ResourcesImpl.groupFromResourceId(resourceId), 
				ResourcesImpl.nameFromResourceId(resourceId));
	}

	
	@Override
	public VirtualMachineImpl get(String resourceGroup, String name) throws Exception {
		return createWrapper(getNativeEntity(resourceGroup, name));
	}

	
	@Override
	public DefinitionBlank define(String name) throws Exception {
		return createWrapper(name);
	}

	
	/***************************************************
	 * Helpers
	 ***************************************************/
	
	@Override
	protected List<com.microsoft.azure.management.compute.models.VirtualMachine> getNativeEntities(String groupName) throws Exception {
		if(groupName != null) {
			return azure.computeManagementClient().getVirtualMachinesOperations().list(groupName).getVirtualMachines();
		} else {
			return azure.computeManagementClient().getVirtualMachinesOperations().listAll(null).getVirtualMachines();
		}
	}
	
	@Override
	protected com.microsoft.azure.management.compute.models.VirtualMachine getNativeEntity(String groupName, String name) throws Exception {
		return azure.computeManagementClient().getVirtualMachinesOperations().get(groupName, name).getVirtualMachine();
	}
	
	@Override 
	protected VirtualMachineImpl createWrapper(com.microsoft.azure.management.compute.models.VirtualMachine nativeItem) {
		return new VirtualMachineImpl(nativeItem);
	}
	
	
	VirtualMachineImpl createWrapper(String name) {
		com.microsoft.azure.management.compute.models.VirtualMachine azureVM = new com.microsoft.azure.management.compute.models.VirtualMachine();
		azureVM.setName(name);
		azureVM.setType("Microsoft.Compute/virtualMachines");
		azureVM.setId(name);
		azureVM.setOSProfile(new OSProfile());
		//azureVM.setAvailabilitySetReference(new AvailabilitySetReference());
		azureVM.setHardwareProfile(new HardwareProfile());
		
		// Default storage profile
		StorageProfile storageProfile = new StorageProfile();
		azureVM.setStorageProfile(storageProfile);
		storageProfile.setImageReference(new ImageReference());
		
		// Default OS disk
		OSDisk osDisk = new OSDisk("osdisk", new VirtualHardDisk(), DiskCreateOptionTypes.FROMIMAGE);
		storageProfile.setOSDisk(osDisk);
		osDisk.setCaching(CachingTypes.NONE);
		
		// Default network profile
		NetworkProfile networkProfile = new NetworkProfile();
		azureVM.setNetworkProfile(networkProfile);
		networkProfile.setNetworkInterfaces(new ArrayList<NetworkInterfaceReference>());
		
		//TODO prepare the rest
		
		return new VirtualMachineImpl(azureVM);
	}
	
	
	/***************************************************
	 * Implements logic for individual Virtual Machine
	 ***************************************************/
	private class VirtualMachineImpl
		extends 
			GroupableResourceBaseImpl<VirtualMachine, com.microsoft.azure.management.compute.models.VirtualMachine>
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
		
		private boolean isExistingAvailabilitySet;
		private String availabilitySetId;
		
		private boolean isExistingNetwork;
		private String networkId;
		
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
					return new URI(this.availabilitySetId);
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
			return (p == null) ? null : p.getComputerName();
		}
		
		@Override
		public String customData() {
			OSProfile p = this.inner().getOSProfile();
			return (p == null) ? null : p.getCustomData();
		}
		
		@Override
		public boolean isLinux() {
			OSProfile p = this.inner().getOSProfile();
			return (p == null) ? false : (this.inner().getOSProfile().getLinuxConfiguration() != null);
		}
		
		@Override
		public boolean isWindows() {
			OSProfile p = this.inner().getOSProfile();
			return (p == null) ? false : (this.inner().getOSProfile().getWindowsConfiguration() != null);
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
		public VirtualMachineImpl withRegion(String regionName) {
			super.withRegion(regionName);
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
			super.withGroupExisting(name);
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
			super.withGroupNew(name);
			return this;
		}

		@Override
		public VirtualMachineImpl withTags(Map<String, String> tags) {
			super.withTags(tags);
			return this;
		}

		@Override
		public VirtualMachineImpl withTag(String name, String value) {
			super.withTag(name, value);
			return this;
		}
		
		@Override
		public VirtualMachineImpl withoutTag(String name) {
			super.withoutTag(name);
			return this;
		}
		
		@Override
		public VirtualMachineImpl withAvailabilitySetExisting(String id) {
			this.availabilitySetId = id;
			this.isExistingAvailabilitySet = true;
			AvailabilitySetReference availabilitySetRef = this.inner().getAvailabilitySetReference();
			if(id == null) {
				this.inner().setAvailabilitySetReference(null);
				return this;
			} else  if(availabilitySetRef == null) {
				this.inner().setAvailabilitySetReference(availabilitySetRef = new AvailabilitySetReference());
			}
			availabilitySetRef.setReferenceUri(id);
			return this;
		}


		@Override
		public VirtualMachineImpl withAvailabilitySetExisting(AvailabilitySet availabilitySet) {
			return this.withAvailabilitySetExisting(availabilitySet.id());
		}


		@Override
		public VirtualMachineImpl withAvailabiliytSetExisting(
				com.microsoft.azure.management.compute.models.AvailabilitySet availabilitySet) {
			return this.withAvailabilitySetExisting(availabilitySet.getId());
		}

		
		@Override
		public VirtualMachineImpl withAvailabilitySetExisting(URI uri) {
			return this.withAvailabilitySetExisting(uri.toString());
		}

		
		@Override
		public VirtualMachineImpl withComputerName(String computerName) {
			this.inner().getOSProfile().setComputerName(computerName);
			return this;
		}
		
		
		@Override
		public VirtualMachineImpl withNetworkInterfaceExisting(String resourceId, boolean isPrimary) {
			NetworkInterfaceReference nicref = new NetworkInterfaceReference();
			if(isPrimary) {
				for(NetworkInterfaceReference n : this.inner().getNetworkProfile().getNetworkInterfaces()) {
					n.setPrimary(false);
				}
			}
			this.inner().getNetworkProfile().getNetworkInterfaces().add(nicref);
			nicref.setReferenceUri(resourceId);
			nicref.setPrimary(isPrimary);
			return this;
		}


		@Override
		public DefinitionProvisionable withNetworkInterfaceExisting(NetworkInterface networkInterface, boolean isPrimary) {
			return this.withNetworkInterfaceExisting(networkInterface.id(), isPrimary);
		}

		
		/*******************************************************
		 * Verbs
		 *******************************************************/
		
		@Override
		public VirtualMachineImpl provision() throws Exception {
			// Ensure group
			Group group = this.ensureGroup(azure);
			
			// Ensure storage account
			StorageAccount storageAccount = this.ensureStorageAccount(group.name());

			// Ensure virtual network
			Network network = this.ensureNetwork(group.name());
			
			// Ensure default computer name
			if(this.computerName() == null) {
				this.withComputerName(this.name());
			}
			
			URL diskBlob = new URL(new URL(storageAccount.primaryBlobEndpoint(), "vhd" + this.name() + "/"), "vhd" + this.name() + ".vhd");
			this.inner().getStorageProfile().getOSDisk().getVirtualHardDisk().setUri(diskBlob.toString());

			azure.computeManagementClient().getVirtualMachinesOperations().createOrUpdate(this.group(), this.inner());
			return this;
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
		
		// Gets or creates if needed the specified virtual network
		private Network ensureNetwork(String groupName) {
			return null;
		}
	}
}
