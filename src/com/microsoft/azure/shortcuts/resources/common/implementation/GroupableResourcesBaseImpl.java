/**
* Copyright (c) Microsoft Corporation
* 
* All rights reserved. 
* 
* MIT License
* 
* Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files 
* (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, 
* publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, 
* subject to the following conditions:
* 
* The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
* 
* THE SOFTWARE IS PROVIDED *AS IS*, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF 
* MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR 
* ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH 
* THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package com.microsoft.azure.shortcuts.resources.common.implementation;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.microsoft.azure.shortcuts.common.implementation.EntitiesImpl;
import com.microsoft.azure.shortcuts.resources.implementation.ResourcesImpl;
import com.microsoft.windowsazure.core.ResourceBaseExtended;

public abstract class GroupableResourcesBaseImpl<T, W, I extends ResourceBaseExtended> extends EntitiesImpl<T> {
	protected GroupableResourcesBaseImpl(T azure) {
		super(azure);
	}
	
	protected abstract List<I> getNativeEntities(String group) throws Exception;
	protected abstract I getNativeEntity(String group, String name) throws Exception;
	protected abstract W createWrapper(I nativeItem);
	
	public Map<String, W> list(String groupName) throws Exception {
		HashMap<String, W> wrappers = new HashMap<>();
		for(I nativeItem : getNativeEntities(groupName)) {
			wrappers.put(nativeItem.getId(), createWrapper(nativeItem));
		}
		return Collections.unmodifiableMap(wrappers);
	}
	
	public W get(String groupName, String name) throws Exception {
		return createWrapper(getNativeEntity(groupName, name));
	}
	
	public W get(String id) throws Exception {
		return get(
			ResourcesImpl.groupFromResourceId(id), 
			ResourcesImpl.nameFromResourceId(id));
	}
}
