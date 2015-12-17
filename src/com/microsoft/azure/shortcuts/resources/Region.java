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

/**
 * Enumeration of the Azure datacenter regions. See https://azure.microsoft.com/regions/
 * @author marcins
 *
 */
public enum Region {
	US_WEST("westus", "West US"),
	US_CENTRAL("centralus", "Central US"),
	US_EAST("eastus", "East US"),
	US_EAST2("eastus2", "East US 2"),
	US_NORTH_CENTRAL("nothcentralus", "North Central US"),
	US_SOUTH_CENTRAL("southcentralus", "South Central US"),
	EUROPE_NORTH("northeurope", "North Europe"),
	EUROPE_WEST("westeurope", "West Europe"),
	ASIA_EAST("eastasia",  "East Asia"),
	ASIA_SOUTHEAST("southeastasia", "South East Asia"),
	JAPAN_EAST("japaneast", "Japan East"),
	JAPAN_WEST("japanwest", "Japan West"),
	BRAZIL_SOUTH("brazilsouth", "Brazil South"),
	AUSTRALIA_EAST("australiaeast", "Australia East"),
	AUSTRALIA_SOUTHEAST("australiasoutheast", "Australia Southeast"),
	INDIA_CENTRAL("centralindia", "Central India"),
	INDIA_SOUTH("southindia", "South India"),
	INDIA_WEST("westindia", "West India");
	
	private final String name;
	public final String label;
	Region(String name, String label) {
		this.name = name;
		this.label = label;
	}
	
	@Override
	public String toString() {
		return this.name;
	}
	
	
	/**
	 * @param label The region (location) label to look for
	 * @return The Region constant representing the region whose label matches the one provided
	 */
	public static Region fromLabel(String label) {
		for(Region region : Region.values()) {
			if(region.label.equalsIgnoreCase(label)) {
				return region;
			}
		}
		
		return null;
	}
}
