create database indiepim character set utf8mb4 collate utf8mb4_unicode_ci;
create user 'indiepim'@'localhost' identified by 'password';
grant all on indiepim.* to 'indiepim'@'localhost';