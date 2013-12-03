# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table account (
  account_id                bigint auto_increment not null,
  full_name                 varchar(255),
  token                     varchar(255),
  secret                    varchar(255),
  trends_band_id            integer,
  music_provider            varchar(255),
  constraint pk_account primary key (account_id))
;

create table band (
  band_id                   integer auto_increment not null,
  band_name                 varchar(255),
  is_trends_band            tinyint(1) default 0,
  account_account_id        bigint,
  created                   datetime,
  constraint pk_band primary key (band_id))
;

create table keyword (
  keyword_id                bigint auto_increment not null,
  text                      varchar(255),
  is_hashtag                tinyint(1) default 0,
  created                   datetime,
  constraint pk_keyword primary key (keyword_id))
;

create table media_result (
  media_result_id           integer auto_increment not null,
  title                     varchar(255),
  url                       varchar(255),
  song_id                   varchar(255),
  account_account_id        bigint,
  created                   datetime,
  is_local                  tinyint(1) default 0,
  constraint pk_media_result primary key (media_result_id))
;

create table spoken (
  spoken_id                 integer auto_increment not null,
  user_id                   bigint,
  tweet_id                  bigint,
  keyword_id                bigint,
  created                   datetime,
  constraint pk_spoken primary key (spoken_id))
;

create table tweet (
  tweet_id                  bigint auto_increment not null,
  original_id               bigint,
  text                      varchar(255),
  audio_url                 varchar(255),
  username                  varchar(255),
  sentiment_color           varchar(255),
  created                   datetime,
  constraint pk_tweet primary key (tweet_id))
;


create table band_keyword (
  band_band_id                   integer not null,
  keyword_keyword_id             bigint not null,
  constraint pk_band_keyword primary key (band_band_id, keyword_keyword_id))
;

create table keyword_tweet (
  keyword_keyword_id             bigint not null,
  tweet_tweet_id                 bigint not null,
  constraint pk_keyword_tweet primary key (keyword_keyword_id, tweet_tweet_id))
;

create table keyword_band (
  keyword_keyword_id             bigint not null,
  band_band_id                   integer not null,
  constraint pk_keyword_band primary key (keyword_keyword_id, band_band_id))
;

create table tweet_keyword (
  tweet_tweet_id                 bigint not null,
  keyword_keyword_id             bigint not null,
  constraint pk_tweet_keyword primary key (tweet_tweet_id, keyword_keyword_id))
;
alter table band add constraint fk_band_account_1 foreign key (account_account_id) references account (account_id) on delete restrict on update restrict;
create index ix_band_account_1 on band (account_account_id);
alter table media_result add constraint fk_media_result_account_2 foreign key (account_account_id) references account (account_id) on delete restrict on update restrict;
create index ix_media_result_account_2 on media_result (account_account_id);



alter table band_keyword add constraint fk_band_keyword_band_01 foreign key (band_band_id) references band (band_id) on delete restrict on update restrict;

alter table band_keyword add constraint fk_band_keyword_keyword_02 foreign key (keyword_keyword_id) references keyword (keyword_id) on delete restrict on update restrict;

alter table keyword_tweet add constraint fk_keyword_tweet_keyword_01 foreign key (keyword_keyword_id) references keyword (keyword_id) on delete restrict on update restrict;

alter table keyword_tweet add constraint fk_keyword_tweet_tweet_02 foreign key (tweet_tweet_id) references tweet (tweet_id) on delete restrict on update restrict;

alter table keyword_band add constraint fk_keyword_band_keyword_01 foreign key (keyword_keyword_id) references keyword (keyword_id) on delete restrict on update restrict;

alter table keyword_band add constraint fk_keyword_band_band_02 foreign key (band_band_id) references band (band_id) on delete restrict on update restrict;

alter table tweet_keyword add constraint fk_tweet_keyword_tweet_01 foreign key (tweet_tweet_id) references tweet (tweet_id) on delete restrict on update restrict;

alter table tweet_keyword add constraint fk_tweet_keyword_keyword_02 foreign key (keyword_keyword_id) references keyword (keyword_id) on delete restrict on update restrict;

# --- !Downs

SET FOREIGN_KEY_CHECKS=0;

drop table account;

drop table band;

drop table band_keyword;

drop table keyword;

drop table keyword_tweet;

drop table keyword_band;

drop table media_result;

drop table spoken;

drop table tweet;

drop table tweet_keyword;

SET FOREIGN_KEY_CHECKS=1;

