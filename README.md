# azure-shortcuts-for-java

The goal of this project is to provide a radically simplified Java API for Azure. It follows a flavor of modern API design patterns (builder, fluent) optimized for readability, writeability and succinctness.

> :warning: **NOTE**: this is currently an experimental labs project/work in progress.

Here's an example for creating a virtual network, which is very representative of the approach followed by the shortcuts:

```java
azure.networks().define("mynetwork")
    .withRegion("US West")
    .withExistingGroup("<resource-group-name>")
    .withAddressSpace("10.0.0.0/28")
    .withSubnet("Foo", "10.0.0.0/29")
    .withSubnet("Bar", "10.0.0.8/29")
    .provision();
```

The shortcuts library supports APIs for both the "modern" ARM (Azure Resource Model) model as well as the "classic" ASM (Azure Service Model), using similar API patterns whenever reasonable.

> :warning: **NOTE**: the ASM portion might not be developed much further and might not be released to the public.

A lot of short code samples are in the packages `com.microsoft.azure.shortcuts.resources.samples` [for ARM](https://github.com/Microsoft/azure-shortcuts-for-java/tree/master/src/com/microsoft/azure/shortcuts/resources/samples) and `com.microsoft.azure.shortcuts.services.samples` [for ASM](https://github.com/Microsoft/azure-shortcuts-for-java/tree/master/src/com/microsoft/azure/shortcuts/services/samples).

It is *not* currently a goal of this library to cover all of the Azure API surface. Rather, it is to drastically simplify the hardest of the most important scenarios that developers have been running into. For everything else, the [Azure SDK for Java](https://github.com/Azure/azure-sdk-for-java) is the fallback, which this project is also built on.

## Setting up the dev machine

To work on this project, it's easiest to use Eclipse and Maven (kudos to Ted Gao for [the pointers](http://ted-gao.blogspot.com/2011/09/using-maven-to-manage-library.html)):

1. Create a directory for the workspace
2. In that workspace directory, run `mvn -Declipse.workspace=. eclipse:configure-workspace`
3. In the project directory, after `git-clone`, run `mvn eclipse:eclipse -DdownloadSources=true`
4. In Eclipse, use  the workspace folder created earlier, and import the project into it (don't copy)

## Usage pre-requisites

* Java 7+

> :warning: **NOTE**: Although the project is currently based on Java 7, switching to Java 8 is under consideration, as v8 offers some important programming features it'd make a lot of sense to take advantage of (especially lambda support).  

* Azure SDK for Java v0.9.0 (installed by the pom.xml file, so no need to install separately)
* An Azure subscription

## Scope

Everything that is explicitly documented in this readme is being tested. The samples are excerpts from automation tests. Some typos are still occasionally possible - sorry! Someday this will be more automated for maximum reliability. But the general principles this project aspires to follow rigorously are *"Documentation is code"*.

There are no JavaDocs (yet). Someday there will be. Note though that the point of this API design is to *minimize* the user's dependence on API documentation. The API should "just make sense". So expect the JavaDocs to be rather minimalistic.

## Programming patterns 

If you skip over this section and jump directly to the [examples](#examples), chances are it will "just make sense". But if you'd like to learn more about the design approach in the abstract, read on:

The key design principles behind the shortcuts API are: to be **intuitive, succint, consistent, and preventing you from winding up in an invalid state**.

There are a small handful of general patterns to be aware of; once you remember these, everything else should be self-explanatory:

### Creating new entities

Other than `new Azure()`, there are **no constructors anywhere**. To create a new instance of some type of cloud entity (e.g. `Network`), you use the top level "collection" of those objects (hanging off of the `Azure` client object) as the factory. And yes, there is only one single client object to instantiate and deal with. 

In more detail: 

1. start with the "collection" of those objects hanging off as a member of the `Azure` client instance object (e.g. `azure.networks()`), 
2. then call `.define("name-of-the-new-entity")` on that collection. This starts the "definition". 
3. from that point on, use command chaining (i.e. '.' dots) to specify the various required and optional parameters. They all look like this: `.with*()` (e.g. `.withGroup("myresourcegroup")`). Note that due to the special way the shortcuts APi is designed, after each "with"-setter, AutoComplete will only suggest the set of setters that are valid/required at that stage of the definition. This way, it will force you to continue specifying the suggested "with" setters until you see `.provision()` among the possible choices.
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
* [Network Interfaces](#network-interfaces)
* [Public IP Addresses](#public-ip-addresses)
* [Cloud Services](#cloud-services)
* [Storage Accounts](#storage-accounts)
* [Regions](#regions)
* [Resource Groups](#resource-groups)
* [Resources](#resources)
* [Resource Providers](#resource-providers)
* [Availability Sets](#availability-sets)

### Creating an authenticated client

This is the first step for all the other examples.:

> *ARM*: import from the `com.microsoft.azure.shortcuts.resources.*` packages

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

> *ASM*: import from `com.microsoft.azure.shortcuts.services.*` packages

```java
String publishSettingsPath = "<your file>.publishsettings";
String subscriptionId = "<subscription-GUID>";
final Azure azure = new Azure(publishSettingsPath, subscriptionId);
```

### Virtual Machines

#### Creating a Linux VM in a new, default cloud service with SSH set up

> :triangular_flag_on_post: **TODO**: *ARM*: import from the `com.microsoft.azure.shortcuts.resources.*` packages

> *ASM*: import from `com.microsoft.azure.shortcuts.services.*` packages

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


#### Creating a Linux VM in a new cloud service in an existing virtual network

> :triangular_flag_on_post: **TODO**: *ARM*: import from the `com.microsoft.azure.shortcuts.resources.*` packages 

> *ASM*: import from `com.microsoft.azure.shortcuts.services.*` packages

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


#### Creating a Windows VM in an existing cloud service

> *ARM*: import from the `com.microsoft.azure.shortcuts.resources.*` packages

There are many variations possible of the following minimalistic approach:
 
```java
VirtualMachine vmWin = azure.virtualMachines().define("<vm-name>")
	.withRegion(Region.US_WEST)
    .withNewGroup("<new-group-name>")
    .withNewNetwork("10.0.0.0/28")
    .withPrivateIpAddressDynamic()
    .withNewPublicIpAddress("<new-domain-label>")
    .withAdminUsername("<admin-user-name>")
    .withAdminPassword("<password>")
    .withLatestImage("MicrosoftWindowsServer", "WindowsServer", "2008-R2-SP1")
    .withSize(Size.Type.BASIC_A1)
    .withNewStorageAccount()
    .provision();
```		

As a shortcut, this approach combines the provisioning or selection of the related required resources into one statement. 

For example, a new group can be provisioned for the virtual machine (`.withNewGroup(...)`) or an existing one can be selected (`.withExistingGroup(...)`). 

Similarly, a new virtual network can be provisioned (`.withNewNetwork(...)`) or an existing one can be used (`.withExistingNetwork(...)`).

The private IP within the virtual network can be either dynamically allocated (`.withPrivateIpDynamic()`) or statically (`.withPrivateIpStatic("<private-ip-address>")`).

Associating the VM with a public IP is optional. An existing public IP can be assigned (`.withExistingPublicIpAddress(...)`), or a new one created (`.withNewPublicIpAddress()`). If new, then it can be optionally associated with a leaf domain label which will form the DNS record for this VM (`.withNewPublicIpAddress("<new-domain-label>")`).

If specifying the IP addresses and the network explicitly like in the above example, a new network interface is created implicitly behind the scenes and set as the primary interface for the virtual machine. The IP addresses go into the primary IP configuration for that network interface. If you want to use an already existing network interface in your subscription, then instead of `.withNewNetwork()`, invoke `.withExistingNetworkInterface()`. This will also skip over the selection of the rest of the networking information.

Creating a virtual machine requires a storage account to keep the VHD in. A new storage account can be requested (`.withNewStorageAccount()`) or an existing one (`.withExistingStorageAccount()`).
  
Any such related resource that is created in the process of provisioning a virtual machine will be provisioned in the same resource group and region as the virtual machine.

> *ASM*: import from `com.microsoft.azure.shortcuts.services.*` packages

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

#### Listing VMs

All virtual machine names (or ids) in a subscription: 

> *ARM*: import from `com.microsoft.azure.shortcuts.resources.*` packages

```java
Map<String, VirtualMachine> vms = azure.virtualMachines().list();
System.out.println(String.format("Virtual machines: \n\t%s", String.join("\n\t", vms.keySet())));
```
Virtual machines in a specific resource group (resource model "ARM" only)
```java
Map<String, VirtualMachine> vms = azure.virtualMachines().list("<group-name>");
System.out.println(String.format("Virtual machines: \n\t%s", String.join("\n\t", vms.keySet())));
```

> *ASM*: import from `com.microsoft.azure.shortcuts.services.*` packages

```java
List<String> vmNames = azure.virtualMachines().names();
```


#### Getting information about a VM

> *ARM*: import from the `com.microsoft.azure.shortcuts.resources.*` packages

Using the resource id:
```java
VirtualMachine vm = azure.virtualMachines("<resource-id>");
```
Using the group name and virtual machine name:
```java
VirtualMachine vm = azure.virtualMachines("<group-name>", "<vm-name>");
```

>*ASM*: import from `com.microsoft.azure.shortcuts.services.*` packages

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

#### Listing available VM sizes

> *ARM*: import from the `com.microsoft.azure.shortcuts.resources.*` packages

You need to specify the region to get the sizes. The returned sizes are indexed by their name:
```java
Map<String, Size> sizes = azure.sizes().list("westus");
```
Therefore, to get the names only:
```java
Set<String> sizeNames = azure.sizes().list("westus").keySet();
```

> *ASM*: import from `com.microsoft.azure.shortcuts.services.*` packages

```java
boolean supportingVM = true;
boolean supportingCloudServices = false;

List<String> sizeNames = azure.sizes().list(supportingVM, supportingCloudServices);
```

#### Listing available OS image names

> :triangular_flag_on_post: **TODO**: *ARM*: import from the `com.microsoft.azure.shortcuts.resources.*` packages

> *ASM*: import from `com.microsoft.azure.shortcuts.services.*` packages

OS images as a map, indexed by name:
```java
Map<String, OSImage> osImages = azure.osImages().list();
```
OS image names only:
```java
Set<String> osImageNames = azure.osImages().list().keySet();
```

### Virtual Networks

#### Creating a virtual network

> *ARM*: import from the `com.microsoft.azure.shortcuts.resources.*` packages

With an explicitly defined address space, a default subnet containing the entirety of the IP address space, in a new auto-generated resource group:
```java
Network network = azure.networks().define("<new-network-name>")
	.withRegion(Region.US_WEST)
	.withNewGroup()
	.withAddressSpace("10.0.0.0/28")
	.provision();
```
With multiple, explicitly defined subnets and an existing resource group:
```java
azure.networks().define(newNetworkName + "2")
    .withRegion(Region.US_WEST)
    .withExistingGroup("<existing-group-name>")
    .withAddressSpace("10.0.0.0/28")
    .withSubnet("Foo", "10.0.0.0/29")
    .withSubnet("Bar", "10.0.0.8/29")
    .provision();
```

> *ASM*: import from `com.microsoft.azure.shortcuts.services.*` packages

With an explicitly defined address space and a default subnet containing the entirety of the IP address space:
```java
azure.networks().define("mynetwork")
	.withRegion("West US")	
	.withAddressSpace("10.0.0.0/29")
	.provision();
```
With multiple, explicitly defined subnets:
```java
azure.networks().define("mynetwork")
	.withRegion("US West")
	.withAddressSpace("10.0.0.0/28")
	.withSubnet("Foo", "10.0.0.0/29")
	.withSubnet("Bar", "10.0.0.8/29")
	.provision();
```

#### Listing virtual networks 

> *ARM*: import from the `com.microsoft.azure.shortcuts.resources.*` packages

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

> *ASM*: import from `com.microsoft.azure.shortcuts.services.*` packages

All network names in a subscription:
```java
List<String> virtualNetworkNames = azure.networks().names();
```

#### Getting information about a virtual network

> *ARM*: import from the `com.microsoft.azure.shortcuts.resources.*` packages

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

> *ASM*: import from `com.microsoft.azure.shortcuts.services.*` packages

By providing the virtual network name:
```java
Network network = azure.networks("<network-name>");
```

#### Deleting a virtual network

> *ARM*: import from the `com.microsoft.azure.shortcuts.resources.*` packages

Any of the following methods:
```java
azure.networks().delete("<network-resource-id>");

azure.networks().delete("<resource-group-name>", "<network-name>");

azure.networks("<network-resource-id>").delete();

azure.networks("<resource-group-name>", "<network-name>").delete();
```

> *ASM*: import from `com.microsoft.azure.shortcuts.services.*` packages

```java
azure.networks().delete("mynetwork");
```

### Network Interfaces

> This applies only to ARM, so import from the `com.microsoft.azure.shortcuts.resources.*` packages

#### Creating a network interface

When using the minimum set of required inputs, a new resource group is created automatically, in the same region, with a name derived from the NIC's name. A virtual network providing the subnet the NIC is to be associated with is also created automatically, with one subnet covering the entirety of the address space:
```java
NetworkInterface nicMinimal = azure.networkInterfaces().define(newNetworkInterfaceName)
    .withRegion(Region.US_WEST)
    .withNewGroup("<new-resource-group-name>")
    .withNewNetwork("10.0.0.0/28")
    .withPrivateIpAddressDynamic()
    .withoutPublicIpAddress()
    .provision();
```
Creating a network interface with a new resource group, dynamic private IP and a new, dynamically allocated public IP with a leaf domain label automatically generated based on the name of the NIC:
```java
NetworkInterface nic = azure.networkInterfaces().define("<new-nic-name>")
	.withRegion(Region.US_WEST)
    .withExistingGroup("<existing-group-name>")
    .withExistingNetwork(network)
    .withSubnet("subnet1")
    .withPrivateIpAddressStatic("10.0.0.5")
    .withNewPublicIpAddress()
    .withTag("hello", "world")
    .provision();
```

#### Listing network interfaces

In the subscription (all resource groups):
```java
Map<String, NetworkInterface> nics = azure.networkInterfaces().list();
```
In a specific resource group: 
```java
Map<String, NetworkInterface> nics = azure.networkInterfaces().list("<resource-group-name>");
```

#### Getting information about an existing network interface

Using its resource id:
```java
NetworkInterface nic = azure.networkInterfaces().get("<resource-id>");
```
or:
```java
NetworkInterface nic = azure.networkInterfaces("<resource-id>");
```
Using its resource group and name:
```java
NetworkInterface nic = azure.networkInterfaces().get("<resource-group-name>", "<network-interface-name>");
```
or
```java
NetworkInterface nic = azure.networkInterfaces("<resource-group-name>", "<network-interface-name>");
```

#### Deleting a network interface

Any of the following approaches:
```java
azure.networkInterfaces().delete("<resource-id>");

azure.networkInterfaces().delete("<resource-group-name>", "<network-interface-name>");

azure.networkInterfaces("<resource-id>").delete();

azure.networkInterfaces("<resource-group-name>", "<network-interface-name>").delete();
```

## Public IP Addresses

> This applies only to ARM, so import from the `com.microsoft.azure.shortcuts.resources.*` packages

### Creating a public IP address

Providing minimal inputs will result in a public IP address for which a resource group will be automatically generated, dynamic IP allocation will be enabled and a leaf domain name will be specified, derived from the provided name:
```java
PublicIpAddress pipMinimal = azure.publicIpAddresses().define("<new-public-address-name>")
	.withRegion(Region.US_WEST)
   	.withNewGroup()
    .provision();
```
With static IP allocation, an explicitly defined leaf domain label and a tag:
```java
PublicIpAddress pip = azure.publicIpAddresses().define(newPublicIpAddressName + "2")
	.withRegion(Region.US_WEST)
    .withExistingGroup(existingGroupName)
    .withLeafDomainLabel("hellomarcins")
    .withStaticIp()
    .withTag("hello", "world")
    .provision();
```

### Listing public IP addresses

From the entire subscription, as a `Map` indexed by name:
```java
Map<String, PublicIpAddress> pips = azure.publicIpAddresses().list();
```
From a specific resource group, as a `Map` indexed by name:
```java
Map<String, PublicIpAddress> pips = azure.publicIpAddresses().list("my-resoruce-group-name");
```

### Getting information about an existing public IP address:

#### Getting information about an existing network interface

Using its resource id:
```java
PublicIpAddress pip = azure.publicIpAddresses().get("resource-id");
```
or:
```java
PublicIpAddress pip = azure.publicIpAddresses("resource-id");
```
Using its resource group and name:
```java
PublicIpAddress pip  = azure.publicIpAddresses().get("<resource-group-name>", "<pip-name>");
```
or
```java
PublicIpAddress pip  = azure.publicIpAddresses("<resource-group-name>", "<pip-name>");
```

#### Deleting a public IP address

Any of the following methods:
```java
azure.publicIpAddresses().delete("<pip-resource-id>");

azure.publicIpAddresses().delete("<resource-group-name>", "<pip-name>");

azure.publicIpAddresses("<pip-resource-id>").delete();

azure.publicIpAddresses("<resource-group-name>", "<pip-name>").delete();
```

### Cloud Services

> Cloud services are only supported in the ASM model in Azure today. So this section is only applicable to working with in the "classic" mode. The packages to import from are under `com.microsoft.azure.shortcuts.services.*`.

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

#### Getting information about a cloud service

```java
CloudService cloudService = azure.cloudServices("myservice");
```

#### Deleting a cloud service

```java
azure.cloudServices().delete(serviceName);
```

### Storage Accounts

#### Creating a storage account

> *ARM*: import from the `com.microsoft.azure.shortcuts.resources.*` packages

With the required minimum set of input parameters:
```java
StorageAccount storageAccount = azure.storageAccounts().define("<new-storage-account-name>")
    .withRegion(Region.US_WEST)
    .withNewGroup()
    .provision();```
In an existing resource group:
```java
azure.storageAccounts().define("<new-storage-account-name>")
    .withRegion(Region.US_WEST)
    .withAccountType(AccountType.StandardLRS)
    .withExistingGroup("<existing-group-name>")
    .provision();
```

> *ASM*: import from `com.microsoft.azure.shortcuts.services.*` packages

```java
azure.storageAccounts().define("mystorage")
	.withRegion("West US")
	.provision();
```

#### Listing storage accounts in a subscription

> *ARM*: import from `com.microsoft.azure.shortcuts.resources.*` packages
> *ASM*: import from `com.microsoft.azure.shortcuts.services.*` packages

As a map, indexed by name:
```java
Map<String, StorageAccount> storageAccounts = azure.storageAccounts().list();
```
Names only:
```java
List<String> storageAccountNames = azure.storageAccounts().list().keySet();
```

> *ARM only*:

Storage accounts in a selected resource group:
```java
Map<String, StorageAccount> storageAccounts = azure.storageAccounts().list("<resource-group-name>");
```

#### Updating a storage account

> :triangular_flag_on_post: **TODO**: *ARM*: import from the `com.microsoft.azure.shortcuts.resources.*` packages

> *ASM*: import from `com.microsoft.azure.shortcuts.services.*` packages

```java
azure.storageAccounts().update("<storage-account-name>")
	.withDescription("Updated")
	.withLabel("Updated")
	.apply();
```

#### Getting information about a storage account

> *ARM*: import from the `com.microsoft.azure.shortcuts.resources.*` packages

Getting a storage account using its unique resource id using any of the following methods:
```java
StorageAccount storageAccount = azure.storageAccounts().get("<storage-account-id>");

StorageAccount storageAccount = azure.storageAccounts("<storage-account-id>");

StorageAccount storageAccount = azure.storageAccounts("<group-name>", "<storage-account-name>");
```

> *ASM*: import from `com.microsoft.azure.shortcuts.services.*` packages

```java
StorageAccount storageAccount = azure.storageAccounts(accountName);
```

#### Deleting a storage account

> *ARM*: import from the `com.microsoft.azure.shortcuts.resources.*` packages

Any of the following methods:
```java
azure.storageAccounts().delete("<storage-account-resource-id>");

azure.storageAccounts().delete("<resource-group-name>", "<storage-account-name>");

azure.storageAccounts("<storage-account-resource-id>").delete();

azure.storageAccounts("<resource-group-name>", "<storage-account-name>").delete();
```

> *ASM*: import from `com.microsoft.azure.shortcuts.services.*` packages

```java
azure.storageAccounts().delete("mystorage");
```

### Regions

#### Listing regions

> *ARM*: import from the `com.microsoft.azure.shortcuts.resources.*` packages

The `Region` enum provides the list (as constants) of all the possible Azure locations.

```java
Region[] regions = Region.values();
```

> *ASM*: import from `com.microsoft.azure.shortcuts.services.*` packages

Listing all regions names:
```java
Set<String> regionNames = azure.regions().keySet();
```
Listing region names supporting a specific capability from the `LocationsAvailableServiceNames` options:
```java
Set<String> regionNames = azure.regions().list(LocationAvailableServiceNames.HIGHMEMORY).keySet();    	
```

#### Getting information about a specific region

> :triangular_flag_on_post: **TODO**: *ARM*: import from the `com.microsoft.azure.shortcuts.resources.*` packages

> *ASM*: import from `com.microsoft.azure.shortcuts.services.*` packages

Getting information, such as the list of available services or virtual machine sizes, from a specific region:
```java
Region region = azure.regions("West US"); 
List<String> availableServices = region.availableServices();
List<String> availableVMSizes = region.availableVirtualMachineSizes();
```

### Resource Groups

> This applies only to ARM, so import from the `com.microsoft.azure.shortcuts.resources.*` packages

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
You can also pass an instance of `Map<String, String>` with all the tags in it:
```java
azure.groups().update("<resource-group-name>")
	.withTags(myMap)
	.apply();
```

#### Getting information about a resource group

Either of the following methods:
```java
Group resourceGroup = azure.groups("<resource-group-name>");

Group resourceGroup = azure.groups().get("<resource-group-name>");
```

#### Deleting a resource group

Either of the following methods:
```java
azure.groups().delete("<resource-group-name>");

azure.groups("<resource-group-name>").delete();
````

### Resources

> This applies only to ARM, so import from the `com.microsoft.azure.shortcuts.resources.*` packages

#### Listing resources

All resources in a subscription, indexed by id:
```java
Map<String, Resource> resources = azure.resources().list();
```
Resources in a specific group:
```java
Map<String, Resource> resources = azure.resources().list("<resource-group-name>");
```

#### Getting information about a resource

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

#### Deleting a resource

Using its resource ID:
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

> This applies only to ARM, so import from the `com.microsoft.azure.shortcuts.resources.*` packages

#### Listing resource providers

Providers as a `Map`, indexed by namespace:
```java
Map<String, Provider> providers = azure.providers().list();
```
Namespaces only:
```java
Set<String> providerNamespaces = azure.providers().list().keySet();
```

#### Getting information about a resource provider

Using the namespace of the provider you can get from `providers().names()`:
```java
Provider provider = azure.providers("microsoft.classicstorage");
```

#### Listing provider resource types and their versions

```java
Provider provider = azure.providers("<provider-namespace>");
for(ResourceType t : provider.resourceTypes().values()) {
	System.out.println(String.format("%s: %s", t.name(), Arrays.toString(t.apiVersions())));
}
```

#### Finding the latest API version of a resource type

Either of the following methods;
```java
String latestAPIVersion = azure.providers("<provider-namespace>").resourceTypes().get("<resource-type>").latestApiVersion();

String latestAPIVersion = azure.providers("<provider-namespace>").resourceTypes("<resource-type>").latestApiVersion();
```


### Availability Sets

> This applies only to ARM, so import from the `com.microsoft.azure.shortcuts.resources.*` packages

#### Creating an availability set

With minimum inputs, in its own new default resource group:
```java
azure.availabilitySets().define("myavailabilityset")
    .withRegion(Region.US_WEST)
    .witgGroupNew()
    .provision();
```
Within an existing resource group, and setting a tag:
```java
azure.availabilitySets().define("myavailabilityset")
    .withRegion(Region.US_WEST)
    .withExistingGroup("myexistinggroup")
    .withTag("hello", "world")
    .provision();
```

#### Listing availability sets

Availability sets as a map, in a specific resource group, indexed by id:
```java
Map<String, AvailabilitySet> availabilitySets = azure.availabilitySets().list("myresourcegroup");
```

#### Getting information about an availability set

Using its resource id:
```java
AvailabilitySet availabilitySet = azure.availabilitySets("<resource-id>");
```
or:
```java
AvailabilitySet availabilitySet = azure.availabilitySets().get("<resource-id>");
``` 
Using its resource group and name:
```java
AvailabilitySet availabilitySet = azure.availabilitySets("<group-name>", "<availability-set-name>");
```
or:
```java
AvailabilitySet availabilitySet = azure.availabilitySets().get("<group-name>", "<availability-set-name>");
```

#### Deleting an availability set

Any of the following approaches:
```java
azure.availabilitySets().delete("<resource-id>");

azure.availabilitySets().delete("<resource-group-name>", "<availability-set-name>");

azure.availabilitySets("<resource-id>").delete();

azure.availabilitySets("<resource-group-name>", "<availability-set-name>").delete();
```
