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

import com.microsoft.azure.management.network.models.SecurityRuleAccess;
import com.microsoft.azure.management.network.models.SecurityRuleDirection;
import com.microsoft.azure.shortcuts.common.Attachable;

public interface NetworkSecurityRule {
	
	/**
	 * The possible directions of the network traffic supported by a network security rule
	 */
	public enum Direction {
		INBOUND(SecurityRuleDirection.INBOUND),
		OUTBOUND(SecurityRuleDirection.OUTBOUND);
		
		private final String name;
		private Direction(String name) {
			this.name = name;
		}
		
		@Override
		public String toString() {
			return this.name;
		}
	}


	/**
	 * The possible permission types supported by a network security rule
	 */
	public enum Permission {
		ALLOW(SecurityRuleAccess.ALLOW),
		DENY(SecurityRuleAccess.DENY);
		
		private final String name;
		private Permission(String name) {
			this.name = name;
		}
		
		@Override
		public String toString() {
			return this.name;
		}
	}
	
	
	public interface Definition<PARENT> extends
		DefinitionBlank<PARENT>,
		DefinitionWithDirection<PARENT>,
		DefinitionWithPermission<PARENT>,
		DefinitionWithProtocol<PARENT>,
		DefinitionWithSourceAddress<PARENT>,
		DefinitionWithSourcePort<PARENT>,
		DefinitionAttachable<PARENT>,
		DefinitionWithDestinationAddress<PARENT>,
		DefinitionWithDestinationPort<PARENT>
	{}
		
		
	public interface DefinitionBlank<PARENT> extends DefinitionWithDirectionPermission<PARENT> {}
	
	public interface DefinitionWithDirectionPermission<PARENT> {
		DefinitionWithSourceAddress<PARENT> allowInbound();
		DefinitionWithSourceAddress<PARENT> allowOutbound();
		DefinitionWithSourceAddress<PARENT> denyInbound();
		DefinitionWithSourceAddress<PARENT> denyOutbound();
	}
	
	public interface DefinitionWithDirection<PARENT> {
		DefinitionWithSourceAddress<PARENT> withInboundDirection();
		DefinitionWithSourceAddress<PARENT> withOutboundDirection();
	}
		
	public interface DefinitionWithPermission<PARENT> {
		DefinitionWithDirection<PARENT> withAllowPermission();
		DefinitionWithDirection<PARENT> withDenyPermission();
	}
		
	public interface DefinitionWithSourceAddress<PARENT> {
		DefinitionWithSourcePort<PARENT> fromAddress(String cidr);
		DefinitionWithSourcePort<PARENT> fromAnyAddress();
	}
		
	public interface DefinitionWithSourcePort<PARENT> {
		DefinitionWithDestinationAddress<PARENT> fromPort(int port);
		DefinitionWithDestinationAddress<PARENT> fromAnyPort();
		DefinitionWithDestinationAddress<PARENT> fromPortRange(int from, int to);
	}
		
	public interface DefinitionWithDestinationAddress<PARENT> {
		DefinitionWithDestinationPort<PARENT> toAddress(String cidr);
		DefinitionWithDestinationPort<PARENT> toAnyAddress();
	}	
		
	public interface DefinitionWithDestinationPort<PARENT> {
		DefinitionWithProtocol<PARENT> toPort(int port);
		DefinitionWithProtocol<PARENT> toAnyPort();
		DefinitionWithProtocol<PARENT> toPortRange(int from, int to);
	}
		
	public interface DefinitionWithProtocol<PARENT> {
		DefinitionAttachable<PARENT> withProtocol(Protocol protocol);
		DefinitionAttachable<PARENT> withAnyProtocol();
	}
		
	public interface DefinitionAttachable<PARENT> extends Attachable<PARENT> {
		DefinitionAttachable<PARENT> withPriority(int priority);
	}
}
