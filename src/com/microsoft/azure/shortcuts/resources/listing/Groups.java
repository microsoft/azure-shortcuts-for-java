package com.microsoft.azure.shortcuts.resources.listing;

import com.microsoft.azure.shortcuts.common.implementation.SupportsCreating;
import com.microsoft.azure.shortcuts.common.implementation.SupportsDeleting;
import com.microsoft.azure.shortcuts.common.implementation.SupportsListing;
import com.microsoft.azure.shortcuts.common.implementation.SupportsReading;
import com.microsoft.azure.shortcuts.common.implementation.SupportsUpdating;
import com.microsoft.azure.shortcuts.resources.creation.GroupDefinitionBlank;
import com.microsoft.azure.shortcuts.resources.reading.Group;
import com.microsoft.azure.shortcuts.resources.updating.GroupUpdatableBlank;

public interface Groups 
	extends 
		SupportsListing,
		SupportsReading<Group>,
		SupportsDeleting,
		SupportsUpdating<GroupUpdatableBlank>,
		SupportsCreating<GroupDefinitionBlank> {
}
