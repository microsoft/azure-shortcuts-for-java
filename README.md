# azure-shortcuts-for-java
The goal of this project is to provide a radically simplified API for Azure in Java. It follows a flavor of modern API design patterns (builder, fluent) optimized for readability, writeability and succinctness.

*Note: this is currently an experimental labs project/work in progress*.

Here's an example for creating a virtual network:

```java
azure.networks().define("mynetwork")
    .withRegion("US West")
    .withGroupExisting("<resource-group-name>")
    .withAddressSpace("10.0.0.0/28")
    .withSubnet("Foo", "10.0.0.0/29")
    .withSubnet("Bar", "10.0.0.8/29")
    .provision();
```

The shortcuts library supports APIs for both the "modern" ARM (Azure Resource Model) model as well as the "classic" ASM (Azure Service Model), using similar API patterns whenever reasonable.

> :warning: **NOTE**: the ASM portion might not be developed much further and might not be released to the public.

A lot of short code samples are in the packages `com.microsoft.azure.shortcuts.resources.samples` [for ARM](https://github.com/Microsoft/azure-shortcuts-for-java/tree/master/src/com/microsoft/azure/shortcuts/resources/samples) and `com.microsoft.azure.shortcuts.services.samples` [for ASM](https://github.com/Microsoft/azure-shortcuts-for-java/tree/master/src/com/microsoft/azure/shortcuts/services/samples).

It is *not* currently a goal of this library to cover all of the Azure API surface. Rather, it is to drastically simplify the hardest of the most important scenarios that developers have been running into. For everything else, [Azure SDK for Java](https://github.com/Azure/azure-sdk-for-java) is the fall back, which this project is also built on.

## Setting up the dev machine

To work on this project, it's easiest to use Eclipse and Maven (kudos to Ted Gao for [the pointers](http://ted-gao.blogspot.com/2011/09/using-maven-to-manage-library.html)):

1. Create a directory for the workspace
2. In that workspace directory, run `mvn -Declipse.workspace=. eclipse:configure-workspace`
3. In the project directory, after `git-clone`, run `mvn eclipse:eclipse -DdownloadSources=true`
4. In Eclipse, use  the workspace folder created earlier, and import the project into it (don't copy)

## Usage pre-requisites

* Java 7+
  * > :warning: **NOTE**: Although the project is currently based on Java 7, switching to Java 8 is under consideration, as v8 offers some important programming features it'd make a lot of sense to take advantage of (especially lambda support)*.  
* Azure SDK for Java v0.9.0 (installed by the pom.xml file, so no need to install separately)
* An Azure subscription

## Scope

Everything that is explicitly documented in this readme is being tested. The samples are excerpts from automation tests. Some typos are still occasionally possible - sorry! Someday this will be more automated for maximum reliability... But the general principles this project aspires to follow rigorously are *"Documentation is code"*.

There are no JavaDocs (yet). Someday there will be. Note though that the point of this API design is to *minimize* the user's dependence on API documentation. The API should "just make sense". So expect the JavaDocs to be rather minimalistic.

## Programming patterns 

If you skip over this section and jump directly to the [examples](#examples), chances are it will just make sense. But if you'd like to learn more about the design approach in the abstract, read on:

The key design principles behind the shortcuts API are: to be **intuitive, succint, consistent, and preventing you from winding up in an invalid state**.

There are a small handful of general patterns to be aware of; once you remember these, everything else should be self-explanatory:

### Creating new entities

Other than `new Azure()`, there are **no constructors anywhere**. To create a new instance of some type of cloud entity (e.g. `Network`), you use the top level "collection" of those objects (hanging off of the `Azure` client object) as the factory. And yes, there is only one single client object to instantiate and deal with. 

In more detail: 

1. start with the "collection" of those objects hanging off as a member of the `Azure` client instance object (e.g. `azure.networks()`), 
2. then call `.define("name-of-the-new-entity")` on that collection. This starts the "definition". 
3. from that point on, use command chaining (i.e. '.' dots) to specify the various required and optional parameters. They all look like this: `.with*()` (e.g. `.withGroup("myresourcegroup")`). Note that due to the special way the shortcuts APi is designed, after each "with"-setter, AutoComplete will only suggest the set of setters that are valid/required at that stage of the definition. This way, it will force you to continue specifying the suggested "with" setters until you see `.provision()` among the offered choices. 
3. when `.provision()` becomes available among the AutoComplete choices, it means you have reached a stage in the entity definition where all the other parameters ("with"-setters) are optional (some defaults are assumed.) Calling `.provision()` is what completes the definition and starts the actual provisioning process in the cloud. 
 
### Updating existing entities

Updates to existing entities are also done in a "builder pattern/fluent interface" kind of way: 

1. start with `.update()` on the collection as the "factory" of an update template
2. command-chain the needed `.with*()` settings (usually all optional)
3. and finish off with a call to `.apply()`

In essence, the above is the shortcuts API's take on the "builder pattern + fluent interface + factory pattern + extra smarts" combo in action. It's just that instead of the more traditional `.create()` or `new` naming, the shortcuts use **`.define()`** or **`.update()`** for creating/updating objects. And instead of the more conventional `.build()`, the shortcuts use **`.provision()`** or **`.apply()`**.

### Naming patterns 

In general, the shortcut naming tends to be consistent with the Azure SDK. However, it does not follow the SDK naming blindly. Sometimes, simplicity or succinctness trumps consistency (e.g. Azure SDK has `VirtualNetwork`, shortcuts have `Network`.). 

Some helpful pointers: 

* In the cases when the same class name is used, make sure you reference the right package!
* As for class member naming, it is hard to avoid the impression that the Azure SDK heavily abuses the "get/set" convention. The shortcuts don't. In fact, it is only on the very rare occasion that using the "get" prefix is justified, so you will practically never see it in the shortcuts.
* Since the shortcuts are all about fluent interface, you will not see `.set*(...)` anywhere, only `.with*(...)`. The "with" naming convention in the context of fluent interface setters has been adopted because "set" functions are conventionally expected to return `void`, whereas "with" returns an object. Modern Java API implementations from other vendors are increasingly adopting the same "with" setter naming convention. (e.g. AWS)

And again, a quick look at any of the below code samples should make the above points rather obvious.

### Access to the underlying, wrapped Azure SDK objects

* Many shortcut objects are wrappers of Azure SDK objects. Since the shortcuts might not expose all of the settings available on the underlying Azure SDK classes, for those shortcut objects, to get access to the underlying Azure SDK object, use the `.inner()` call.

* Some Azure SDK objects can also be used as input parameters in APIs where they make sense. For example, when a storage account is expected in some shortcut API, generally it can be provided as either:
  * the shortcut `StorageAccount` object, 
  * the Azure SDK's `StorageAccount` object, 
  * the resource id string (ARM), 
  * or the name (ASM).

## Examples

Inside the `\*.samples` packages, you will find a number of runnable code samples (classes with `main()`). For each of the sample classes, you can just **Debug As** > **Java Application**.

Many of the samples rely on credentials files in the **root of the project**:

* for the **"Classic" ASM-based APIs**, use a *"my.publishsettings"* file. This is the classic Publish-Settings file from Azure.

* for the **"Resource" ARM-based APIs**, you can use the very experimental *"my.authfile"* format containing all the inputs needed by the Azure Active Directory authentication and relying on you setting up a **service principal** for your app. 

Further simplification of the authentication process is a subject of active investigation, but for now you can create the file manually, as per the [Authentication](#creating-an-authenticated-client) section.

**Table of contents:**

* [Authentication](#creating-an-authenticated-client)
* [Virtual Machines](#virtual-machines)
* [Virtual Networks](#virtual-networks)
* [Cloud Services](#cloud-services-asm-only)
* [Storage Accounts](#storage-accounts)
* [Regions](#regions)
* [Resource Groups](#resource-groups-arm-only)
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

> :warning: **NOTE**: Active Directory auth for ARM currently requires a lot of inputs and token management logic. To simplify matters, the above constructor assumes you have set up a service principal for your application and can put the required inputs into this experimental PublishSettings-like XML file in the following format:

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

You can just save a file with these contents and use it as your "auth-file" in the example above.

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
> :triangular_flag_on_post: **TODO** 


#### Creating a Linux VM in a new cloud service in an existing virtual network

*ASM*: import from `com.microsoft.azure.shortcuts.services.*` packages

```java
azure.virtualMachines().define("mylinuxvm")
	.withNetworkExisting(network)
	.withSize("Small")
	.withAdminUsername("test")
	.withAdminPassword("Xyz.098")
	.withLinuxImage("b39f27a8b8c64d52b05eac6a62ebad85__Ubuntu-12_04_5_LTS-amd64-server-20150413-en-us-30GB")
	.withTcpEndpoint(22)
	.withCloudServiceNew("mycloudservice")
	.withSubnet("mysubnet")
	.provision();
```

*ARM*: import from the `com.microsoft.azure.shortcuts.resources.*` packages
> :triangular_flag_on_post: **TODO** 

#### Creating a Windows VM in an existing cloud service

*ASM*: import from `com.microsoft.azure.shortcuts.services.*` packages

```java
azure.virtualMachines().define("mywinvm")
	.withCloudServiceExisting("mycloudservice")
	.withSize("Small")
	.withAdminUsername("test")
	.withAdminPassword("Xyz.098")
	.withWindowsImage("a699494373c04fc0bc8f2bb1389d6106__Windows-Server-2012-R2-201504.01-en.us-127GB.vhd")
	.withTcpEndpoint(3389)
	.provision();
```

*ARM*: import from the `com.microsoft.azure.shortcuts.resources.*` packages
> :triangular_flag_on_post: **TODO** 


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
VirtualMachine vm = azure.virtualMachines("mylinuxvm");
System.out.println(String.format("Reading information about vm: %s\n"
	+ "\tDeployment name: %s\n"
	+ "\tService name: %s\n"
	+ "\tSize: %s\n"
	+ "\tStatus: %s\n"
	+ "\tNetwork %s\n"
	+ "\tAffinity group %s\n",
	vm.id(),
	vm.deployment(),
	vm.cloudService(),
	vm.size(),
	vm.status().toString(),
	vm.network(),
	vm.affinityGroup()
));
```

*ARM*: import from the `com.microsoft.azure.shortcuts.resources.*` packages

Using the resource id:

```java
VirtualMachine vm = azure.virtualMachines("<resource-id>");
```

Using the group name and virtual machine name:

```java
VirtualMachine vm = azure.virtualMachines("<group-name>", "<vm-name>");
```


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
> :triangular_flag_on_post: **TODO** 


### Virtual Networks

#### Creating a virtual network with an explicitly defined address space and a default subnet

*ASM*: import from `com.microsoft.azure.shortcuts.services.*` packages

```java
azure.networks().define("mynetwork")
	.withRegion("West US")	
	.withAddressSpace("10.0.0.0/29")
	.provision();
```

*ARM*: import from the `com.microsoft.azure.shortcuts.resources.*` packages

```java
azure.networks().define("mynetwork")
	.withRegion(Region.US_WEST)
    .withAddressSpace("10.0.0.0/29")
    .provision();
```

#### Creating a virtual network with multiple, explicitly defined subnets, in an existing resource group

*ASM*: import from `com.microsoft.azure.shortcuts.services.*` packages

```java
azure.networks().define("mynetwork")
	.withRegion("US West")
	.withGroupExisting("<resource-group-name>")
	.withAddressSpace("10.0.0.0/28")
	.withSubnet("Foo", "10.0.0.0/29")
	.withSubnet("Bar", "10.0.0.8/29")
	.provision();
```

*ARM*: import from the `com.microsoft.azure.shortcuts.resources.*` packages

```java
azure.networks().define(newNetworkName + "2")
    .withGroupExisting(existingGroupName)
    .withRegion(Region.US_WEST)
    .withAddressSpace("10.0.0.0/28")
    .withSubnet("Foo", "10.0.0.0/29")
    .withSubnet("Bar", "10.0.0.8/29")
    .provision();
```


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
Network network = azure.networks("<network-name>");
```

*ARM*: import from the `com.microsoft.azure.shortcuts.resources.*` packages

By providing a virtual network resource ID (returned as a key in `networks().list()`):

```java
Network network = azure.networks("<network-resource-id>");
```

or by providing the group name and the virtual network name:

```java
Network network = azure.networks("<group-name>", "<network-name>");
```

The subnets of the virtual network are available from `network.subnets()`.
The IP addresses of DNS servers associated with the virtual network are available from `network.dnsServerIPs()`.
The address spaces (in CIDR format) of the virtual network are available from `network.addressSpaces()`.


#### Deleting a virtual network

*ASM*: import from `com.microsoft.azure.shortcuts.services.*` packages

```java
azure.networks().delete("mynetwork");
```

*ARM*: import from the `com.microsoft.azure.shortcuts.resources.*` packages

```java
azure.networks().delete("<network-resource-id>");
```

or

```java
azure.networks().delete("<resource-group-name>", "<network-name>");
```

or 

```java
azure.networks("<network-resource-id>").delete();
```

or

```java
azure.networks("<resource-group-name>", "<network-name>").delete();
```


### Cloud Services (ASM Only)

Cloud services are only supported in the ASM model in Azure today. So this section is only applicable to working with in the "classic" mode. The packages to import from are under `com.microsoft.azure.shortcuts.services.*`.

#### Creating a cloud service

```java
azure.cloudServices().define("myservice")
	.withRegion("West US")
	.provision();
```

#### Listing cloud services in a subscription

Cloud services as a map, indexed by name:

```java
Map<String, CloudService> cloudServices = azure.cloudServices().list();
```

Cloud service names only:

```java
Set<String> cloudServiceNames = azure.cloudServices().list().keySet();
```

#### Updating an existing cloud service

```java
azure.cloudServices().update("myservice")
	.withDescription("Updated")
	.withLabel("Updated")
	.apply();
```

#### Reading information about a cloud service

```java
CloudService cloudService = azure.cloudServices("myservice");

System.out.println(String.format("Found cloud service: %s\n"
	+ "\tLabel: %s\n"
	+ "\tDescription: %s\n"
	+ "\tRegion: %s\n"
	+ "\tCreated: %s\n"
	+ "\tModified: %s\n"
	+ "\tAffinity group: %s\n"
	+ "\tReverse DNS FQDN: %s\n",
	cloudService.id(),
	cloudService.label(),
	cloudService.description(),
	cloudService.region(),
	cloudService.created().getTime(),
	cloudService.modified().getTime(),
	cloudService.affinityGroup(),
	cloudService.reverseDnsFqdn()));
```

#### Deleting a cloud service

```java
azure.cloudServices().delete(serviceName);
```

### Storage Accounts

#### Creating a storage account

*ASM*: import from `com.microsoft.azure.shortcuts.services.*` packages

```java
azure.storageAccounts().define("mystorage")
	.withRegion("West US")
	.provision();
```

*ARM*: import from the `com.microsoft.azure.shortcuts.resources.*` packages

With the required minimum set of input parameters:

```java
azure.storageAccounts().define("<storage-account-name>")
    .withRegion(Region.US_WEST)
    .provision();
```

In an existing resource group:

```java
azure.storageAccounts().define(storeName)
    .withRegion(Region.US_WEST)
    .withAccountType(AccountType.StandardLRS)
    .withGroupExisting("lenatest")
    .provision();
```

#### Listing storage accounts in a subscription

*ASM*: import from `com.microsoft.azure.shortcuts.services.*` packages
*ARM*: import from the `com.microsoft.azure.shortcuts.resources.*` packages

As a map, indexed by name:

```java
Map<String, StorageAccount> storageAccounts = azure.storageAccounts().list();
```

Names only:

```java
List<String> storageAccountNames = azure.storageAccounts().list().keySet();
```

*ARM only*:

Storage accounts in a selected resource group:

```java
Map<String, StorageAccount> storageAccounts = azure.storageAccounts().list("<resource-group-name>");
```

#### Updating a storage account

*ASM*: import from `com.microsoft.azure.shortcuts.services.*` packages

```java
azure.storageAccounts().update("<storage-account-name>")
	.withDescription("Updated")
	.withLabel("Updated")
	.apply();
```

*ARM*: import from the `com.microsoft.azure.shortcuts.resources.*` packages
> :triangular_flag_on_post: **TODO** 

#### Reading information about a storage account

*ASM*: import from `com.microsoft.azure.shortcuts.services.*` packages

```java
StorageAccount storageAccount = azure.storageAccounts(accountName);

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

	storageAccount.id(),
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

Getting a storage account using its unique resource id:

```java
StorageAccount storageAccount = azure.storageAccounts().get("<storage-account-id>");
```

or, shorter:

```java
StorageAccount storageAccount = azure.storageAccounts("<storage-account-id>");
```

Getting a storage account using its group and name:

```java
StorageAccount storageAccount = azure.storageAccounts("<group-name>", "<storage-account-name>");
```


#### Deleting a storage account

*ASM*: import from `com.microsoft.azure.shortcuts.services.*` packages

```java
azure.storageAccounts().delete("mystorage");
```

*ARM*: import from the `com.microsoft.azure.shortcuts.resources.*` packages

```java
azure.storageAccounts().delete("<storage-account-resource-id>");
```

or

```java
azure.storageAccounts().delete("<resource-group-name>", "<storage-account-name>");
```

or 

```java
azure.storageAccounts("<storage-account-resource-id>").delete();
```

or

```java
azure.storageAccounts("<resource-group-name>", "<storage-account-name>").delete();
```

### Regions

#### Listing regions

*ASM*: import from `com.microsoft.azure.shortcuts.services.*` packages

Listing all regions names:

```java
Set<String> regionNames = azure.regions().keySet();
```

Listing region names supporting a specific capability from the `LocationsAvailableServiceNames` options:

```java
Set<String> regionNames = azure.regions().list(LocationAvailableServiceNames.HIGHMEMORY).keySet();    	
```

*ARM*: import from the `com.microsoft.azure.shortcuts.resources.*` packages

The `Region` enum provides the list (as constants) of all the possible Azure locations.

```java
Region[] regions = Region.values();
```


#### Getting information about a specific region

*ASM*: import from `com.microsoft.azure.shortcuts.services.*` packages

Getting information, such as the list of available services or virtual machine sizes, from a specific region:

```java
Region region = azure.regions("West US"); 
List<String> availableServices = region.availableServices();
List<String> availableVMSizes = region.availableVirtualMachineSizes();
```

*ARM*: import from the `com.microsoft.azure.shortcuts.resources.*` packages
> :triangular_flag_on_post: **TODO** 

### Resource Groups (ARM Only)

This applies only to ARM, so import from the `com.microsoft.azure.shortcuts.resources.*` packages

#### Creating a resource group

```java
azure.groups().define("myResourceGroup")
	.withRegion(Region.US_WEST)
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
Group resourceGroup = azure.groups(resourceGroup);
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
azure.groups().delete("<resource-group-name>");
```

or

```java
azure.groups("<resource-group-name>").delete();
````

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
Resource resource = azure.resources("<resource-id>");
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
			
	resource.id(),
	resource.group(),
	resource.provider(),
	resource.region(),
	resource.name(),
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
Provider provider = azure.providers("microsoft.classicstorage");
System.out.println(String.format("Found provider: %s\n" 
    + "\tRegistration State: %s\n"
    + "\tAPI versions for resource types:",
    provider.id(),
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
Provider provider = azure.providers("<provider-namespace>");
for(ResourceType t : provider.resourceTypes().values()) {
	System.out.println(String.format("%s: %s", t.name(), Arrays.toString(t.apiVersions())));
}
```

#### Finding the latest API version of a resource type

```java
String latestAPIVersion = azure.providers("<provider-namespace>").resourceTypes().get("<resource-type>").latestApiVersion();
```

Or shortcut:

```java
String latestAPIVersion = azure.providers("<provider-namespace>").resourceTypes("<resource-type>").latestApiVersion();
```


### Availability Sets

This applies only to ARM, so import from the `com.microsoft.azure.shortcuts.resources.*` packages

#### Creating an availability set

With minimum inputs, in its own separate default resource group:

```java
azure.availabilitySets().define("myavailabilityset")
    .withRegion(Region.US_WEST)
    .provision();
```

Within an existing resource group, and setting a tag:

```java
azure.availabilitySets().define("myavailabilityset")
    .withRegion(Region.US_WEST)
    .withGroupExisting("myexistinggroup")
    .withTag("hello", "world")
    .provision();
```


#### Listing availability sets

Availability sets as a map, in a specific resource group, indexed by id:

```java
Map<String, AvailabilitySet> availabilitySets = azure.availabilitySets().list("myresourcegroup");
```


#### Deleting availability sets

```java
azure.availabilitySets().delete("<resource-id>");
```

or

```java
azure.availabilitySets().delete("<resource-group-name>", "<availability-set-name>");
```

or 

```java
azure.availabilitySets("<resource-id>").delete();
```

or

```java
azure.availabilitySets("<resource-group-name>", "<availability-set-name>").delete();
```
