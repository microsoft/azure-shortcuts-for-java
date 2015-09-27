package com.microsoft.azure.shortcuts.resources.updating;

import com.microsoft.azure.shortcuts.resources.reading.Resource;
import com.microsoft.azure.shortcuts.updating.Deletable;

public interface Updatable <T> extends Resource, Deletable {
    T apply() throws Exception;
}