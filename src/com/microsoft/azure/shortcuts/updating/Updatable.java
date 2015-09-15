package com.microsoft.azure.shortcuts.updating;

import com.microsoft.azure.shortcuts.reading.Named;

public interface Updatable<T> extends Named, Deletable {
	T apply() throws Exception;
}
