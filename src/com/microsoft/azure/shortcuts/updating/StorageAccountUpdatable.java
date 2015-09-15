package com.microsoft.azure.shortcuts.updating;

public interface StorageAccountUpdatable extends Updatable<StorageAccountUpdatable> {
	StorageAccountUpdatable withType(String type);
	StorageAccountUpdatable withDescription(String description);
	StorageAccountUpdatable withLabel(String label);
}
