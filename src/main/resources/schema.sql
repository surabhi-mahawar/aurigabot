CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS users (
    id uuid DEFAULT uuid_generate_v4 () PRIMARY KEY,
    name varchar(100) NOT NULL,
    mobile varchar(10) NULL,
    email VARCHAR(255) NOT NULL,
    username varchar(100) NOT NULL,
    employee_id uuid NOT NULL UNIQUE,
    joined_on timestamp without time zone,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,


);

CREATE TABLE IF NOT EXISTS userMessage (
    id uuid DEFAULT uuid_generate_v4 (),

    command_id uuid FOREIGN KEY REFERENCES commands(id),
    flow_id uuid FOREIGN KEY REFERENCES flow(id),

    index int NOT NULL,
    from_user_id uuid FOREIGN KEY REFERENCES users(id),
    from_source text NOT NULL,
    to_user_id uuid FOREIGN KEY REFERENCES users(id),
    to_source text NOT NULL,

    channel varchar(50) NOT NULL,
    provider varchar(50) NOT NULL,
    message text NOT NULL,
    status varchar(20) NOT NULL,
    received_at timestamp without time zone,
    sent_at timestamp without time zone,
    delivered_at timestamp without time zone,
    read_at timestamp without time zone





);
CREATE TABLE IF NOT EXISTS commands (
    id uuid DEFAULT uuid_generate_v4 () PRIMARY KEY,
    command_type VARCHAR(255) NOT NULL,
    description text NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,


);

CREATE TABLE IF NOT EXISTS flow(
    id uuid DEFAULT uuid_generate_v4 () PRIMARY KEY,
    command_id uuid FOREIGN KEY REFERENCES commands(id),
    question text NOT NULL,
    index integer NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP

);

CREATE TABLE IF NOT EXISTS leave_request (
    id uuid DEFAULT uuid_generate_v4 () PRIMARY KEY,
    employee_id uuid FOREIGN KEY REFERENCES users(id),
    reason text NOT NULL,
    from_date DATE NOT NULL,
    to_date DATE NOT NULL,
    status varchar(20)  NOT NULL,
    approved_by uuid FOREIGN KEY REFERENCES users(id),
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE IF NOT EXISTS leave_balance (
    id uuid DEFAULT uuid_generate_v4 () PRIMARY KEY,
    employee_id uuid FOREIGN KEY REFERENCES users(employee_id),
    cl integer DEFAULT 0,
    pl integer DEFAULT 0,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS employee_manager(
    id uuid DEFAULT uuid_generate_v4 () PRIMARY KEY ,
    employee_id uuid FOREIGN KEY REFERENCES users(id),
    manager_id uuid FOREIGN KEY REFERENCES users(id)
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);
