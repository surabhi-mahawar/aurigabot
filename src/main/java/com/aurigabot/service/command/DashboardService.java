package com.aurigabot.service.command;

import com.aurigabot.entity.LeaveRequest;
import com.aurigabot.entity.User;
import com.aurigabot.repository.LeaveRequestRepository;
import com.aurigabot.repository.UserRepository;
import com.aurigabot.dto.UserMessageDto;
import lombok.Builder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Function;

@Service
public class DashboardService {
    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BirthdayService birthdayService;

    /**
     * Process /dashboard command - send list of applied leaves, birthdays etc
     * @param userMessageDto
     * @param commandType
     * @param index
     * @param user
     * @return
     */
    public Mono<UserMessageDto> processDashboardRequest(UserMessageDto userMessageDto, String commandType, int index, User user) {
        return leaveRequestRepository.findByEmployeeId(userMessageDto.getToUserId()).collectList().map(new Function<List<LeaveRequest>, Mono<UserMessageDto>>() {
            //todo change text of the greeting message
            String message="Hey! "+ user.getName() +"\n\n";
            @Override
            public Mono<UserMessageDto> apply(List<LeaveRequest> leaveRequests) {
                if(leaveRequests.size()==0){
                    message += "There are no pending or approved leaves.\n";
                } else {
                    if(leaveRequests.size() > 0){
                        message += "List of leaves: \n";
                    }
                    for(int i=0;i<leaveRequests.size();i++){
                        message += "From: "+leaveRequests.get(i).getFromDate().toString()
                                +", to: "+leaveRequests.get(i).getFromDate().toString()
                                +", status: "+leaveRequests.get(i).getStatus()+"\n";
                    }
                }
                userMessageDto.setMessage(message);
                return birthdayService.processBirthdayRequest(userMessageDto,"/birthday",0);
            }
        }).flatMap(new Function<Mono<UserMessageDto>, Mono<? extends UserMessageDto>>() {
            @Override
            public Mono<? extends UserMessageDto> apply(Mono<UserMessageDto> userMessageDtoMono) {
                return userMessageDtoMono;
            }
        });
    }
}
