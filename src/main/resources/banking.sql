create table company
(
  companyId      uuid primary key,
    name text not null
);

create table USERS
(
   userId        uuid primary key,
    companyId uuid not null references company on delete restrict
);

create table wallet
(
    walletId        uuid primary key,
    balance    decimal    not null,
    currency   varchar(3) not null,
    companyId uuid       not null references company on delete restrict,
    isMaster  boolean    not null
);

create table card
(
    cardId            uuid primary key,
    walletId      uuid        not null references wallet on delete restrict,
    currency        varchar(3)  not null,
    balance         decimal     not null,
    number          varchar(16) not null,
    expirationDate timestamp   not null,
    ccv             varchar(3)  not null,
    userId         uuid        not null references users on delete restrict,
    isBlocked     boolean     not null
);

create table transfer
(
    id                 uuid primary key,
    tstamp          timestamp  not null,
    amount             decimal    not null,
    originCurrency    varchar(3) not null,
    targetCurrency    varchar(3) not null,
    conversionFee     int,
    origin  text       not null,
    target     text       not null
);


insert into company (companyId, name)
VALUES ('6ca1e2b7-f11c-4f95-945f-78f75a09382d', 'Holding');

insert into company ( companyId , name)
VALUES ('9d6493b3-b550-49a5-9a55-a5b0568225fc', 'mdulac Corporation');

insert into users (userId, companyId)
values ('f0ef3449-f5f8-4e07-82ee-683247e11dc3',
        '9d6493b3-b550-49a5-9a55-a5b0568225fc');

insert into users (userId, companyId)
values ('76c4785e-da22-4cfe-8495-27ae4d6f9c15',
        '9d6493b3-b550-49a5-9a55-a5b0568225fc');

        insert into wallet (walletId, balance, currency, companyId, isMaster)
        values ('c7f3c868-ea95-4072-a507-1543799d26fe',
                100,
                'EUR',
                '9d6493b3-b550-49a5-9a55-a5b0568225fc',
                false);

        insert into wallet (walletId, balance, currency, companyId, isMaster)
        values ('10f6d391-11cd-463b-8a51-c95bc4580d2e',
                100,
                'EUR',
                '9d6493b3-b550-49a5-9a55-a5b0568225fc',
                false);

        insert into wallet (walletId, balance, currency,companyId, isMaster)
        values ('130081e3-3677-46b9-b7eb-518a3e1f8f19',
                100,
                'USD',
                '9d6493b3-b550-49a5-9a55-a5b0568225fc',
                false);



insert into wallet (walletId, balance, currency,companyId, isMaster)
values ('b678f3a1-e01e-49cf-9eb3-7c03eed3d4ee',
        0,
        'EUR',
        '6ca1e2b7-f11c-4f95-945f-78f75a09382d',
        true);

insert into wallet (walletId, balance, currency, companyId, isMaster)
values ('78f0f545-1f44-48cd-9aed-d992a88d09a6',
        0,
        'USD',
        '6ca1e2b7-f11c-4f95-945f-78f75a09382d',
        true);

insert into wallet (walletId, balance, currency,companyId, isMaster)
values ('1ccd1744-e3be-41d5-9337-59f0de9e8718',
        0,
        'GBP',
        '6ca1e2b7-f11c-4f95-945f-78f75a09382d',
        true);


insert into card (cardId, walletId, currency, balance, number, expirationDate, ccv, userId,  isBlocked)
values ('48a9cf36-32b2-4b79-8964-020ce6f234f9',
        'c7f3c868-ea95-4072-a507-1543799d26fe',
        'EUR',
        500,
        '1111111111111111',
        '2020-08-10 13:00:00',
        '123',
        'f0ef3449-f5f8-4e07-82ee-683247e11dc3',
        false);

insert into card (cardId, walletId, currency, balance, number, expirationDate, ccv, userId, isBlocked)
values ('5244839a-0272-4786-bb29-6423623578b5',
        'c7f3c868-ea95-4072-a507-1543799d26fe',
        'EUR',
        1000,
        '1111111111111111',
        '2021-08-10 13:00:00',
        '123',
        '76c4785e-da22-4cfe-8495-27ae4d6f9c15',
        false);
