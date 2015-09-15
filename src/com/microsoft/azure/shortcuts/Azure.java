package com.microsoft.azure.shortcuts;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;

import com.microsoft.azure.shortcuts.creation.*;
import com.microsoft.azure.shortcuts.reading.*;
import com.microsoft.azure.shortcuts.updating.CloudServiceUpdatable;
import com.microsoft.azure.shortcuts.updating.NetworkUpdatable;
import com.microsoft.azure.shortcuts.updating.StorageAccountUpdatable;
import com.microsoft.azure.shortcuts.updating.VirtualMachineUpdatable;
import com.microsoft.windowsazure.Configuration;
import com.microsoft.windowsazure.management.configuration.PublishSettingsLoader;
import com.microsoft.windowsazure.management.ManagementClient;
import com.microsoft.windowsazure.management.ManagementService;
import com.microsoft.windowsazure.management.models.LocationsListResponse.Location;
import com.microsoft.windowsazure.management.models.RoleSizeListResponse.RoleSize;
import com.microsoft.windowsazure.management.network.NetworkManagementClient;
import com.microsoft.windowsazure.management.network.NetworkManagementService;
import com.microsoft.windowsazure.management.network.models.NetworkListResponse.VirtualNetworkSite;
import com.microsoft.windowsazure.management.network.models.NetworkSetConfigurationParameters;
import com.microsoft.windowsazure.management.storage.StorageManagementClient;
import com.microsoft.windowsazure.management.storage.StorageManagementService;
import com.microsoft.windowsazure.management.storage.models.GeoRegionStatus;
import com.microsoft.windowsazure.management.storage.models.StorageAccountCreateParameters;
import com.microsoft.windowsazure.management.storage.models.StorageAccountGetResponse;
import com.microsoft.windowsazure.management.storage.models.StorageAccountProperties;
import com.microsoft.windowsazure.management.storage.models.StorageAccountStatus;
import com.microsoft.windowsazure.management.storage.models.StorageAccountTypes;
import com.microsoft.windowsazure.management.storage.models.StorageAccountUpdateParameters;
import com.microsoft.windowsazure.management.compute.ComputeManagementClient;
import com.microsoft.windowsazure.management.compute.ComputeManagementService;
import com.microsoft.windowsazure.management.compute.models.ConfigurationSet;
import com.microsoft.windowsazure.management.compute.models.ConfigurationSetTypes;
import com.microsoft.windowsazure.management.compute.models.DeploymentGetResponse;
import com.microsoft.windowsazure.management.compute.models.DeploymentSlot;
import com.microsoft.windowsazure.management.compute.models.DeploymentStatus;
import com.microsoft.windowsazure.management.compute.models.HostedServiceCreateParameters;
import com.microsoft.windowsazure.management.compute.models.HostedServiceGetResponse;
import com.microsoft.windowsazure.management.compute.models.HostedServiceUpdateParameters;
import com.microsoft.windowsazure.management.compute.models.InputEndpoint;
import com.microsoft.windowsazure.management.compute.models.OSVirtualHardDisk;
import com.microsoft.windowsazure.management.compute.models.Role;
import com.microsoft.windowsazure.management.compute.models.VirtualMachineCreateDeploymentParameters;
import com.microsoft.windowsazure.management.compute.models.HostedServiceListResponse.HostedService;
import com.microsoft.windowsazure.management.compute.models.VirtualMachineCreateParameters;
import com.microsoft.windowsazure.management.compute.models.VirtualMachineGetResponse;
import com.microsoft.windowsazure.management.compute.models.VirtualMachineOSImageGetResponse;
import com.microsoft.windowsazure.management.compute.models.VirtualMachineOSImageListResponse.VirtualMachineOSImage;
import com.microsoft.windowsazure.management.compute.models.VirtualMachineRoleType;

import org.apache.commons.lang3.NotImplementedException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import com.microsoft.windowsazure.exception.ServiceException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;

public class Azure {
	private Configuration configuration= null;
	private ManagementClient management = null;
	private ComputeManagementClient compute = null;
	private StorageManagementClient storage = null;
	private NetworkManagementClient networking = null;
	
	public final Regions regions = new Regions();
	public final Sizes sizes = new Sizes();
	public final OSImages osImages = new OSImages();
	public final StorageAccounts storageAccounts = new StorageAccounts();
	public final CloudServices cloudServices = new CloudServices();
	public final Networks networks = new Networks();
	public final VirtualMachines virtualMachines = new VirtualMachines();
	
	// Base implementation for named entities
	private abstract class NamedImpl implements Named {
		final protected String name;
		
		private NamedImpl(String name) {
			this.name = name;
		}
		
		@Override
		public String name() {
			return this.name;
		}
		
		@Override
		public String toString() {
			return this.name();
		}
	}
	
	// Construct based on credentials from a publishsettings file for the selected subscription
	public Azure(String publishSettingsPath, String subscriptionId) throws IOException {
		this.configuration = PublishSettingsLoader.createManagementConfiguration(publishSettingsPath, subscriptionId);
		this.management = ManagementService.create(configuration);
		this.compute = ComputeManagementService.create(configuration);
		this.storage = StorageManagementService.create(configuration);
		this.networking = NetworkManagementService.create(configuration);		
	}

	
	// Requires class to support updating entities
	private interface SupportsUpdating<T> {
		T update(String name);
	}
	
	
	// Requires class to support creating entities
	private interface SupportsCreating<T> {
		T define(String name);
	}
	
	
	// Requires class to support deleting entities
	private interface SupportsDeleting {
		void delete(String name) throws Exception;
	}
	
	
	// Requires class to support reading entities
	private interface SupportsReading<T> {
		T get(String name) throws Exception;
	}
	
	
	// Requires class to support listing entities
	private interface SupportsListing {
		String[] list();
	}
	

	/**
	 * Nested class encapsulating the API related to virtual networks
	 * @author marcins
	 * 
	 */
	public class VirtualMachines implements 
		SupportsCreating<VirtualMachineDefinitionBlank>,
		SupportsReading<VirtualMachine>,
		SupportsListing,
		SupportsDeleting {
		
		private VirtualMachines() {}
		
		// Lists all virtual machines
		public String[] list() {
			String[] serviceNames = cloudServices.list();
			ArrayList<String> vms = new ArrayList<String>();
			
			// Find all virtual machine roles within cloud services 
			for(String serviceName : serviceNames) {
				try {
					DeploymentGetResponse deployment = compute.getDeploymentsOperations().getBySlot(serviceName, DeploymentSlot.Production);
					for(Role role : deployment.getRoles()) {
						if(role.getRoleType().equalsIgnoreCase(VirtualMachineRoleType.PersistentVMRole.toString())) {
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
				return compute.getDeploymentsOperations().getBySlot(serviceName, DeploymentSlot.Production);
			} else {
				return compute.getDeploymentsOperations().getByName(serviceName, deploymentName);
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
			final VirtualMachineGetResponse vmResponse = compute.getVirtualMachinesOperations().get(serviceName, deploymentName, roleName);
			vm.size = vmResponse.getRoleSize();
			
			// Get service-level data
			//TODO Make it lazily evaluated
			CloudService service = cloudServices.get(serviceName);
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
				virtualMachines.delete(this.name);
			}
			

			// Provisions a new virtual machine in a new service
			public VirtualMachineImpl provision() throws Exception {
				// Get affinity group and region from existing resources
				if(this.cloudServiceName != null && this.isExistingCloudService) {
					// Get from existing cloud service
					final CloudService cloudService = cloudServices.get(this.cloudServiceName);
					this.affinityGroup = cloudService.affinityGroup();
					this.region = cloudService.region();
				} else if(this.network != null) {
					// Get from network
					final Network network = networks.get(this.network);
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
					StorageAccountDefinitionBlank storageDefinition  = storageAccounts.define(storeName);
					StorageAccountDefinitionProvisionable storageProvisionable  = (this.affinityGroup != null)
						? storageDefinition.withAffinityGroup(this.affinityGroup)
						: storageDefinition.withRegion(this.region);
					storageProvisionable.provision();
					this.storageAccountName = storeName;
				}
	
				// Determine URL and verify location of VHD blob to use
				StorageAccount act = storageAccounts.get(this.storageAccountName);
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
					CloudServiceDefinitionBlank serviceDefinition = cloudServices.define(
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
					role.setRoleType(VirtualMachineRoleType.PersistentVMRole.toString());
					role.setOSVirtualHardDisk(osDisk);
					
					ArrayList<Role> roles = new ArrayList<Role>(Arrays.asList(role));
	
					// Create a new deployment
					final VirtualMachineCreateDeploymentParameters vmCreateParams = new VirtualMachineCreateDeploymentParameters();
					vmCreateParams.setRoles(roles);
					vmCreateParams.setDeploymentSlot(DeploymentSlot.Production);
					vmCreateParams.setLabel(this.deploymentLabel);
					vmCreateParams.setName(this.deploymentName);
					vmCreateParams.setVirtualNetworkName(this.network);
					
					compute.getVirtualMachinesOperations().createDeployment(
						(this.cloudServiceName != null) 
						? this.cloudServiceName
						: this.name,
						vmCreateParams);
					
				} else {
					// Get existing deployment from production
					final String deploymentName = compute.getDeploymentsOperations().getBySlot(this.cloudServiceName, DeploymentSlot.Production).getName();
					
					// Deploy into existing cloud service
					final VirtualMachineCreateParameters vmCreateParams = new VirtualMachineCreateParameters();
					vmCreateParams.setRoleName(this.name);
					vmCreateParams.setRoleSize(this.size);
					vmCreateParams.setConfigurationSets(configs);
					vmCreateParams.setOSVirtualHardDisk(osDisk);
					vmCreateParams.setProvisionGuestAgent(this.guestAgent);	
					compute.getVirtualMachinesOperations().create(this.cloudServiceName, deploymentName, vmCreateParams);
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

			public VirtualMachineImpl withAffinityGroup(String affinityGroup) {
				this.affinityGroup = affinityGroup;
				return this;
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

	
	// Nested class encapsulating the API related to virtual networks
	public class Networks implements 
		SupportsCreating<NetworkDefinitionBlank>,
		SupportsDeleting,
		SupportsListing {
		
		private Networks() {}

		// Returns information about existing network
		public Network get(String name) throws Exception {
			String networkConfig = networking.getNetworksOperations().getConfiguration().getConfiguration();

			// Correct for garbage prefix in XML returned by Azure
			networkConfig = networkConfig.substring(networkConfig.indexOf('<'));

			NetworkImpl network  = new NetworkImpl(name);
			final String siteXpath = "/*[local-name()='NetworkConfiguration']"
					+ "/*[local-name()='VirtualNetworkConfiguration']"
					+ "/*[local-name()='VirtualNetworkSites']"
					+ "/*[local-name()='VirtualNetworkSite' and @name='" + name + "']";

			final String cidrXpath = siteXpath + "/*[local-name()='AddressSpace']/*[local-name()='AddressPrefix']";
			network.cidr = findXMLNode(networkConfig, cidrXpath).getTextContent();

			// Determine affinity group
			for(VirtualNetworkSite site : networking.getNetworksOperations().list().getVirtualNetworkSites()) {
				if(site.getName().equalsIgnoreCase(name)) {
					network.affinityGroup = site.getAffinityGroup();
					network.label = site.getLabel();
					network.region = site.getLocation();
					// Read subnets
					for(com.microsoft.windowsazure.management.network.models.NetworkListResponse.Subnet s : site.getSubnets()) {
						// TODO: Other subnet properties
						
						NetworkImpl.SubnetImpl subnet = network.new SubnetImpl(s.getName());
						network.subnets.add(subnet);
						subnet.cidr = s.getAddressPrefix();
					}
					// TODO Other data
					break;
				}
			}
			
			return network;
		}
		
		// Starts a new network definition
		public NetworkDefinitionBlank define(String name) {
			return new NetworkImpl(name);
		}
		
		
		// Requests a network configuration update based on the XML netconfig representation
		private void updateNetworkConfig(String xml) throws Exception {
			NetworkSetConfigurationParameters params = new NetworkSetConfigurationParameters();
			params.setConfiguration(xml);
			networking.getNetworksOperations().setConfiguration(params);
		}
		
		
		// Deletes the specified network
		public void delete(String name) throws Exception {
			//  XPath to the network XML to delete
			final String xpath = String.format(
					"/*[local-name()='NetworkConfiguration']"
					+ "/*[local-name()='VirtualNetworkConfiguration']"
					+ "/*[local-name()='VirtualNetworkSites']"
					+ "/*[local-name()='VirtualNetworkSite' and @name='%s']", name);
			
			// Get current network configuration
			String networkConfig = networking.getNetworksOperations().getConfiguration().getConfiguration();
			
			// Correct for garbage prefix in XML returned by Azure
			networkConfig = networkConfig.substring(networkConfig.indexOf('<'));

			// Delete the network from the config using the xpath
			final String newConfig = deleteXMLElement(networkConfig, xpath);
			
			// Update the network configuration
			updateNetworkConfig(newConfig);
		}
		
		
		// Lists existing virtual networks
		public String[] list() {
			try {
				final ArrayList<VirtualNetworkSite> networks = networking.getNetworksOperations()
						.list().getVirtualNetworkSites();
				String[] names = new String[networks.size()];
				int i=0;
				for(VirtualNetworkSite network : networks) {
					names[i++] = network.getName();
				}
				return names;
			} catch (Exception e) {
				// Not very actionable, so just return an empty array
				return new String[0];
			}	
		}
		
		
		// Encapsulates the required and optional parameters for a network
		private class NetworkImpl 
			extends NamedImpl 
			implements 
				NetworkDefinitionBlank, 
				NetworkDefinitionWithCidr, 
				NetworkDefinitionProvisionable,
				NetworkUpdatable,
				Network {

			private String region, cidr, affinityGroup, label;
			private ArrayList<NetworkImpl.SubnetImpl> subnets = new ArrayList<NetworkImpl.SubnetImpl>();
			
			private NetworkImpl(String name) {
				super(name);
			}
						
			public NetworkImpl apply() throws Exception {
				// TODO Auto-generated method stub
				throw new NotImplementedException("Not yet implemented.");
			}
			
			public void delete() throws Exception {
				networks.delete(this.name);
			}

			public NetworkImpl withRegion(String region) {
				this.region = region;
				return this;
			}
			
			public NetworkImpl withCidr(String cidr) {
				this.cidr = cidr;
				return this;
			}
			
			
			// Provisions a new network
			public NetworkImpl provision() throws Exception {
				// If no subnets specified, create a default subnet containing the whole network
				if(this.subnets.size() == 0) {
					SubnetImpl subnet = new SubnetImpl("Subnet-1");
					subnet.cidr = this.cidr;
					this.subnets.add(subnet);
				}

				// Declare the subnets
				final String subnetTemplate = "<Subnet name=\"${subnetName}\"><AddressPrefix>${subnetCidr}</AddressPrefix></Subnet>";				
				StringBuilder subnetsSection = new StringBuilder();
				for(SubnetImpl subnet : this.subnets) {
					subnetsSection.append(subnetTemplate
						.replace("${subnetName}", subnet.name())
						.replace("${subnetCidr}", subnet.cidr()));
				}
				
				// Network site XML template
				final String networkTemplate = 
					"<VirtualNetworkSite name=\"${name}\" Location=\"${location}\">"
					+ "<AddressSpace><AddressPrefix>${cidr}</AddressPrefix></AddressSpace>"
					+ "<Subnets>${subnets}</Subnets>"
					+ "</VirtualNetworkSite>";
				
				// Create network site description based on the inputs and the template
				final String networkDescription = networkTemplate
					.replace("${name}", this.name)
					.replace("${location}", this.region)
					.replace("${cidr}", this.cidr)
					.replace("${subnets}", subnetsSection.toString());
				
				// Get current network configuration
				String networkConfig = networking.getNetworksOperations().getConfiguration().getConfiguration();
				
				// Correct for garbage prefix in XML returned by Azure
				networkConfig = networkConfig.substring(networkConfig.indexOf('<'));
				
				// XPath to the parent of virtual networks in network configuration XML
				final String parentXPath = "/*[local-name()='NetworkConfiguration']"
					+ "/*[local-name()='VirtualNetworkConfiguration']"
					+ "/*[local-name()='VirtualNetworkSites']";				

				// Add the new network to the configuration
				String newConfig = insertXMLElement(networkConfig, networkDescription, parentXPath);
				
				// Submit the new network config to Azure
				updateNetworkConfig(newConfig);
				
				return this;
			}
			
			public String cidr() {
				return this.cidr;
			}
			
			public String region() {
				return this.region;
			}

			public String affinityGroup() {
				return this.affinityGroup;
			}

			public String label() {
				return this.label;
			}

			@Override
			public Subnet[] subnets() {
				return subnets.toArray(new Subnet[0]);
			}
			
			// Implementation of Subnet
			private class SubnetImpl 
				extends NamedImpl 
				implements Network.Subnet {
				
				private String cidr;

				private SubnetImpl(String name) {
					super(name);
				}
				
				@Override
				public String cidr() {
					return this.cidr;
				}
			
			}

			@Override
			public NetworkDefinitionProvisionable withSubnet(String name, String cidr) {
				SubnetImpl subnet = new SubnetImpl(name);
				subnet.cidr = cidr;
				this.subnets.add(subnet);
				return this;
			}
		}
	}

	
	// Nested class encapsulating the API related to cloud services
	public class CloudServices implements 
		SupportsListing,
		SupportsReading<CloudService>,
		SupportsCreating<CloudServiceDefinitionBlank>,
		SupportsDeleting,
		SupportsUpdating<CloudServiceUpdatable> {
		
		private CloudServices() {}
		
		private class CloudServiceImpl 
			extends NamedImpl 
			implements 
				CloudServiceDefinitionBlank, 
				CloudServiceDefinitionProvisionable,
				CloudService,
				CloudServiceUpdatable {
			
			private String region, description, affinityGroup, label, reverseDnsFqdn;
			Calendar created, lastModified;
			
			private CloudServiceImpl(String name) {
				super(name.toLowerCase());
			}

			// Delete this cloud service
			public void delete() throws Exception {
				cloudServices.delete(this.name);
			}
			
			
			// Provision a new cloud service
			public CloudServiceImpl provision() throws Exception {
				final HostedServiceCreateParameters params = new HostedServiceCreateParameters();
				params.setAffinityGroup(this.affinityGroup);
				params.setDescription(this.description);
				params.setLabel((this.label == null) ? this.name : this.label);
				params.setLocation(this.region);
				params.setServiceName(this.name);
				params.setReverseDnsFqdn(this.reverseDnsFqdn);

				compute.getHostedServicesOperations().create(params);
				return this;
			}

			
			// Apply updates to the cloud service
			public CloudServiceImpl apply() throws Exception {
				HostedServiceUpdateParameters params = new HostedServiceUpdateParameters();
				params.setDescription(this.description);
				params.setLabel(this.label);
				params.setReverseDnsFqdn(this.reverseDnsFqdn);
				compute.getHostedServicesOperations().update(this.name, params);
				return this;
			}

			
			public CloudServiceImpl withRegion(String region) {
				this.region =region;
				return this;
			}
			
			public CloudServiceImpl withAffinityGroup(String affinityGroup) {
				this.affinityGroup = affinityGroup;
				throw new NotImplementedException("withNetwork() not yet implemented");
				//TODO: return this;
			}
			
			public CloudServiceImpl withDescription(String description) {
				this.description = description;
				return this;
			}
			
			public CloudServiceImpl withLabel(String label) {
				this.label = label;
				return this;
			}
			
			public CloudServiceImpl withReverseDnsFqdn(String fqdn) {
				this.reverseDnsFqdn= fqdn;
				return this;
			}
			
			public String region() {
				return this.region;
			}

			public String description() {
				return this.description;
			}

			public String label() {
				return this.label;
			}

			public String reverseDnsFqdn() {
				return this.reverseDnsFqdn;
			}

			public Calendar created() {
				return this.created;
			}

			public Calendar modified() {
				return this.lastModified;
			}

			public String affinityGroup() {
				return this.affinityGroup;
			}
		}
		
		
		// Starts a new cloud service definition
		public CloudServiceDefinitionBlank define(String name) {
			return new CloudServiceImpl(name);
		}
		
		
		// Deletes the specified cloud service
		public void delete(String name) throws Exception {
			compute.getHostedServicesOperations().delete(name);
		}
		
		
		// Starts a cloud service update
		public CloudServiceUpdatable update(String name) {
			return new CloudServiceImpl(name);
		}
		
		
		// Return the list of cloud services
		public String[] list() {
			try {
				final ArrayList<HostedService> services = compute.getHostedServicesOperations()
						.list().getHostedServices();
				String[] names = new String[services.size()];
				int i = 0;
				for(HostedService cloudService: services) {
					names[i++]= cloudService.getServiceName();
				}
				return names;
			} catch (Exception e) {
				// Not very actionable, so just return an empty array
				return new String[0];
			}
		}
		
		
		// Return the specified cloud service information
		public CloudService get(String name) throws Exception {
			CloudServiceImpl cloudService = new CloudServiceImpl(name);
			HostedServiceGetResponse response = compute.getHostedServicesOperations().get(name);
			cloudService.description = response.getProperties().getDescription();
			cloudService.label = response.getProperties().getLabel();
			cloudService.region = response.getProperties().getLocation();
			cloudService.reverseDnsFqdn = response.getProperties().getReverseDnsFqdn();
			cloudService.created = response.getProperties().getDateCreated();
			cloudService.lastModified = response.getProperties().getDateLastModified();
			cloudService.affinityGroup = response.getProperties().getAffinityGroup();

			return cloudService;
		}
	}
	
	
	// Nested class encapsulating the API related to storage accounts
	public class StorageAccounts implements 
		SupportsCreating<StorageAccountDefinitionBlank>, 
		SupportsUpdating<StorageAccountUpdatable>,
		SupportsListing,
		SupportsReading<StorageAccount>,
		SupportsDeleting {
		
		private StorageAccounts() {}
		
		// Starts a new storage account update
		public StorageAccountUpdatable update(String name) {
			return new StorageAccountImpl(name);
		}

		
		// Starts a new storage account definition
		public StorageAccountDefinitionBlank define(String name) {
			return new StorageAccountImpl(name);
		}
		
		
		// Deletes the specified storage account
		public void delete(String accountName) throws IOException, ServiceException {
			storage.getStorageAccountsOperations().delete(accountName);
		}
		
		
		// Return the list of storage accounts
		public String[] list() {
			try {
				final ArrayList<com.microsoft.windowsazure.management.storage.models.StorageAccount> storageAccounts = storage.getStorageAccountsOperations()
						.list().getStorageAccounts();
				String[] names = new String[storageAccounts.size()];
				int i = 0;
				for(com.microsoft.windowsazure.management.storage.models.StorageAccount store: storageAccounts) {
					names[i++]= store.getName();
				}
				return names;
			} catch (Exception e) {
				// Not very actionable, so just return an empty array
				return new String[0];
			}
		}
		
		
		// Gets storage account information
		public StorageAccount get(String name) throws Exception {
			StorageAccountImpl storageAccount = new StorageAccountImpl(name);
			StorageAccountGetResponse response = storage.getStorageAccountsOperations().get(name);
			StorageAccountProperties properties =  response.getStorageAccount().getProperties();
			storageAccount.affinityGroup = properties.getAffinityGroup();
			storageAccount.description = properties.getDescription();
			storageAccount.label = properties.getLabel();
			storageAccount.geoPrimaryRegion = properties.getGeoPrimaryRegion();
			storageAccount.geoSecondaryRegion = properties.getGeoSecondaryRegion();
			storageAccount.region = properties.getLocation();
			storageAccount.status = properties.getStatus();
			storageAccount.lastFailoverTime = properties.getLastGeoFailoverTime();
			storageAccount.geoPrimaryRegionStatus = properties.getStatusOfGeoPrimaryRegion();
			storageAccount.geoSecondaryRegionStatus = properties.getStatusOfGeoSecondaryRegion();
			storageAccount.endpoints = properties.getEndpoints().toArray(new URI[0]);
			storageAccount.type = properties.getAccountType();
			return storageAccount;
		}

		
		// Nested class encapsulating the API related to creating new storage accounts
		private class StorageAccountImpl 
			extends NamedImpl
			implements 
				StorageAccountDefinitionBlank, 
				StorageAccountDefinitionProvisionable,
				StorageAccountUpdatable,
				StorageAccount {
			
			private String region, affinityGroup, type, label, description, geoPrimaryRegion, geoSecondaryRegion;
			private StorageAccountStatus status;
			private Calendar lastFailoverTime;
			private GeoRegionStatus geoPrimaryRegionStatus, geoSecondaryRegionStatus;
			public URI[] endpoints;
			
			private StorageAccountImpl(String name) {
				super(name.toLowerCase());
			}
			
			// Creates a new storage account
			public StorageAccountImpl provision() throws Exception {
				final StorageAccountCreateParameters params = new StorageAccountCreateParameters();
				params.setName(this.name.toLowerCase());
				params.setLocation(this.region);
				params.setAffinityGroup(this.affinityGroup);
				params.setDescription(this.description);
				params.setLabel((this.label == null) ? this.name : this.label);
				params.setAccountType((this.type == null) ? StorageAccountTypes.STANDARD_LRS : this.type);
				storage.getStorageAccountsOperations().create(params);
				return this;
			}
			
						
			// Apply changes to the storage account
			public StorageAccountImpl apply() throws Exception {
				StorageAccountUpdateParameters params = new StorageAccountUpdateParameters();
				params.setAccountType(this.type);
				params.setDescription(this.description);
				params.setLabel(this.label);
				storage.getStorageAccountsOperations().update(this.name, params);
				return this;
			}

			
			// Deletes this storage account
			public void delete() throws Exception {
				storageAccounts.delete(this.name);
			}

			
			public StorageAccountImpl withRegion(String region) {
				this.region =region;
				return this;
			}
						
			public StorageAccountImpl withType(String type) {
				this.type = type;
				return this;
			}
			
			public StorageAccountImpl withLabel(String label) {
				this.label= label;
				return this;
			}
			
			public StorageAccountImpl withDescription(String description) {
				this.description = description;
				return this;
			}

			public StorageAccountDefinitionProvisionable withAffinityGroup(String affinityGroup) {
				this.affinityGroup = affinityGroup;
				return this;
			}

			public String description() {
				return this.description;
			}

			public String label() {
				return this.label;
			}

			public String geoPrimaryRegion() {
				return this.geoPrimaryRegion;
			}

			public GeoRegionStatus geoPrimaryRegionStatus() {
				return this.geoPrimaryRegionStatus;
			}

			public String geoSecondaryRegion() {
				return this.geoSecondaryRegion;
			}

			public GeoRegionStatus geoSecondaryRegionStatus() {
				return this.geoSecondaryRegionStatus;
			}

			public String region() {
				return this.region;
			}

			public StorageAccountStatus status() {
				return this.status;
			}

			public Calendar lastGeoFailoverTime() {
				return this.lastFailoverTime;
			}

			public URI[] endpoints() {
				return this.endpoints;
			}

			public String type() {
				return this.type;
			}


			public String affinityGroup() {
				return this.affinityGroup;
			}

		}		
	}
	
	
	// Nested class encapsulating the API related to OS images
	public class OSImages implements 
		SupportsListing,
		SupportsReading<OSImage> {
		
		private OSImages() {}

		// Returns the list of available OS image names
		public String[] list() {
			try {
				ArrayList<VirtualMachineOSImage> images = compute.getVirtualMachineOSImagesOperations().list().getImages();
				String[] names = new String[images.size()];
				int i=0;
				for(VirtualMachineOSImage image : images) {
					names[i++] = image.getName();
					//image.
				}
				return names;
			} catch (Exception e) {
				// Not very actionable, so just return an empty array
				return new String[0];
			}
		}
		
		
		// Returns OS image information
		public OSImage get(String name) throws Exception {
			OSImageImpl osImage = new OSImageImpl(name);
			VirtualMachineOSImageGetResponse response = compute.getVirtualMachineOSImagesOperations().get(name);
			osImage.category = response.getCategory();
			osImage.description = response.getDescription();
			osImage.eula = response.getEula();
			osImage.iconUri = response.getIconUri();
			osImage.family = response.getImageFamily();
			osImage.ioType = response.getIOType();
			osImage.label = response.getLabel();
			osImage.language = response.getLanguage();
			osImage.regions = response.getLocation().split(";");
			osImage.logicalSizeInGB = response.getLogicalSizeInGB();
			osImage.mediaLink = response.getMediaLinkUri();
			osImage.operatingSystemType = response.getOperatingSystemType();
			osImage.privacyUri = response.getPrivacyUri();
			osImage.publishedDate = response.getPublishedDate();
			osImage.publisher = response.getPublisherName();
			osImage.recommendedVMSize = response.getRecommendedVMSize();
			osImage.smallIconUri = response.getSmallIconUri();
			osImage.isPremium = response.isPremium();
			osImage.isShownInGui = response.isShowInGui();
			
			return osImage;
		}

		
		// Encapsulated information about an image
		private class OSImageImpl 
			extends NamedImpl 
			implements OSImage {
			
			private String category, description, eula, family, ioType, label, language, operatingSystemType, publisher, recommendedVMSize;
			private String[] regions;
			private URI privacyUri, mediaLink, iconUri, smallIconUri;
			private Calendar publishedDate;
			private boolean isPremium, isShownInGui;
			private double logicalSizeInGB;
			
			private OSImageImpl(String name) {
				super(name);
			}

			public String category() {
				return this.category;
			}

			public String description() {
				return this.description;
			}

			public String eula() {
				return this.eula;
			}

			public URI iconUri() {
				return this.iconUri;
			}

			public String family() {
				return this.family;
			}

			public String ioType() {
				return this.ioType;
			}

			public String label() {
				return this.label;
			}

			public String language() {
				return this.language;
			}

			public String[] regions() {
				return this.regions;
			}

			public double logicalSizeInGB() {
				return this.logicalSizeInGB;
			}

			public URI mediaLink() {
				return this.mediaLink;
			}

			public String operatingSystemType() {
				return this.operatingSystemType;
			}

			public URI privacyUri() {
				return this.privacyUri;
			}

			public Calendar publishedDate() {
				return this.publishedDate;
			}

			public String publisher() {
				return this.publisher;
			}

			public String recommendedVMSize() {
				return this.recommendedVMSize;
			}

			public URI smallIconUri() {
				return this.smallIconUri;
			}

			public boolean isPremium() {
				return this.isPremium;
			}

			public boolean isShownInGui() {
				return this.isShownInGui;
			}
		}
		
		
	}

	
	// Nested class encapsulating the API related to VM sizes
	public class Sizes implements 
		SupportsListing {
		
		private Sizes() {}
		
		// Return the list of available size names supporting the specified type of compute service
		public String[] list(boolean supportingVM, boolean supportingCloudServices) {
			try {
				ArrayList<RoleSize> sizes = management.getRoleSizesOperations().list().getRoleSizes();
				String[] names = new String[sizes.size()];
				int i=0;
				for(RoleSize size : sizes) {
					if(supportingVM && size.isSupportedByVirtualMachines() 
					|| supportingCloudServices && size.isSupportedByWebWorkerRoles()) {
						names[i++] = size.getName();
					}
				}
				return names;
			} catch (Exception e) {
				// Not very actionable, so just return an empty array
				return new String[0];
			}			
		}

		// Returns all available size names
		public String[] list() {
			return list(true, true);
		}
	}
	
	
	// Nested class encapsulating the API related to locations
	public class Regions implements 
		SupportsListing {
		private Regions() {}
		
		// Return the list of available region names supporting the specified service type, 
		// which must be one of the constants from the LocationAvailableServiceNames class, or all if null
		public String[] list(String serviceType) {
			try {
				ArrayList<Location> locations = management.getLocationsOperations().list().getLocations();
				String[] names = new String[locations.size()];
				int i=0;
				for(Location location : locations) {
					if(null == serviceType || location.getAvailableServices().contains(serviceType)) {
						names[i++] = location.getName();
					}
				}
				return names;
			} catch (Exception e) {
				// Not very actionable, so just return an empty array
				return new String[0];
			}
		}

		// Lists all regions
		public String[] list() {
			return list(null);
		}
	}

	
	// Returns the first node matching the xpath in the xml
	private static Node findXMLNode(String xml, String xpath) throws XPathExpressionException {
		final InputSource parentSource = new InputSource(new StringReader(xml));
		final XPath xpathObject = XPathFactory.newInstance().newXPath();
		return (Node) xpathObject.evaluate(xpath, parentSource, XPathConstants.NODE);
	}
	
	
	// Returns the XML document as a string
	private static String XMLtoString(Document doc) {
		try {
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			StringWriter writer = new StringWriter();
			transformer.transform(new DOMSource(doc), new StreamResult(writer));
			return writer.getBuffer().toString();
		} catch(Exception e) {
			return null;
		}
	}
	
	
	// Inserts XML string as a child node into another XML string based on the provided xpath
	private static String insertXMLElement(String parentXML, String childXMLElement, String parentXPath) {
		try {
			// Find parent node based on XPath
			final Node parentNode = findXMLNode(parentXML, parentXPath);

			// Parse child XML as Node to insert
			final InputSource insertionSource = new InputSource(new StringReader(childXMLElement));
			final Document childDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(insertionSource);

			// Insert as the last child of the parent
			final Document parentDoc = parentNode.getOwnerDocument();
			parentNode.appendChild(parentDoc.importNode(childDoc.getDocumentElement(), true));

			// Transform into a string
			return XMLtoString(parentDoc);
		} catch (Exception e) {
			return null;
		}
	}
	
	
	// Deletes the XML element from the provided XML string based on the XPath
	private static String deleteXMLElement(String xml, String xpath) {
		try {
			final Node node = findXMLNode(xml, xpath);
			Node parent = node.getParentNode();
			parent.removeChild(node);
			return XMLtoString(parent.getOwnerDocument());
		} catch (XPathExpressionException e) {
			return null;
		}
	}
}
