package com.aurigabot.entity.converters;

import com.aurigabot.entity.LeaveRequest;
import com.aurigabot.enums.LeaveStatus;
import com.aurigabot.enums.LeaveType;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.r2dbc.mapping.OutboundRow;
import org.springframework.r2dbc.core.Parameter;

import java.time.LocalDate;
import java.util.UUID;

/**
 * LeaveRequest entity write converter
 * To resolve issue of "nested entities not supported", R2dbc does not support the nested entities.
 */
@WritingConverter
public class LeaveRequestWriteConverter implements Converter<LeaveRequest, OutboundRow> {

    @Override
    public OutboundRow convert(LeaveRequest leaveRequest) {

        OutboundRow row = new OutboundRow();
        if (leaveRequest.getId()==null){
            row.put("id", Parameter.from(UUID.randomUUID()));
        }
        else {
            row.put("id",Parameter.from(leaveRequest.getId()));
        }
        if(leaveRequest.getEmployeeId() != null) {
            row.put("employee_id", Parameter.from(leaveRequest.getEmployeeId().getId()));
        }
//        row.remove("user");

//        LocalDateTime createdAt;
//        if(leaveRequest.getCreatedAt() != null) {
//            createdAt = userMessage.getCreatedAt();
//        } else {
//            createdAt = LocalDateTime.now();
//        }

        row.put("from_date",Parameter.fromOrEmpty(leaveRequest.getFromDate(), LocalDate.class));
        row.put("to_date",Parameter.fromOrEmpty(leaveRequest.getToDate(), LocalDate.class));
        row.put("reason", Parameter.fromOrEmpty(leaveRequest.getReason(), String.class));
        row.put("status", Parameter.fromOrEmpty(leaveRequest.getStatus(), LeaveStatus.class));
        row.put("leave_type", Parameter.fromOrEmpty(leaveRequest.getLeaveType(), LeaveType.class));
        row.put("approved_by", Parameter.fromOrEmpty(leaveRequest.getApprovedBy(),UUID.class));
        return row;
    }


}
