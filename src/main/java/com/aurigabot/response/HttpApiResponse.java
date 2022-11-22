package com.aurigabot.response;

import com.aurigabot.utils.DateUtil;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class HttpApiResponse {
    @Builder.Default
    public String timestamp = DateUtil.convertLocalDateTimeToFormat(LocalDateTime.now());
    public Integer status;
    public String error;
    public String message;
    public String path;
    public Object result;
}
