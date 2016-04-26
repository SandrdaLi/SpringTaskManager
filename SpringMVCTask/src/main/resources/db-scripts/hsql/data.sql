
-- User Table

INSERT INTO USER(NAME, USER_NAME, PASSWORD, DOB) VALUES('John Smith', 'johnsmith', 'password123', to_date('08/02/1977', 'MM/DD/YYYY'));
/
INSERT INTO USER(NAME, USER_NAME, PASSWORD, DOB) VALUES('Alice Dupont', 'aliced', 'password345', to_date('01/01/1975', 'MM/DD/YYYY'));
/
-- Task Table

 INSERT INTO TASK (NAME, STATUS, PRIORITY, CREATED_USER_ID, CREATED_DATE, ASSIGNEE_USER_ID, COMPLETED_DATE)
 VALUES ('Project Planning','Closed',1,0,to_date('04/01/2016', 'MM/DD/YYYY'),1,to_date('04/20/2016', 'MM/DD/YYYY'));
 INSERT INTO TASK (NAME, STATUS, PRIORITY, CREATED_USER_ID, CREATED_DATE, ASSIGNEE_USER_ID, COMPLETED_DATE)
 VALUES ('Project Testing','Open',1,0,to_date('04/01/2016', 'MM/DD/YYYY'),1,to_date('04/30/2016', 'MM/DD/YYYY'));
 INSERT INTO TASK (NAME, STATUS, PRIORITY, CREATED_USER_ID, CREATED_DATE, ASSIGNEE_USER_ID, COMPLETED_DATE)
 VALUES ('Project Delivery','Open',1,0,to_date('04/01/2016', 'MM/DD/YYYY'),1,to_date('05/30/2016', 'MM/DD/YYYY'));

 --alter table TASK add constraint FK_TASK_CREATOR foreign key (CREATED_USER_ID) references USER(ID);
 --alter table TASK add constraint FK_TASK_ASSIGNEE foreign key (ASSIGNEE_USER_ID) references USER(ID);

