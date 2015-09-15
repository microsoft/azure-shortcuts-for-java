package com.microsoft.azure.shortcuts.creation;

import com.microsoft.azure.shortcuts.creation.VirtualMachineDefinitionWithAdminPassword;

public interface VirtualMachineDefinitionWithAdminUsername {
	VirtualMachineDefinitionWithAdminPassword withAdminUsername(String name); 
}
