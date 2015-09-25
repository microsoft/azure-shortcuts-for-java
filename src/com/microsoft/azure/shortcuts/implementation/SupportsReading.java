package com.microsoft.azure.shortcuts.implementation;

// Requires class to support reading entities
public interface SupportsReading<T> {
	T get(String name) throws Exception;
}
