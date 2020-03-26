package org.opcufoundation.ua.examples.entity;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;


@Entity
@Table(name = "TAG")
public class Tag_Entity {
	
	@Id
	@GeneratedValue
	@Column (name="TAGID")
	private Long TagId;
	
	@Column (name="DisplayName")
	private String DisplayName;
	//DisplayName 
	@Column (name="NodeId")
	private String NodeId;
	
	@Column (name="NodeClass")
	private String NodeClass;
	
	public String getNodeValue() {
		return NodeValue;
	}

	public void setNodeValue(String nodeValue) {
		NodeValue = nodeValue;
	}

	@Column (name="NodeValue")
	private String NodeValue;

	public String getDisplayName() {
		return DisplayName;
	}

	public void setDisplayName(String displayName) {
		this.DisplayName = displayName;
	}

	public String getNodeId() {
		return NodeId;
	}

	public void setNodeId(String nodeId) {
		this.NodeId = nodeId;
	}

	public String getNodeClass() {
		return NodeClass;
	}

	public void setNodeClass(String nodeClass) {
		this.NodeClass = nodeClass;
	}
	

}
