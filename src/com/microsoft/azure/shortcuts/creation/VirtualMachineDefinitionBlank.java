package com.microsoft.azure.shortcuts.creation;

import com.microsoft.azure.shortcuts.creation.VirtualMachineDefinitionWithSize;

public interface VirtualMachineDefinitionBlank {
	VirtualMachineDefinitionWithSize withRegion(String region);
	VirtualMachineDefinitionWithSize withNetwork(String network);
	VirtualMachineDefinitionWithSize withExistingCloudService(String service);
	VirtualMachineDefinitionWithSize withAffinityGroup(String affinityGroup);
}

