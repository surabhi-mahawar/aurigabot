package com.dynamos.aurigabot.entity;

import com.dynamos.aurigabot.enums.CommandType;
import lombok.Builder;
import lombok.Data;

import javax.persistence.*;
import java.sql.Date;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@Data
@Table(name ="command")
public class Command {
    @Id
    private UUID id;
    @Enumerated(EnumType.STRING)
    private CommandType commandType;
    private String description;

//    @OneToOne(fetch = FetchType.LAZY, mappedBy = "command")
//    private Flow flow;
//
//    @OneToOne(fetch = FetchType.LAZY, mappedBy = "command")
//    private UserMessage message;
}
