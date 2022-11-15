insert into users (id, name, mobile, email, username, employee_id, dob, password, role) values ('89326ca8-f4cf-4756-b180-8636824345bd', 'Superadmin', '9876543210', 'superadmin@aurigait.com', 'superadmin', '2bae1a9d-ab78-4de7-9230-82e4e448721d', '2010-01-12', 'password', 'ADMIN');

insert into users (name, mobile, email, username, employee_id, dob, password, role) values ('surabhi', '7597185708', 'surabhi.mahawar@aurigait.com', 'surabhi', '69609f6f-9ce8-418d-97e1-41f2a4621a70', '1994-06-28', 'password', 'EMPLOYEE');

insert into flow (command_type, question, index, payload) values ('/leave', 'Please enter the reason for leave', 0, '{"question": "Please enter the reason for leave", "questionType": "TEXT", "choices": null, "media_url": null}');

insert into flow (command_type, question, index, payload) values ('/leave', 'Please enter the from date', 1, '{"question": "Please enter the from date", "questionType": "TEXT", "choices": null, "media_url": null}');

insert into flow (command_type, question, index, payload) values ('/leave', 'Please enter the to date', 2, '{"question": "Please enter the to date", "questionType": "TEXT", "choices": null, "media_url": null}');

insert into flow (command_type, question, index, payload) values ('/leave', 'Please enter the leave type', 3, '{"question": "Please enter the leave type", "questionType": "TEXT", "choices": [{"key": "CL", "text": "CL"}, {"key": "PL", "text": "PL"}], "media_url": null}');

insert into flow (command_type, question, index, payload) values ('/regTelegramUser', 'Please enter your email id', 0, '{"question": "Please enter your email id", "questionType": "TEXT", "choices": null, "media_url": null}');