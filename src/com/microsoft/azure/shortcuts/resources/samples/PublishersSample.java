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

import com.microsoft.azure.shortcuts.resources.Offer;
import com.microsoft.azure.shortcuts.resources.Publisher;
import com.microsoft.azure.shortcuts.resources.Region;
import com.microsoft.azure.shortcuts.resources.SKU;
import com.microsoft.azure.shortcuts.resources.implementation.Subscription;

public class PublishersSample {
    public static void main(String[] args) {
        try {
            Subscription subscription = Subscription.authenticate("my.azureauth", null);
            test(subscription);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void test(Subscription subscription) throws Exception {
    	// List publishers
    	Map<String, Publisher> publishers = subscription.publishers().asMap(Region.US_WEST);
    	for(Publisher p : publishers.values()) {
    		printPublisher(p);
    	}
    	
    	Publisher publisher = subscription.publishers().get(publishers.keySet().iterator().next());
    	printPublisher(publisher);
    	
    	publisher = subscription.publishers(Region.US_WEST, "Canonical");
    	printPublisher(publisher);
    }
    
    
    private static void printPublisher(Publisher publisher) {
    	StringBuilder info = new StringBuilder();
    	info
    		.append(String.format("Publisher: %s\n", publisher.name()));

    	try {
			for(Offer offer : publisher.offers().values()) {
				info.append(String.format("\tOffer: %s\n", offer.name()));
				
				for(SKU sku : offer.skus().values()) {
					info.append(String.format("\t\tSKU: %s\n", sku.name()));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
    	
    	System.out.println(info.toString());
    }
}
