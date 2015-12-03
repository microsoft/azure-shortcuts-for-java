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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.microsoft.azure.management.compute.models.AvailabilitySetReference;
import com.microsoft.azure.management.compute.models.BootDiagnostics;
import com.microsoft.azure.management.compute.models.DataDisk;
import com.microsoft.azure.management.compute.models.DiagnosticsProfile;
import com.microsoft.azure.management.compute.models.ImageReference;
import com.microsoft.azure.management.compute.models.NetworkInterfaceReference;
import com.microsoft.azure.management.compute.models.OSProfile;
import com.microsoft.azure.management.compute.models.StorageProfile;
import com.microsoft.azure.management.compute.models.VirtualMachineExtension;
import com.microsoft.azure.shortcuts.common.implementation.EntitiesImpl;
import com.microsoft.azure.shortcuts.common.implementation.NamedRefreshableWrapperImpl;
import com.microsoft.azure.shortcuts.resources.VirtualMachine;
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

	
	/***************************************************
	 * Implements logic for individual Virtual Machine
	 ***************************************************/
	private class VirtualMachineImpl
		extends 
			NamedRefreshableWrapperImpl<VirtualMachine, com.microsoft.azure.management.compute.models.VirtualMachine> 
		implements 
			VirtualMachine {

		private VirtualMachineImpl(com.microsoft.azure.management.compute.models.VirtualMachine azureVM) {
			super(azureVM.getId(), azureVM);
		}

		@Override
		public VirtualMachineImpl refresh() throws Exception {
			this.setInner(azure.computeManagementClient().getVirtualMachinesOperations().get(
				ResourcesImpl.groupFromResourceId(this.name()),
				ResourcesImpl.nameFromResourceId(this.name())).getVirtualMachine());
			return this;
		}

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
		public String region() {
			return this.inner().getLocation();
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

		@Override
		public String id() {
			return this.inner().getId();
		}

		@Override
		public String type() {
			return this.inner().getType();
		}

		@Override
		public Map<String, String> tags() {
			return Collections.unmodifiableMap(this.inner().getTags());
		}
	}
}
