package org.opcufoundation.ua.examples.entity;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;


@Entity
@Table(name = "TAG")
public class Tag_Entity {
	@Id
	@Column (name="DisplayName")
	private String DisplayName;
	
	@OneToMany(mappedBy="TAG",cascade=CascadeType.PERSIST)
	private List<TagValue_Entity> tagvalueList = new ArrayList<TagValue_Entity>();
	
	public List<TagValue_Entity> getTagvalueList() {
		return tagvalueList;
	}

	public void setTagvalueList(List<TagValue_Entity> tagvalueList) {
		this.tagvalueList = tagvalueList;
	}

	@Column (name="NodeId")
	private String NodeId;
	
	@Column (name="NodeClass")
	private String NodeClass;

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
