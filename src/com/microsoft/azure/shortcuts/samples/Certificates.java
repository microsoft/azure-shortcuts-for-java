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
package com.microsoft.azure.shortcuts.samples;

import java.io.File;

import com.microsoft.azure.shortcuts.Azure;
import com.microsoft.azure.shortcuts.Utils;

// Tests VM sizes
public class Certificates {
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
		File pfxFile = new File(new File(System.getProperty("user.home"), "Desktop"), "test.pfx");
		File jdkFilePath = new File(System.getenv("JAVA_HOME"));
		File cerFile = new File(new File(System.getProperty("user.home"), "Desktop"), "test.cer");
		String password = "Abcd.1234", alias = "test";
		
		Utils.createCertPkcs12(pfxFile, jdkFilePath, alias, password, alias, 3650);
		Utils.createCertPublicFromPkcs12(pfxFile, cerFile, jdkFilePath, alias, password);
	}
}
