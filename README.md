# azure-shortcuts-for-java

The goal of this project is to provide a radically simplified Java API for Azure. It follows a flavor of modern API design patterns (builder, fluent) optimized for readability, writeability and succinctness.

> :warning: **NOTE**: this is currently an experimental labs project/work in progress.

Here's an example for creating a virtual network, which is very representative of the approach followed by the shortcuts:

```java
azure.networks().define("mynetwork")
    .withRegion("US West")
    .withExistingResourceGroup("<resource-group-name>")
    .withAddressSpace("10.0.0.0/28")
    .withSubnet("Foo", "10.0.0.0/29")
    .withSubnet("Bar", "10.0.0.8/29")
    .provision();
```

The shortcuts library supports APIs "modern" ARM (Azure Resource Model) model. The previous "classic" ASM (Azure Service Model) API is no longer maintained nor documented.

A lot of short code samples are in the packages `com.microsoft.azure.shortcuts.resources.samples` (https://github.com/Microsoft/azure-shortcuts-for-java/tree/master/src/com/microsoft/azure/shortcuts/resources/samples).

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
3. from that point on, use command chaining (i.e. '.' dots) to specify the various required and optional parameters. They all look like this: `.with*()` (e.g. `.withExistingResourceGroup("myresourcegroup")`). Note that due to the special way the shortcuts APi is designed, after each "with"-setter, AutoComplete will only suggest the set of setters that are valid/required at that stage of the definition. This way, it will force you to continue specifying the suggested "with" setters until you see `.provision()` among the possible choices.
4. many resource types in Azure (e.g. virtual machines) require other resources (e.g. resource group, storage acocunt) to be already present. The `.with*` setters often enable you to either select an existing related resource (`.withExisting*()`) or to request a new such resource to be created on the fly (`.withNew*()`). When created on the fly, it is created in the region and resource group.
5. when `.provision()` becomes available among the AutoComplete choices, it means you have reached a stage in the entity definition where all the other parameters ("with"-setters) are optional (some defaults are assumed.) Calling `.provision()` is what completes the definition and starts the actual provisioning process in the cloud. 
 
### Updating existing entities

Updates to existing entities are also done in a "builder pattern/fluent interface" kind of way: 

1. start with `.update()` on the collection as the "factory" of an update template
2. command-chain the needed `.with*()` settings (usually all optional)
3. and finish off with a call to `.apply()`

In essence, the above is the shortcuts API's take on the "builder pattern + fluent interface + factory pattern + extra smarts" combo in action. It's just that instead of the more traditional `.create()` or `new` naming, the shortcuts use **`.define()`** or **`.update()`** for creating/updating objects. And instead of the more conventional `.build()`, the shortcuts use **`.provision()`** or **`.apply()`**.

> :warning: TODO Update functionality is only beginning to be implemented in the shortcuts

### Naming patterns 

In general, the shortcut naming tends to be consistent with the Azure SDK. However, it does not follow the SDK naming rigorously. Sometimes, simplicity or succinctness trumps consistency (e.g. Azure SDK has `VirtualNetwork`, shortcuts have `Network`.). 

Some helpful pointers: 

* In the cases when the same class name is used, make sure you reference the right package!
* As for class member naming, it is hard to avoid the impression that the Azure SDK heavily abuses the "get/set" convention. The shortcuts don't. In fact, it is only on the very rare occasion that using the "get" prefix is justified, so you will practically never see it in the shortcuts.
* Since the shortcuts are all about fluent interface, you will not see `.set*(...)` anywhere, only `.with*(...)`. The "with" naming convention in the context of fluent interface setters has been adopted because "set" functions are conventionally expected to return `void`, whereas "with" returns an object. Modern Java API implementations from other vendors are increasingly adopting the same "with" setter naming convention. (e.g. AWS)

And again, a quick look at any of the below code samples should make the above points rather obvious.

### Access to the underlying, wrapped Azure SDK objects

* Many shortcut objects are wrappers of Azure SDK objects. Since the shortcuts might not expose all of the settings available on the underlying Azure SDK classes, for those shortcut objects, to get access to the underlying Azure SDK object, use the `.inner()` function.

* Some Azure SDK objects can also be used as input parameters in APIs where they make sense. For example, when a storage account is expected in some shortcut API, generally it can be provided as either:
  * the shortcut `StorageAccount` object, 
  * the Azure SDK's `StorageAccount` object, 
  * or the resource id string. 

## Examples

Inside the `\*.samples` packages, you will find a number of runnable code samples (classes with `main()`). For each of the sample classes, you can just **Debug As** > **Java Application**.

Many of the samples rely on credentials files in the **root of the project**:

For the **"Resource" ARM-based APIs** specifically, you can use the very experimental *"my.authfile"* format containing all the inputs needed by the Azure Active Directory authentication and relying on you setting up a **service principal** for your app. 

Further simplification of the authentication process is a subject of active investigation, but for now you can create the file manually, as per the [Authentication](#creating-an-authenticated-client) section.

**Table of contents:**

* [Authentication](#creating-an-authenticated-client)
* [Virtual Machines](#virtual-machines)
* [Virtual Networks](#virtual-networks)
* [Network Interfaces](#network-interfaces)
* [Public IP Addresses](#public-ip-addresses)
* [Network Security Groups](#network-security-groups)
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


### Virtual Machines

#### Creating a Windows VM

> *ARM*: import from the `com.microsoft.azure.shortcuts.resources.*` packages

There are many variations possible of the following minimalistic approach:
 
```java
VirtualMachine vmWin = azure.virtualMachines().define("<vm-name>")
	.withRegion(Region.US_WEST)
    .withNewResourceGroup("<new-group-name>")
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

For example, a new resource group can be provisioned for the virtual machine (`.withNewResourceGroup(...)`) or an existing one can be selected (`.withExistingResourceGroup(...)`). 

Similarly, a new virtual network can be provisioned (`.withNewNetwork(...)`) or an existing one can be used (`.withExistingNetwork(...)`).

The private IP within the virtual network can be either dynamically allocated (`.withPrivateIpDynamic()`) or statically (`.withPrivateIpStatic("<private-ip-address>")`).

Associating the VM with a public IP is optional. An existing public IP can be assigned (`.withExistingPublicIpAddress(...)`), or a new one created (`.withNewPublicIpAddress()`). If new, then it can be optionally associated with a leaf domain label which will form the DNS record for this VM (`.withNewPublicIpAddress("<new-domain-label>")`).

If specifying the IP addresses and the network explicitly like in the above example, a new network interface is created implicitly behind the scenes and set as the primary interface for the virtual machine. The IP addresses go into the primary IP configuration for that network interface. If you want to use an already existing network interface in your subscription, then instead of `.withNewNetwork()`, invoke `.withExistingNetworkInterface()`. This will also skip over the selection of the rest of the networking information.

Creating a virtual machine requires a storage account to keep the VHD in. A new storage account can be requested (`.withNewStorageAccount()`) or an existing one (`.withExistingStorageAccount()`).

Any such related resource that is created in the process of provisioning a virtual machine will be provisioned in the same resource group and region as the virtual machine.

##### Optional settings

A number of settings are optional so they can be specified at the provisionable stage of the virtual machine definition, i.e. at the stage at which `.provision()` is available among the members. For example, the above example can rewritten to separate the provisionable stage from the required stages:

```java
VirtualMachine.DefinitionProvisionable vmProvisionable = azure.virtualMachines().define("vm" + deploymentId)
	.withRegion(Region.US_WEST)
    .withNewResourceGroup(groupName)
    .withNewNetwork("10.0.0.0/28")
    .withPrivateIpAddressDynamic()
    .withNewPublicIpAddress("vm" + deploymentId)
    .withAdminUsername("shortcuts")
    .withAdminPassword("Abcd.1234")
    .withLatestImage("MicrosoftWindowsServer", "WindowsServer", "2008-R2-SP1")
    .withSize(Size.Type.BASIC_A1)
    .withNewStorageAccount()
    .withNewDataDisk(100);
```

At this stage, additional settings can be specified that are optional before `provision()` is invoked. 

###### Attaching data disks

Based on the earlier provisionable definition, the following code attaches 2 **new** empty data disks to the virtual machine definition. Their logical unit number (LUN) is set automatically based on the order of attachment:

```java
vmProvisionable
	.withNewDataDisk(100) 	// Attach a 100 GB disk as LUN 1
    .withNewDataDisk(200); 	// Attach a 200 GB disk as LUN 2
```

Attaching an existing VHD file as a data disk:

```java
vmProvisionable
	.withExistingDataDisk("https://vm1455045717874store.blob.core.windows.net/vm1455045717874/disk0.vhd")
```

###### Selecting availability set

Based on the earlier provisionable definition, the following code specifies a new availability set to be created for this virtual machine to be associated with:

```java
vmProvisionable = vmProvisionable
	.withNewAvailabilitySet("myAvailabilitySet");
```

#### Listing VMs

All virtual machine names (or ids) in a subscription: 

> *ARM*: import from `com.microsoft.azure.shortcuts.resources.*` packages

```java
Map<String, VirtualMachine> vms = azure.virtualMachines().asMap();
System.out.println(String.format("Virtual machines: \n\t%s", String.join("\n\t", vms.keySet())));
```
Virtual machines in a specific resource group (resource model "ARM" only)
```java
Map<String, VirtualMachine> vms = azure.virtualMachines().asMap("<group-name>");
System.out.println(String.format("Virtual machines: \n\t%s", String.join("\n\t", vms.keySet())));
```

#### Getting information about a VM

> *ARM*: import from the `com.microsoft.azure.shortcuts.resources.*` packages

Using the resource id:
```java
VirtualMachine vm = azure.virtualMachines("<resource-id>");
```
Using the resource group name and virtual machine name:
```java
VirtualMachine vm = azure.virtualMachines("<resource-group-name>", "<vm-name>");
```

#### Listing available VM sizes

> *ARM*: import from the `com.microsoft.azure.shortcuts.resources.*` packages

You need to specify the region to get the sizes. The returned sizes are indexed by their name:
```java
Map<String, Size> sizes = azure.sizes().asMap("westus");
```
Therefore, to get the names only:
```java
Set<String> sizeNames = azure.sizes().asMap("westus").keySet();
```

#### Listing available OS image names

> :triangular_flag_on_post: **TODO**: 

#### Deleting a virtual machine

Any of the following approaches:
```java
azure.virtualMachines().delete("<vm-resource-id>");

azure.virtualMachines().delete("<resource-group-name>", "<vm-name>");

azure.virtualMachines("<vm-resource-id>").delete();

azure.virtualMachines("<resource-group-name>", "<vm-name>").delete();
```

#### Stopping a virtual machine

By resource group and name:
```java
azure.virtualMachines().get("<resource-group-name>", "<vm-name>").stop();
```
By resource id:
```java
azure.virtualMachines().get("<vm-resource-id>").stop();
```

#### Starting a stopped virtual machine

By resource group and name:
```java
azure.virtualMachines().get("<resource-group-name>", "<vm-name>").start();
```
By resource id:
```java
azure.virtualMachines().get("<vm-resource-id>").start();
```

#### Restarting a virtual machine

By resource group and name:
```java
azure.virtualMachines().get("<resource-group-name>", "<vm-name>").restart();
```
By resource id:
```java
azure.virtualMachines().get("<vm-resource-id>").restart();
```

#### Deallocating a virtual machine

By resource group and name:
```java
azure.virtualMachines().get("<resource-group-name>", "<vm-name>").deallocate();
```
By resource id:
```java
azure.virtualMachines().get("<vm-resource-id>").deallocate();
```


### Virtual Networks

#### Creating a virtual network

> *ARM*: import from the `com.microsoft.azure.shortcuts.resources.*` packages

With an explicitly defined address space, a default subnet containing the entirety of the IP address space, in a new auto-generated resource group:
```java
Network network = azure.networks().define("<new-network-name>")
	.withRegion(Region.US_WEST)
	.withNewResourceGroup()
	.withAddressSpace("10.0.0.0/28")
	.provision();
```
With multiple, explicitly defined subnets and an existing resource group:
```java
azure.networks().define(newNetworkName + "2")
    .withRegion(Region.US_WEST)
    .withExistingResourceGroup("<existing-resource-group-name>")
    .withAddressSpace("10.0.0.0/28")
    .withSubnet("Foo", "10.0.0.0/29")
    .withSubnet("Bar", "10.0.0.8/29")
    .provision();
```

#### Listing virtual networks 

> *ARM*: import from the `com.microsoft.azure.shortcuts.resources.*` packages

All networks in a subscription, as a map indexed by resource id:
```java
Map<String, Network> networks = azure.networks().asMap();
```
Resource ids only:
```java
Set<String> networkIds = azure.networks().asMap().keySet();
```
Networks in a specific resource group:
```java
Map<String, Network> networks = azure.networks().asMap("<resource-group-name">);
```

#### Getting information about a virtual network

> *ARM*: import from the `com.microsoft.azure.shortcuts.resources.*` packages

By providing a virtual network resource ID (returned as a key in `networks().asMap()`):
```java
Network network = azure.networks("<network-resource-id>");
```
or by providing the resource group name and the virtual network name:
```java
Network network = azure.networks("<resource-group-name>", "<network-name>");
```
The subnets of the virtual network are available from `network.subnets()`.
The IP addresses of DNS servers associated with the virtual network are available from `network.dnsServerIPs()`.
The address spaces (in CIDR format) of the virtual network are available from `network.addressSpaces()`.

#### Deleting a virtual network

> *ARM*: import from the `com.microsoft.azure.shortcuts.resources.*` packages

Any of the following methods:
```java
azure.networks().delete("<network-resource-id>");

azure.networks().delete("<resource-group-name>", "<network-name>");

azure.networks("<network-resource-id>").delete();

azure.networks("<resource-group-name>", "<network-name>").delete();
```

### Network Interfaces

> *ARM*: import from the `com.microsoft.azure.shortcuts.resources.*` packages

#### Creating a network interface

When using the minimum set of required inputs, a new resource group is created automatically, in the same region, with a name derived from the NIC's name. A virtual network providing the subnet the NIC is to be associated with is also created automatically, with one subnet covering the entirety of the address space:
```java
NetworkInterface nicMinimal = azure.networkInterfaces().define(newNetworkInterfaceName)
    .withRegion(Region.US_WEST)
    .withNewResourceGroup("<new-resource-group-name>")
    .withNewNetwork("10.0.0.0/28")
    .withPrivateIpAddressDynamic()
    .withoutPublicIpAddress()
    .provision();
```
Creating a network interface with a new resource group, dynamic private IP and a new, dynamically allocated public IP with a leaf domain label automatically generated based on the name of the NIC:
```java
NetworkInterface nic = azure.networkInterfaces().define("<new-nic-name>")
	.withRegion(Region.US_WEST)
    .withExistingResourceGroup("<existing-group-name>")
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
Map<String, NetworkInterface> nics = azure.networkInterfaces().asMap();
```
In a specific resource group: 
```java
Map<String, NetworkInterface> nics = azure.networkInterfaces().asMap("<resource-group-name>");
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

### Public IP Addresses

> *ARM*: import from the `com.microsoft.azure.shortcuts.resources.*` packages

#### Creating a public IP address

Providing minimal inputs will result in a public IP address for which a resource group will be automatically generated, dynamic IP allocation will be enabled and a leaf domain name will be specified, derived from the provided name:
```java
PublicIpAddress pipMinimal = azure.publicIpAddresses().define("<new-public-address-name>")
	.withRegion(Region.US_WEST)
   	.withNewResourceGroup()
    .provision();
```
With static IP allocation, an explicitly defined leaf domain label and a tag:
```java
PublicIpAddress pip = azure.publicIpAddresses().define(newPublicIpAddressName + "2")
	.withRegion(Region.US_WEST)
    .withExistingResourceGroup(existingGroupName)
    .withLeafDomainLabel("hellomarcins")
    .withStaticIp()
    .withTag("hello", "world")
    .provision();
```

#### Listing public IP addresses

From the entire subscription, as a `Map` indexed by name:
```java
Map<String, PublicIpAddress> pips = azure.publicIpAddresses().asMap();
```
From a specific resource group, as a `Map` indexed by name:
```java
Map<String, PublicIpAddress> pips = azure.publicIpAddresses().asMap("my-resoruce-group-name");
```

#### Getting information about an existing public IP address:

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

### Network Security Groups

> *ARM*: import from the `com.microsoft.azure.shortcuts.resources.*` packages

#### Creating a network security group

Providing minimal inputs will result in a network security group for which a resource group will be automatically generated and a default set of rules applied:
```java
NetworkSecurityGroup nsgMinimal = azure.networkSecurityGroups().define("<network-security-group-name>")
	.withRegion(Region.US_WEST)
	.withNewResourceGroup()
	.provision();
```
With an optional tag:
```java
NetworkSecurityGroup nsg = azure.networkSecurityGroups().define("<network-security-group-name>")
    .withRegion(Region.US_WEST)
    .withExistingResourceGroup("<existing-group-name>")
    .withTag("hello", "world")
    .provision();
```

#### Listing network security groups

From the entire subscription, as a `Map` indexed by id:
```java
Map<String, NetworkSecurityGroup> nsgs = azure.networkSecurityGroups().asMap();
```
From a specific resource group, as a `Map` indexed by id:
```java
Map<String, NetworkSecurityGroup> nsgs = azure.networkSecurityGroups().asMap("<resource-group-name>");
```

#### Getting information about an existing network security group:

Using its resource id:
```java
NetworkSecurityGroup nsg = azure.networkSecurityGroups().get("<resource-id>");
```
or:
```java
NetworkSecurityGroup nsg = azure.networkSecurityGroups("<resource-id>");
```
Using its resource group and name:
```java
NetworkSecurityGroup nsg  = azure.networkSecurityGroups().get("<resource-group-name>", "<nsg-name>");
```
or
```java
NetworkSecurityGroup nsg  = azure.networkSecurityGroups("<resource-group-name>", "<nsg-name>");
```

#### Deleting a network security group

Any of the following methods:
```java
azure.networkSecurityGroups().delete("<nsg-resource-id>");

azure.networkSecurityGroups().delete("<resource-group-name>", "<nsg-name>");

azure.networkSecurityGroups("<nsg-resource-id>").delete();

azure.networkSecurityGroups("<resource-group-name>", "<nsg-name>").delete();
```

### Storage Accounts

#### Creating a storage account

> *ARM*: import from the `com.microsoft.azure.shortcuts.resources.*` packages

With the required minimum set of input parameters:
```java
StorageAccount storageAccount = azure.storageAccounts().define("<new-storage-account-name>")
    .withRegion(Region.US_WEST)
    .withNewResourceGroup()
    .provision();
```
In an existing resource group:
```java
azure.storageAccounts().define("<new-storage-account-name>")
    .withRegion(Region.US_WEST)
    .withAccountType(AccountType.StandardLRS)
    .withExistingResourceGroup("<existing-resource-group-name>")
    .provision();
```

#### Listing storage accounts in a subscription

> *ARM*: import from `com.microsoft.azure.shortcuts.resources.*` packages

As a map, indexed by name:
```java
Map<String, StorageAccount> storageAccounts = azure.storageAccounts().asMap();
```
Names only:
```java
List<String> storageAccountNames = azure.storageAccounts().asMap().keySet();
```

Storage accounts in a selected resource group:
```java
Map<String, StorageAccount> storageAccounts = azure.storageAccounts().asMap("<resource-group-name>");
```

#### Updating a storage account

> :triangular_flag_on_post: **TODO**: 

#### Getting information about a storage account

> *ARM*: import from the `com.microsoft.azure.shortcuts.resources.*` packages

Getting a storage account using its unique resource id using any of the following methods:
```java
StorageAccount storageAccount = azure.storageAccounts().get("<storage-account-id>");

StorageAccount storageAccount = azure.storageAccounts("<storage-account-id>");

StorageAccount storageAccount = azure.storageAccounts("<resource-group-name>", "<storage-account-name>");
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

### Regions

#### Listing regions

> *ARM*: import from the `com.microsoft.azure.shortcuts.resources.*` packages

The `Region` enum provides the list (as constants) of all the possible Azure locations.

```java
Region[] regions = Region.values();
```

#### Getting information about a specific region

> :triangular_flag_on_post: **TODO**


### Resource Groups

> *ARM*: import from the `com.microsoft.azure.shortcuts.resources.*` packages

#### Creating a resource group

```java
azure.resourceGroups().define("myResourceGroup")
	.withRegion(Region.US_WEST)
	.withTag("hello", "world")
    .provision();
```

#### Listing resource groups

Indexed by name:
```java
Map<String, ResourceGroup> resourceGroups = azure.resourceGroups().asMap();
```
Names only:
```java
Set<String> resourceGroupNames = azure.resourceGroups().asMap().keySet();
```

#### Updating a resource group (changing its tags)

Tags are key/value pairs.
```java
azure.resourceGroups().update("<resource-group-name>")
	.withTag("foo", "bar")
	.withoutTag("hello")
	.apply();
```
You can also pass an instance of `Map<String, String>` with all the tags in it:
```java
azure.resourceGroups().update("<resource-group-name>")
	.withTags(myMap)
	.apply();
```

#### Getting information about a resource group

Either of the following methods:
```java
ResourceGroup resourceGroup = azure.resourceGroups("<resource-group-name>");

ResourceGroup resourceGroup = azure.resourceGroups().get("<resource-group-name>");
```

#### Deleting a resource group

Either of the following methods:
```java
azure.resourceGroups().delete("<resource-group-name>");

azure.resourceGroups("<resource-group-name>").delete();
````

### Resources

> This applies only to ARM, so import from the `com.microsoft.azure.shortcuts.resources.*` packages

#### Listing resources

All resources in a subscription, indexed by id:
```java
Map<String, Resource> resources = azure.resources().asMap();
```
Resources in a specific resource group:
```java
Map<String, Resource> resources = azure.resources().asMap("<resource-group-name>");
```

#### Getting information about a resource

If you know the full ID of the resource (e.g. you got it from the `resources().asMap().keySet()`), then:
```java
Resource resource = azure.resources("<resource-id>");
```
Else, if you know the resource name, type, provider and resource group, then:
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
azure.resources().delete("<short-name>", "<resource-type>", "<provider-namespace>", "<resource-group-name>");
```
Or, if you've already gotten a reference to a `Resource` object (represented by `resource` below) from `get()`, then:
```java
resource.delete();
```

### Resource Providers

> *ARM*: import from the `com.microsoft.azure.shortcuts.resources.*` packages

#### Listing resource providers

Providers as a `Map`, indexed by namespace:
```java
Map<String, Provider> providers = azure.providers().asMap();
```
Namespaces only:
```java
Set<String> providerNamespaces = azure.providers().asMap().keySet();
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

> *ARM*: import from the `com.microsoft.azure.shortcuts.resources.*` packages

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
    .withExistingResourceGroup("<existing-resource-group-name>")
    .withTag("hello", "world")
    .provision();
```

#### Listing availability sets

Availability sets as a map, in a specific resource group, indexed by id:
```java
Map<String, AvailabilitySet> availabilitySets = azure.availabilitySets().asMap("<resource-group-name>");
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
AvailabilitySet availabilitySet = azure.availabilitySets("<resource-group-name>", "<availability-set-name>");
```
or:
```java
AvailabilitySet availabilitySet = azure.availabilitySets().get("<resource-group-name>", "<availability-set-name>");
```

#### Deleting an availability set

Any of the following approaches:
```java
azure.availabilitySets().delete("<resource-id>");

azure.availabilitySets().delete("<resource-group-name>", "<availability-set-name>");

azure.availabilitySets("<resource-id>").delete();

azure.availabilitySets("<resource-group-name>", "<availability-set-name>").delete();
```
