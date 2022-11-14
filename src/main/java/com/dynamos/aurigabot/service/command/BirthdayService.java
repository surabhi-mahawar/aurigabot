package com.dynamos.aurigabot.service.command;

import com.dynamos.aurigabot.dto.MessagePayloadDto;
import com.dynamos.aurigabot.dto.UserMessageDto;
import com.dynamos.aurigabot.entity.LeaveRequest;
import com.dynamos.aurigabot.entity.User;
import com.dynamos.aurigabot.enums.MessagePayloadType;
import com.dynamos.aurigabot.repository.LeaveRequestRepository;
import com.dynamos.aurigabot.repository.UserRepository;
import lombok.Builder;
import reactor.core.publisher.Mono;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.List;
import java.util.function.Function;

@Builder
public class BirthdayService {
    private UserRepository userRepository;

    /**
     * Process /birthday command and return list of birthdays for today
     * @param userMessageDto
     * @param commandType
     * @param index
     * @return
     */
    public Mono<UserMessageDto> processBirthdayRequest( UserMessageDto userMessageDto,String commandType, int index) {
        SimpleDateFormat mdyFormat = new SimpleDateFormat("yyyy-MM-dd");
        java.util.Date currentDate = java.util.Date.from(Instant.now());
        String dateStr = mdyFormat.format(currentDate);

        java.util.Date dob2 = null;
        try {
            dob2 = mdyFormat.parse(dateStr);

            return userRepository.findAllByDob(dob2).collectList().map(new Function<List<User>, UserMessageDto>() {
                @Override
                public UserMessageDto apply(List<User> users) {

                    String message = "";
                    if(userMessageDto.getMessage() != null) {
                        message = userMessageDto.getMessage()+"\n";
                    }

                    if (users.size() == 0){
                        message += "There are no birthdays today.";
                    } else {
                        message += "Please find the list of birthdays for today.\n";
                        for (User u : users){
                            message += "\n"+u.getName();
                        }
                    }

                    userMessageDto.setMessage(message);
                    MessagePayloadDto payload = MessagePayloadDto.builder()
                            .message(message)
                            .msgType(MessagePayloadType.TEXT)
                            .build();

                    userMessageDto.setPayload(payload);

                    return userMessageDto;
                }
            });
        } catch (ParseException e) {
//            throw new RuntimeException(e);
            return Mono.just(null);
        }
    }
}
