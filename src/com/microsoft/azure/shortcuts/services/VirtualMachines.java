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
import java.util.Arrays;
import java.util.HashMap;

import org.apache.commons.lang3.NotImplementedException;

import com.microsoft.azure.shortcuts.common.implementation.NamedImpl;
import com.microsoft.azure.shortcuts.common.implementation.SupportsDeleting;
import com.microsoft.azure.shortcuts.common.implementation.SupportsListing;
import com.microsoft.azure.shortcuts.common.implementation.SupportsReading;
import com.microsoft.azure.shortcuts.services.creation.CloudServiceDefinitionBlank;
import com.microsoft.azure.shortcuts.services.creation.CloudServiceDefinitionProvisionable;
import com.microsoft.azure.shortcuts.services.creation.StorageAccountDefinitionBlank;
import com.microsoft.azure.shortcuts.services.creation.StorageAccountDefinitionProvisionable;
import com.microsoft.azure.shortcuts.services.creation.VirtualMachineDefinitionBlank;
import com.microsoft.azure.shortcuts.services.creation.VirtualMachineDefinitionLinuxProvisionable;
import com.microsoft.azure.shortcuts.services.creation.VirtualMachineDefinitionProvisionable;
import com.microsoft.azure.shortcuts.services.creation.VirtualMachineDefinitionWindowsProvisionable;
import com.microsoft.azure.shortcuts.services.creation.VirtualMachineDefinitionWithAdminPassword;
import com.microsoft.azure.shortcuts.services.creation.VirtualMachineDefinitionWithAdminUsername;
import com.microsoft.azure.shortcuts.services.creation.VirtualMachineDefinitionWithImage;
import com.microsoft.azure.shortcuts.services.creation.VirtualMachineDefinitionWithSize;
import com.microsoft.azure.shortcuts.services.implementation.SupportsCreating;
import com.microsoft.azure.shortcuts.services.reading.CloudService;
import com.microsoft.azure.shortcuts.services.reading.Network;
import com.microsoft.azure.shortcuts.services.reading.StorageAccount;
import com.microsoft.azure.shortcuts.services.reading.VirtualMachine;
import com.microsoft.azure.shortcuts.services.updating.VirtualMachineUpdatable;
import com.microsoft.windowsazure.management.compute.models.ConfigurationSet;
import com.microsoft.windowsazure.management.compute.models.ConfigurationSetTypes;
import com.microsoft.windowsazure.management.compute.models.DeploymentGetResponse;
import com.microsoft.windowsazure.management.compute.models.DeploymentSlot;
import com.microsoft.windowsazure.management.compute.models.DeploymentStatus;
import com.microsoft.windowsazure.management.compute.models.InputEndpoint;
import com.microsoft.windowsazure.management.compute.models.OSVirtualHardDisk;
import com.microsoft.windowsazure.management.compute.models.Role;
import com.microsoft.windowsazure.management.compute.models.VirtualMachineCreateDeploymentParameters;
import com.microsoft.windowsazure.management.compute.models.VirtualMachineCreateParameters;
import com.microsoft.windowsazure.management.compute.models.VirtualMachineGetResponse;
import com.microsoft.windowsazure.management.compute.models.VirtualMachineRoleType;

/**
 * Class encapsulating the API related to virtual networks
 * @author marcins
 * 
 */
public class VirtualMachines implements 
	SupportsCreating<VirtualMachineDefinitionBlank>,
	SupportsReading<VirtualMachine>,
	SupportsListing,
	SupportsDeleting {
	
	final Azure azure;
	
	VirtualMachines(Azure azure) {
		this.azure = azure;
	}
	
	// Lists all virtual machines
	public String[] list() {
		String[] serviceNames = azure.cloudServices.list();
		ArrayList<String> vms = new ArrayList<String>();
		
		// Find all virtual machine roles within cloud services 
		for(String serviceName : serviceNames) {
			try {
				DeploymentGetResponse deployment = azure.computeManagementClient().getDeploymentsOperations().getBySlot(serviceName, DeploymentSlot.PRODUCTION);
				for(Role role : deployment.getRoles()) {
					if(role.getRoleType().equalsIgnoreCase(VirtualMachineRoleType.PERSISTENTVMROLE.toString())) {
						vms.add(serviceName + "." + role.getRoleName());
					}
				}
			} catch (Exception e) {
				continue;
			}
		}
		
		return vms.toArray(new String[0]);
	}

	
	// Starts a new definition for a virtual machine
	public VirtualMachineDefinitionBlank define(String name) {
		return new VirtualMachineImpl(name);
	}
	
	
	// Returns service name from fully qualified vm name
	private String serviceNameFromVmName(String vmName) {
		return vmName.split("\\.")[0];
	}
	
	
	// Returns role name (if any) from fully qualifies vm name, else null
	private String roleNameFromVmName(String vmName) {
		String[] parts = vmName.split("\\.");
		if(parts.length > 1 ) {
			return parts[parts.length - 1];
		} else {
			return null;
		}
	}
	
	
	// Returns deployment name (if any) from fully qualified vm name, or null
	private String deploymentNameFromVMName(String vmName) {
		String[] parts = vmName.split("\\.");
		if(parts.length > 2) {
			return parts[1];
		} else {
			return null;
		}
	}
	
	
	// Gets the deployment from Azure based on the fully qualified vm name
	private DeploymentGetResponse getDeployment(String vmName) throws Exception {
		final String serviceName = serviceNameFromVmName(vmName);
		String deploymentName;
		if(null == (deploymentName = deploymentNameFromVMName(vmName))) {
			return azure.computeManagementClient().getDeploymentsOperations().getBySlot(serviceName, DeploymentSlot.PRODUCTION);
		} else {
			return azure.computeManagementClient().getDeploymentsOperations().getByName(serviceName, deploymentName);
		}
	}
	
	
	// Gets the VM role (if any) from Azure from the deployment
	private Role getVmRole(DeploymentGetResponse deployment) {
		ArrayList<Role> roles = deployment.getRoles();
		for(Role role : roles) {
			if(role.getRoleType().equalsIgnoreCase(VirtualMachineRoleType.PERSISTENTVMROLE.toString())) {
				return role;
			}
		}
		
		return null;
	}

	
	// Returns the data for an existing virtual machine, where the VM name (if any) is qualified with the service name and deployment name (if any): 
	// "<cloud-service-name>.<deployment-name>.<vm-name>"
	// or "<cloud-service-name>.<vm-name>" where dpeloyment slot is assumed to be Production
	// or "<cloud-service-name>" where deployment slot is assumed to be Production and the first role is assumed to be the right one
	public VirtualMachine get(String name) throws Exception {
		// Determine service name
		final String serviceName = serviceNameFromVmName(name);
		
		// Determine deployment
		final DeploymentGetResponse deploymentResponse = getDeployment(name);
		final String deploymentName = deploymentResponse.getName();

		// Determine role
		final Role role = getVmRole(deploymentResponse);
		if(role == null) {
			throw new Exception("No virtual machine found in this service");
		}
		final String roleName = role.getRoleName();
		
		// Instantiate VM with fully qualified name
		final VirtualMachineImpl vm = new VirtualMachineImpl(serviceName + "." + deploymentName + "." + roleName);

		// Get role properties
		final VirtualMachineGetResponse vmResponse = azure.computeManagementClient().getVirtualMachinesOperations().get(serviceName, deploymentName, roleName);
		vm.size = vmResponse.getRoleSize();
		
		// Get service-level data
		//TODO Make it lazily evaluated
		CloudService service = azure.cloudServices.get(serviceName);
		vm.cloudServiceName = serviceName;
		vm.affinityGroup = service.affinityGroup();
		vm.region = service.region();
		
		// Get deployment-level data
		vm.deploymentName = deploymentName;
		vm.deploymentLabel = deploymentResponse.getLabel();
		vm.status = deploymentResponse.getStatus();
		vm.network = deploymentResponse.getVirtualNetworkName();
					
		// Process config data
		for(ConfigurationSet config : vmResponse.getConfigurationSets()) {
			if(config.getAdminPassword() != null) {
				vm.adminPassword = config.getAdminPassword();
			}
			
			if(config.getAdminUserName() != null) {
				vm.adminUsername = config.getAdminUserName();
			}
			
			if(config.getComputerName() != null) {
				vm.computerName = config.getComputerName();
			}
			
			vm.isLinux = (config.getConfigurationSetType().equalsIgnoreCase(ConfigurationSetTypes.LINUXPROVISIONINGCONFIGURATION));
			vm.isWindows = (config.getConfigurationSetType().equalsIgnoreCase(ConfigurationSetTypes.WINDOWSPROVISIONINGCONFIGURATION));
			
			if(config.getHostName() != null) {
				vm.hostName = config.getHostName();
			}
			
			// TODO: Support endpoints
			if(config.getInputEndpoints() != null) {
				
			}
			
			if(config.getUserName() != null) {
				vm.adminUsername = config.getUserName();
			}
			
			if(config.getUserPassword() != null) {
				vm.adminPassword = config.getUserPassword();
			}
		}
		
		// TODO Get other data
		
		return vm;
	}


	// Deletes a virtual machine
	public void delete(String name) throws Exception {
		// TODO
		if(name == null) {
			throw new Exception("Missing VM name");
		}
		
		throw new NotImplementedException("Not yet implemented.");
	}
	

	// Implements virtual machine logic
	private class VirtualMachineImpl 
		extends NamedImpl
		implements 
			VirtualMachineDefinitionBlank, 
			VirtualMachineDefinitionLinuxProvisionable,
			VirtualMachineDefinitionWindowsProvisionable,
			VirtualMachineDefinitionWithAdminUsername, 
			VirtualMachineDefinitionWithImage, 
			VirtualMachineDefinitionWithAdminPassword, 
			VirtualMachineDefinitionWithSize,
			VirtualMachine,
			VirtualMachineUpdatable {

		private String affinityGroup, network, size, region, linuxImage, windowsImage, adminUsername, adminPassword, 
			computerName, hostName, deploymentName, deploymentLabel, cloudServiceName, storageAccountName, subnet, roleName;
		boolean autoUpdate = true, guestAgent = true, isLinux, isWindows, isExistingCloudService;
		DeploymentStatus status;
		final ArrayList<Integer> tcpPorts = new ArrayList<Integer>();
		final HashMap<Integer, Integer> privatePorts = new HashMap<Integer, Integer>();
		final HashMap<Integer, String> endpointNames = new HashMap<Integer, String>();
		
		private VirtualMachineImpl(String name) { 
			super(name);
			this.deploymentLabel = this.deploymentName = this.hostName = this.computerName = name;
		}
		
		// Applies updates to the virtual machine
		public VirtualMachineImpl apply() throws Exception {
			throw new NotImplementedException("Not yet implemented");
			// TODO return this;
		}


		// Deletes this virtual machine
		public void delete() throws Exception {
			azure.virtualMachines.delete(this.name);
		}
		

		// Provisions a new virtual machine in a new service
		public VirtualMachineImpl provision() throws Exception {
			// Get affinity group and region from existing resources
			if(this.cloudServiceName != null && this.isExistingCloudService) {
				// Get from existing cloud service
				final CloudService cloudService = azure.cloudServices.get(this.cloudServiceName);
				this.affinityGroup = cloudService.affinityGroup();
				this.region = cloudService.region();
			} else if(this.network != null) {
				// Get from network
				final Network network = azure.networks.get(this.network);
				this.affinityGroup = network.affinityGroup();
				this.region = network.region();
				
				// Enable first subnet from network by default, if none specified
				if(this.subnet == null) {
					this.subnet = network.subnets()[0].name();
				}
			}
			
			// Create storage account if not specified
			if(this.storageAccountName == null) {
				final String storeName = "store" + System.currentTimeMillis();
				StorageAccountDefinitionBlank storageDefinition  = azure.storageAccounts.define(storeName);
				StorageAccountDefinitionProvisionable storageProvisionable  = storageDefinition.withRegion(this.region);
				storageProvisionable.provision();
				this.storageAccountName = storeName;
			}

			// Determine URL and verify location of VHD blob to use
			StorageAccount act = azure.storageAccounts.get(this.storageAccountName);
			if(!this.region.equalsIgnoreCase(act.region())) {
				throw new Exception("Storage account is not in the same region.");
			}
			String vhdURL = null;
			for(URI uri : act.endpoints()) {
				if(uri.toASCIIString().contains(".blob.")) {
					vhdURL = uri.toASCIIString();
					break;
				}
			}
			vhdURL += "vhd/" + this.name + ".vhd";

			// Prepare TCP endpoints
			final ArrayList<InputEndpoint> endpoints = new ArrayList<InputEndpoint>();
			for(int port : this.tcpPorts) {
				InputEndpoint endpoint = new InputEndpoint();
				endpoint.setProtocol("tcp");
				endpoint.setName((endpointNames.containsKey(port)) ? endpointNames.get(port) : "port" + port);
				endpoint.setLocalPort(privatePorts.containsKey(port) ? privatePorts.get(port) : port);
				endpoint.setPort(port);
				endpoints.add(endpoint);
			}
			
			// Create net configuration set
			ConfigurationSet netConfigSet = new ConfigurationSet();
			netConfigSet.setConfigurationSetType(ConfigurationSetTypes.NETWORKCONFIGURATION);
			netConfigSet.setInputEndpoints(endpoints);
			netConfigSet.setSubnetNames(new ArrayList<String>(Arrays.asList(this.subnet)));
			
			// Create login
			String image = (this.linuxImage != null) ? this.linuxImage : this.windowsImage;
			ConfigurationSet osConfigSet = new ConfigurationSet();
			if(this.linuxImage != null) {
				isLinux = true;
				osConfigSet.setConfigurationSetType(ConfigurationSetTypes.LINUXPROVISIONINGCONFIGURATION);
				osConfigSet.setUserName(this.adminUsername);
				osConfigSet.setUserPassword(this.adminPassword);
				osConfigSet.setDisableSshPasswordAuthentication(false);
				osConfigSet.setHostName(this.hostName);
			} else if(this.windowsImage != null) {
				isWindows = true;
				osConfigSet.setConfigurationSetType(ConfigurationSetTypes.WINDOWSPROVISIONINGCONFIGURATION);
				osConfigSet.setAdminUserName(this.adminUsername);
				osConfigSet.setAdminPassword(this.adminPassword);
				osConfigSet.setEnableAutomaticUpdates(this.autoUpdate);
				osConfigSet.setComputerName(this.computerName);
			} else {
				throw new Exception("Missing image");
			}
			
			// Prepare configuration sets collection
			ArrayList<ConfigurationSet> configs = 
				new ArrayList<ConfigurationSet>(Arrays.asList(netConfigSet, osConfigSet));
			
			// Creates OS virtual disk
			OSVirtualHardDisk osDisk = new OSVirtualHardDisk();
			osDisk.setMediaLink(new URI(vhdURL));
			osDisk.setSourceImageName(image);

			// Determine if to create a new cloud service deployment or add to existing
			if(!this.isExistingCloudService) {
				// Create a new cloud service using the same name as the VM
				CloudServiceDefinitionBlank serviceDefinition = azure.cloudServices.define(
						this.cloudServiceName != null
						? this.cloudServiceName
						: this.name);
				CloudServiceDefinitionProvisionable serviceProvisionable = 
						(this.affinityGroup != null) 
						? serviceDefinition.withAffinityGroup(this.affinityGroup) 
						: serviceDefinition.withRegion(this.region);
				serviceProvisionable.provision();
				
				// Prepare role definition
				Role role = new Role();
				role.setConfigurationSets(configs);
				role.setProvisionGuestAgent(this.guestAgent);
				role.setRoleName(this.name);
				role.setRoleSize(this.size);
				role.setRoleType(VirtualMachineRoleType.PERSISTENTVMROLE.toString());
				role.setOSVirtualHardDisk(osDisk);
				
				ArrayList<Role> roles = new ArrayList<Role>(Arrays.asList(role));

				// Create a new deployment
				final VirtualMachineCreateDeploymentParameters vmCreateParams = new VirtualMachineCreateDeploymentParameters();
				vmCreateParams.setRoles(roles);
				vmCreateParams.setDeploymentSlot(DeploymentSlot.PRODUCTION);
				vmCreateParams.setLabel(this.deploymentLabel);
				vmCreateParams.setName(this.deploymentName);
				vmCreateParams.setVirtualNetworkName(this.network);
				
				azure.computeManagementClient().getVirtualMachinesOperations().createDeployment(
					(this.cloudServiceName != null) 
					? this.cloudServiceName
					: this.name,
					vmCreateParams);
				
			} else {
				// Get existing deployment from production
				final String deploymentName = azure.computeManagementClient().getDeploymentsOperations().getBySlot(this.cloudServiceName, DeploymentSlot.PRODUCTION).getName();
				
				// Deploy into existing cloud service
				final VirtualMachineCreateParameters vmCreateParams = new VirtualMachineCreateParameters();
				vmCreateParams.setRoleName(this.name);
				vmCreateParams.setRoleSize(this.size);
				vmCreateParams.setConfigurationSets(configs);
				vmCreateParams.setOSVirtualHardDisk(osDisk);
				vmCreateParams.setProvisionGuestAgent(this.guestAgent);	
				azure.computeManagementClient().getVirtualMachinesOperations().create(this.cloudServiceName, deploymentName, vmCreateParams);
			}
			
			return this;
		}
		
		
		public DeploymentStatus status() {
			return this.status;
		}

		public VirtualMachineImpl withNetwork(String network) {
			this.network = network;
			return this;
		}
		
		public String network() {
			return this.network;
		}

		public VirtualMachineImpl withSize(String size) {
			this.size = size;
			return this;
		}

		public String size() {
			return this.size;
		}
		
		public VirtualMachineImpl withRegion(String region) {
			this.region = region;
			return this;
		}
		
		public String region() {
			return this.region;
		}
		
		@Override
		public String roleName() {
			return this.roleName;
		}
		
		public VirtualMachineImpl withLinuxImage(String image) {
			this.linuxImage = image;
			return this;
		}
		
		public VirtualMachineImpl withWindowsImage(String image) {
			this.windowsImage = image;
			return this;
		}
		
		public VirtualMachineImpl withAdminUsername(String userName) {
			this.adminUsername = userName;
			return this;
		}
		
		public VirtualMachineImpl withAdminPassword(String password) {
			this.adminPassword = password;
			return this;
		}
		
		public VirtualMachineImpl withTcpEndpoint(int port) {
			this.tcpPorts.add(port);
			return this;
		}
		
		public VirtualMachineImpl withTcpEndpoint(int publicPort, int privatePort) {
			this.tcpPorts.add(publicPort);
			this.privatePorts.put(publicPort, privatePort);
			return this;
		}

		public VirtualMachineImpl withTcpEndpoint(int publicPort, int privatePort, String name) {
			withTcpEndpoint(publicPort, privatePort);
			this.privatePorts.put(publicPort, privatePort);
			this.endpointNames.put(publicPort, name);
			return this;
		}

		public VirtualMachineImpl withAutoUpdate(boolean autoUpdate) {
			this.autoUpdate = autoUpdate;
			return this;
		}

		public VirtualMachineImpl withComputerName(String name) throws Exception {
			if(name == null || name.length() < 1 || name.length() > 15) {
				throw new Exception("Computer name not valid");
			} else {
				this.computerName = name;
			}
			return this;
		}
		
		public VirtualMachineImpl withHostName(String name) throws Exception {
			if(name == null || name.length() < 1 || name.length() > 64) {
				throw new Exception("Host name not valid.");
			} else {
				this.hostName = name;
			}
			return this;
		}

		public VirtualMachineImpl withGuestAgent(boolean provision) {
			this.guestAgent = provision;
			return this;
		}

		public VirtualMachineImpl withDeployment(String name)  {
			this.deploymentName = name;
			return this;
		}
		
		public String deployment() {
			return this.deploymentName;
		}

		public VirtualMachineImpl withDeploymentLabel(String name) {
			this.deploymentLabel = name;
			return this;
		}
		
		public VirtualMachineImpl withExistingCloudService(String name) {
			this.cloudServiceName = name.toLowerCase();
			this.isExistingCloudService = true;
			return this;
		}

		public String cloudService() {
			return this.cloudServiceName;
		}

		public VirtualMachineDefinitionProvisionable withStorageAccount(String name) {
			this.storageAccountName = name.toLowerCase();
			return this;
		}

		public boolean isLinux() {
			return this.isLinux;
		}

		public boolean isWindows() {
			return this.isWindows;
		}

		public String affinityGroup() {
			return this.affinityGroup;
		}

		public VirtualMachineImpl withNewCloudService(String name) {
			this.cloudServiceName = name;
			this.isExistingCloudService = false;
			return this;
		}

		@Override
		public VirtualMachineImpl withSubnet(String subnet) {
			this.subnet = subnet;
			return this;
		}
	}
}
