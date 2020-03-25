package org.opcfoundation.ua.examples;

import java.io.Serializable;

public class opcTagVO implements Serializable{
	private String DisplayName;
	private Boolean IsForward;
	private String NodeClass;
	private String NodeId;
	
	public opcTagVO() {}
	
	public opcTagVO(String DisplayName,Boolean IsForward,String NodeClass,String NodeId) {
		this.DisplayName = DisplayName;
		this.IsForward = IsForward;
		this.NodeClass = NodeClass;
		this.NodeId = NodeId;
	}
	
	public String getDisplayName() {
		return DisplayName;
	}

	public void setDisplayName(String displayName) {
		DisplayName = displayName;
	}

	public Boolean getIsForward() {
		return IsForward;
	}

	public void setIsForward(Boolean isForward) {
		IsForward = isForward;
	}

	public String getNodeClass() {
		return NodeClass;
	}

	public void setNodeClass(String nodeClass) {
		NodeClass = nodeClass;
	}

	public String getNodeId() {
		return NodeId;
	}

	public void setNodeId(String nodeId) {
		NodeId = nodeId;
	}
	
	
	
}
