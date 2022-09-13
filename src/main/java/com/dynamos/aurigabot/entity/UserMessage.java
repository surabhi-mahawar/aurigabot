package com.dynamos.aurigabot.entity;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.dynamos.aurigabot.enums.Status;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@Entity
@Table(name = "user_message")
public class UserMessage {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "command_id")
	private Commands commands;
	
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name ="flow_id")
	private Flow flow;
	
	private int index;
	
	private long fromUserId;
	private long toUserId;
	private String message;
	@Enumerated(EnumType.STRING)
	private Status status;
	private Date receivedAt;
	private Date sentAt;
	private Date deliveredAt;
	private Date readAt;
	private String provider;
	

}
