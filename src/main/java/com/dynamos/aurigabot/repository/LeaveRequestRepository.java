package com.dynamos.aurigabot.repository;

import com.dynamos.aurigabot.entity.LeaveRequest;
import com.dynamos.aurigabot.entity.UserMessage;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface LeaveRequestRepository extends R2dbcRepository<LeaveRequest, UUID> {
    @Query("select leave_request.*,\n" +
            "            users.id as u_id, users.name as u_name, users.username as u_username, users.email as u_email, users.password as u_password, users.mobile as u_mobile,\n" +
            "            users.employee_id as u_employee_id,users.telegram_chat_id as u_telegram_chat_id,users.joined_on as u_joined_on,users.dob as u_dob, users.role as u_role\n" +
            "             from leave_request left join users on users.id=leave_request.employee_id\n" +
            "            where users.id = :uuid")
    Flux<LeaveRequest> findByEmployeeId(UUID uuid);
}
