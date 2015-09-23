/**
 * Copyright Microsoft Corp.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.microsoft.azure.shortcuts.resources;

public class ResourceManagementSamples {
    public static void main(String[] args) {
        String subscriptionId = "<subscription_id>";
        String tenantId = "<tenant_id>";
        String clientId = "<client_id>";
        String clientKey = "<client_key>";

//        String publishSettingsPath = "/Users/farlen/stuff/publish.publishsettings";

        try {
            AzureResources azureResources = new AzureResources(subscriptionId, tenantId, clientId, clientKey);
//            AzureResources azureResources = new AzureResources(publishSettingsPath, subscriptionId);
            azureResources.storageAccounts.define("lenaresourcegroup", "lenatestresources2").withRegion("West US").provision();

            System.out.println(azureResources.storageAccounts.list());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
