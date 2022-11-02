package com.dynamos.aurigabot.entity;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.r2dbc.postgresql.codec.Json;
import lombok.Builder;
import lombok.Data;
import org.hibernate.annotations.Type;

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

    private String commmandType;
    @Type(type = "jsonb")
    @JsonSerialize
    @JsonDeserialize
    private Json payload;
//    @OneToOne(fetch = FetchType.LAZY, mappedBy = "flow")
//    private UserMessage message;

    private String question;
    private Integer index;
//    private Date createdAt;
//    private Date updatedAt;

}

