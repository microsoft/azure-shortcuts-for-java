package com.microsoft.azure.shortcuts.resources;

import com.microsoft.azure.shortcuts.common.SupportsGetting;
import com.microsoft.azure.shortcuts.common.SupportsListingEntities;
import com.microsoft.azure.shortcuts.resources.common.SupportsGettingByGroup;
import com.microsoft.azure.shortcuts.resources.common.SupportsListingEntitiesByGroup;

public interface VirtualMachines extends
	SupportsListingEntities<VirtualMachine>,
	SupportsListingEntitiesByGroup<VirtualMachine>,
	SupportsGetting<VirtualMachine>,
	SupportsGettingByGroup<VirtualMachine>{
}
