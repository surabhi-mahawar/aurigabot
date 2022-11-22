package com.aurigabot.controller.inbound;

import com.aurigabot.adapters.AbstractAdapter;
import com.aurigabot.adapters.WebPortalAdapter;
import com.aurigabot.model.webPortal.InboundMessage;
import com.aurigabot.repository.FlowRepository;
import com.aurigabot.repository.LeaveRequestRepository;
import com.aurigabot.repository.UserMessageRepository;
import com.aurigabot.repository.UserRepository;
import com.aurigabot.response.HttpApiResponse;
import com.aurigabot.service.message.InboundMessageService;
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

    @Autowired
    private FlowRepository flowRepository;

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

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
                .flowRepository(flowRepository)
                .userMessageRepository(userMessageRepository)
                .leaveRequestRepository(leaveRequestRepository)
                .build();

        HttpApiResponse response = HttpApiResponse.builder()
                .status(HttpStatus.OK.value())
                .path("/inbound/webMessage")
                .build();

        return inboundMessageService.processInboundMessage(response, message);
    }
}
