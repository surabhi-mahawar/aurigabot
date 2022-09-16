package com.dynamos.aurigabot.entity;

import lombok.Builder;
import lombok.Data;

import javax.persistence.Id;
import java.sql.Date;
import java.util.UUID;

@Builder
@Data
public class Flow {
    @Id
    private UUID id;

    private String question;
    private Long index;
    private Date createdAt;
    private Date updatedAt;

}

