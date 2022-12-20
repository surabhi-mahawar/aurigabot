package com.aurigabot.entity;

import com.aurigabot.dto.DateValidationDto;
import com.aurigabot.dto.FlowPayloadDto;
import com.aurigabot.dto.TextValidationDto;
import com.aurigabot.dto.ValidationDto;
import com.aurigabot.enums.CommandType;
import lombok.*;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.UUID;

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
    private FlowPayloadDto payload;

    private String question;
    private Integer index;
    @Type(type = "jsonb")
    private ValidationDto validation;
}

