package com.microsoft.azure.shortcuts.implementation;

import com.microsoft.azure.shortcuts.reading.Named;

// Base implementation for named entities
public abstract class NamedImpl implements Named {
	final protected String name;
	
	protected NamedImpl(String name) {
		this.name = name;
	}
	
	@Override
	public String name() {
		return this.name;
	}
	
	@Override
	public String toString() {
		return this.name();
	}
}


