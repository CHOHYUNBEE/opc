package org.opcufoundation.ua.examples.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "TAGVALUE")
public class TagValue_Entity {
	
	@Id
	@GeneratedValue
	@Column (name="TAGVALUEID")
	private Long TagValueId;
	
	@ManyToOne
	@JoinColumn(name="DisplayName")
	private Tag_Entity TAG;
	
	public Tag_Entity getTAG() {
		return TAG;
	}

	public void setTAG(Tag_Entity tAG) {
		if(this.TAG != null) {
			this.TAG.getTagvalueList().remove(this);
		}
		this.TAG = tAG;
		tAG.getTagvalueList().add(this);
	}

	@Column (name="SERVERPICOSECORDS")
	private String ServerPicoseconds;
	
	@Column (name="SERVERTIMESTAMP")
	private String ServerTimestamp;
	
	@Column (name="SOURCEPICOSECORDS")
	private String SourcePicoseconds;
	
	@Column (name="SOURCETIMESTAMP")
	private String SourceTimestamp;
	
	@Column (name="STATUSECODE")
	private String StatusCode;
	
	@Column (name="TAGVALUE")
	private String Value;

	public Long getTagValueId() {
		return TagValueId;
	}

	public void setTagValueId(Long tagValueId) {
		TagValueId = tagValueId;
	}



	public String getServerPicoseconds() {
		return ServerPicoseconds;
	}

	public void setServerPicoseconds(String serverPicoseconds) {
		this.ServerPicoseconds = serverPicoseconds;
	}

	public String getServerTimestamp() {
		return ServerTimestamp;
	}

	public void setServerTimestamp(String serverTimestamp) {
		this.ServerTimestamp = serverTimestamp;
	}

	public String getSourcePicoseconds() {
		return SourcePicoseconds;
	}

	public void setSourcePicoseconds(String sourcePicoseconds) {
		this.SourcePicoseconds = sourcePicoseconds;
	}

	public String getSourceTimestamp() {
		return SourceTimestamp;
	}

	public void setSourceTimestamp(String sourceTimestamp) {
		this.SourceTimestamp = sourceTimestamp;
	}

	public String getStatusCode() {
		return StatusCode;
	}

	public void setStatusCode(String statusCode) {
		this.StatusCode = statusCode;
	}

	public String getValue() {
		return Value;
	}

	public void setValue(String value) {
		this.Value = value;
	}
	

}
