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
	
//	@ManyToOne
//	@JoinColumn(name="DisplayName")
//	private Tag_Entity DisplayName;
	
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
	

}
