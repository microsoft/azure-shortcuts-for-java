package com.microsoft.azure.shortcuts.implementation;


// Requires class to support deleting entities
public interface SupportsDeleting {
	void delete(String name) throws Exception;
}
