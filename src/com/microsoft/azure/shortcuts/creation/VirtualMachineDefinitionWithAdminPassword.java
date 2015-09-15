package com.microsoft.azure.shortcuts.creation;

public interface VirtualMachineDefinitionWithAdminPassword {
	VirtualMachineDefinitionWithImage withAdminPassword(String password);
}
