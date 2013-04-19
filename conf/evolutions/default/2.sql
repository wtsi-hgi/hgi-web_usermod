# --- !Ups

insert into user (id) values ('nc');
insert into role_type (id, name, description) values (0, 'CREATE_PROJECT', 'Create a new project.');
insert into role_type (id, name, description) values (1, 'SET_GLOBAL_ROLE', 'Ability to set all roles.');

insert into role_type (id, name, description) values (10, 'DELEGATE', 'Delegate any roles possessed in this project.');
insert into parameter_type (id, name, description) values (10, 'PROJECT', 'Project authority is granted for.');
insert into role_type_parameter_type (role_type_id, parameter_type_id) values (10,10);

# --- !Downs

delete from user;
delete from parameter_type;
delete from role_type;