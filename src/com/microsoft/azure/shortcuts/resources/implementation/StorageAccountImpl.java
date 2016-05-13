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

import java.net.MalformedURLException;
import java.net.URL;

import com.microsoft.azure.management.storage.models.AccountType;
import com.microsoft.azure.management.storage.models.StorageAccountCreateParameters;
import com.microsoft.azure.shortcuts.resources.StorageAccount;

class StorageAccountImpl 
	extends 
		GroupableResourceBaseImpl<
			StorageAccount, 
			com.microsoft.azure.management.storage.models.StorageAccount,
			StorageAccountImpl,
			StorageAccountsImpl>
	implements
		StorageAccount,
		StorageAccount.Definition {
	
	StorageAccountImpl(
			com.microsoft.azure.management.storage.models.StorageAccount azureStorageAccount, 
			StorageAccountsImpl collection) {
		super(azureStorageAccount.getId(), azureStorageAccount, collection);
	}
	
	
	/***********************************************************
	 * Getters
	 ***********************************************************/
	
	@Override
	public URL primaryBlobEndpoint() {
		try {
			return this.inner().getPrimaryEndpoints().getBlob().toURL();
		} catch (MalformedURLException e) {
			return null;
		}
	}
	
	@Override
	public AccountType accountType() {
		return this.inner().getAccountType();
	}
	
	
	/**************************************************************
	 * Setters (fluent interface)
	 **************************************************************/
	
	@Override
	public StorageAccountImpl withAccountType(AccountType type) {
		this.inner().setAccountType(type);
		return this;
	}
	
	
	/************************************************************
	 * Verbs
	 ************************************************************/
	
	@Override
	public StorageAccount create() throws Exception {
		// Create group if needed
		ensureGroup();
	
		// Assume default account type if needed
		if(this.accountType() == null) {
			this.withAccountType(AccountType.StandardLRS);
		}
		
		StorageAccountCreateParameters params = new StorageAccountCreateParameters();
		params.setLocation(this.region());
		params.setAccountType(this.accountType());
		params.setTags(this.inner().getTags());
	
		this.subscription().storageManagementClient().getStorageAccountsOperations().create(this.groupName, this.name(), params);
		return this.subscription().storageAccounts().get(this.groupName, this.name());
	}
	
	
	@Override
	public StorageAccountImpl refresh() throws Exception {
		this.setInner(this.collection.getNativeEntity(
			ResourcesImpl.groupFromResourceId(this.id()), 
			ResourcesImpl.nameFromResourceId(this.id())));
		return this;
	}
	
	
	@Override
	public void delete() throws Exception {
		this.subscription().storageAccounts().delete(this.id());
	}
}
