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

import java.util.ArrayList;
import java.util.List;

import com.microsoft.azure.management.compute.models.CachingTypes;
import com.microsoft.azure.management.compute.models.DiskCreateOptionTypes;
import com.microsoft.azure.management.compute.models.HardwareProfile;
import com.microsoft.azure.management.compute.models.ImageReference;
import com.microsoft.azure.management.compute.models.NetworkInterfaceReference;
import com.microsoft.azure.management.compute.models.NetworkProfile;
import com.microsoft.azure.management.compute.models.OSDisk;
import com.microsoft.azure.management.compute.models.OSProfile;
import com.microsoft.azure.management.compute.models.StorageProfile;
import com.microsoft.azure.management.compute.models.VirtualHardDisk;
import com.microsoft.azure.shortcuts.resources.Size;
import com.microsoft.azure.shortcuts.resources.VirtualMachine;
import com.microsoft.azure.shortcuts.resources.VirtualMachines;

public class VirtualMachinesImpl
	extends GroupableResourcesBaseImpl<
		VirtualMachine, 
		com.microsoft.azure.management.compute.models.VirtualMachine,
		VirtualMachineImpl>
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
		
		// Default size
		azureVM.getHardwareProfile().setVirtualMachineSize(Size.Type.BASIC_A1.toString());
		return wrap(azureVM);
	}
	
	@Override
	public void delete(String groupName, String name) throws Exception {
		subscription.computeManagementClient().getVirtualMachinesOperations().delete(groupName, name);
	}

	
	/***************************************************
	 * Helpers
	 ***************************************************/
	
	@Override
	protected List<com.microsoft.azure.management.compute.models.VirtualMachine> getNativeEntities(String groupName) throws Exception {
		if(groupName != null) {
			return subscription.computeManagementClient().getVirtualMachinesOperations().list(groupName).getVirtualMachines();
		} else {
			return subscription.computeManagementClient().getVirtualMachinesOperations().listAll(null).getVirtualMachines();
		}
	}
	
	@Override
	protected com.microsoft.azure.management.compute.models.VirtualMachine getNativeEntity(String groupName, String name) throws Exception {
		return subscription.computeManagementClient().getVirtualMachinesOperations().get(groupName, name).getVirtualMachine();
	}
	
	@Override 
	protected VirtualMachineImpl wrap(com.microsoft.azure.management.compute.models.VirtualMachine nativeItem) {
		return new VirtualMachineImpl(nativeItem, this);
	}
}
