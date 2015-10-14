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
import java.util.List;

import org.apache.commons.lang3.NotImplementedException;

import com.microsoft.azure.shortcuts.common.implementation.NamedRefreshableImpl;
import com.microsoft.azure.shortcuts.services.creation.CloudServiceDefinitionBlank;
import com.microsoft.azure.shortcuts.services.creation.CloudServiceDefinitionProvisionable;
import com.microsoft.azure.shortcuts.services.creation.VirtualMachineDefinitionBlank;
import com.microsoft.azure.shortcuts.services.creation.VirtualMachineDefinitionLinuxProvisionable;
import com.microsoft.azure.shortcuts.services.creation.VirtualMachineDefinitionWindowsProvisionable;
import com.microsoft.azure.shortcuts.services.creation.VirtualMachineDefinitionWithAdminPassword;
import com.microsoft.azure.shortcuts.services.creation.VirtualMachineDefinitionWithAdminUsername;
import com.microsoft.azure.shortcuts.services.creation.VirtualMachineDefinitionWithImage;
import com.microsoft.azure.shortcuts.services.creation.VirtualMachineDefinitionWithSize;
import com.microsoft.azure.shortcuts.services.listing.VirtualMachines;
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
public class VirtualMachinesImpl implements VirtualMachines {	
	final Azure azure;
	
	VirtualMachinesImpl(Azure azure) {
		this.azure = azure;
	}
	
	
	// Helper class for dealing with VM ids
	// Format: [{serviceName}.[{deploymentName}.]]roleName}
	// If serviceName is blank, assume same as roleName
	// If deploymentName is blank, assume PRODUCTION
	private static class VirtualMachineId {
		private static String[] getParts(String id) {
			if(id == null) {
				return null;
			}
			
			String[] parts = id.split("\\.");
			if(parts.length < 1) {
				return null;
			} else {
				return parts;
			}
		}
		
		// Return service name from id
		public static String serviceFromId(String id) {
			String[] parts = getParts(id);
			if(parts == null) {
				return null;
			} else {			
				return parts[0];
			}
		}
		
		// Return deployment name from id if present, else null
		public static String deploymentFromId(String id) {
			String[] parts = getParts(id);
			if(parts == null) {
				return null;
			} else if(parts.length == 3) {
				return parts[2];
			} else {
				return null;
			}
		}
		
		// Return role name from id
		public static String roleFromId(String id) {
			String[] parts = getParts(id);
			return parts[parts.length-1];
		}
		
		
		// Create a VM id based on cloud service, deployment name and role name
		public static String createId(String service, String deployment, String role) {
			if(role == null) {
				return  null;
			}
			
			StringBuilder id = new StringBuilder(service != null ? service : role); // Default service name to role name
			id.append(".");
			
			if(deployment != null) {
				id.append(deployment);
				id.append(".");
			}

			id.append(role);
			return id.toString();
		}
		
		
		public static String withServiceName(String service, String id) {
			return createId(service, deploymentFromId(id), roleFromId(id));
		}
		
		
		public static String withDeploymentName(String deployment, String id) {
			return createId(serviceFromId(id), deployment, roleFromId(id));
		}
		
		
		public static String withRoleName(String role, String id) {
			return createId(serviceFromId(id), deploymentFromId(id), role);
		}
	}
	
	
	@Override
	public List<String> list() throws Exception {
		List<String> serviceNames = azure.cloudServices().list();
		ArrayList<String> vms = new ArrayList<String>();
		
		// Find all virtual machine roles within cloud services 
		for(String serviceName : serviceNames) {
			try {
				DeploymentGetResponse deployment = azure.computeManagementClient().getDeploymentsOperations().getBySlot(serviceName, DeploymentSlot.Production);
				for(Role role : deployment.getRoles()) {
					if(role.getRoleType().equalsIgnoreCase(VirtualMachineRoleType.PersistentVMRole.toString())) {
						vms.add(serviceName + "." + role.getRoleName());
					}
				}
			} catch (Exception e) {
				continue;
			}
		}
		
		return vms;
	}

	
	@Override
	public VirtualMachineDefinitionBlank define(String name) {
		return new VirtualMachineImpl(name, true);
	}
	
	
	// Gets the deployment from Azure based on the fully qualified vm name
	private DeploymentGetResponse getDeployment(String vmName) throws Exception {
		final String serviceName = VirtualMachineId.serviceFromId(vmName);
		String deploymentName;
		if(null == (deploymentName = VirtualMachineId.deploymentFromId(vmName))) {
			return azure.computeManagementClient().getDeploymentsOperations().getBySlot(serviceName, DeploymentSlot.Production);
		} else {
			return azure.computeManagementClient().getDeploymentsOperations().getByName(serviceName, deploymentName);
		}
	}
	
	
	// Gets the VM role (if any) from Azure from the deployment
	private Role getVmRole(DeploymentGetResponse deployment) {
		ArrayList<Role> roles = deployment.getRoles();
		for(Role role : roles) {
			if(role.getRoleType().equalsIgnoreCase(VirtualMachineRoleType.PersistentVMRole.toString())) {
				return role;
			}
		}
		return null;
	}

	
	// Returns the data for an existing virtual machine, where the VM name (if any) is qualified with the service name and deployment name (if any): 
	// "<cloud-service-name>.<deployment-name>.<vm-name>"
	// or "<cloud-service-name>.<vm-name>" where deployment slot is assumed to be Production
	// or "<cloud-service-name>" where deployment slot is assumed to be Production and the first role is assumed to be the right one
	@Override
	public VirtualMachine get(String name) throws Exception {
		VirtualMachineImpl vm = new VirtualMachineImpl(name, false);
		return vm.refresh();
	}


	@Override
	public void delete(String name) throws Exception {
		// TODO
		if(name == null) {
			throw new Exception("Missing VM name");
		}
		
		throw new NotImplementedException("Not yet implemented.");
	}
	

	// Implements virtual machine logic
	private class VirtualMachineImpl 
		extends NamedRefreshableImpl<VirtualMachine>
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
			computerName, hostName, deploymentLabel, storageAccountName, subnet;
		boolean autoUpdate = true, guestAgent = true, isLinux, isWindows, isExistingCloudService;
		DeploymentStatus status;
		final ArrayList<Integer> tcpPorts = new ArrayList<Integer>();
		final HashMap<Integer, Integer> privatePorts = new HashMap<Integer, Integer>();
		final HashMap<Integer, String> endpointNames = new HashMap<Integer, String>();
		
		private VirtualMachineImpl(String name, boolean initialized) { 
			super(name, initialized);
			this.deploymentLabel = this.hostName = this.computerName = name;
			withDeployment(name);
		}
		
		
		/***********************************************************
		 * Getters
		 ***********************************************************/

		@Override
		public DeploymentStatus status() throws Exception {
			//TODO: This should be getStatus and should call Azure each time (never cached)
			ensureInitialized();
			return this.status;
		}

		@Override
		public String network() throws Exception {
			ensureInitialized();
			return this.network;
		}

		@Override
		public String size() throws Exception {
			ensureInitialized();
			return this.size;
		}
		
		@Override
		public String region() throws Exception {
			ensureInitialized();
			return this.region;
		}
		
		@Override
		public String roleName() throws Exception {
			return VirtualMachineId.roleFromId(this.name);
		}
		
		@Override
		public String deployment() throws Exception {
			return VirtualMachineId.deploymentFromId(this.name);
		}

		@Override
		public String cloudService() throws Exception  {
			return VirtualMachineId.serviceFromId(this.name);
		}

		@Override
		public boolean isLinux() throws Exception  {
			ensureInitialized();
			return this.isLinux;
		}

		@Override
		public boolean isWindows() throws Exception  {
			ensureInitialized();
			return this.isWindows;
		}

		@Override
		public String affinityGroup() throws Exception  {
			ensureInitialized();
			return this.affinityGroup;
		}


		/**************************************************************
		 * Setters (fluent interface)
		 **************************************************************/
		
		@Override
		public VirtualMachineImpl withNetwork(String network) {
			this.network = network;
			return this;
		}
		
		@Override
		public VirtualMachineImpl withSize(String size) {
			this.size = size;
			return this;
		}

		@Override
		public VirtualMachineImpl withRegion(String region) {
			this.region = region;
			return this;
		}
		
		@Override
		public VirtualMachineImpl withLinuxImage(String image) {
			this.linuxImage = image;
			return this;
		}
		
		@Override
		public VirtualMachineImpl withWindowsImage(String image) {
			this.windowsImage = image;
			return this;
		}
		
		@Override
		public VirtualMachineImpl withAdminUsername(String userName) {
			this.adminUsername = userName;
			return this;
		}
		
		@Override
		public VirtualMachineImpl withAdminPassword(String password) {
			this.adminPassword = password;
			return this;
		}
		
		@Override
		public VirtualMachineImpl withTcpEndpoint(int port) {
			this.tcpPorts.add(port);
			return this;
		}
		
		@Override
		public VirtualMachineImpl withTcpEndpoint(int publicPort, int privatePort) {
			this.tcpPorts.add(publicPort);
			this.privatePorts.put(publicPort, privatePort);
			return this;
		}

		@Override
		public VirtualMachineImpl withTcpEndpoint(int publicPort, int privatePort, String name) {
			withTcpEndpoint(publicPort, privatePort);
			this.privatePorts.put(publicPort, privatePort);
			this.endpointNames.put(publicPort, name);
			return this;
		}

		@Override
		public VirtualMachineImpl withAutoUpdate(boolean autoUpdate) {
			this.autoUpdate = autoUpdate;
			return this;
		}

		@Override
		public VirtualMachineImpl withComputerName(String name) throws Exception {
			if(name == null || name.length() < 1 || name.length() > 15) {
				throw new Exception("Computer name not valid");
			} else {
				this.computerName = name;
			}
			return this;
		}
		
		@Override
		public VirtualMachineImpl withHostName(String name) throws Exception {
			if(name == null || name.length() < 1 || name.length() > 64) {
				throw new Exception("Host name not valid.");
			} else {
				this.hostName = name;
			}
			return this;
		}

		@Override
		public VirtualMachineImpl withGuestAgent(boolean provision) {
			this.guestAgent = provision;
			return this;
		}

		@Override
		public VirtualMachineImpl withDeployment(String name)  {
			this.setName(VirtualMachineId.withDeploymentName(name, this.name()));
			return this;
		}
		
		@Override
		public VirtualMachineImpl withDeploymentLabel(String name) {
			this.deploymentLabel = name;
			return this;
		}
		
		@Override
		public VirtualMachineImpl withExistingCloudService(String name) {
			this.setName(VirtualMachineId.withServiceName(name.toLowerCase(), this.name()));			
			this.isExistingCloudService = true;
			return this;
		}
		
		@Override
		public VirtualMachineImpl withNewCloudService(String name) {
			this.setName(VirtualMachineId.withServiceName(name.toLowerCase(), this.name()));			
			this.isExistingCloudService = false;
			return this;
		}

		@Override
		public VirtualMachineImpl withSubnet(String subnet) {
			this.subnet = subnet;
			return this;
		}

		@Override
		public VirtualMachineImpl withStorageAccount(String name) {
			this.storageAccountName = name.toLowerCase();
			return this;
		}
		
		private VirtualMachineImpl withRoleName(String name) {
			this.setName(VirtualMachineId.withRoleName(name.toLowerCase(), this.name()));			
			return this;
		}

		
		/************************************************************
		 * Verbs
		 ************************************************************/
		
		@Override
		public VirtualMachineImpl apply() throws Exception {
			throw new NotImplementedException("Not yet implemented");
			// TODO return this;
		}

		
		@Override
		public void delete() throws Exception {
			azure.virtualMachines().delete(this.name);
		}
		

		@Override
		public VirtualMachineImpl provision() throws Exception {
			// Get affinity group and region from existing resources
			if(this.cloudService() != null && this.isExistingCloudService) {
				// Get from existing cloud service
				final CloudService cloudService = azure.cloudServices().get(this.cloudService());
				this.affinityGroup = cloudService.affinityGroup();
				this.region = cloudService.region();
			} else if(this.network != null) {
				// Get from network
				final Network network = azure.networks().get(this.network);
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
				azure.storageAccounts().define(storeName)
					.withRegion(this.region)
					.provision();
				this.storageAccountName = storeName;
			}

			// Determine URL and verify location of VHD blob to use
			StorageAccount act = azure.storageAccounts().get(this.storageAccountName);
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
				CloudServiceDefinitionBlank serviceDefinition = azure.cloudServices().define(this.cloudService());
				CloudServiceDefinitionProvisionable serviceProvisionable = 
						(this.affinityGroup != null) 
						? serviceDefinition.withAffinityGroup(this.affinityGroup) 
						: serviceDefinition.withRegion(this.region);
				serviceProvisionable.provision();
				
				// Prepare role definition
				Role role = new Role();
				role.setConfigurationSets(configs);
				role.setProvisionGuestAgent(this.guestAgent);
				role.setRoleName(this.roleName());
				role.setRoleSize(this.size);
				role.setRoleType(VirtualMachineRoleType.PersistentVMRole.toString());
				role.setOSVirtualHardDisk(osDisk);
				
				ArrayList<Role> roles = new ArrayList<Role>(Arrays.asList(role));

				// Create a new deployment
				final VirtualMachineCreateDeploymentParameters vmCreateParams = new VirtualMachineCreateDeploymentParameters();
				vmCreateParams.setRoles(roles);
				vmCreateParams.setDeploymentSlot(DeploymentSlot.Production);
				vmCreateParams.setLabel(this.deploymentLabel);
				vmCreateParams.setName(this.deployment());
				vmCreateParams.setVirtualNetworkName(this.network == null ? "" : this.network);
				
				azure.computeManagementClient().getVirtualMachinesOperations().createDeployment(this.cloudService(), vmCreateParams);
				
			} else {
				// Get existing deployment from production
				final String deploymentName = azure.computeManagementClient().getDeploymentsOperations().getBySlot(this.cloudService(), DeploymentSlot.Production).getName();
				
				// Deploy into existing cloud service
				final VirtualMachineCreateParameters vmCreateParams = new VirtualMachineCreateParameters();
				vmCreateParams.setRoleName(this.name);
				vmCreateParams.setRoleSize(this.size);
				vmCreateParams.setConfigurationSets(configs);
				vmCreateParams.setOSVirtualHardDisk(osDisk);
				vmCreateParams.setProvisionGuestAgent(this.guestAgent);	
				azure.computeManagementClient().getVirtualMachinesOperations().create(this.cloudService(), deploymentName, vmCreateParams);
			}
			
			return this;
		}


		@Override
		public VirtualMachine refresh() throws Exception {
			final DeploymentGetResponse deploymentResponse = getDeployment(this.name);
			this.withDeployment(deploymentResponse.getName());

			// Determine role
			final Role role = getVmRole(deploymentResponse);
			if(role == null) {
				throw new Exception("No virtual machine found in this service");
			}
			
			this.withRoleName(role.getRoleName());

			// Get role properties
			final VirtualMachineGetResponse vmResponse = azure.computeManagementClient().getVirtualMachinesOperations().get(
					this.cloudService(), this.deployment(), this.roleName());
			this.size = vmResponse.getRoleSize();

			// Get service-level data
			//TODO Make it lazily evaluated
			CloudService service = azure.cloudServices().get(this.cloudService());
			this.affinityGroup = service.affinityGroup();
			this.region = service.region();
			
			// Get deployment-level data
			this.deploymentLabel = deploymentResponse.getLabel();
			this.status = deploymentResponse.getStatus();
			this.network = deploymentResponse.getVirtualNetworkName();
						
			// Process config data
			for(ConfigurationSet config : vmResponse.getConfigurationSets()) {
				if(config.getAdminPassword() != null) {
					this.adminPassword = config.getAdminPassword();
				}
				
				if(config.getAdminUserName() != null) {
					this.adminUsername = config.getAdminUserName();
				}
				
				if(config.getComputerName() != null) {
					this.computerName = config.getComputerName();
				}
				
				this.isLinux = (config.getConfigurationSetType().equalsIgnoreCase(ConfigurationSetTypes.LINUXPROVISIONINGCONFIGURATION));
				this.isWindows = (config.getConfigurationSetType().equalsIgnoreCase(ConfigurationSetTypes.WINDOWSPROVISIONINGCONFIGURATION));
				
				if(config.getHostName() != null) {
					this.hostName = config.getHostName();
				}
				
				// TODO: Support endpoints
				if(config.getInputEndpoints() != null) {
					
				}
				
				if(config.getUserName() != null) {
					this.adminUsername = config.getUserName();
				}
				
				if(config.getUserPassword() != null) {
					this.adminPassword = config.getUserPassword();
				}
			}
			
			// TODO Get other data
			this.initialized = true;
			return this;
		}
	}
}
