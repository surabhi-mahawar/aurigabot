package com.aurigabot.entity;

import com.aurigabot.dto.FlowPayloadDto;
import com.aurigabot.dto.Validation;
import com.aurigabot.enums.CommandType;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.r2dbc.postgresql.codec.Json;
import lombok.*;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.io.Serializable;
import java.util.UUID;

@Setter
@Getter
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "flow")
public class Flow implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "command_type")
    private CommandType commandType;

    @Type(type = "jsonb")
    private FlowPayloadDto payload;
//    @OneToOne(fetch = FetchType.LAZY, mappedBy = "flow")
//    private UserMessage message;

    private String question;
    private Integer index;

    @Type(type = "jsonb")
    @JsonSerialize
    @JsonDeserialize
    private Validation validation;
//    private Date createdAt;
//    private Date updatedAt;

}

