package com.microsoft.azure.shortcuts.reading;

import java.net.URI;
import java.util.Calendar;

public interface OSImage extends Named {
	String category();
	String description();
	String eula();
	URI iconUri();
	String family();
	String ioType();
	String label();
	String language();
	String[] regions();
	double logicalSizeInGB();
	URI mediaLink();
	String operatingSystemType();
	URI privacyUri();
	Calendar publishedDate();
	String publisher();
	String recommendedVMSize();
	URI smallIconUri();
	boolean isPremium();
	boolean isShownInGui();
}
