package com.dynamos.aurigabot.entity;

import lombok.Builder;
import lombok.Data;

import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import java.sql.Date;
import java.util.UUID;

@Builder
@Data
public class Flow {
    @Id
    private UUID id;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="command_id")
    private Commands commands;

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "flow")
    private UserMessage message;

    private String question;
    private Long index;
    private Date createdAt;
    private Date updatedAt;

}

