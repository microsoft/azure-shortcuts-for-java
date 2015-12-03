# azure-shortcuts-for-java
The goal of this project is to provide a radically simplified API for Azure in Java, following a flavor of modern API design patterns (builder, fluent) optimized for readability, writeability and succinctness. 

*Note: this is currently an experimental labs project/work in progress*.

The shortcuts library supports both the "modern" ARM (Azure Resource Model) model as well as the "classic" ASM (Azure Service Model), using similar API patterns whenever reasonable.

A lot of short code samples are in the packages `com.microsoft.azure.shortcuts.resources.samples` (for ARM) and `com.microsoft.azure.shortcuts.services.samples` (for ASM).

It is *not* currently a goal of this library to cover all of the Azure API surface, but rather to drastically simplify the hardest of the most important scenarios that developers have been running into. For everything else, [Azure SDK for Java](https://github.com/Azure/azure-sdk-for-java) is the fall back, which this project is also built on.

## Setting up the dev machine

To work on this project, it's easiest to use Eclipse and Maven (kudos to Ted Gao for [the pointers](http://ted-gao.blogspot.com/2011/09/using-maven-to-manage-library.html)):

1. Create a directory for the workspace
2. In that workspace directory, run `mvn -Declipse.workspace=. eclipse:configure-workspace`
3. In the project directory, after `git-clone`, run `mvn eclipse:eclipse -DdownloadSources=true`
4. In Eclipse, use  the workspace folder created earlier, and import the project into it (don't copy)

## Usage pre-requisites

* Java 7+
* Azure SDK for Java v0.9.0 or higher
* An Azure subscription

## Examples

Inside the `\*.samples` packages, you will find a number of runnable sample code (classes with `main()`). For each of the sample classes, you can just **Debug As** > **Java Application**.

Many of the samples rely on credentials files in the root of the project:

* for the **"Classic" ASM-based APIs**, use a *"my.publishsettings"* file. This is the classical Publish-Settings file from Azure.

* for the **"Resource" ARM-based APIs**, you can use the experimental *"my.authfile"* containing all the inputs needed by the Azure Active Directory authentication and relying on you setting up a service principal for your app. Further simplification of the authentication process is an area of active investigation, but for now you can create the file manually, as per the [Authentication](#creating-an-authenticated-client) section.    

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
Azure azure = Azure.authenticate(authFilePath, subscriptionId);
```

*Note: Active Directory auth for ARM currently requires a lot of inputs and token management logic. To simplify matters, the above constructor assumes you have set up a service principal for your application and can put the required inputs into this experimental PublishSettings-like XML file in the following format:*

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
azure.virtualMachines().define("mylinuxvm")
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
azure.virtualMachines().define("mylinuxvm")
	.withExistingNetwork(network)
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
azure.virtualMachines().define("mywinvm")
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


#### Listing VMs

All virtual machine names (or ids) in a subscription: 

*ASM*: import from `com.microsoft.azure.shortcuts.services.*` packages

```java
List<String> vmNames = azure.virtualMachines().names();
```

*ARM*: import from `com.microsoft.azure.shortcuts.resources.*` packages

```java
Map<String, VirtualMachine> vms = azure.virtualMachines().list();
System.out.println(String.format("Virtual machines: \n\t%s", String.join("\n\t", vms.keySet())));
```

Virtual machines in a specific resource group (resource model "ARM" only)

```java
Map<String, VirtualMachine> vms = azure.virtualMachines().list("<group-name>");
System.out.println(String.format("Virtual machines: \n\t%s", String.join("\n\t", vms.keySet())));
```


#### Reading information about a VM

*ASM*: import from `com.microsoft.azure.shortcuts.services.*` packages

```java
VirtualMachine vm = azure.virtualMachines().get("mylinuxvm");
System.out.println(String.format("Reading information about vm: %s\n"
	+ "\tDeployment name: %s\n"
	+ "\tService name: %s\n"
	+ "\tSize: %s\n"
	+ "\tStatus: %s\n"
	+ "\tNetwork %s\n"
	+ "\tAffinity group %s\n",
	vm.name(),
	vm.deployment(),
	vm.cloudService(),
	vm.size(),
	vm.status().toString(),
	vm.network(),
	vm.affinityGroup()
));
```

*ARM*: import from the `com.microsoft.azure.shortcuts.resources.*` packages
{TODO}


#### Listing available VM sizes

*ASM*: import from `com.microsoft.azure.shortcuts.services.*` packages

```java
boolean supportingVM = true;
boolean supportingCloudServices = false;

List<String> sizeNames = azure.sizes().list(supportingVM, supportingCloudServices);
```

*ARM*: import from the `com.microsoft.azure.shortcuts.resources.*` packages

You need to specify the region to get the sizes. The returned sizes are indexed by their name:

```java
Map<String, Size> sizes = azure.sizes().list("westus");
```

Therefore, to get the names only:

```java
Set<String> sizeNames = azure.sizes().list("westus").keySet();
```

#### Listing available OS image names

*ASM*: import from `com.microsoft.azure.shortcuts.services.*` packages

OS images as a map, indexed by name:

```java
Map<String, OSImage> osImages = azure.osImages().list();
```

Cloud service names only:

```java
Set<String> osImageNames = azure.osImages().list().keySet();
```

*ARM*: import from the `com.microsoft.azure.shortcuts.resources.*` packages
{TODO}


### Virtual Networks

#### Creating a virtual network with a default subnet

*ASM*: import from `com.microsoft.azure.shortcuts.services.*` packages

```java
azure.networks().define("mynetwork")
	.withRegion("West US")
	.withCidr("10.0.0.0/29")
	.provision();
```

*ARM*: import from the `com.microsoft.azure.shortcuts.resources.*` packages
*[TODO]*


#### Creating a virtual network with multiple, explicitly defined subnets

*ASM*: import from `com.microsoft.azure.shortcuts.services.*` packages

```java
azure.networks().define("mynetwork")
	.withRegion("West US")
	.withCidr("10.0.0.0/28")
	.withSubnet("Foo", "10.0.0.0/29")
	.withSubnet("Bar", "10.0.0.8/29")
	.provision();
```

*ARM*: import from the `com.microsoft.azure.shortcuts.resources.*` packages
*[TODO]*

#### Listing virtual networks 

*ASM*: import from `com.microsoft.azure.shortcuts.services.*` packages

All network names in a subscription:

```java
List<String> virtualNetworkNames = azure.networks().names();
```

*ARM*: import from the `com.microsoft.azure.shortcuts.resources.*` packages

All networks in a subscription, as a map indexed by resource id:

```java
Map<String, Network> networks = azure.networks().list();
```

Resource ids only:

```java
Set<String> networkIds = azure.networks().list().keySet();
```

Networks in a specific resource group:

```java
Map<String, Network> networks = azure.networks().list("<resource-group-name">);
```


#### Reading information about a virtual network

*ASM*: import from `com.microsoft.azure.shortcuts.services.*` packages

By providing the virtual network name:

```java
Network network = azure.networks().get("mynetwork");
```

*ARM*: import from the `com.microsoft.azure.shortcuts.resources.*` packages

By providing a virtual network resource ID (returned as a key in `networks().list()`):

```java
Network network = azure.networks().get("<network-resource-id>");

StringBuilder output = new StringBuilder();
output
	.append(String.format("Neywork ID: %s\n", network.name()))
	.append(String.format("Provisioning state: %s\n", network.provisioningState()))
	.append(String.format("Address prefixes: %s\n", StringUtils.join(network.addressPrefixes(), ", ")))
	.append(String.format("DNS servers: %s\n", StringUtils.join(network.dnsServers(), ", ")));
System.out.println(output.toString());
```

The subnets of a virtual network are available from `network.subnets()`.


#### Deleting a virtual network

*ASM*: import from `com.microsoft.azure.shortcuts.services.*` packages

```java
azure.networks().delete("mynetwork");
```

*ARM*: import from the `com.microsoft.azure.shortcuts.resources.*` packages
{TODO}

### Cloud Services

#### Creating a cloud service

*ASM*: import from `com.microsoft.azure.shortcuts.services.*` packages

```java
azure.cloudServices().define("myservice")
	.withRegion("West US")
	.provision();
```

*ARM*: import from the `com.microsoft.azure.shortcuts.resources.*` packages
*[TODO]*

#### Listing cloud services in a subscription

*ASM*: import from `com.microsoft.azure.shortcuts.services.*` packages

Cloud services as a map, indexed by name:

```java
Map<String, CloudService> cloudServices = azure.cloudServices().list();
```

Cloud service names only:

```java
Set<String> cloudServiceNames = azure.cloudServices().list().keySet();
```

*ARM*: import from the `com.microsoft.azure.shortcuts.resources.*` packages
{TODO}


#### Updating an existing cloud service

*ASM*: import from `com.microsoft.azure.shortcuts.services.*` packages

```java
azure.cloudServices().update("myservice")
	.withDescription("Updated")
	.withLabel("Updated")
	.apply();
```

*ARM*: import from the `com.microsoft.azure.shortcuts.resources.*` packages
{TODO}

#### Reading information about a cloud service

*ASM*: import from `com.microsoft.azure.shortcuts.services.*` packages

```java
CloudService cloudService = azure.cloudServices().get("myservice");

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
{TODO}

#### Deleting a cloud service

*ASM*: import from `com.microsoft.azure.shortcuts.services.*` packages

```java
azure.cloudServices().delete(serviceName);
```

*ARM*: import from the `com.microsoft.azure.shortcuts.resources.*` packages
{TODO}

### Storage Accounts

#### Creating a storage account

*ASM*: import from `com.microsoft.azure.shortcuts.services.*` packages

```java
azure.storageAccounts().define("mystorage")
	.withRegion("West US")
	.provision();
```

*ARM*: import from the `com.microsoft.azure.shortcuts.resources.*` packages
*[TODO]*


#### Listing storage accounts in a subscription

*ASM*: import from `com.microsoft.azure.shortcuts.services.*` packages

As a map, indexed by name:

```java
Map<String, StorageAccount> storageAccounts = azure.storageAccounts().list();
```

Names only:

```java
List<String> storageAccountNames = azure.storageAccounts().list().keySet();
```

*ARM*: import from the `com.microsoft.azure.shortcuts.resources.*` packages
{TODO}


#### Updating a storage account

*ASM*: import from `com.microsoft.azure.shortcuts.services.*` packages

```java
azure.storageAccounts().update("mystorage")
	.withDescription("Updated")
	.withLabel("Updated")
	.apply();
```

*ARM*: import from the `com.microsoft.azure.shortcuts.resources.*` packages
{TODO}

#### Reading information about a storage account

*ASM*: import from `com.microsoft.azure.shortcuts.services.*` packages

```java
StorageAccount storageAccount = azure.storageAccounts().get(accountName);

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
	StringUtils.join(storageAccount.endpoints(), ", "),
	storageAccount.type()
));
```

*ARM*: import from the `com.microsoft.azure.shortcuts.resources.*` packages
{TODO}


#### Deleting a storage account

*ASM*: import from `com.microsoft.azure.shortcuts.services.*` packages

```java
azure.storageAccounts().update("mystorage")
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
List<String> regionNames = azure.regions().names();
```

Listing regions supporting a specific capability from the `LocationsAvailableServiceNames` options:

```
List<String> regionNames = azure.regions().list(LocationAvailableServiceNames.HIGHMEMORY);    	
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

Indexed by name:

```java
Map<String, Group> groups = azure.groups().list();
```

Names only:
```java
Set<String> groupNames = azure.groups().list().keySet();
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

#### Listing resources

All resources in a subscription, indexed by id:
 
```java
Map<String, Resource> resources = azure.resources().list();
```

Resources in a specific group:

```java
Map<String, Resource> resources = azure.resources().list("<resource-group-name>");
```

#### Reading information about a resource

If you know the full ID of the resource (e.g. you got it from the `resources().list().keySet()`), then:

```java
Resource resource = azure.resources().get("<resource-id>");
```
Else, if you know the resource name, type, provider and group, then:

```java
Resource resource = azure.resources().get(
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
	resource.provisioningState()
));
```

#### Deleting a resource

Using its ID:

```java
azure.resources().delete("<resource-id">);
```

Or using its metadata:

```java
azure.resources().delete("<short-name>", "<resource-type>", "<provider-namespace>", "<group-name>");
```

Or, if you've already gotten a reference to a `Resource` object (represented by `resource` below) from `get()`, then:

```java
resource.delete();
```

### Resource Providers

This applies only to ARM, so import from the `com.microsoft.azure.shortcuts.resources.*` packages

#### Listing resource providers

Providers as a map, indexed by namespace:

```java
Map<String, Provider> providers = azure.providers().list();
```

Namespaces only:

```java
Set<String> providerNamespaces = azure.providers().list().keySet();
```


#### Reading information about a resource provider

Using the namespace of the provider you can get from `providers().names()`:

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