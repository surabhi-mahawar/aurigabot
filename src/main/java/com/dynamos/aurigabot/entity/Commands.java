package com.dynamos.aurigabot.entity;

import com.dynamos.aurigabot.enums.CommandType;
import lombok.Builder;
import lombok.Data;

import javax.persistence.*;
import java.sql.Date;
import java.util.UUID;

@Builder
@Data
@Table(name ="commands")
public class Commands {
    @Id
    private UUID id;
    @Enumerated(EnumType.STRING)
    private CommandType commandType;
    private String description;
    private Date createdAt;
    private Date updatedAt;

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "commands")
    private Flow flow;

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "commands")
    private UserMessage message;
}
