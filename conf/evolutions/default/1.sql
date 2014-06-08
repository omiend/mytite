# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table performance (
  id                        bigint auto_increment not null,
  festival_id               bigint,
  stage_id                  bigint,
  artist                    varchar(255),
  time                      varchar(255),
  time_frame                varchar(255),
  create_date               datetime not null,
  update_date               datetime not null,
  constraint pk_performance primary key (id))
;

create table stage (
  id                        bigint auto_increment not null,
  festival_id               bigint,
  stage_name                varchar(255),
  sort                      varchar(255),
  color                     varchar(255),
  create_date               datetime not null,
  update_date               datetime not null,
  constraint pk_stage primary key (id))
;

create table festival (
  id                        bigint auto_increment not null,
  festival_name             varchar(255),
  twitter_id                bigint,
  description               longtext,
  create_date               datetime not null,
  update_date               datetime not null,
  constraint pk_festival primary key (id))
;

create table twitter_user (
  id                          bigint auto_increment not null,
  twitter_id                  bigint UNIQUE,
  twitter_name                varchar(255),
  twitter_screen_name         varchar(255),
  twitter_profiel_image_url   varchar(255),
  twitter_description         text,
  twitter_access_token        varchar(255),
  twitter_access_token_secret varchar(255),
  create_date                 datetime not null,
  update_date                 datetime not null,
  constraint pk_twitter_user primary key (id))
;

create table heart (
  id                        bigint auto_increment not null,
  festival_id               bigint,
  twitter_id                bigint,
  create_date               datetime not null,
  update_date               datetime not null,
  constraint pk_heart primary key (id))
;

alter table performance add constraint fk_performance_timeTable_1 foreign key (festival_id) references festival (id) on delete restrict on update restrict;
create index ix_performance_timeTable_1 on performance (festival_id);
alter table performance add constraint fk_performance_stage_2 foreign key (stage_id) references stage (id) on delete restrict on update restrict;
create index ix_performance_stage_2 on performance (stage_id);
alter table stage add constraint fk_stage_festival_3 foreign key (festival_id) references festival (id) on delete restrict on update restrict;
create index ix_stage_festival_3 on stage (festival_id);
alter table festival add constraint fk_festival_twitterUser_4 foreign key (twitter_id) references twitter_user (twitter_id) on delete restrict on update restrict;
create index ix_festival_twitterUser_4 on festival (twitter_id);
alter table stage add constraint fk_heart_festival_5 foreign key (festival_id) references festival (id) on delete restrict on update restrict;
create index ix_heart_festival_5 on heart (festival_id);

# --- !Downs

SET FOREIGN_KEY_CHECKS=0;

drop table performance;

drop table stage;

drop table festival;

drop table twitter_user;

SET FOREIGN_KEY_CHECKS=1;
