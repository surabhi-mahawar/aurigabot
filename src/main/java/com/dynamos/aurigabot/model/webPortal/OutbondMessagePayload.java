package com.dynamos.aurigabot.model.webPortal;

import lombok.*;

import javax.xml.bind.annotation.XmlRootElement;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@XmlRootElement
@Builder
public class OutbondMessagePayload {
    private String title;
    private String msg_type;
}
