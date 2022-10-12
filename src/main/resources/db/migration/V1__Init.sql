create table solar_systems
(
    id               bigint unsigned auto_increment
        primary key,
    name             varchar(255)   not null,
    security_status  double(17, 16) not null,
    constellation_id int unsigned   not null
);

create index solar_systems_name_index
    on solar_systems (name(3));

create table tokens
(
    id                      bigint auto_increment
        primary key,
    access_token_expires_at datetime      null,
    access_token_issued_at  datetime      null,
    access_token_value      varchar(2000) null,
    refresh_token_issued_at datetime      null,
    refresh_token_value     varchar(255)  null
);

create table token_scopes
(
    token_id            bigint       not null,
    access_token_scopes varchar(255) null,
    constraint token_scopes_tokens_id_fk
        foreign key (token_id) references tokens (id)
);

create table users
(
    id         bigint       not null
        primary key,
    created_at datetime     null,
    name       varchar(255) null,
    token_id   bigint       null,
    constraint users_tokens_id_fk
        foreign key (token_id) references tokens (id)
);

create table route_favorites
(
    id      bigint auto_increment
        primary key,
    user_id bigint       null,
    name    varchar(255) null,
    constraint route_favorites_users_id_fk
        foreign key (user_id) references users (id)
);

create table route_favorite_points
(
    route_favorite_id bigint not null,
    points            int    null,
    constraint route_favorite_points_route_favorites_id_fk
        foreign key (route_favorite_id) references route_favorites (id)
);

