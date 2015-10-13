# azure-shortcuts-for-java
The goal of this project is to provide a radically simplified API for Azure in Java, following a variant of fluent interface with the builder design pattern. 

*Note: this is currently an experimental labs project/work in progress*

The shortcuts library supports both the "modern" ARM (Azure Resource Model) model as well as the "classic" ASM (Azure Service Model), using similar API patterns whenever reasonable. 

A lot of short code samples are in the `com.microsoft.azure.shortcuts.resources.samples` (for ARM) and `com.microsoft.azure.shortcuts.services.samples` (for ASM) packages.

it is *not* currently a goal of this library to cover all of the Azure API surface, but to drastically simplify the hardest of the most important scenarios that developers have been running into. For everything else, [Azure SDK for Java](https://github.com/Azure/azure-sdk-for-java) is the fall back.

## Pre-requisites
* Java 7+
* Azure SDK for Java v0.8.0 or higher
* An Azure subscription

## Examples

* [Authentication](#creating-an-authenticated-client)
* [Virtual Machines](#virtual-machines)
* [Virtual Networks](#virtual-networks)
* [Cloud Services](#cloud-services)
* [Storage Accounts](#storage-accounts)
* [Regions](#regions)
* [Resource Groups](#resource-groups)
* [Resources](#resources)
* [Resource Providers](#resource-providers)

### Creating an authenticated client

This is the first step for all the other examples.:

*ASM*: import from `com.microsoft.azure.shortcuts.services.*` packages

```java
String publishSettingsPath = "<your file>.publishsettings";
String subscriptionId = "<subscription-GUID>";
final Azure azure = new Azure(publishSettingsPath, subscriptionId);
```

*ARM*: import from the `com.microsoft.azure.shortcuts.resources.*` packages

```java
String authFilePath = "<my-auth-file>"; // See explanation below
String subscriptionId = "<subscription-GUID>";
Azure azure = new Azure(authFilePath, subscriptionId);
```

*Note: Active Directory auth for ARM currently requires a lot of inputs and token management logic. So to simplify matters, the above constructor assumes you have set up a service principal for your application and can put the required inputs into this experimental PublishSettings-like XML file in the following format:*

```xml
<azureShortcutsAuth>
	<subscription 
		id="<subscription id>" 
		tenant="<tenant id>" 
		client="<client id>" 
		key="<client key>"
		managementURI="https://management.core.windows.net/"
		baseURL="https://management.azure.com/"
		authURL="https://login.windows.net/"
		/>
</azureShortcutsAuth>
```


### Virtual Machines

#### Creating a Linux VM in a new, default cloud service with SSH set up

*ASM*: import from `com.microsoft.azure.shortcuts.services.*` packages

```java
azure.virtualMachines.define("mylinuxvm")
	.withRegion("West US")
	.withSize("Small")
	.withAdminUsername("test")
	.withAdminPassword("Xyz.098")
	.withLinuxImage("b39f27a8b8c64d52b05eac6a62ebad85__Ubuntu-12_04_5_LTS-amd64-server-20150413-en-us-30GB")
	.withTcpEndpoint(22)
	.provision();
```

*ARM*: import from the `com.microsoft.azure.shortcuts.resources.*` packages
*[TODO]*


#### Creating a Linux VM in a new cloud service in an existing virtual network

*ASM*: import from `com.microsoft.azure.shortcuts.services.*` packages

```java
azure.virtualMachines.define("mylinuxvm")
	.withNetwork(network)
	.withSize("Small")
	.withAdminUsername("test")
	.withAdminPassword("Xyz.098")
	.withLinuxImage("b39f27a8b8c64d52b05eac6a62ebad85__Ubuntu-12_04_5_LTS-amd64-server-20150413-en-us-30GB")
	.withTcpEndpoint(22)
	.withNewCloudService("mycloudservice")
	.withSubnet("mysubnet")
	.provision();
```

*ARM*: import from the `com.microsoft.azure.shortcuts.resources.*` packages
*[TODO]*

#### Creating a Windows VM in an existing cloud service

*ASM*: import from `com.microsoft.azure.shortcuts.services.*` packages

```java
azure.virtualMachines.define("mywinvm")
	.withExistingCloudService("mycloudservice")
	.withSize("Small")
	.withAdminUsername("test")
	.withAdminPassword("Xyz.098")
	.withWindowsImage("a699494373c04fc0bc8f2bb1389d6106__Windows-Server-2012-R2-201504.01-en.us-127GB.vhd")
	.withTcpEndpoint(3389)
	.provision();
```

*ARM*: import from the `com.microsoft.azure.shortcuts.resources.*` packages
*[TODO]*


#### Listing VMs in a subscription

*ASM*: import from `com.microsoft.azure.shortcuts.services.*` packages

```java
List<String> vmNames = azure.virtualMachines.list();
```

*ARM*: import from the `com.microsoft.azure.shortcuts.resources.*` packages
*[TODO]*


#### Reading information about a VM

*ASM*: import from `com.microsoft.azure.shortcuts.services.*` packages

```java
VirtualMachine vm = azure.virtualMachines.get("mylinuxvm");
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
```

*ARM*: import from the `com.microsoft.azure.shortcuts.resources.*` packages
*[TODO]*


#### Listing available VM sizes

*ASM*: import from `com.microsoft.azure.shortcuts.services.*` packages

```java
boolean supportingVM = true;
boolean supportingCloudServices = false;

List<String> sizeNames = azure.sizes.list(supportingVM, supportingCloudServices);
```

*ARM*: import from the `com.microsoft.azure.shortcuts.resources.*` packages
{TODO}


#### Listing available OS image names

*ASM*: import from `com.microsoft.azure.shortcuts.services.*` packages

```java
List<String> OsImageNames = azure.osImages.list();
```

*ARM*: import from the `com.microsoft.azure.shortcuts.resources.*` packages
*[TODO]*


### Virtual Networks

#### Creating a virtual network with a default subnet

*ASM*: import from `com.microsoft.azure.shortcuts.services.*` packages

```java
azure.networks.define("mynetwork")
	.withRegion("West US")
	.withCidr("10.0.0.0/29")
	.provision();
```

*ARM*: import from the `com.microsoft.azure.shortcuts.resources.*` packages
*[TODO]*


#### Creating a virtual network with multiple, explicitly defined subnets

*ASM*: import from `com.microsoft.azure.shortcuts.services.*` packages

```java
azure.networks.define("mynetwork")
	.withRegion("West US")
	.withCidr("10.0.0.0/28")
	.withSubnet("Foo", "10.0.0.0/29")
	.withSubnet("Bar", "10.0.0.8/29")
	.provision();
```

*ARM*: import from the `com.microsoft.azure.shortcuts.resources.*` packages
*[TODO]*

#### Listing virtual networks in a subscription

*ASM*: import from `com.microsoft.azure.shortcuts.services.*` packages

```java
List<String> virtualNetworkNames = azure.networks.list();
```

*ARM*: import from the `com.microsoft.azure.shortcuts.resources.*` packages
{TODO}


#### Reading information about a virtual network

*ASM*: import from `com.microsoft.azure.shortcuts.services.*` packages

```java
Network network = azure.networks.get("mynetwork");

System.out.println(String.format("Network found: %s\n"
	+ "\tRegion: %s\n"
	+ "\tCIDR: %s\n"
	+ "\tAffinity group: %s\n"
	+ "\tSubnets: %s\n",
	network.name(),
	network.region(),
	network.cidr(),
	network.affinityGroup(),
	Arrays.toString(network.subnets())
));
```

*ARM*: import from the `com.microsoft.azure.shortcuts.resources.*` packages
*[TODO]*

#### Deleting a virtual network

*ASM*: import from `com.microsoft.azure.shortcuts.services.*` packages

```java
azure.networks.delete("mynetwork");
```

*ARM*: import from the `com.microsoft.azure.shortcuts.resources.*` packages
*[TODO]*

### Cloud Services

#### Creating a cloud service

*ASM*: import from `com.microsoft.azure.shortcuts.services.*` packages

```java
azure.cloudServices.define("myservice")
	.withRegion("West US")
	.provision();
```

*ARM*: import from the `com.microsoft.azure.shortcuts.resources.*` packages
*[TODO]*

#### Listing cloud services in a subscription

*ASM*: import from `com.microsoft.azure.shortcuts.services.*` packages

```java
List<String> cloudServiceNames = azure.cloudServices.list();
```

*ARM*: import from the `com.microsoft.azure.shortcuts.resources.*` packages
*[TODO]*


#### Updating an existing cloud service

*ASM*: import from `com.microsoft.azure.shortcuts.services.*` packages

```java
azure.cloudServices.update("myservice")
	.withDescription("Updated")
	.withLabel("Updated")
	.apply();
```

*ARM*: import from the `com.microsoft.azure.shortcuts.resources.*` packages
*[TODO]*

#### Reading information about a cloud service

*ASM*: import from `com.microsoft.azure.shortcuts.services.*` packages

```java
CloudService cloudService = azure.cloudServices.get("myservice");

System.out.println(String.format("Found cloud service: %s\n"
	+ "\tLabel: %s\n"
	+ "\tDescription: %s\n"
	+ "\tRegion: %s\n"
	+ "\tCreated: %s\n"
	+ "\tModified: %s\n"
	+ "\tAffinity group: %s\n"
	+ "\tReverse DNS FQDN: %s\n",
	cloudService.name(),
	cloudService.label(),
	cloudService.description(),
	cloudService.region(),
	cloudService.created().getTime(),
	cloudService.modified().getTime(),
	cloudService.affinityGroup(),
	cloudService.reverseDnsFqdn()));
```

*ARM*: import from the `com.microsoft.azure.shortcuts.resources.*` packages
*[TODO]*

#### Deleting a cloud service

*ASM*: import from `com.microsoft.azure.shortcuts.services.*` packages

```java
azure.cloudServices.delete(serviceName);
```

*ARM*: import from the `com.microsoft.azure.shortcuts.resources.*` packages
*[TODO]*

### Storage Accounts

#### Creating a storage account

*ASM*: import from `com.microsoft.azure.shortcuts.services.*` packages

```java
azure.storageAccounts.define("mystorage")
	.withRegion("West US")
	.provision();
```

*ARM*: import from the `com.microsoft.azure.shortcuts.resources.*` packages
*[TODO]*


#### Listing storage accounts in a subscription

*ASM*: import from `com.microsoft.azure.shortcuts.services.*` packages

```java
List<String> storageAccountNames = azure.storageAccounts.list();
```

*ARM*: import from the `com.microsoft.azure.shortcuts.resources.*` packages
{TODO}


#### Updating a storage account

*ASM*: import from `com.microsoft.azure.shortcuts.services.*` packages

```java
azure.storageAccounts.update("mystorage")
	.withDescription("Updated")
	.withLabel("Updated")
	.apply();
```

*ARM*: import from the `com.microsoft.azure.shortcuts.resources.*` packages
*[TODO]*

#### Reading information about a storage account

*ASM*: import from `com.microsoft.azure.shortcuts.services.*` packages

```java
StorageAccount storageAccount = azure.storageAccounts.get(accountName);

System.out.println(String.format("Found storage account: %s\n"
	+ "\tAffinity group: %s\n"
	+ "\tLabel: %s\n"
	+ "\tDescription: %s\n"
	+ "\tGeo primary region: %s\n"
	+ "\tGeo primary region status: %s\n"
	+ "\tGeo secondary region: %s\n"
	+ "\tGeo secondary region status: %s\n"
	+ "\tLast geo failover time: %s\n"
	+ "\tRegion: %s\n"
	+ "\tStatus: %s\n"
	+ "\tEndpoints: %s\n"
	+ "\tType: %s\n",

	storageAccount.name(),
	storageAccount.affinityGroup(),
	storageAccount.label(),
	storageAccount.description(),
	storageAccount.geoPrimaryRegion(),
	storageAccount.geoPrimaryRegionStatus(),
	storageAccount.geoSecondaryRegion(),
	storageAccount.geoSecondaryRegionStatus(),
	(storageAccount.lastGeoFailoverTime()!=null) ? storageAccount.lastGeoFailoverTime().getTime() : null,
	storageAccount.region(),
	storageAccount.status(),
	Arrays.toString(storageAccount.endpoints()),
	storageAccount.type()
));
```

*ARM*: import from the `com.microsoft.azure.shortcuts.resources.*` packages
*[TODO]*


#### Deleting a storage account

*ASM*: import from `com.microsoft.azure.shortcuts.services.*` packages

```java
azure.storageAccounts.update("mystorage")
	.withDescription("Updated")
	.withLabel("Updated")
	.apply();
```

*ARM*: import from the `com.microsoft.azure.shortcuts.resources.*` packages
{TODO}


### Regions


#### Listing regions

*ASM*: import from `com.microsoft.azure.shortcuts.services.*` packages

Listing all regions:

```java
List<String> regionNames = azure.regions.list();
```

Listing regions supporting a specific capability from the `LocationsAvailableServiceNames` options:

```
List<String> regionNames = azure.regions.list(LocationAvailableServiceNames.HIGHMEMORY);    	
```

*ARM*: import from the `com.microsoft.azure.shortcuts.resources.*` packages {TODO}


### Resource Groups

This applies only to ARM, so import from the `com.microsoft.azure.shortcuts.resources.*` packages

#### Creating a resource group
```java
azure.groups().define("myResourceGroup")
	.withRegion("West US")
	.withTag("hello", "world")
    .provision();
```

#### Listing resource groups

```java
List<String> resourceGroupNames = azure.groups().list();
```

#### Updating a resource group (changing its tags)

Tags are key/value pairs.

```java
azure.groups().update("<resource-group-name>")
	.withTag("foo", "bar")
	.withoutTag("hello")
	.apply();

```

You can also pass an instance of HashMap<String, String> with all the tags in it:

```java
azure.groups().update("<resource-group-name>")
	.withTags(myHashMap)
	.apply();
```


#### Reading information about a resource group

```java
String resourceGroup="<resource-group-name>";		
Group resourceGroup = azure.groups().get(resourceGroup);
System.out.println(String.format("Found group: %s\n"
		+ "\tRegion: %s\n"
		+ "\tID: %s\n"
		+ "\tTags: %s\n"
		+ "\tProvisioning state: %s\n",
		resourceGroup.name(),
		resourceGroup.region(),
		resourceGroup.id(),
		resourceGroup.tags().toString(),
		resourceGroup.getProvisioningState()));
```

#### Deleting a resource group

```java
String group = "<resource-group-name>";
System.out.println("Deleting group " + group);
	azure.groups().delete(group);
```

### Resources

This applies only to ARM, so import from the `com.microsoft.azure.shortcuts.resources.*` packages

#### Listing resources (by ID)

All resources in a subscription:

```java
List<String> resourceIds = azure.resources.list();
```

Resources in a specific group:

```java
List<String> resourceIds = azure.resources.list("<resource-group-name>");
```

#### Reading information about a resource

If you know the full ID of the resource (e.g. you got it from the `resources.list()`), then:

```java
Resource resource = azure.resources.get("<resource-id>");
```
Else, if you know the resource name, type, provider and group, then:

```java
Resource resource = azure.resources.get(
	"<resource-name>",
	"<resource-type>",
	"<resource-provider-namespace>",
	"<resource-group>");
```

And then getting at the properties, for example: 

```java
System.out.println(String.format("Found resource ID: %s\n"
	+ "\tGroup: %s\n"
	+ "\tProvider: %s\n"
	+ "\tRegion: %s\n"
	+ "\tShort name: %s\n"
	+ "\tTags: %s\n"
	+ "\tType: %s\n"
	+ "\tProvisioning state %s\n",
			
	resource.name(),
	resource.group(),
	resource.provider(),
	resource.region(),
	resource.shortName(),
	resource.tags(),
	resource.type(),
	resource.getProvisioningState()
));
```

#### Deleting a resource

Using its ID:

```java
azure.resources.delete("<resource-id">);
```

Or using its metadata:

```java
azure.resources.delete("<short-name>", "<resource-type>", "<provider-namespace>", "<group-name>");
```

Or, if you've already gotten a reference to a `Resource` object (represented by `resource` below) from `get()`, then:

```java
resource.delete();
```

### Resource Providers

This applies only to ARM, so import from the `com.microsoft.azure.shortcuts.resources.*` packages

#### Listing resource providers (by namespace)

```java
List<String> providerNamespaces = azure.providers().list();
```

#### Reading information about a resource provider

Using the namespace of the provider you can get from `providers().list()`:

```java
Provider provider = azure.providers().get("microsoft.classicstorage");
System.out.println(String.format("Found provider: %s\n" 
    + "\tRegistration State: %s\n"
    + "\tAPI versions for resource types:",
    provider.name(),
    provider.registrationState()));
```

**Currently known providers:**

* Microsoft.ApiManagement,
* Microsoft.AppService,
* Microsoft.Batch,
* microsoft.cache,
* microsoft.classiccompute,
* microsoft.classicnetwork,
* microsoft.classicstorage,
* Microsoft.Compute,
* Microsoft.Insights,
* Microsoft.KeyVault,
* Microsoft.Media,
* Microsoft.MobileEngagement,
* Microsoft.Network,
* Microsoft.OperationalInsights,
* microsoft.sql,
* Microsoft.Storage,
* Microsoft.StreamAnalytics,
* Microsoft.Web,
* Microsoft.ADHybridHealthService,
* Microsoft.Authorization,
* Microsoft.Automation,
* Microsoft.BingMaps,
* Microsoft.BizTalkServices,
* Microsoft.DataFactory,
* Microsoft.Devices,
* Microsoft.DevTestLab,
* Microsoft.DocumentDB,
* Microsoft.DomainRegistration,
* Microsoft.DynamicsLcs,
* Microsoft.EventHub,
* Microsoft.Features,
* Microsoft.Logic,
* Microsoft.MarketplaceOrdering,
* Microsoft.NotificationHubs,
* Microsoft.Resources,
* Microsoft.Scheduler,
* Microsoft.Search,
* Microsoft.ServiceBus,
* microsoft.support,
* microsoft.visualstudio,
* NewRelic.APM,
* Sendgrid.Email,
* SuccessBricks.ClearDB
	

#### Listing provider resource types and their versions

```java
Provider provider = azure.providers().get("<provider-namespace>");
for(ResourceType t : provider.resourceTypes().values()) {
	System.out.println(String.format("%s: %s", t.name(), Arrays.toString(t.apiVersions())));
}
```

#### Finding the latest API version of a resource type

```java
String latestAPIVersion = azure.providers().get("<provider-namespace>").resourceTypes().get("<resource-type>").latestApiVersion();
```

Or shortcut:

```java
String latestAPIVersion = azure.providers().get("<provider-namespace>").resourceTypes("<resource-type>").latestApiVersion();
```