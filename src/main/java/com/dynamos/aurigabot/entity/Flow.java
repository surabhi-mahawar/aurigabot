package com.dynamos.aurigabot.entity;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@Entity
@Table(name = "flow")
public class Flow {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="command_id")
    private Commands commands;
	
	private String question;
	private int index;
	private Date createdAt;
	private Date updatedAt;
	
	@OneToOne(fetch = FetchType.LAZY, mappedBy = "flow")
	private UserMessage message;

}
