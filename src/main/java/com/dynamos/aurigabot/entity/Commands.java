package com.dynamos.aurigabot.entity;

import com.dynamos.aurigabot.enums.CommandType;
import lombok.Builder;
import lombok.Data;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
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
}
