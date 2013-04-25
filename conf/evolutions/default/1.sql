
# --- !Ups

create table "PARAMETER_TYPES" ("id" BIGINT GENERATED BY DEFAULT AS IDENTITY(START WITH 1) NOT NULL PRIMARY KEY,"name" VARCHAR NOT NULL,"description" VARCHAR);
create table "PARAMETERS" ("id" BIGINT NOT NULL PRIMARY KEY,"role_id" BIGINT NOT NULL,"parameter_type_id" BIGINT NOT NULL,"value" VARCHAR NOT NULL);
create table "ROLE_TYPE_PARAMETER_TYPE" ("role_type_id" BIGINT NOT NULL,"parameter_type_id" BIGINT NOT NULL);
create table "ROLE_TYPES" ("id" BIGINT GENERATED BY DEFAULT AS IDENTITY(START WITH 1) NOT NULL PRIMARY KEY,"name" VARCHAR NOT NULL,"description" VARCHAR);
create index "idx_name" on "ROLE_TYPES" ("name");
create table "ROLES" ("id" BIGINT GENERATED BY DEFAULT AS IDENTITY(START WITH 1) NOT NULL PRIMARY KEY,"role_type_id" BIGINT NOT NULL);
create table "USER_ROLES" ("user_id" BIGINT NOT NULL,"role_id" BIGINT NOT NULL);
create table "USERS" ("id" BIGINT NOT NULL PRIMARY KEY,"sid" VARCHAR NOT NULL);
alter table "PARAMETERS" add constraint "p_role_fk" foreign key("role_id") references "ROLES"("id") on update NO ACTION on delete NO ACTION;
alter table "PARAMETERS" add constraint "p_parameter_type_fk" foreign key("parameter_type_id") references "PARAMETER_TYPES"("id") on update NO ACTION on delete NO ACTION;
alter table "ROLE_TYPE_PARAMETER_TYPE" add constraint "rtpt_role_type_fk" foreign key("role_type_id") references "ROLE_TYPES"("id") on update NO ACTION on delete NO ACTION;
alter table "ROLE_TYPE_PARAMETER_TYPE" add constraint "rtpt_parameter_type_fk" foreign key("parameter_type_id") references "PARAMETER_TYPES"("id") on update NO ACTION on delete NO ACTION;
alter table "ROLES" add constraint "r_role_type_fk" foreign key("role_type_id") references "ROLE_TYPES"("id") on update NO ACTION on delete NO ACTION;
alter table "USER_ROLES" add constraint "ur_user_fk" foreign key("user_id") references "USERS"("id") on update NO ACTION on delete NO ACTION;
alter table "USER_ROLES" add constraint "ur_role_fk" foreign key("role_id") references "ROLES"("id") on update NO ACTION on delete NO ACTION;

# --- !Downs

alter table "PARAMETERS" drop constraint "p_role_fk";
alter table "PARAMETERS" drop constraint "p_parameter_type_fk";
alter table "ROLE_TYPE_PARAMETER_TYPE" drop constraint "rtpt_role_type_fk";
alter table "ROLE_TYPE_PARAMETER_TYPE" drop constraint "rtpt_parameter_type_fk";
alter table "ROLES" drop constraint "r_role_type_fk";
alter table "USER_ROLES" drop constraint "ur_user_fk";
alter table "USER_ROLES" drop constraint "ur_role_fk";
drop table "PARAMETER_TYPES";
drop table "PARAMETERS";
drop table "ROLE_TYPE_PARAMETER_TYPE";
drop table "ROLE_TYPES";
drop table "ROLES";
drop table "USER_ROLES";
drop table "USERS";

