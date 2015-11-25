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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.NotImplementedException;

import com.microsoft.azure.shortcuts.common.implementation.EntitiesImpl;
import com.microsoft.azure.shortcuts.common.implementation.NamedRefreshableImpl;
import com.microsoft.azure.shortcuts.services.CloudService;
import com.microsoft.azure.shortcuts.services.Network;
import com.microsoft.azure.shortcuts.services.Region;
import com.microsoft.azure.shortcuts.services.StorageAccount;
import com.microsoft.azure.shortcuts.services.VirtualMachine;
import com.microsoft.azure.shortcuts.services.VirtualMachines;
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
import com.microsoft.windowsazure.management.compute.models.VirtualMachineRoleType;

/**
 * Class encapsulating the API related to virtual networks
 * @author marcins
 * 
 */
public class VirtualMachinesImpl 
	extends EntitiesImpl<Azure>
	implements VirtualMachines {	
	
	VirtualMachinesImpl(Azure azure) {
		super(azure);
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
	public List<String> names() throws Exception {
		Set<String> serviceNames = azure.cloudServices().list().keySet();
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
	public VirtualMachineImpl define(String name) throws Exception {
		return new VirtualMachineImpl(name);
	}
	
	
	// Gets the deployment from Azure based on the fully qualified vm name
	private DeploymentGetResponse getDeployment(String vmId) throws Exception {
		final String serviceName = VirtualMachineId.serviceFromId(vmId); // If only role name given, assume cloud service name is the same
		String deploymentName = VirtualMachineId.deploymentFromId(vmId);
		if(null == deploymentName) {
			// If no deployment name given, assume Production slot
			return azure.computeManagementClient().getDeploymentsOperations().getBySlot(serviceName, DeploymentSlot.Production);
		} else {
			// If deployment name given, use it
			return azure.computeManagementClient().getDeploymentsOperations().getByName(serviceName, deploymentName);
		}
	}
	
	
	// Gets the VM role (if any) from Azure from the deployment
	private Role getVmRole(DeploymentGetResponse deployment, String roleName) {
		ArrayList<Role> roles = deployment.getRoles();
		for(Role role : roles) {
			if(role.getRoleType().equalsIgnoreCase(VirtualMachineRoleType.PersistentVMRole.toString())
					&& role.getRoleName().equalsIgnoreCase(roleName)) {
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
		return new VirtualMachineImpl(name).refresh();
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
			VirtualMachine.DefinitionBlank, 
			VirtualMachine.DefinitionLinuxProvisionable,
			VirtualMachine.DefinitionWindowsProvisionable,
			VirtualMachine.DefinitionWithAdminUsername, 
			VirtualMachine.DefinitionWithImage, 
			VirtualMachine.DefinitionWithAdminPassword, 
			VirtualMachine.DefinitionWithSize,
			VirtualMachine,
			VirtualMachine.Update {

		private DeploymentGetResponse azureDeployment = new DeploymentGetResponse();
		private Role azureRole = new Role();
				
		private String affinityGroup, region, linuxImage, windowsImage, storageAccountName, subnet;
		boolean isExistingCloudService;
		final ArrayList<Integer> tcpPorts = new ArrayList<Integer>();
		final HashMap<Integer, Integer> privatePorts = new HashMap<Integer, Integer>();
		final HashMap<Integer, String> endpointNames = new HashMap<Integer, String>();
		
		private VirtualMachineImpl(String name) throws Exception { 
			super(name);
			final String defaultRoleName = VirtualMachineId.roleFromId(name);
			final String defaultDeploymentName = VirtualMachineId.deploymentFromId(name);
			
			// Deployment defaults
			this.azureDeployment.setName(defaultDeploymentName);
			this.azureDeployment.setDeploymentSlot(DeploymentSlot.Production);
			this.azureDeployment.setLabel(defaultRoleName);
			
			// Role defaults
			this.azureRole.setRoleName(defaultRoleName);
			this.azureRole.setRoleType(VirtualMachineRoleType.PersistentVMRole.toString());
			this.azureRole.setConfigurationSets(new ArrayList<ConfigurationSet>());
			
			this.withComputerName(defaultRoleName);
			this.withHostName(defaultRoleName);
		}
		
		
		/***********************************************************
		 * Getters
		 ***********************************************************/

		//@Override TODO Not public because not currently returned by Azure
		public String adminPassword() throws Exception {
			return ensureOSConfigurationSet().getAdminPassword();
		}
		
		//@Override TODO Not public because not currently returned by Azure
		public String adminUserName() throws Exception {
			return ensureOSConfigurationSet().getAdminUserName();
		}
		
		//@Override TODO Not public because not currently returned by Azure
		public String computerName() throws Exception {
			return ensureOSConfigurationSet().getComputerName();
		}

		//@Override TODO Not public because not currently returned by Azure
		public String hostName() throws Exception {
			return ensureOSConfigurationSet().getHostName();
		}
		
		//@Override TODO Not public because not currently returned by Azure
		public boolean autoUpdate() {
			ConfigurationSet config = ensureOSConfigurationSet();
			return (config.isEnableAutomaticUpdates() != null) ? config.isEnableAutomaticUpdates().booleanValue() : false;
		}

		@Override
		public DeploymentStatus status() throws Exception {
			return this.azureDeployment.getStatus();
		}

		@Override
		public String network() throws Exception {
			return this.azureDeployment.getVirtualNetworkName();
		}
		
		@Override
		public String deploymentLabel() throws Exception {
			return this.azureDeployment.getLabel();
		}		

		@Override
		public String deployment() throws Exception {
			return this.azureDeployment.getName();
		}
		
		@Override
		public Calendar createdTime() throws Exception {
			return this.azureDeployment.getCreatedTime();
		}

		@Override
		public DeploymentSlot deploymentSlot() throws Exception {
			return this.azureDeployment.getDeploymentSlot();
		}

		@Override
		public Map<String, String> extendedDeploymentProperties() throws Exception {
			return Collections.unmodifiableMap(this.azureDeployment.getExtendedProperties());
		}

		@Override
		public Calendar lastModifiedTime() throws Exception {
			return this.azureDeployment.getLastModifiedTime();
		}
		
		@Override
		public String reservedIPName() throws Exception {
			return this.azureDeployment.getReservedIPName();
		}
		
		@Override
		public URI deploymentUri() throws Exception {
			return this.azureDeployment.getUri();
		}

		@Override
		public Boolean isDeploymentLocked() throws Exception {
			return this.azureDeployment.isLocked();
		}

		@Override
		public String availabilitySet() throws Exception {
			return this.azureRole.getAvailabilitySetName();
		}
		
		@Override
		public String defaultWinRmCertificateThumbprint() throws Exception {
			return this.azureRole.getDefaultWinRmCertificateThumbprint();
		}

		@Override
		public String roleLabel() throws Exception {
			return this.azureRole.getLabel();
		}
		
		@Override
		public URI mediaLocation() throws Exception {
			return this.azureRole.getMediaLocation();
		}
		
		@Override
		public String osVersion() throws Exception {
			return this.azureRole.getOSVersion();
		}
		
		@Override
		public String imageName() throws Exception {
			return this.azureRole.getVMImageName();
		}
		
		@Override
		public boolean hasGuestAgent() throws Exception {
			return ((this.azureRole.isProvisionGuestAgent() == null) ? false : this.azureRole.isProvisionGuestAgent().booleanValue());
		}
				
		@Override
		public String size() throws Exception {
			return this.azureRole.getRoleSize();
		}
		
		@Override
		public String region() throws Exception {
			return this.region;
		}
		
		@Override
		public String roleName() throws Exception {
			return this.azureRole.getRoleName();
		}
		
		@Override
		public String cloudService() throws Exception  {
			return VirtualMachineId.serviceFromId(this.name);
		}

		//@Override //TODO: Currently broken in Azure SDK
		public boolean isLinux() throws Exception  {
			return ensureOSConfigurationSet().getConfigurationSetType().equalsIgnoreCase(ConfigurationSetTypes.LINUXPROVISIONINGCONFIGURATION);
		}

		//@Override //TODO: Currently broken in Azure SDK
		public boolean isWindows() throws Exception  {
			return ensureOSConfigurationSet().getConfigurationSetType().equalsIgnoreCase(ConfigurationSetTypes.WINDOWSPROVISIONINGCONFIGURATION);
		}

		@Override
		public String affinityGroup() throws Exception  {
			return this.affinityGroup;
		}


		/**************************************************************
		 * Setters (fluent interface)
		 **************************************************************/
		
		@Override
		public VirtualMachineImpl withNetwork(String network) {
			this.azureDeployment.setVirtualNetworkName(network);
			return this;
		}

		@Override
		public VirtualMachineImpl withNetwork(Network network) {
			return this.withNetwork(network.name());
		}

		@Override
		public VirtualMachineImpl withSize(String size) {
			this.azureRole.setRoleSize(size);
			return this;
		}

		@Override
		public VirtualMachineImpl withRegion(String region) {
			this.region = region;
			return this;
		}

		@Override
		public VirtualMachineImpl withRegion(Region region) {
			return this.withRegion(region.name());
		}

		@Override
		public VirtualMachineImpl withLinuxImage(String image) {
			this.linuxImage = image;
			ensureOSConfigurationSet().setConfigurationSetType(ConfigurationSetTypes.LINUXPROVISIONINGCONFIGURATION);
			return this;
		}
		
		@Override
		public VirtualMachineImpl withWindowsImage(String image) {
			this.windowsImage = image;
			ensureOSConfigurationSet().setConfigurationSetType(ConfigurationSetTypes.WINDOWSPROVISIONINGCONFIGURATION);
			return this;
		}
		
		@Override
		public VirtualMachineImpl withAdminPassword(String password) {
			ConfigurationSet config = ensureOSConfigurationSet();
			config.setAdminPassword(password);
			if(config.getUserPassword()==null) {
				config.setUserPassword(password);
			}
			return this;
		}
		
		@Override
		public VirtualMachineImpl withAdminUsername(String userName) {
			ConfigurationSet osConfig = ensureOSConfigurationSet();
			osConfig.setAdminUserName(userName);
			if(osConfig.getUserName()==null) {
				osConfig.setUserName(userName);
			}
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
			this.ensureOSConfigurationSet().setEnableAutomaticUpdates(autoUpdate);
			return this;
		}

		@Override
		public VirtualMachineImpl withComputerName(String name) throws Exception {
			ensureOSConfigurationSet().setComputerName(name);
			return this;
		}
		
		@Override
		public VirtualMachineImpl withHostName(String name) throws Exception {
			ensureOSConfigurationSet().setHostName(name);
			return this;
		}

		@Override
		public VirtualMachineImpl withGuestAgent(boolean provision) {
			this.azureRole.setProvisionGuestAgent(provision);
			return this;
		}

		@Override
		public VirtualMachineImpl withDeployment(String name)  {
			this.setName(VirtualMachineId.withDeploymentName(name, this.name()));
			this.azureDeployment.setName(VirtualMachineId.deploymentFromId(this.name()));
			return this;
		}
		
		@Override
		public VirtualMachineImpl withDeploymentLabel(String name) {
			this.azureDeployment.setLabel(name);
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
			} else if(this.azureDeployment.getVirtualNetworkName() != null) {
				// Get from network
				final Network network = azure.networks().get(this.azureDeployment.getVirtualNetworkName());
				this.affinityGroup = network.affinityGroup();
				this.region = network.region();
				
				// Enable first subnet from network by default, if none specified
				if(this.subnet == null) {
					this.subnet = (String)network.subnets().keySet().toArray()[0];
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
			ConfigurationSet netConfigSet = this.ensureNetConfigurationSet();
			netConfigSet.setInputEndpoints(endpoints);
			if(this.subnet != null) {
				netConfigSet.setSubnetNames(new ArrayList<String>(Arrays.asList(this.subnet)));
			}
			
			// Determine image
			String image = (this.linuxImage != null) ? this.linuxImage : this.windowsImage;
			
			// Creates OS virtual disk
			OSVirtualHardDisk osDisk = new OSVirtualHardDisk();
			osDisk.setMediaLink(new URI(vhdURL));
			osDisk.setSourceImageName(image);

			// Determine if to create a new cloud service deployment or add to existing
			if(!this.isExistingCloudService) {
				// Create a new cloud service using the same name as the VM
				CloudService.DefinitionBlank serviceDefinition = azure.cloudServices().define(this.cloudService());
				CloudService.DefinitionProvisionable serviceProvisionable = 
						(this.affinityGroup != null) 
						? serviceDefinition.withAffinityGroup(this.affinityGroup) 
						: serviceDefinition.withRegion(this.region);
				serviceProvisionable.provision();
				
				// Prepare role definition
				this.azureRole.setOSVirtualHardDisk(osDisk);
				ArrayList<Role> roles = new ArrayList<Role>(Arrays.asList(this.azureRole));

				// Create a new deployment
				if(this.deployment() == null) {
					this.withDeployment(this.cloudService());
				}
				final VirtualMachineCreateDeploymentParameters vmCreateParams = new VirtualMachineCreateDeploymentParameters();
				vmCreateParams.setRoles(roles);
				vmCreateParams.setDeploymentSlot(this.deploymentSlot());
				vmCreateParams.setLabel(this.deploymentLabel());
				vmCreateParams.setName(this.deployment());
				vmCreateParams.setVirtualNetworkName(this.network());

				azure.computeManagementClient().getVirtualMachinesOperations().createDeployment(this.cloudService(), vmCreateParams);
				
			} else {
				// Get existing deployment from production
				final String deploymentName = azure.computeManagementClient().getDeploymentsOperations().getBySlot(this.cloudService(), DeploymentSlot.Production).getName();
				
				// Deploy into existing cloud service
				final VirtualMachineCreateParameters vmCreateParams = new VirtualMachineCreateParameters();
				vmCreateParams.setRoleName(this.roleName());
				vmCreateParams.setRoleSize(this.size());
				vmCreateParams.setConfigurationSets(this.azureRole.getConfigurationSets());
				vmCreateParams.setOSVirtualHardDisk(osDisk);
				vmCreateParams.setProvisionGuestAgent(this.hasGuestAgent());	
				azure.computeManagementClient().getVirtualMachinesOperations().create(this.cloudService(), deploymentName, vmCreateParams);
			}
			
			return this;
		}


		@Override
		public VirtualMachine refresh() throws Exception {
			// Read deployment
			this.azureDeployment =  getDeployment(this.name());
			this.withDeployment(this.azureDeployment.getName());

			// Read role
			this.azureRole = getVmRole(this.azureDeployment, this.roleName());
			this.withRoleName(this.roleName());
			//final VirtualMachineGetResponse vmResponse = azure.computeManagementClient().getVirtualMachinesOperations().get(
			//		this.cloudService(), this.deployment(), this.roleName());
			
			// Get service-level data
			CloudService service = azure.cloudServices().get(this.cloudService());
			this.affinityGroup = service.affinityGroup();
			this.region = service.region();
			
			// TODO Get other data
			return this;
		}
		
		
		/*************************************************************
		 * Helpers
		 * @throws Exception 
		 *************************************************************/
		
		// Helper returning or creating a default OS configuration
		private ConfigurationSet ensureOSConfigurationSet() {
			final ArrayList<ConfigurationSet> configSets = this.azureRole.getConfigurationSets();
			for(ConfigurationSet config : configSets) {
				if(config.getConfigurationSetType().equalsIgnoreCase(ConfigurationSetTypes.LINUXPROVISIONINGCONFIGURATION)						
					|| config.getConfigurationSetType().equalsIgnoreCase(ConfigurationSetTypes.WINDOWSPROVISIONINGCONFIGURATION)) {
					return config;
				}
			}
			
			ConfigurationSet osConfig = new ConfigurationSet();
			osConfig.setConfigurationSetType(ConfigurationSetTypes.WINDOWSPROVISIONINGCONFIGURATION);
			configSets.add(osConfig);
			osConfig.setDisableSshPasswordAuthentication(false);
			return osConfig;
		}
		
		
		// Helper returning or creating a default network configuration
		private ConfigurationSet ensureNetConfigurationSet() {
			final ArrayList<ConfigurationSet> configSets = this.azureRole.getConfigurationSets();
			for(ConfigurationSet config : configSets) {
				if(config.getConfigurationSetType().equalsIgnoreCase(ConfigurationSetTypes.NETWORKCONFIGURATION)) {
					return config;
				}
			}
			
			ConfigurationSet netConfig = new ConfigurationSet();
			netConfig.setConfigurationSetType(ConfigurationSetTypes.NETWORKCONFIGURATION);
			netConfig.setInputEndpoints(new ArrayList<InputEndpoint>());
			configSets.add(netConfig);
			return netConfig;
		}
	}
}
