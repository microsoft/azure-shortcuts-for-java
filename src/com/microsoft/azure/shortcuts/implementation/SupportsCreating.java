package com.microsoft.azure.shortcuts.implementation;


// Requires class to support creating entities
public interface SupportsCreating<T> {
	T define(String name);
}
