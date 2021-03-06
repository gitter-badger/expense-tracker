--
-- CREATE TABLES
--
create table et_audit (id bigint GENERATED BY DEFAULT AS IDENTITY NOT NULL PRIMARY KEY,
createdby varchar(50), 
createddate timestamp, 
recorddata varchar(512));
-- Groups
-- 
create table et_groups (id bigint GENERATED BY DEFAULT AS IDENTITY NOT NULL PRIMARY KEY,
groupname varchar(50) NOT NULL UNIQUE, 
groupdescription varchar(100),
active smallint NOT NULL,
creationdate timestamp,
lastupdateddate timestamp);
--
-- Users
create table users (id bigint GENERATED BY DEFAULT AS IDENTITY NOT NULL PRIMARY KEY,
username varchar(50) NOT NULL UNIQUE, 
password varchar(50) NOT NULL, 
firstname varchar(16),
lastname varchar(16),
middleinit varchar(1),
emailid varchar(32),
createddate timestamp, 
lastupdateddate timestamp, 
lastmodifiedby varchar(50),
createdby varchar(50),
pwdchangeneeded smallint NOT NULL,
creditcardnumber varchar(16),
phonenumber varchar(32),
enabled smallint NOT NULL);
--
-- Group User join table
create table et_group_user (
group_id bigint NOT NULL,
user_id bigint NOT NULL,
constraint fk_group_user_group foreign key(group_id) references et_groups(id),
constraint fk_group_user_user foreign key(user_id) references users(id));
--
-- Roles
create table roles (id bigint GENERATED BY DEFAULT AS IDENTITY NOT NULL PRIMARY KEY,
role varchar(50) NOT NULL UNIQUE);
--
-- authorities
create table authorities (id bigint GENERATED BY DEFAULT AS IDENTITY NOT NULL PRIMARY KEY,
username varchar(50) NOT NULL,
authority varchar(50) NOT NULL,
grantedby varchar(50),
user_id bigint NOT NULL,  
constraint fk_authorities_roles foreign key(authority) references roles(role),
constraint fk_authorities_grantedby foreign key(grantedby) references users(username),
constraint fk_authorities_users_id foreign key(user_id) references users(id),
constraint fk_authorities_users_name foreign key(username) references users(username));
--
--
--
-- Settlement table
create table et_settlement (id bigint GENERATED BY DEFAULT AS IDENTITY (START WITH 1000, INCREMENT BY 1) NOT NULL PRIMARY KEY, 
cyclestartdate timestamp NOT NULL,
cycleenddate timestamp NOT NULL,
createddate timestamp, 
closeddate timestamp, 
createdby varchar(50),
accountmanager varchar(50),
volume float NOT NULL,
settlementcompleted smallint NOT NULL,
constraint fk_settlement_accountmanager foreign key(accountmanager) references users(username),
constraint fk_settlement_createdby foreign key(createdby) references users(username));
--
-- Reports Table
create table et_reports (id bigint GENERATED BY DEFAULT AS IDENTITY (START WITH 1000, INCREMENT BY 1) NOT NULL PRIMARY KEY, 
createddate timestamp, 
reportcontent blob(2M),
reporttype varchar(16) NOT NULL,
reportname varchar(32) NOT NULL,
settlementid bigint NOT NULL,
constraint fk_reports_settlementid foreign key(settlementid) references et_settlement(id));
--
-- user settlement
create table et_user_settlement (id bigint GENERATED BY DEFAULT AS IDENTITY (START WITH 1000, INCREMENT BY 1) NOT NULL PRIMARY KEY, 
setteleddate timestamp, 
username varchar(50) NOT NULL,
settlement_id bigint NOT NULL,
amount float NOT NULL,
usershare float NOT NULL,
userpaid float NOT NULL,
settlementcompleted smallint NOT NULL,
constraint fk_u_settlement_user foreign key(username) references users(username),
constraint fk_u_settlement_settlement foreign key(settlement_id) references et_settlement(id));
--
-- expense
create table et_expense (id bigint GENERATED BY DEFAULT AS IDENTITY NOT NULL PRIMARY KEY, 
paidby varchar(50) NOT NULL, 
settlement_id bigint,
amount float, 
date timestamp, 
createdby varchar(50),
lastmodifiedby varchar(50),
createddate timestamp, 
lastmodifieddate timestamp, 
description varchar(64), 
category varchar(32),
constraint fk_expense_settlement foreign key(settlement_id) references et_settlement(id),
constraint fk_exp_createdby_users foreign key(createdby) references users(username),
constraint fk_exp_lastmodifiedby_users foreign key(lastmodifiedby) references users(username),
constraint fk_expense_users foreign key(paidby) references users(username));
--
-- user_expense
create table et_user_expense(id bigint GENERATED BY DEFAULT AS IDENTITY NOT NULL PRIMARY KEY, 
username varchar(50) NOT NULL,
discountpercent float NOT NULL,
amount float NOT NULL,
expense_id bigint NOT NULL, 
constraint fk_u_expense_users foreign key(username) references users(username), 
constraint fk_u_expense_expense foreign key(expense_id) references et_expense(id));
--
--
-- Payments Table
create table et_payments(id bigint GENERATED BY DEFAULT AS IDENTITY NOT NULL PRIMARY KEY, 
paymentdate timestamp NOT NULL,
amount float NOT NULL,
username varchar(50) NOT NULL,
constraint fk_payments_users foreign key(username) references users(username));
