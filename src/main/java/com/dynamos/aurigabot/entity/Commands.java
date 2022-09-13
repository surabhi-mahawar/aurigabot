package com.dynamos.aurigabot.entity;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.dynamos.aurigabot.enums.CommandType;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "commands")
public class Commands {
	@Id
	@GeneratedValue (strategy = GenerationType.IDENTITY)
	private long id;
	@Enumerated(EnumType.STRING)
	private CommandType command;
	private String description;
	private Date createdAt;
	private Date updatedAt;
	
	@OneToOne(fetch = FetchType.LAZY, mappedBy = "commands")
	private Flow flow;
	
	@OneToOne(fetch = FetchType.LAZY, mappedBy = "commands")
	private UserMessage message;
	

}
