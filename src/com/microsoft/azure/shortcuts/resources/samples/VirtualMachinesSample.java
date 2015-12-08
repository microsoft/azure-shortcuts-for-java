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

package com.microsoft.azure.shortcuts.resources.samples;

import java.util.Map;

import com.microsoft.azure.shortcuts.resources.Size;
import com.microsoft.azure.shortcuts.resources.VirtualMachine;
import com.microsoft.azure.shortcuts.resources.implementation.Azure;

// Tests resources
public class VirtualMachinesSample {
    public static void main(String[] args) {
        try {
            Azure azure = Azure.authenticate("my.azureauth", null);
            test(azure);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    

    public static void test(Azure azure) throws Exception {
    	// Creating a Linux VM
    	azure.virtualMachines().define("marcinstestvm")
    		.withRegion("westus")
    		.withAdminUsername("test")
    		.withAdminPassword("Abcd.1234")
    		.withImagePublishedBy("MicrosoftWindowsServer")
    		.withImageOffer("WindowsServer")
    		.withImageSKU("2008-R2-SP1")
    		.withLatestImageVersion()
    		.withSize(Size.Type.BASIC_A1)
    		.withStorageAccountNew("marcinsvmtest")
    		.withGroupNew("marcinsvmtest");
    		//.provision();
    	
    	// Listing all virtual machine ids in a subscription
    	Map<String, VirtualMachine> vms = azure.virtualMachines().list();
    	System.out.println(String.format("Virtual machines: \n\t%s", String.join("\n\t", vms.keySet())));

    	// Listing vms in a specific group
    	String groupName = "group1444089227523";
    	Map<String, VirtualMachine> vmsInGroup = azure.virtualMachines().list(groupName);
    	System.out.println(String.format("Virtual machines: \n\t%s", String.join("\n\t", vmsInGroup.keySet())));
    	
    	// Listing virtual machines as objects
    	String vmID = null;
    	for(VirtualMachine vm : vms.values()) {
    		if(vmID == null) {
    			vmID = vm.id();
    		}
    		printVM(vm);
    	}
    	
    	// Getting a specific virtual machine using its id
    	VirtualMachine vm = azure.virtualMachines(vmID);
    	printVM(vm);
    	
    	// Getting a specific virtual machine using its group and name
    	vm = azure.virtualMachines(groupName, vm.computerName());
    	printVM(vm);

	}
    
    
    private static void printVM(VirtualMachine vm) {
		StringBuilder info = new StringBuilder();
		info
			.append(String.format("Information about vm: %s\n", vm.id()))
			.append(String.format("\tAdmin username: %s\n", vm.adminUserName()))
			.append(String.format("\tAvailability set URI: %s\n", vm.availabilitySet()))
			.append(String.format("\tBoot diagnostics storage URI: %s\n", vm.bootDiagnosticsStorage()))
			.append(String.format("\tComputer name: %s\n", vm.computerName()))
			.append(String.format("\tCustom data: %s\n", vm.customData()))
			.append(String.format("\tNumber of data disks: %d\n", vm.dataDisks().size()))
			.append(String.format("\tNumber of extensions: %d\n", vm.extensions().size()))
			.append(String.format("\tGroup: %s\n", vm.group()))
			;
			
		System.out.println(info.toString());
    }
 }
