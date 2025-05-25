-- Таблица с пользователями
create table if not exists users(
                                    id bigserial primary key,
                                    first_name varchar(256) not null,
    last_name varchar(256) not null,
    age integer not null,
    active boolean not null);

insert into users(first_name, last_name, age, active) values ('Иван', 'Иванов', 30, true);
insert into users(first_name, last_name, age, active) values ('Пётр', 'Петров', 25, false);
insert into users(first_name, last_name, age, active) values ('Мария', 'Сидорова', 28, true);