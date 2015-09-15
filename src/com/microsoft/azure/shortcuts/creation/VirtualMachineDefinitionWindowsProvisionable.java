package com.microsoft.azure.shortcuts.creation;

import com.microsoft.azure.shortcuts.creation.VirtualMachineDefinitionWindowsProvisionable;

// Windows-specific optional parameters
public interface VirtualMachineDefinitionWindowsProvisionable extends VirtualMachineDefinitionProvisionable {
	VirtualMachineDefinitionWindowsProvisionable withAutoUpdate(boolean autoUpdate);
	VirtualMachineDefinitionWindowsProvisionable withComputerName(String name) throws Exception;
}
