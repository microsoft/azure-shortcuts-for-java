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

import com.microsoft.azure.management.compute.models.VirtualMachineSizeTypes;
import com.microsoft.azure.shortcuts.common.Indexable;

public interface Size extends 
	Indexable {
	
	int maxDataDiskCount();
	int memoryInMB();
	int numberOfCores();
	int osDiskSizeInMB();
	int resourceDiskSizeInMB();
	Type toSizeType();

	
	public enum Type {
		BASIC_A0(VirtualMachineSizeTypes.BASIC_A0),
		BASIC_A1(VirtualMachineSizeTypes.BASIC_A1),
		BASIC_A2(VirtualMachineSizeTypes.BASIC_A2),
		BASIC_A3(VirtualMachineSizeTypes.BASIC_A3),
		BASIC_A4(VirtualMachineSizeTypes.BASIC_A4),
		STANDARD_A0(VirtualMachineSizeTypes.STANDARD_A0),
		STANDARD_A1(VirtualMachineSizeTypes.STANDARD_A1),
		STANDARD_A2(VirtualMachineSizeTypes.STANDARD_A2),
		STANDARD_A3(VirtualMachineSizeTypes.STANDARD_A3),
		STANDARD_A4(VirtualMachineSizeTypes.STANDARD_A4),
		STANDARD_A5(VirtualMachineSizeTypes.STANDARD_A5),
		STANDARD_A6(VirtualMachineSizeTypes.STANDARD_A6),
		STANDARD_A7(VirtualMachineSizeTypes.STANDARD_A7),
		STANDARD_A8(VirtualMachineSizeTypes.STANDARD_A8),
		STANDARD_A9(VirtualMachineSizeTypes.STANDARD_A9),
		STANDARD_G1(VirtualMachineSizeTypes.STANDARD_G1),
		STANDARD_G2(VirtualMachineSizeTypes.STANDARD_G2),
		STANDARD_G3(VirtualMachineSizeTypes.STANDARD_G3),
		STANDARD_G4(VirtualMachineSizeTypes.STANDARD_G4),
		STANDARD_G5(VirtualMachineSizeTypes.STANDARD_G5);

		private final String type;

		Type(String name) {
			this.type = name;
		}
		
		public String toString() {
			return this.type;
		}
		
		public static Type fromString(String sizeName) {
			for(Type t : Type.values()) {
				if(sizeName.equalsIgnoreCase(t.toString())) {
					return t;
				}
			}
			return null;
		}
	}
}
