# azure-shortcuts-for-java
A radically simplified API for Azure in Java, following a variant of fluent interface with the builder design pattern.

*Note: this is currently an experimental labs project/work in progress*

## Pre-requisites
* Java 7+
* Azure SDK for Java v0.7.0
* An Azure subscription

## Examples

### Creating an authenticated client:

This is the first step for all the other examples. Currently, it only support the legacy "publish-settings" based way of authenticating:

```java
String publishSettingsPath = "<your file>.publishsettings";
String subscriptionId = "<subscription-GUID>";
final Azure azure = new Azure(publishSettingsPath, subscriptionId);
```

### Virtual Machines

#### Creating a Linux VM in a new, default cloud service with SSH set up

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

#### Creating a Linux VM in a new cloud service in an existing virtual network

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

#### Creating a Windows VM in an existing cloud service

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


#### Listing VMs in a subscription

```java
System.out.println("Virtual machines: "+ Arrays.toString(
	azure.virtualMachines.list())); 
```

#### Reading information about a VM

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

#### Listing available VM sizes

```java
boolean supportingVM = true;
boolean supportingCloudServices = false;

System.out.println("Available VM sizes: " + Arrays.toString(
	azure.sizes.list(supportingVM, supportingCloudServices)));
```


### Virtual Networks

#### Creating a virtual network with a default subnet

```java
azure.networks.define("mynetwork")
	.withRegion("West US")
	.withCidr("10.0.0.0/29")
	.provision();
```

#### Creating a virtual network with multiple, explicitly defined subnets

```java
azure.networks.define("mynetwork")
	.withRegion("West US")
	.withCidr("10.0.0.0/28")
	.withSubnet("Foo", "10.0.0.0/29")
	.withSubnet("Bar", "10.0.0.8/29")
	.provision();
```

#### Listing virtual networks in a subscription

```java
System.out.println("My virtual networks: " + Arrays.toString(
	azure.networks.list()));
```

#### Reading information about a virtual network
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

#### Deleting a virtual network

```java
azure.networks.delete("mynetwork");
```

### Cloud Services

#### Creating a cloud service

```java
azure.cloudServices.define("myservice")
	.withRegion("West US")
	.provision();
```

#### Listing cloud services in a subscription

```java
System.out.println("My cloud services: " + Arrays.toString(
	azure.cloudServices.list()));
```

#### Updating an existing cloud service

```java
azure.cloudServices.update("myservice")
	.withDescription("Updated")
	.withLabel("Updated")
	.apply();
```

#### Reading information about a cloud service

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

#### Deleting a cloud service

```java
azure.cloudServices.delete(serviceName);
```

### Storage Accounts

#### Creating a storage account

```java
azure.storageAccounts.define("mystorage")
	.withRegion("West US")
	.provision();
```

#### Listing storage accounts in a subscription

```java
System.out.println("My storage accounts: " + Arrays.toString(
	azure.storageAccounts.list()));
```

#### Updating a storage account

```java
azure.storageAccounts.update("mystorage")
	.withDescription("Updated")
	.withLabel("Updated")
	.apply();
```

#### Reading information about a storage account

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

#### Deleting a storage account

```java
azure.storageAccounts.update("mystorage")
	.withDescription("Updated")
	.withLabel("Updated")
	.apply();
```

### Listing available regions

This returns regions supporting Virtual Machines specifically. For all regions, use the parameter-less overload.

```java
System.out.println("Available regions: " + Arrays.toString(
	azure.regions.list(LocationAvailableServiceNames.PERSISTENTVMROLE)));
```