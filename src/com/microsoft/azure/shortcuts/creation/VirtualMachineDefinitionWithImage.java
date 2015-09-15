package com.microsoft.azure.shortcuts.creation;

public interface VirtualMachineDefinitionWithImage {
	VirtualMachineDefinitionLinuxProvisionable withLinuxImage(String image);		
	VirtualMachineDefinitionWindowsProvisionable withWindowsImage(String image);
}