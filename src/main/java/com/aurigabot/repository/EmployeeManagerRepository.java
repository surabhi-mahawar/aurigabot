package com.aurigabot.repository;

import com.aurigabot.entity.EmployeeManager;
import com.aurigabot.entity.User;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface EmployeeManagerRepository extends R2dbcRepository<EmployeeManager,Long> {

//    @Query("select employee_manager.*, " +
//            "            users.id as u_id, users.name as u_name, users.username as u_username, users.email as u_email, users.password as u_password, users.mobile as u_mobile,\n" +
//            "            users.employee_id as u_employee_id,users.telegram_chat_id as u_telegram_chat_id,users.joined_on as u_joined_on,users.dob as u_dob, users.role as u_role,users1.*\n" +
//            "             from employee_manager left join users on users.id=employee_manager.manager_id\n" +"left join users as users1 on users1.id=employee_manager.employee_id as employee_details"  +
//            "            where employee_manager.employee_id = :user")
    @Query("select employee_manager.manager_id,employee_manager.employee_id,\n" +
            "                     users.id as u_id, users.name as u_name, users.username as u_username, users.email as u_email, users.password as u_password, users.mobile as u_mobile,\n" +
            "                 users.employee_id as u_employee_id,users.telegram_chat_id as u_telegram_chat_id,users.joined_on as u_joined_on,users.dob as u_dob, users.role as u_role,\n" +
            "                  users1.* from employee_manager left join users on users.id=employee_manager.manager_id left join users as users1 on users1.id=employee_manager.employee_id \n" +
            "                    where employee_manager.employee_id = :userId;")
    Flux<EmployeeManager> findByEmployee(UUID userId);
    @Query("select employee_manager.manager_id,employee_manager.employee_id,\n" +
            "                     users.id as u_id, users.name as u_name, users.username as u_username, users.email as u_email, users.password as u_password, users.mobile as u_mobile,\n" +
            "                 users.employee_id as u_employee_id,users.telegram_chat_id as u_telegram_chat_id,users.joined_on as u_joined_on,users.dob as u_dob, users.role as u_role,\n" +
            "                  users1.* from employee_manager left join users on users.id=employee_manager.manager_id left join users as users1 on users1.id=employee_manager.employee_id \n" +
            "                    where employee_manager.manager_id = :userId;")
    Flux<EmployeeManager> findByManager(UUID userId);
}
