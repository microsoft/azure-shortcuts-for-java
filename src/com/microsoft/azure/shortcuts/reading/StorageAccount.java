package com.microsoft.azure.shortcuts.reading;

import java.net.URI;
import java.util.Calendar;

import com.microsoft.windowsazure.management.storage.models.GeoRegionStatus;
import com.microsoft.windowsazure.management.storage.models.StorageAccountStatus;


public interface StorageAccount extends Named {
	String affinityGroup();
	String description();
	String label();
	String geoPrimaryRegion();
	GeoRegionStatus geoPrimaryRegionStatus();
	String geoSecondaryRegion();
	GeoRegionStatus geoSecondaryRegionStatus();
	String region();
	StorageAccountStatus status();
	Calendar lastGeoFailoverTime();
	URI[] endpoints();
	String type();
}
