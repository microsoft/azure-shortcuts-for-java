package com.microsoft.azure.shortcuts.creation;

// Encapsulates the provisioning method 
public interface Provisionable<T> {
	T provision() throws Exception;
}