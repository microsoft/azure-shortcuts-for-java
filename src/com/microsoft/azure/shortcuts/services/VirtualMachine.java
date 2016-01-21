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
import com.microsoft.azure.shortcuts.common.Indexable;
import com.microsoft.azure.shortcuts.common.Provisionable;
import com.microsoft.azure.shortcuts.common.Refreshable;
import com.microsoft.azure.shortcuts.common.Updatable;
import com.microsoft.windowsazure.management.compute.models.DeploymentSlot;
import com.microsoft.windowsazure.management.compute.models.DeploymentStatus;
import com.microsoft.windowsazure.management.compute.models.HostedServiceListResponse.HostedService;

public interface VirtualMachine extends 
	Indexable,
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
	 * A virtual machine definition requiring a region (location) to be specified 
	 */
	public interface DefinitionWithRegion<T> {
		T withRegion(String region);
		T withRegion(Region region);
	}
	
	/**
	 * A virtual machine definition requiring an existing virtual network to be specified
	 */
	public interface DefinitionWithExistingNetwork<T> {
		T withExistingNetwork(String network);
		T withExistingNetwork(Network network);		
	}
	
	
	/**
	 * A virtual machine definition requiring an existing cloud service to be specified
	 */
	public interface DefinitionWithExistingCloudService<T> {
		T withExistingCloudService(String serviceName);
		T withExistingCloudService(CloudService cloudService);
		T withExistingCloudService(HostedService hostedService);
	}
	
	/**
	 * A new blank virtual machine definition requiring the initial required set of parameters to be specified
	 */
	public interface DefinitionBlank extends 
		DefinitionWithRegion<DefinitionWithSize>,
		DefinitionWithExistingNetwork<DefinitionWithSize>,
		DefinitionWithExistingCloudService<DefinitionWithSize> {
	}

	
	/**
	 * A virtual machine definition requiring a TCP endpoint to be specified
	 */
	public interface DefinitionWithTcpEndpoint<T> {
		T withTcpEndpoint(int publicPort);
		T withTcpEndpoint(int publicPort, int privatePort);
		T withTcpEndpoint(int publicPort, int privatePort, String name);		
	}

	/**
	 * A virtual machine definition requiring the presence of a guest agent to be specified
	 */
	public interface DefinitionWithGuestAgent<T> {
		T withGuestAgent(boolean enabled);		
	}
	
	/**
	 * A virtual machine definition requiring a deployment name to be specified
	 */
	public interface DefinitionWithDeployment<T> {
		T withDeployment(String name);
	}
	
	/**
	 * A virtual machine definition requiring a deployment label to be specified
	 */
	public interface DefinitionWithDeploymentLabel<T> {
		T withDeploymentLabel(String label);
	}

	/**
	 * A virtual machine definition requiring an existing storage account to specified
	 */
	public interface DefinitionWithExistingStorageAccount<T> {
		T withExistingStorageAccount(String name);
		T withExistingStorageAccount(StorageAccount account);
		T withExistingStorageAccount(com.microsoft.windowsazure.management.storage.models.StorageAccount account);
	}

	/**
	 * A virtual machine definition requiring an new cloud service to be specified
	 */
	public interface DefinitionWithNewCloudService<T> {
		T withNewCloudService(String name);
		T withNewCloudService(CloudService.DefinitionProvisionable cloudServiceDefinition);
	}

	/**
	 * A virtual machine definition requiring a subnet to be specified
	 */
	public interface DefinitionWithSubnet<T> {
		T withSubnet(String subnet);
	}

	
	/**
	 * A new virtual machine definition with sufficient input parameters specified to be provisioned in the cloud
	 */
	public interface DefinitionProvisionable extends 
		DefinitionWithTcpEndpoint<DefinitionProvisionable>,
		DefinitionWithGuestAgent<DefinitionProvisionable>,
		DefinitionWithDeployment<DefinitionProvisionable>,
		DefinitionWithDeploymentLabel<DefinitionProvisionable>,
		DefinitionWithExistingStorageAccount<DefinitionProvisionable>,
		DefinitionWithNewCloudService<DefinitionProvisionable>,
		DefinitionWithSubnet<DefinitionProvisionable>,
		Provisionable<UpdateBlank> {
	}
	
	
	/**
	 * A new virtual machine definition requiring the host name to be specified
	 */
	public interface DefinitionWithHostName<T> {
		T withHostName(String name) throws Exception;
	}
	
	/**
	 * A new Linux virtual machine definition with sufficient input parameters specified to be provisioned in the cloud
	 */
	public interface DefinitionLinuxProvisionable extends 
		DefinitionWithHostName<DefinitionLinuxProvisionable>,
		DefinitionProvisionable {
	}
	
	/** 
	 * A new virtual machine definition requiring the enablement of AutoUpdate to be specified
	 */
	public interface DefinitionWithAutoUpdate<T> {
		T withAutoUpdate(boolean autoUpdate);
	}

	/** 
	 * A new virtual machine definition requiring the computer name to be specified
	 */
	public interface WithComputerName<T> {
		T withComputerName(String name) throws Exception;
	}

	/**
	 * A new Windows virtual machine definition with sufficient input parameters specified to be provisioned in the cloud
	 */
	public interface DefinitionWindowsProvisionable extends 
		DefinitionWithAutoUpdate<DefinitionWindowsProvisionable>,
		WithComputerName<DefinitionWindowsProvisionable>,
		DefinitionProvisionable {
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

