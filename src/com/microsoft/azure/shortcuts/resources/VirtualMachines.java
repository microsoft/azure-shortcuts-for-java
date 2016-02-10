package com.microsoft.azure.shortcuts.resources;

import com.microsoft.azure.shortcuts.common.SupportsCreating;
import com.microsoft.azure.shortcuts.common.SupportsDeleting;
import com.microsoft.azure.shortcuts.common.SupportsGetting;
import com.microsoft.azure.shortcuts.common.SupportsListing;
import com.microsoft.azure.shortcuts.resources.common.SupportsDeletingByGroup;
import com.microsoft.azure.shortcuts.resources.common.SupportsGettingByGroup;
import com.microsoft.azure.shortcuts.resources.common.SupportsListingByGroup;

public interface VirtualMachines extends
	SupportsListing<VirtualMachine>,
	SupportsListingByGroup<VirtualMachine>,
	SupportsGetting<VirtualMachine>,
	SupportsGettingByGroup<VirtualMachine>,
	SupportsCreating<VirtualMachine.DefinitionBlank>, 
	SupportsDeleting,
	SupportsDeletingByGroup {}
