package com.dynamos.aurigabot.controller.inbound;

import com.dynamos.aurigabot.adapters.AbstractAdapter;
import com.dynamos.aurigabot.adapters.WebPortalAdapter;
import com.dynamos.aurigabot.model.webPortal.InboundMessage;
import com.dynamos.aurigabot.repository.UserMessageRepository;
import com.dynamos.aurigabot.repository.UserRepository;
import com.dynamos.aurigabot.response.HttpApiResponse;
import com.dynamos.aurigabot.service.InboundMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(value = "/inbound")
public class WebPortalController {
    @Autowired
    public UserRepository userRepository;

    @Autowired
    public UserMessageRepository userMessageRepository;

    @Value("${web.portal.url}")
    public String outboundUrl;

    /**
     * Receives inbound message from web channel and process it.
     * @param message
     */
    @PostMapping(value = "/webMessage", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<HttpApiResponse> webMessage(@RequestBody InboundMessage message) {
        AbstractAdapter adapter = new WebPortalAdapter(outboundUrl);
        InboundMessageService inboundMessageService = InboundMessageService.builder()
                .adapter(adapter)
                .userRepository(userRepository)
                .userMessageRepository(userMessageRepository)
                .build();

        HttpApiResponse response = HttpApiResponse.builder()
                .status(HttpStatus.OK.value())
                .path("/inbound/webMessage")
                .build();

        return inboundMessageService.processInboundMessage(response, message);
    }
}
