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
package com.microsoft.azure.shortcuts.services.samples;

import java.util.Arrays;

import com.microsoft.azure.shortcuts.services.Azure;
import com.microsoft.azure.shortcuts.services.reading.VirtualMachine;

//Tests Virtual Machines
public class VirtualMachines {
	public static void main(String[] args) {
		String publishSettingsPath = "my.publishsettings";
		String subscriptionId = "9657ab5d-4a4a-4fd2-ae7a-4cd9fbd030ef";

		try {
			// Instantiate Azure management class
			final Azure azure = new Azure(publishSettingsPath, subscriptionId);

			test(azure);
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}

	
	public static void test(Azure azure) throws Exception {
		final String timeStamp = String.valueOf(System.currentTimeMillis()).substring(5);
			
		// Create a new network
		final String network = "net" + timeStamp;
		System.out.println(String.format("Creating virtual network named '%s'...", network));
		azure.networks.define(network)
			.withRegion("West US")
			.withCidr("10.0.0.0/28")
			.withSubnet("Foo", "10.0.0.0/29")
			.withSubnet("Bar", "10.0.0.8/29")
			.provision();
			
		// Create a Linux VM in that network
		final String vmName2 = "vl" + timeStamp;
		System.out.println(String.format("Creating virtual machine named '%s'...", vmName2));
		final String cloudService2 = "cs" + timeStamp;
		azure.virtualMachines.define(vmName2)
			.withNetwork(network)
			.withSize("Small")
			.withAdminUsername("marcins")
			.withAdminPassword("Abcd.1234")
			.withLinuxImage("b39f27a8b8c64d52b05eac6a62ebad85__Ubuntu-12_04_5_LTS-amd64-server-20150413-en-us-30GB")
			.withTcpEndpoint(22)
			.withNewCloudService(cloudService2)
			.withSubnet("Foo")
			.provision();

		// Create a Linux VM in a new service
		final String vmName = "vm" + timeStamp;
		System.out.println(String.format("Creating virtual machine named '%s'...", vmName));
		azure.virtualMachines.define(vmName)
			.withRegion("West US")
			.withSize("Small")
			.withAdminUsername("marcins")
			.withAdminPassword("Abcd.1234")
			.withLinuxImage("b39f27a8b8c64d52b05eac6a62ebad85__Ubuntu-12_04_5_LTS-amd64-server-20150413-en-us-30GB")
			.withHostName(vmName)
			.withTcpEndpoint(22)
			.provision();

		// Add a Windows VM to the same service deployment
		final String vmNameWin = "wm" + timeStamp;
		azure.virtualMachines.define(vmNameWin)
			.withExistingCloudService(vmName)
			.withSize("Small")
			.withAdminUsername("marcins")
			.withAdminPassword("Abcd.1234")
			.withWindowsImage("a699494373c04fc0bc8f2bb1389d6106__Windows-Server-2012-R2-201504.01-en.us-127GB.vhd")
			.withTcpEndpoint(3389)
			.provision();		
			
		// List virtual machines
		System.out.println("Virtual machines: "+ Arrays.toString(
			azure.virtualMachines.list()));
			
		// Get information about the created Linux vm
		VirtualMachine vm = azure.virtualMachines.get(vmName);
		System.out.println(String.format("Reading information about vm: %s\n"
				+ "\tDeployment name: %s\n"
				+ "\tService name: %s\n"
				+ "\tSize: %s\n"
				+ "\tStatus: %s\n"
				+ "\tWindows? %s\n"
				+ "\tLinux? %s\n"
				+ "\tNetwork %s\n"
				+ "\tAffinity group %s\n",
				vm.name(),
				vm.deployment(),
				vm.cloudService(),
				vm.size(),
				vm.status().toString(),
				vm.isWindows(),
				vm.isLinux(),
				vm.network(),
				vm.affinityGroup()
				));
			
		// Get information about the created Windows vm
		vm = azure.virtualMachines.get(vmName + "." + vmNameWin);
		System.out.println(String.format("Reading information about vm: %s\n"
				+ "\tDeployment name: %s\n"
				+ "\tService name: %s\n"
				+ "\tSize: %s\n"
				+ "\tStatus: %s\n"
				+ "\tWindows? %s\n"
				+ "\tLinux? %s\n"
				+ "\tNetwork %s\n"
				+ "\tAffinity group %s\n",
				vm.name(),
				vm.deployment(),
				vm.cloudService(),
				vm.size(),
				vm.status().toString(),
				vm.isWindows(),
				vm.isLinux(),
				vm.network(),
				vm.affinityGroup()
				));
			
		// Get information about the second Linux VM
		vm = azure.virtualMachines.get(cloudService2 + "." + vmName2);
		System.out.println(String.format("Reading information about vm: %s\n"
				+ "\tDeployment name: %s\n"
				+ "\tService name: %s\n"
				+ "\tSize: %s\n"
				+ "\tStatus: %s\n"
				+ "\tWindows? %s\n"
				+ "\tLinux? %s\n"
				+ "\tNetwork %s\n"
				+ "\tAffinity group %s\n",
				vm.name(),
				vm.deployment(),
				vm.cloudService(),
				vm.size(),
				vm.status().toString(),
				vm.isWindows(),
				vm.isLinux(),
				vm.network(),
				vm.affinityGroup()
				));
	
	}
}
