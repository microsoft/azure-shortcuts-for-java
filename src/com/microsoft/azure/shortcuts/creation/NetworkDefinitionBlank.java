package com.microsoft.azure.shortcuts.creation;

import com.microsoft.azure.shortcuts.creation.NetworkDefinitionWithCidr;

public interface NetworkDefinitionBlank {
	NetworkDefinitionWithCidr withRegion(String region);
}
