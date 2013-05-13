
# --- !Ups

create table `PARAMETER_TYPES` (`id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,`name` VARCHAR(254) NOT NULL,`description` VARCHAR(254));
create table `PARAMETERS` (`id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,`role_id` BIGINT NOT NULL,`parameter_type_id` BIGINT NOT NULL,`value` VARCHAR(254) NOT NULL);
create table `ROLE_TYPE_PARAMETER_TYPE` (`role_type_id` BIGINT NOT NULL,`parameter_type_id` BIGINT NOT NULL);
create table `ROLE_TYPES` (`id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,`name` VARCHAR(254) NOT NULL,`description` VARCHAR(254));
create index `idx_name` on `ROLE_TYPES` (`name`);
create table `ROLES` (`id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,`role_type_id` BIGINT NOT NULL);
create table `USER_ROLES` (`user_id` BIGINT NOT NULL,`role_id` BIGINT NOT NULL);
create table `USERS` (`id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,`sid` VARCHAR(254) NOT NULL);
alter table `PARAMETERS` add constraint `p_role_fk` foreign key(`role_id`) references `ROLES`(`id`) on update NO ACTION on delete NO ACTION;
alter table `PARAMETERS` add constraint `p_parameter_type_fk` foreign key(`parameter_type_id`) references `PARAMETER_TYPES`(`id`) on update NO ACTION on delete NO ACTION;
alter table `ROLE_TYPE_PARAMETER_TYPE` add constraint `rtpt_role_type_fk` foreign key(`role_type_id`) references `ROLE_TYPES`(`id`) on update NO ACTION on delete NO ACTION;
alter table `ROLE_TYPE_PARAMETER_TYPE` add constraint `rtpt_parameter_type_fk` foreign key(`parameter_type_id`) references `PARAMETER_TYPES`(`id`) on update NO ACTION on delete NO ACTION;
alter table `ROLES` add constraint `r_role_type_fk` foreign key(`role_type_id`) references `ROLE_TYPES`(`id`) on update NO ACTION on delete NO ACTION;
alter table `USER_ROLES` add constraint `ur_user_fk` foreign key(`user_id`) references `USERS`(`id`) on update NO ACTION on delete NO ACTION;
alter table `USER_ROLES` add constraint `ur_role_fk` foreign key(`role_id`) references `ROLES`(`id`) on update NO ACTION on delete NO ACTION;

# --- !Downs

ALTER TABLE PARAMETERS DROP FOREIGN KEY p_role_fk;
ALTER TABLE PARAMETERS DROP FOREIGN KEY p_parameter_type_fk;
ALTER TABLE ROLE_TYPE_PARAMETER_TYPE DROP FOREIGN KEY rtpt_role_type_fk;
ALTER TABLE ROLE_TYPE_PARAMETER_TYPE DROP FOREIGN KEY rtpt_parameter_type_fk;
ALTER TABLE ROLES DROP FOREIGN KEY r_role_type_fk;
ALTER TABLE USER_ROLES DROP FOREIGN KEY ur_user_fk;
ALTER TABLE USER_ROLES DROP FOREIGN KEY ur_role_fk;
drop table `PARAMETER_TYPES`;
drop table `PARAMETERS`;
drop table `ROLE_TYPE_PARAMETER_TYPE`;
drop table `ROLE_TYPES`;
drop table `ROLES`;
drop table `USER_ROLES`;
drop table `USERS`;

