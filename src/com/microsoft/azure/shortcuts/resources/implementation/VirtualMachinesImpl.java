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
import com.microsoft.azure.management.compute.models.VirtualMachineCaptureParameters;
import com.microsoft.azure.management.compute.models.VirtualMachineExtension;
import com.microsoft.azure.shortcuts.common.implementation.EntitiesImpl;
import com.microsoft.azure.shortcuts.resources.AvailabilitySet;
import com.microsoft.azure.shortcuts.resources.ResourceGroup;
import com.microsoft.azure.shortcuts.resources.Network;
import com.microsoft.azure.shortcuts.resources.NetworkInterface;
import com.microsoft.azure.shortcuts.resources.PublicIpAddress;
import com.microsoft.azure.shortcuts.resources.Size;
import com.microsoft.azure.shortcuts.resources.StorageAccount;
import com.microsoft.azure.shortcuts.resources.VirtualMachine;
import com.microsoft.azure.shortcuts.resources.common.implementation.GroupableResourcesBaseImpl;
import com.microsoft.azure.shortcuts.resources.common.implementation.NetworkableGroupableResourceBaseImpl;
import com.microsoft.azure.shortcuts.resources.VirtualMachines;

public class VirtualMachinesImpl
	extends GroupableResourcesBaseImpl<
		VirtualMachine, 
		com.microsoft.azure.management.compute.models.VirtualMachine,
		VirtualMachinesImpl.VirtualMachineImpl>
	implements VirtualMachines {
	
	VirtualMachinesImpl(Subscription subscription) {
		super(subscription);
	}

	/*****************************************************
	 * Verbs
	 *****************************************************/
	
	@Override
	public VirtualMachineImpl define(String name) throws Exception {
		com.microsoft.azure.management.compute.models.VirtualMachine azureVM = new com.microsoft.azure.management.compute.models.VirtualMachine();
		azureVM.setName(name);
		azureVM.setType("Microsoft.Compute/virtualMachines");
		azureVM.setId(name);
		azureVM.setOSProfile(new OSProfile());
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
		
		return wrap(azureVM);
	}
	
	@Override
	public void delete(String groupName, String name) throws Exception {
		azure.computeManagementClient().getVirtualMachinesOperations().delete(groupName, name);
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
	protected VirtualMachineImpl wrap(com.microsoft.azure.management.compute.models.VirtualMachine nativeItem) {
		return new VirtualMachineImpl(nativeItem, this);
	}
		
	
	/***************************************************
	 * Implements logic for individual Virtual Machine
	 ***************************************************/
	class VirtualMachineImpl
		extends 
			NetworkableGroupableResourceBaseImpl<
				VirtualMachine, 
				com.microsoft.azure.management.compute.models.VirtualMachine,
				VirtualMachineImpl>
		implements 
			VirtualMachine,
			VirtualMachine.DefinitionBlank,
			VirtualMachine.DefinitionWithGroup,
			VirtualMachine.DefinitionWithNetworking,
			VirtualMachine.DefinitionWithSubnet,
			VirtualMachine.DefinitionWithPrivateIp,
			VirtualMachine.DefinitionWithPublicIp,
			VirtualMachine.DefinitionWithAdminUsername,
			VirtualMachine.DefinitionWithAdminPassword,
			VirtualMachine.DefinitionWithImage,
			VirtualMachine.DefinitionProvisionable {

		private boolean isExistingStorageAccount;
		private String storageAccountId;
		
		private boolean isExistingAvailabilitySet;
		private String availabilitySetId;
		
		private boolean isExistingPrimaryNIC;
		private String nicId;
		
		private VirtualMachineImpl(com.microsoft.azure.management.compute.models.VirtualMachine azureVM, EntitiesImpl<Subscription> collection) {
			super(azureVM.getId(), azureVM, collection);
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
		public VirtualMachineImpl withAdminPassword(String password) {
			this.inner().getOSProfile().setAdminPassword(password);
			return this;
		}


		@Override
		public VirtualMachineImpl withAdminUsername(String username) {
			this.inner().getOSProfile().setAdminUsername(username);
			return this;
		}

		@Override
		public VirtualMachineImpl withLatestImage(String publisher, String offer, String sku) {
			return this.withImage(publisher, offer, sku, "latest");
		}

		@Override
		public VirtualMachineImpl withImage(String publisher, String offer, String sku, String version) {
			ImageReference imageReference = this.inner().getStorageProfile().getImageReference();
			imageReference.setPublisher(publisher);
			imageReference.setOffer(offer);
			imageReference.setSku(sku);
			imageReference.setVersion(version);
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
		public VirtualMachineImpl withExistingStorageAccount(String name) {
			this.storageAccountId = name;
			this.isExistingStorageAccount = true;
			return this;
		}

		@Override
		public VirtualMachineImpl withExistingStorageAccount(StorageAccount storageAccount) {
			return this.withExistingStorageAccount(storageAccount.name());
		}

		@Override
		public VirtualMachineImpl withExistingStorageAccount(com.microsoft.azure.management.storage.models.StorageAccount storageAccount) {
			return this.withExistingStorageAccount(storageAccount.getName());
		}

		@Override
		public VirtualMachineImpl withNewStorageAccount(String name) {
			this.storageAccountId = name;
			this.isExistingStorageAccount = false;
			return this;
		}
		
		@Override
		public VirtualMachineImpl withNewStorageAccount() {
			return this.withNewStorageAccount((String)null);
		}
		
		@Override
		public VirtualMachineImpl withNewStorageAccount(StorageAccount.DefinitionProvisionable definition) throws Exception {
			return this.withExistingStorageAccount(definition.provision());
		}
		
		@Override
		public VirtualMachineImpl withExistingAvailabilitySet(String id) {
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
		public VirtualMachineImpl withExistingAvailabilitySet(AvailabilitySet availabilitySet) {
			return this.withExistingAvailabilitySet(availabilitySet.id());
		}


		@Override
		public VirtualMachineImpl withExistingAvailabilitySet(
				com.microsoft.azure.management.compute.models.AvailabilitySet availabilitySet) {
			return this.withExistingAvailabilitySet(availabilitySet.getId());
		}

		
		@Override
		public VirtualMachineImpl withExistingAvailabilitySet(URI uri) {
			return this.withExistingAvailabilitySet(uri.toString());
		}

		@Override
		public VirtualMachineImpl withNewAvailabilitySet(String name) {
			this.isExistingAvailabilitySet = false;
			this.availabilitySetId = name;
			return this;
		}

		@Override
		public VirtualMachineImpl withNewAvailabilitySet() {
			return this.withNewAvailabilitySet((String)null);
		}

		@Override
		public VirtualMachineImpl withNewAvailabilitySet(com.microsoft.azure.shortcuts.resources.AvailabilitySet.DefinitionProvisionable definition) throws Exception {
			return this.withExistingAvailabilitySet(definition.provision());
		}

		@Override
		public VirtualMachineImpl withComputerName(String computerName) {
			this.inner().getOSProfile().setComputerName(computerName);
			return this;
		}
		
		
		@Override
		public VirtualMachineImpl withExistingNetworkInterface(String resourceId) {
			NetworkInterfaceReference nicref = new NetworkInterfaceReference();
			for(NetworkInterfaceReference n : this.inner().getNetworkProfile().getNetworkInterfaces()) {
				n.setPrimary(false);
			}
			this.inner().getNetworkProfile().getNetworkInterfaces().add(nicref);
			nicref.setReferenceUri(resourceId);
			nicref.setPrimary(true);
			return this;
		}

		@Override
		public VirtualMachineImpl withExistingNetworkInterface(NetworkInterface networkInterface) {
			return this.withExistingNetworkInterface(networkInterface.id());
		}
		
		@Override
		public VirtualMachineImpl withExistingNetworkInterface(com.microsoft.azure.management.network.models.NetworkInterface networkInterface) {
			return this.withExistingNetworkInterface(networkInterface.getId());
		}


		@Override
		public DefinitionProvisionable withNewDataDisk(int diskSizeGB) {
			DataDisk disk = new DataDisk();
			this.inner().getStorageProfile().getDataDisks().add(disk);
			disk.setCreateOption(DiskCreateOptionTypes.EMPTY);
			disk.setDiskSizeGB(diskSizeGB);
			return this;
		}

		@Override
		public DefinitionProvisionable withExistingDataDisk(URI vhdUri) {
			return this.withExistingDataDisk(vhdUri.toString());
		}

		@Override
		public DefinitionProvisionable withExistingDataDisk(String vhdUri) {
			DataDisk disk = new DataDisk();
			this.inner().getStorageProfile().getDataDisks().add(disk);
			disk.setCreateOption(DiskCreateOptionTypes.ATTACH);
			VirtualHardDisk vhd = new VirtualHardDisk();
			disk.setVirtualHardDisk(vhd);
			vhd.setUri(vhdUri);
			return this;
		}
		
		
		/*******************************************************
		 * Verbs
		 *******************************************************/
		@Override
		public void delete() throws Exception {
			this.collection.azure().virtualMachines().delete(this.id());
		}

		@Override
		public VirtualMachineImpl stop() throws Exception {
			this.collection.azure().computeManagementClient().getVirtualMachinesOperations().powerOff(this.resourceGroup(), this.name());
			return this;
		}
		
		@Override
		public VirtualMachineImpl restart() throws Exception {
			this.collection.azure().computeManagementClient().getVirtualMachinesOperations().restart(this.resourceGroup(), this.name());
			return this;
		}

		@Override
		public VirtualMachineImpl deallocate() throws Exception {
			this.collection.azure().computeManagementClient().getVirtualMachinesOperations().deallocate(this.resourceGroup(), this.name());
			return this;
		}
		
		@Override
		public VirtualMachineImpl start() throws Exception {
			this.collection.azure().computeManagementClient().getVirtualMachinesOperations().start(this.resourceGroup(), this.name());
			return this;
		}
		
		@Override
		public VirtualMachineImpl capture(String containerName, String diskNamePrefix, boolean overwrite) throws Exception {
			VirtualMachineCaptureParameters params = new VirtualMachineCaptureParameters();
			params.setDestinationContainerName(containerName.toLowerCase());
			params.setVirtualHardDiskNamePrefix(diskNamePrefix);
			params.setOverwrite(overwrite);
			this.collection.azure().computeManagementClient().getVirtualMachinesOperations().capture(this.resourceGroup(), this.name(), params);
			return this;
		}
		
		@Override
		public VirtualMachineImpl generalize() throws Exception {
			this.collection.azure().computeManagementClient().getVirtualMachinesOperations().generalize(this.resourceGroup(), this.name());
			return this;
		}
		
		
		@Override
		public VirtualMachine provision() throws Exception {
			// Ensure group
			ResourceGroup group = this.ensureGroup();
			
			// Ensure storage account
			StorageAccount storageAccount = this.ensureStorageAccount(group.name()); // TODO Remove group refernce

			// Ensure virtual network
			Network network = ensureNetwork();
			
			// Ensure subnet
			Network.Subnet subnet = ensureSubnet(network);
			
			// Ensure public IP address
			PublicIpAddress pip = ensurePublicIpAddress();
			
			// Ensure primary NIC
			NetworkInterface nic = this.ensureNetworkInterface(group.name(), network, subnet, pip); // TODO Remove group reference
			if(nic != null) {
				this.withExistingNetworkInterface(nic);
			}
			
			// Ensure availability set (optional)
			AvailabilitySet set = this.ensureAvailabilitySet(group.name()); //TODO Remove group refereence?
			if(set != null) {
				this.withExistingAvailabilitySet(set);
			}
			
			// Ensure default computer name
			if(this.computerName() == null) {
				this.withComputerName(this.name());
			}
			
			// Ensure data disks
			ensureDataDisks(storageAccount);
			
			URL container = new URL(storageAccount.primaryBlobEndpoint(), this.name() + "/");
			URL diskBlob = new URL(container, "osDisk.vhd");
			this.inner().getStorageProfile().getOSDisk().getVirtualHardDisk().setUri(diskBlob.toString());

			this.collection.azure().computeManagementClient().getVirtualMachinesOperations().createOrUpdate(this.resourceGroup(), this.inner());
			return get(this.groupName, this.name());
		}
		
		
		@Override
		public VirtualMachineImpl refresh() throws Exception {
			this.setInner(getNativeEntity(
				ResourcesImpl.groupFromResourceId(this.id()),
				ResourcesImpl.nameFromResourceId(this.id())));
			return this;
		}
		
		
		/**************************************************
		 * Helpers
		 **************************************************/
				
		// Gets or creates if needed the specified storage account
		private StorageAccount ensureStorageAccount(String groupName) throws Exception {
			if(!this.isExistingStorageAccount) {
				// Create a new storage account
				if(this.storageAccountId == null) {
					// Generate a name if needed
					this.storageAccountId = this.name() + "store";
				}
				
				StorageAccount storageAccount = azure.storageAccounts().define(this.storageAccountId)
					.withRegion(this.region())
					.withExistingResourceGroup(groupName)
					.provision();
				this.isExistingStorageAccount = true;
				return storageAccount;
				
			} else {
				return this.collection.azure().storageAccounts(this.storageAccountId);
			}
		}
		
		
		// Makes sure vhds are configured properly based on the storage account
		private void ensureDataDisks(StorageAccount storageAccount) throws Exception {
			int i = 0;
			for(DataDisk dataDisk : this.inner().getStorageProfile().getDataDisks()) {
				VirtualHardDisk vhd = dataDisk.getVirtualHardDisk();
				if(vhd== null) {
					vhd = new VirtualHardDisk();
					dataDisk.setVirtualHardDisk(vhd);
				}
				
				// Autogenerate name if needed
				if(dataDisk.getName() == null) {
					dataDisk.setName("disk" + i);
				}
				
				// Autogenerate LUN if needed
				if(dataDisk.getLun()==0) {
					dataDisk.setLun(i);
				}
				
				// Assume no caching if not set
				if(dataDisk.getCaching() == null) {
					dataDisk.setCaching(CachingTypes.NONE);
				}
				
				// Autogenerate URI from name
				if(vhd.getUri() == null) {
					URL container = new URL(storageAccount.primaryBlobEndpoint(), this.name() + "/");
					URL diskBlob = new URL(container, dataDisk.getName() + ".vhd");
					vhd.setUri(diskBlob.toString());
				}
				
				i++;
			}
		}
		
		// Gets or creates if needed the specified availability set
		private AvailabilitySet ensureAvailabilitySet(String groupName) throws Exception {
			if(!this.isExistingAvailabilitySet) {
				// Create a new availability set
				if(this.availabilitySetId == null) {
					// Generate a name if needed
					this.availabilitySetId = this.name() + "set";
				}
				
				AvailabilitySet availabilitySet = azure.availabilitySets().define(this.availabilitySetId)
					.withRegion(this.region())
					.withExistingResourceGroup(groupName)
					.provision();
				this.isExistingAvailabilitySet = true;
				return availabilitySet;
			} else if(this.availabilitySetId == null) {
				return null;
			} else {
				return this.collection.azure().availabilitySets(this.availabilitySetId);
			}
		}
		

		// Gets or creates if needed the specified network interface
		private NetworkInterface ensureNetworkInterface(String groupName, Network network, Network.Subnet subnet, PublicIpAddress pip) throws Exception {
			if(!this.isExistingPrimaryNIC) {
				// Create a new NIC
				if(this.nicId == null) {
					// Generate a name if needed
					this.nicId = this.name() + "nic";
				}
				
				NetworkInterface nic = azure.networkInterfaces().define(this.nicId)
					.withRegion(this.region())
					.withExistingResourceGroup(groupName)
					.withExistingNetwork(network)
					.withSubnet(subnet.id())
					.withPrivateIpAddressStatic(this.privateIpAddress)
					.withExistingPublicIpAddress(pip)
					.provision();
				this.isExistingPrimaryNIC = true;
				return nic;
			} else {
				return this.collection.azure().networkInterfaces(this.nicId);
			}
		}
	}
}
