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
package com.microsoft.azure.shortcuts.services.reading;

import java.net.URI;
import java.util.Calendar;
import java.util.Map;

import com.microsoft.azure.shortcuts.common.reading.Named;
import com.microsoft.azure.shortcuts.services.updating.VirtualMachineUpdatableBlank;
import com.microsoft.windowsazure.management.compute.models.DeploymentSlot;
import com.microsoft.windowsazure.management.compute.models.DeploymentStatus;

public interface VirtualMachine extends 
	Named,
	VirtualMachineUpdatableBlank {
	
	String size() throws Exception;
	String deployment() throws Exception;
	String cloudService() throws Exception;
	String network() throws Exception;
	String region() throws Exception;
	String affinityGroup() throws Exception;
	DeploymentStatus status() throws Exception;
	//boolean isLinux() throws Exception; //TODO: Currently broken in the SDK/Azure
	//boolean isWindows() throws Exception; // TODO: Currently broken in the SDK/Azure
	String roleName() throws Exception;
	Calendar createdTime() throws Exception;
	DeploymentSlot deploymentSlot() throws Exception;
	Map<String, String> extendedDeploymentProperties() throws Exception;
	Calendar lastModifiedTime() throws Exception;
	String reservedIPName() throws Exception;
	URI deploymentUri() throws Exception;
	Boolean isDeploymentLocked() throws Exception;
	String availabilitySet() throws Exception;
	String defaultWinRmCertificateThumbprint() throws Exception;
	String roleLabel() throws Exception;
	URI mediaLocation() throws Exception;
	String osVersion() throws Exception;
	String imageName() throws Exception;
	Boolean hasGuestAgent() throws Exception;
}

