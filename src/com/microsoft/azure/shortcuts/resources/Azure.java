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
package com.microsoft.azure.shortcuts.resources;

import com.microsoft.azure.management.compute.ComputeManagementClient;
import com.microsoft.azure.management.compute.ComputeManagementService;
import com.microsoft.azure.management.network.NetworkResourceProviderClient;
import com.microsoft.azure.management.network.NetworkResourceProviderService;
import com.microsoft.azure.management.resources.ResourceManagementClient;
import com.microsoft.azure.management.resources.ResourceManagementService;
import com.microsoft.azure.management.storage.StorageManagementClient;
import com.microsoft.azure.management.storage.StorageManagementService;
import com.microsoft.azure.shortcuts.common.Utils;
import com.microsoft.azure.shortcuts.resources.listing.Groups;
import com.microsoft.azure.shortcuts.resources.listing.Providers;
import com.microsoft.azure.utility.AuthHelper;
import com.microsoft.windowsazure.Configuration;
import com.microsoft.windowsazure.management.configuration.ManagementConfiguration;
import com.microsoft.windowsazure.management.configuration.PublishSettingsLoader;

import java.io.File;
import java.net.URI;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


public class Azure {
    public static String MANAGEMENT_URI = "https://management.core.windows.net/";
    public static String ARM_URL = "https://management.azure.com/";
    public static String ARM_AAD_URL = "https://login.windows.net/";

    private Configuration configuration;
    private ResourceManagementClient resourceManagementClient;
    private StorageManagementClient storageManagementClient;
    private ComputeManagementClient computeManagementClient;
    private NetworkResourceProviderClient networkResourceProviderClient;

    // public final StorageAccounts storageAccounts; TODO
    public final Resources resources;
    private final GroupsImpl groups;
    private final ProvidersImpl providers;

    public Azure(String subscriptionId, String tenantId, String clientId, String clientKey) throws Exception {
    	this(createConfiguration(subscriptionId, tenantId, clientId, clientKey, null, null, null));
    }

    public Azure(String authenticationFilePath, String subscriptionId) throws Exception {
    	this(getConfigFromFile(authenticationFilePath, subscriptionId));
    }
    
    private Azure(Configuration configuration) {
    	this.configuration = configuration;
        // this.storageAccounts = new StorageAccounts(this); TODO
        this.resources = new Resources(this);
        this.groups = new GroupsImpl(this);
        this.providers = new ProvidersImpl(this);
    }
    
    
    // Returns an appropriate authenticated configuration based on the supplied file, automatically detecting if the file is a
    // PublishSettings file or an Azure shortcuts authentication file
    private static Configuration getConfigFromFile(String authFilePath, String subscriptionId) throws Exception {
    	Configuration config = getConfigurationFromAuthXml(new File(authFilePath), subscriptionId);
    	if(config != null) {
    		return config;
    	} else {
    		return PublishSettingsLoader.createManagementConfiguration(authFilePath, subscriptionId);
    	}
    }
    
    //**********************************************************
    //* Getters
    //**********************************************************

    public Groups groups() {
    	return this.groups;
    }
    
    
    public Providers providers() {
    	return this.providers;
    }
    
    
    // Returns an ARM authenticated configuration based on the provided Azure shortcuts authentication file
    // The assumed schema of the file is:
    // <azureShortcutsAuth>
    //   <subscription 
    //		id="<subscription-id>" 
    //		tenant="<tenant-id>"
    //		client="<client-id>"
    //		key="<client-key>"
    //		managementURI="<management-URI>"
    //		baseURL="<base-ARM-URL>"
    //		authURL="<active-directory-login-url>"
    //	 />
    // </azureShortcutsAuth>
    private static Configuration getConfigurationFromAuthXml(File authFile, String subscriptionId) throws Exception {
    	Document xmlDoc = Utils.loadXml(authFile);
    	Element root = xmlDoc.getDocumentElement();    	
    	if(!root.getTagName().equals("azureShortcutsAuth")) {
    		return null;
    	} 
    	
    	NodeList subscriptions = root.getElementsByTagName("subscription");
    	if(subscriptions.getLength() == 0) {
    		throw new ParserConfigurationException("No subscriptions found");
    	}

    	Element subscription = null;
    	if(subscriptionId == null) {
    		// If no specific subscription ID requested, assume the first one
    		subscription = (Element)subscriptions.item(0);
    	} else {
    		// Else, find the subscription with the requested ID
    		for(int i=0; i<subscriptions.getLength(); i++) {
    			subscription = (Element)subscriptions.item(i);
    			if(subscription.getAttribute("id").equals(subscriptionId)) {
    				break;
    			} else {
    				subscription = null;
    			}
    		}
    	}
    	
		if(subscription == null) {
			throw new ParserConfigurationException("Subscription not found");
		}
		
		// Extract service principal information
		subscriptionId = subscription.getAttribute("id");
		String tenantId = subscription.getAttribute("tenant");
		String clientId = subscription.getAttribute("client");
		String clientKey = subscription.getAttribute("key");
		String managementUri = subscription.getAttribute("managementURI");
		String baseUrl = subscription.getAttribute("baseURL");
		String authUrl = subscription.getAttribute("authURL");
		return createConfiguration(subscriptionId, tenantId, clientId, clientKey, managementUri, baseUrl, authUrl);
    }
    
    
    // Returns the compute management client, creating if needed
    ComputeManagementClient computeManagementClient() {
    	if(this.computeManagementClient == null) {
    		this.computeManagementClient = ComputeManagementService.create(this.configuration);
    	}
    	
    	return this.computeManagementClient;
    }
    
    
    // Returns the network management client, creating if needed
    NetworkResourceProviderClient networkManagementClient() {
    	if(this.networkResourceProviderClient == null) {
    		this.networkResourceProviderClient = NetworkResourceProviderService.create(this.configuration);
    	}
    	
    	return this.networkResourceProviderClient;
    }
    
    
    // Returns the resource management client, creating if needed
    ResourceManagementClient resourceManagementClient() {
    	if(this.resourceManagementClient == null) {
    		this.resourceManagementClient = ResourceManagementService.create(this.configuration);
    	}
    	
    	return this.resourceManagementClient;

    }

    
    // Returns the storage management client
    StorageManagementClient storageManagementClient() {
    	if(this.storageManagementClient == null) {
    		this.storageManagementClient = StorageManagementService.create(this.configuration);
    	}
    	
    	return this.storageManagementClient;
    }
    
    
	private static Configuration createConfiguration(
			String subscriptionId, 
			String tenantId, 
			String clientId, 
			String clientKey,
			String managementUri,
			String baseUrl,
			String authUrl) throws Exception {
		
		if(subscriptionId == null) {
			throw new Exception("Missing subscription");
		}
		
		if(baseUrl == null) {
			baseUrl = Azure.ARM_URL;
		}
		URI baseUri = new URI(baseUrl);
		
		if(managementUri == null) {
			managementUri = Azure.MANAGEMENT_URI;
		}
		
		if(authUrl == null) {
			authUrl = Azure.ARM_AAD_URL;
		}
		
		String accessToken = AuthHelper.getAccessTokenFromServicePrincipalCredentials(
			managementUri, 
			authUrl,
			tenantId, 
			clientId, 
			clientKey).getAccessToken();
		
		return ManagementConfiguration.configure(
			(String)null, 
			baseUri,
			subscriptionId,
			accessToken);
	}

}
