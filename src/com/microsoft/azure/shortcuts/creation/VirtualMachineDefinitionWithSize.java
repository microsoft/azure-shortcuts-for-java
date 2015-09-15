package com.microsoft.azure.shortcuts.creation;

import com.microsoft.azure.shortcuts.creation.VirtualMachineDefinitionWithAdminUsername;

public interface VirtualMachineDefinitionWithSize {
	VirtualMachineDefinitionWithAdminUsername withSize(String size);
}
