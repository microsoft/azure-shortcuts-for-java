package com.microsoft.azure.shortcuts.creation;

import com.microsoft.azure.shortcuts.creation.VirtualMachineDefinitionProvisionable;
import com.microsoft.azure.shortcuts.updating.VirtualMachineUpdatable;


// Optional parameters
public interface VirtualMachineDefinitionProvisionable extends Provisionable<VirtualMachineUpdatable> {
	VirtualMachineDefinitionProvisionable withTcpEndpoint(int publicPort);
	VirtualMachineDefinitionProvisionable withTcpEndpoint(int publicPort, int privatePort);
	VirtualMachineDefinitionProvisionable withTcpEndpoint(int publicPort, int privatePort, String name);
	VirtualMachineDefinitionProvisionable withGuestAgent(boolean enabled);
	VirtualMachineDefinitionProvisionable withDeployment(String name);
	VirtualMachineDefinitionProvisionable withDeploymentLabel(String name);
	VirtualMachineDefinitionProvisionable withStorageAccount(String name);
	VirtualMachineDefinitionProvisionable withNewCloudService(String name);
	VirtualMachineDefinitionProvisionable withSubnet(String subnet);
}
