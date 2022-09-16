package com.dynamos.aurigabot.model.webPortal;

import com.dynamos.aurigabot.dto.MessagePayloadChoiceDto;
import lombok.*;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@XmlRootElement
@Builder
public class OutbondMessagePayload {
    private String title;
    private String msg_type;
    private ArrayList<MessagePayloadChoiceDto> choices;
}
