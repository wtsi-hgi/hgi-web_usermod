# User module schema

# --- !Ups

CREATE SEQUENCE parametertype_id_seq;
CREATE TABLE parameter_type (
	id integer NOT NULL DEFAULT nextval('parameter_id_seq'),
	name varchar(255) NOT NULL,
	description varchar(255)
);

CREATE SEQUENCE roletype_id_seq;
CREATE TABLE role_type (
	id integer NOT NULL DEFAULT nextval('roletype_id_seq'),
	name varchar(255) NOT NULL,
  description varchar(255)
);

CREATE TABLE role_type_parameter_type (
  role_type_id integer NOT NULL,
  parameter_type_id integer NOT NULL
);

CREATE SEQUENCE parameter_id_seq;
CREATE TABLE parameter (
  id integer NOT NULL DEFAULT nextval('parameter_id_seq'),
  type_id integer NOT NULL,
  role_id integer NOT NULL,
  value varchar(255)
);

CREATE SEQUENCE role_id_seq;
CREATE TABLE role (
  id integer NOT NULL DEFAULT nextval('role_id_seq'),
  type_id integer NOT NULL
);

CREATE TABLE user (
  id varchar(255) NOT NULL
);

CREATE TABLE user_role (
  user_id integer NOT NULL,
  role_id integer NOT NULL
);

# --- !Downs
DROP TABLE user_role;
DROP TABLE user;
 
DROP TABLE parameter;
DROP TABLE role;

DROP TABLE role_type_parameter_type;
DROP TABLE parameter_type;
DROP TABLE role_type;

DROP SEQUENCE parameter_id_seq;
DROP SEQUENCE roletype_id_seq;