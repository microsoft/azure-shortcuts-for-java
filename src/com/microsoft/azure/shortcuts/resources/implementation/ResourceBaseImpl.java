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
package com.microsoft.azure.shortcuts.resources.implementation;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.microsoft.azure.shortcuts.common.implementation.EntitiesImpl;
import com.microsoft.azure.shortcuts.common.implementation.IndexableRefreshableWrapperImpl;
import com.microsoft.azure.shortcuts.resources.Region;
import com.microsoft.azure.shortcuts.resources.common.ResourceBase;


public abstract class ResourceBaseImpl<
		WRAPPER, 
		INNER extends com.microsoft.windowsazure.core.ResourceBaseExtended,
		WRAPPERIMPL extends ResourceBaseImpl<WRAPPER, INNER, WRAPPERIMPL>>
	extends 
		IndexableRefreshableWrapperImpl<WRAPPER, INNER>
	implements 
		ResourceBase {

	protected final EntitiesImpl<Subscription> collection;
	
	protected ResourceBaseImpl(String id, INNER innerObject, EntitiesImpl<Subscription> collection) {
		super(id, innerObject);
		this.collection = collection;
	}

	/*******************************************
	 * Getters
	 *******************************************/
	
	@Override
	public String region() {
		return this.inner().getLocation();
	}

	@Override
	public Map<String, String> tags() {
		return Collections.unmodifiableMap(this.inner().getTags());
	}

	@Override
	public String id() {
		return this.inner().getId();
	}

	@Override
	public String type() {
		return this.inner().getType();
	}
	
	@Override
	public String name() {
		return this.inner().getName();
	}
	
	/**************************************************
	 * Tag setters
	 **************************************************/

	@SuppressWarnings("unchecked")
	public final WRAPPERIMPL withTags(Map<String, String> tags) {
		this.inner().setTags(new HashMap<>(tags));
		return (WRAPPERIMPL) this;
	}
	
	@SuppressWarnings("unchecked")
	public final WRAPPERIMPL withTag(String name, String value) {
		this.inner().getTags().put(name, value);
		return (WRAPPERIMPL) this;
	}
	
	@SuppressWarnings("unchecked")
	public final WRAPPERIMPL withoutTag(String name) {
		this.inner().getTags().remove(name);
		return (WRAPPERIMPL) this;
	}
	
	/**********************************************
	 * Region setters
	 **********************************************/
	
	@SuppressWarnings("unchecked")
	public final WRAPPERIMPL withRegion(String regionName) {
		this.inner().setLocation(regionName);
		return (WRAPPERIMPL) this;
	}
	
	public final WRAPPERIMPL withRegion(Region region) {
		return this.withRegion(region.toString());
	}
}