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
import java.util.Calendar;
import java.util.Map;

import com.microsoft.azure.shortcuts.common.Deletable;
import com.microsoft.azure.shortcuts.common.Named;
import com.microsoft.azure.shortcuts.common.Provisionable;
import com.microsoft.azure.shortcuts.common.Refreshable;
import com.microsoft.azure.shortcuts.common.Updatable;
import com.microsoft.windowsazure.management.compute.models.DeploymentSlot;
import com.microsoft.windowsazure.management.compute.models.DeploymentStatus;
import com.microsoft.windowsazure.management.compute.models.HostedServiceListResponse.HostedService;

public interface VirtualMachine extends 
	Named,
	Refreshable<VirtualMachine> {
	
	String size() throws Exception;
	String deployment() throws Exception;
	String cloudService() throws Exception;
	String network() throws Exception;
	String region() throws Exception;
	String affinityGroup() throws Exception;
	DeploymentStatus status() throws Exception;
	//boolean isLinux() throws Exception; //TODO: Currently broken in the SDK/Azure
	//boolean isWindows() throws Exception; // TODO: Currently broken in the SDK/Azure
	String roleName() throws Exception;
	Calendar createdTime() throws Exception;
	DeploymentSlot deploymentSlot() throws Exception;
	Map<String, String> extendedDeploymentProperties() throws Exception;
	Calendar lastModifiedTime() throws Exception;
	String reservedIPName() throws Exception;
	URI deploymentUri() throws Exception;
	Boolean isDeploymentLocked() throws Exception;
	String availabilitySet() throws Exception;
	String defaultWinRmCertificateThumbprint() throws Exception;
	String roleLabel() throws Exception;
	URI mediaLocation() throws Exception;
	String osVersion() throws Exception;
	String imageName() throws Exception;
	boolean hasGuestAgent() throws Exception;
	String deploymentLabel() throws Exception;
	
	
	/**
	 * A new blank virtual machine definition
	 */
	public interface DefinitionBlank {
		DefinitionWithSize withRegion(String region);
		DefinitionWithSize withRegion(Region region);
		DefinitionWithSize withNetwork(String network);
		DefinitionWithSize withNetwork(Network network);
		DefinitionWithSize withExistingCloudService(String serviceName);
		DefinitionWithSize withExistingCloudService(CloudService cloudService);
		DefinitionWithSize withExistingCloudService(HostedService hostedService);
	}

	
	/**
	 * A new virtual machine definition with sufficient input parameters specified to be provisioned in the cloud
	 */
	public interface DefinitionProvisionable extends Provisionable<UpdateBlank> {
		DefinitionProvisionable withTcpEndpoint(int publicPort);
		DefinitionProvisionable withTcpEndpoint(int publicPort, int privatePort);
		DefinitionProvisionable withTcpEndpoint(int publicPort, int privatePort, String name);
		DefinitionProvisionable withGuestAgent(boolean enabled);
		DefinitionProvisionable withDeployment(String name);
		DefinitionProvisionable withDeploymentLabel(String name);
		DefinitionProvisionable withExistingStorageAccount(String name);
		DefinitionProvisionable withExistingStorageAccount(StorageAccount account);
		DefinitionProvisionable withExistingStorageAccount(com.microsoft.windowsazure.management.storage.models.StorageAccount account);
		DefinitionProvisionable withNewCloudService(String name);
		DefinitionProvisionable withNewCloudService(CloudService.DefinitionProvisionable cloudServiceDefinition);
		DefinitionProvisionable withSubnet(String subnet);
	}
	
	
	/**
	 * A new Linux virtual machine definition with sufficient input parameters specified to be provisioned in the cloud
	 */
	public interface DefinitionLinuxProvisionable extends DefinitionProvisionable {
		DefinitionLinuxProvisionable withHostName(String name) throws Exception;		
	}

	
	/**
	 * A new Windows virtual machine definition with sufficient input parameters specified to be provisioned in the cloud
	 */
	public interface DefinitionWindowsProvisionable extends VirtualMachine.DefinitionProvisionable {
		DefinitionWindowsProvisionable withAutoUpdate(boolean autoUpdate);
		DefinitionWindowsProvisionable withComputerName(String name) throws Exception;
	}
	
	
	/**
	 * A new virtual machine definition requiring the admin username to be specified
	 */
	public interface DefinitionWithAdminUsername {
		DefinitionWithAdminPassword withAdminUsername(String name); 
	}
	
	
	/**
	 * A new virtual machine definition requiring the admin password to be specified
	 */
	public interface DefinitionWithAdminPassword {
		DefinitionWithImage withAdminPassword(String password);
	}
	
	
	/**
	 * A new virtual machine definition requiring a Linux or Windows images to be specified
	 */
	public interface DefinitionWithImage {
		DefinitionLinuxProvisionable withLinuxImage(String image);		
		DefinitionWindowsProvisionable withWindowsImage(String image);
	}
	

	/**
	 * A new virtual machine definition requiring a VM size to be specified
	 */
	public interface DefinitionWithSize {
		DefinitionWithAdminUsername withSize(String size);
	}

	
	/**
	 * An existing virtual machine update request ready to be applied in the cloud
	 */
	public interface Update extends UpdateBlank, Updatable<Update> {
	}
	

	/**
	 * A blank update request for an existing virtual machine
	 */
	public interface UpdateBlank extends Deletable {
	}
}

