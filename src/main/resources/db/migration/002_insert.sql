insert into users (id, name, mobile, email, username, employee_id, dob, password, role) values ('89326ca8-f4cf-4756-b180-8636824345bd', 'Superadmin', '9876543210', 'superadmin@gmail.com', 'superadmin', '2bae1a9d-ab78-4de7-9230-82e4e448721d', '2010-01-12', 'password', 'ADMIN');

insert into users (id,name, mobile, email, username, employee_id, dob, password, role) values ('bdd45de0-67f2-4c23-ab79-41c17bacf1a5','employee1', '9087654329', 'employee1@gmail.com', 'employee1', '69609f6f-9ce8-418d-97e1-41f2a4621a70', '1994-06-28', 'password', 'EMPLOYEE');

insert into users (id,name, mobile, email, username, employee_id, dob, password, role) values ('c8d62f18-6e9c-44bb-a08c-f04710f728f1','employee2', '9087654328', 'employee2@gmail.com', 'employee2', '6ed4a884-cbe2-4ec9-a31e-f9b59a9777c3', '1998-01-10', 'password', 'EMPLOYEE');

insert into employee_manager (id,employee_id,manager_id) values ('b5a488a3-3e07-49bc-aeda-7fc182076220','bdd45de0-67f2-4c23-ab79-41c17bacf1a5','c8d62f18-6e9c-44bb-a08c-f04710f728f1');

insert into flow (command_type, question, index, payload, validation) values ('/leaverequest', 'Please enter the reason for leave', 0, '{"question": "Please enter the reason for leave", "questionType": "TEXT", "choices": null, "mediaUrl": null}', '{ "fieldType": "TEXT", "textValidationConfig": { "max": "100", "min": "10", "regex": "^[a-zA-Z0-9! ]*$" }}');

insert into flow (command_type, question, index, payload, validation) values ('/leaverequest', 'Please enter the from date', 1, '{"question": "Please enter the from date", "questionType": "TEXT", "choices": null, "mediaUrl": null}', '{"fieldType": "DATE", "dateValidationConfig": { "format": "dd-MM-yyyy" }}');

insert into flow (command_type, question, index, payload, validation) values ('/leaverequest', 'Please enter the to date', 2, '{"question": "Please enter the to date", "questionType": "TEXT", "choices": null, "mediaUrl": null}', '{"fieldType": "DATE", "dateValidationConfig": {  "format": "dd-MM-yyyy" }}');

insert into flow (command_type, question, index, payload, validation) values ('/leaverequest', 'Please enter the leave type', 3, '{"question": "Please enter the leave type", "questionType": "TEXT", "choices": [{"key": "CL", "text": "CL"}, {"key": "PL", "text": "PL"}], "mediaUrl": null}', '{ "fieldType": "TEXT", "textValidationConfig": { "length": "10" }}');

insert into flow (command_type, question, index, payload, validation) values ('/regTelegramUser', 'We do not recognize you, Please enter your email id to confirm your identity.', 0, '{"question": "Please enter your email id", "questionType": "TEXT", "choices": null, "mediaUrl": null}', '{ "fieldType": "TEXT", "textValidationConfig": { "regex": "[A-Z0-9]", "length": "10" }}');

insert into flow (command_type, question, index, payload, validation) values ('/birthday', 'Please enter the date you want to see birthdays of.', 0, '{"question": "Please enter the date you want to see birthdays of.", "questionType": "TEXT", "choices": null, "mediaUrl": null}', '{ "fieldType": "DATE", "dateValidationConfig": { "format": "dd-MM-yyyy" }}');

insert into flow (command_type, question, index, payload, validation) values ('/events', 'Please choose any of the operations', 0, '{"question": "Please choose any of the operations", "questionType": "TEXT", "choices": null, "mediaUrl": null}', '{ "fieldType": "TEXT", "textValidationConfig": { "max": "10"}}');

insert into flow (command_type, question, index, payload, validation) values ('/listevents', 'Please enter start date for events', 0, '{"question": "Please enter start date for events", "questionType": "DATE", "choices": null, "mediaUrl": null}', '{ "fieldType": "DATE", "dateValidationConfig": { "format": "dd-MM-yyyy" }}');
insert into flow (command_type, question, index, payload, validation) values ('/listevents', 'Please enter end date for events', 1, '{"question": "Please enter end date for events", "questionType": "DATE", "choices": null, "mediaUrl": null}', '{ "fieldType": "DATE", "dateValidationConfig": { "format": "dd-MM-yyyy" }}');

insert into flow (command_type, question, index, payload, validation) values ('/createevents', 'Please enter summary for new event', 0, '{"question": "Please enter summary for new event", "questionType": "TEXT", "choices": null, "mediaUrl": null}', '{ "fieldType": "TEXT", "textValidationConfig": { "max": "100", "min": "10" }}');
insert into flow (command_type, question, index, payload, validation) values ('/createevents', 'Please enter location for new event', 1, '{"question": "Please enter location for new event", "questionType": "TEXT", "choices": null, "mediaUrl": null}', '{ "fieldType": "TEXT", "textValidationConfig": { "max": "50", "min": "10" }}');
insert into flow (command_type, question, index, payload, validation) values ('/createevents', 'Please enter description for new event', 2, '{"question": "Please enter description for new event", "questionType": "TEXT", "choices": null, "mediaUrl": null}', '{ "fieldType": "TEXT", "textValidationConfig": { "min": "10" }}');
insert into flow (command_type, question, index, payload, validation) values ('/createevents', 'Please enter start date for new event', 3, '{"question": "Please enter start date for new event", "questionType": "DATE", "choices": null, "mediaUrl": null}', '{ "fieldType": "DATE", "dateValidationConfig": { "format": "dd-MM-yyyy" }}');
insert into flow (command_type, question, index, payload, validation) values ('/createevents', 'Please enter end date for new event', 4, '{"question": "Please enter end date for new event", "questionType": "DATE", "choices": null, "mediaUrl": null}', '{ "fieldType": "DATE", "dateValidationConfig": { "format": "dd-MM-yyyy" }}');
insert into flow (command_type, question, index, payload, validation) values ('/createevents', 'Please enter timezone for new event', 5, '{"question": "Please enter timezone for new event", "questionType": "TEXT", "choices": null, "mediaUrl": null}', '{ "fieldType": "TEXT", "textValidationConfig": { "min": "5" }}');


