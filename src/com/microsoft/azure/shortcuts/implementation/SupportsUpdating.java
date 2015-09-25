package com.microsoft.azure.shortcuts.implementation;


// Requires class to support updating entities
public interface SupportsUpdating<T> {
	T update(String name);
}
