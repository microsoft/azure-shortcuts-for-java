package com.microsoft.azure.shortcuts.updating;

public interface CloudServiceUpdatable extends Updatable<CloudServiceUpdatable> {
	CloudServiceUpdatable withDescription(String description);
	CloudServiceUpdatable withLabel(String label);
	CloudServiceUpdatable withReverseDnsFqdn(String fqdn);
}
