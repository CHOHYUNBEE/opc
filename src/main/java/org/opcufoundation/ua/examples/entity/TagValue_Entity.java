package org.opcufoundation.ua.examples.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "TAGVALUE")
public class TagValue_Entity {
	
	@Id
	@Column (name="TAGVALUEID")
	private Long TagValueId;
	
	@ManyToOne
	@JoinColumn(name="DisplayName")
	private Tag_Entity DisplayName;
	
	@Column (name="SERVERPICOSECORDS")
	private Long ServerPicoseconds;
	
	@Column (name="SERVERTIMESTAMP")
	private Long ServerTimestamp;
	
	@Column (name="SOURCEPICOSECORDS")
	private Long SourcePicoseconds;
	
	@Column (name="SOURCETIMESTAMP")
	private Long SourceTimestamp;
	
	@Column (name="STATUSECODE")
	private Long StatusCode;
	
	@Column (name="TAGVALUE")
	private Long Value;
	

}
