package com.dynamos.aurigabot.response.telegram;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OutboundResponse {
    private Boolean ok;
    private Object result;
}
