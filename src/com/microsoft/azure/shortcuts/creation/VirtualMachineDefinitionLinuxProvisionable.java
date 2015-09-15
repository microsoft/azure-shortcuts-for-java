package com.microsoft.azure.shortcuts.creation;

import com.microsoft.azure.shortcuts.creation.VirtualMachineDefinitionLinuxProvisionable;
import com.microsoft.azure.shortcuts.creation.VirtualMachineDefinitionProvisionable;


// Linux-specific optional parameters
public interface VirtualMachineDefinitionLinuxProvisionable extends VirtualMachineDefinitionProvisionable {
	VirtualMachineDefinitionLinuxProvisionable withHostName(String name) throws Exception;		
}
