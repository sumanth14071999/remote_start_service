--liquibase formatted sql

--changeset remotestart:001-create-remote-start
create table remote_start
(
    id             uuid primary key,
    contract_id    varchar(64) not null,
    evse_uid       varchar(48) not null,
    status         varchar(16) not null,
    failure_reason varchar(64),
    created_at     timestamptz not null,
    updated_at     timestamptz not null
);
--rollback drop table remote_start;
