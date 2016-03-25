package com.microsoft.azure.shortcuts.resources;

import com.microsoft.azure.shortcuts.common.SupportsCreating;
import com.microsoft.azure.shortcuts.common.SupportsDeleting;
import com.microsoft.azure.shortcuts.common.SupportsGetting;
import com.microsoft.azure.shortcuts.common.SupportsListingAsMap;
import com.microsoft.azure.shortcuts.resources.common.SupportsDeletingByGroup;
import com.microsoft.azure.shortcuts.resources.common.SupportsGettingByGroup;
import com.microsoft.azure.shortcuts.resources.common.SupportsListingAsMapByGroup;

public interface VirtualMachines extends
	SupportsListingAsMap<VirtualMachine>,
	SupportsListingAsMapByGroup<VirtualMachine>,
	SupportsGetting<VirtualMachine>,
	SupportsGettingByGroup<VirtualMachine>,
	SupportsCreating<VirtualMachine.DefinitionBlank>, 
	SupportsDeleting,
	SupportsDeletingByGroup {}
