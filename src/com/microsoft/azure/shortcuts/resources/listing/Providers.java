package com.microsoft.azure.shortcuts.resources.listing;

import com.microsoft.azure.shortcuts.common.implementation.SupportsListing;
import com.microsoft.azure.shortcuts.common.implementation.SupportsReading;
import com.microsoft.azure.shortcuts.resources.reading.Provider;

public interface Providers extends 
	SupportsListing,
	SupportsReading<Provider> {
}
