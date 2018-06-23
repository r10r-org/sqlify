create table guestbooks (
    id bigint auto_increment primary key,
    email varchar(255),
    content varchar(255),
    primary key (id)
);