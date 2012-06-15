# JobQueue

# --- !Ups
 
CREATE TABLE JOB_QUEUE (
  jrid UUID NOT NULL,
  batch_id varchar(255) NOT NULL,
  flow_id varchar(255) NOT NULL,
  execution_id varchar(255) NOT NULL,
  phase_id varchar(255) NOT NULL,
  stage_id varchar(255) NOT NULL,
  main_class varchar(255) NOT NULL,
  status varchar(255) NOT NULL,
  exit_code int,
  PRIMARY KEY (jrid)
);

CREATE TABLE JOB_ARGUMENTS (
  jrid UUID NOT NULL,
  name varchar(255) NOT NULL,
  value varchar(255) NOT NULL,
  PRIMARY KEY (jrid, name)
);

CREATE TABLE JOB_PROPERTIES (
  jrid UUID NOT NULL,
  name varchar(255) NOT NULL,
  value varchar(255) NOT NULL,
  PRIMARY KEY (jrid, name)
);

CREATE TABLE JOB_ENV (
  jrid UUID NOT NULL,
  name varchar(255) NOT NULL,
  value varchar(255) NOT NULL,
  PRIMARY KEY (jrid, name)
);

# --- !Downs

DROP TABLE JOB_QUEUE;
DROP TABLE JOB_QUEUE_ARGUMENTS;
DROP TABLE JOB_QUEUE_PROPERTIES;
DROP TABLE JOB_QUEUE_ENV;
