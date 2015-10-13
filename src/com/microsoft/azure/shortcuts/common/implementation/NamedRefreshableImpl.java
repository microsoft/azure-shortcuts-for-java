package com.microsoft.azure.shortcuts.common.implementation;

import com.microsoft.azure.shortcuts.common.reading.Refreshable;

public abstract class NamedRefreshableImpl<T> 
	extends NamedImpl<T>
	implements Refreshable<T> {

	protected boolean initialized = false;
	
	protected NamedRefreshableImpl(String name, boolean initialized) {
		super(name);
		this.initialized = initialized;
	}

	public abstract T refresh() throws Exception;
	
	protected void ensureInitialized() throws Exception {
		if(!this.initialized) {
			refresh();
			this.initialized = true;
		}
	}
}
