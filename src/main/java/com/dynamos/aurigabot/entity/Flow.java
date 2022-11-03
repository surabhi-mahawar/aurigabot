package com.dynamos.aurigabot.entity;

import com.dynamos.aurigabot.enums.CommandType;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.r2dbc.postgresql.codec.Json;
import lombok.*;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.sql.Date;
import java.util.UUID;

@Setter
@Getter
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "flow")
public class Flow {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "command_type")
    private CommandType commandType;

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

