package com.aurigabot.service;

import com.aurigabot.dto.DateValidationDto;
import com.aurigabot.dto.TextValidationDto;
import com.aurigabot.dto.ValidationDto;
import com.aurigabot.entity.LeaveRequest;
import com.aurigabot.enums.FieldType;
import com.aurigabot.utils.DateUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

@Service
public class ValidationService {

public Pair<String,Object> fieldValidator(ValidationDto validationDto, String msg){
    if(validationDto.getFieldType().equals(FieldType.TEXT)){
        return textValidator(validationDto.getTextValidationConfig(),msg);
    } else if (validationDto.getFieldType()==FieldType.DATE) {
        return dateValidator(validationDto.getDateValidationConfig(),msg);
    }
    else {
        return integerValidator(validationDto,msg);
    }

}

    public Pair<String,Object> textValidator(TextValidationDto textValidationDto, String msg){
        String result = "";
        if (textValidationDto.getMin()==null && textValidationDto.getMax()==null){
            result="pass";
            return Pair.of(result,msg);
        } else if (textValidationDto.getMin()==null) {
            if ( msg.length() > textValidationDto.getMax()) {
                result = String.format("Try again !! \nSize must be less than %s", textValidationDto.getMax());
                return Pair.of(result,msg);
            }
        }
        else if (textValidationDto.getMax()==null) {
            if ( msg.length() < textValidationDto.getMin()) {
                result = String.format("Try again !! \nSize must be greater than %s", textValidationDto.getMin());
                return Pair.of(result,msg);
            }
        }
        else{
            if (msg.length()<textValidationDto.getMin() || msg.length()> textValidationDto.getMax()) {
                result = String.format("Try again !! \nSize must be between %s and %s", textValidationDto.getMin(), textValidationDto.getMax());
                return Pair.of(result, msg);
            }
        }
//        if (msg.length() < textValidationDto.getMin() || msg.length() > textValidationDto.getMax()) {
//
//            }
        if (textValidationDto.getRegex()!=null){
            Pattern p = Pattern.compile(textValidationDto.getRegex());
            boolean str = p.matcher(msg).matches();
            if (!str){
                result="Try again !! \nMessage can only contain letters or numbers";
                return Pair.of(result,msg);
            }
        }
        result="pass";
        return Pair.of(result,msg);

    }
    public Pair<String,Object> dateValidator(DateValidationDto validationDto, String msg){
        Pair<String,Object> result = null;

        DateTimeFormatter formatter =DateTimeFormatter.ofPattern(DateUtil.getGlobalDateFormat());
        if(validationDto.getFormat()!=null){
             formatter =DateTimeFormatter.ofPattern(validationDto.getFormat());
        }
        try {
            LocalDate date = LocalDate.parse(msg, formatter);
            if (validationDto.getGte()==null&& validationDto.getLte()==null){
                result=Pair.of("pass",date);
                return result;
            }
            if (validationDto.getGte().equals("now")){
                if(date.compareTo(LocalDate.now()) < 0) {
                result = Pair.of("Try again !! \nEntered date should be greater than or equal to current date.",false);
                return result;
            }}
            if (validationDto.getLte().equals("now")){
                if(date.compareTo(LocalDate.now()) > 0) {
                    result = Pair.of("Try again !! \nEntered date should be before or equal to current date.",false);
                    return result;
                }
                LocalDate dateLt = LocalDate.parse(validationDto.getLte(), formatter);
            if (dateLt.compareTo(dateLt)>0){
                result = Pair.of("Try again !! \nEntered date should be before or equal to"+dateLt,false);
                return result;
            }
                LocalDate dateGt = LocalDate.parse(validationDto.getGte(), formatter);
                if (dateLt.compareTo(dateGt)<0){
                    result = Pair.of("Try again !! \nEntered date should be before or equal to"+dateGt,false);
                    return result;
                }
            }



//            if(date.compareTo(LocalDate.now()) < 0) {
//                result = "Try again !! \nEntered date should be greater than or equal to current date.";
//            } else {
//                if (index == 1) {
//                    leaveRequest.setFromDate(date);
//                    result="pass";
//                } else {
//                    leaveRequest.setToDate(date);
//                    if(leaveRequest.getToDate().compareTo(leaveRequest.getFromDate()) < 0) {
//                        result = "Try again !! \nTo date should be greater than or equal to from date.";
//                    }
//                    else {
//                        result="pass";
//                    }
//                }
//            }
        } catch (DateTimeParseException e){
            result = Pair.of("please enter the date in proper format i.e "+ validationDto.getFormat() +" eg. 11-02-2000",false);
        }
        
        return result;
    }
    public Pair<String,Object> integerValidator(ValidationDto validationDto,String msg){
        return Pair.of("",null);
    }
}
