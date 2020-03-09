CREATE TABLE IF NOT EXISTS stock
(
    id INT NOT NULL AUTO_INCREMENT,
    value INT NOT NULL,
    PRIMARY KEY(id)
);

insert into STOCK select * from (
    select 1, 100 union
    select 2, 50 union
    select 3, 0
) x where not exists(select * from STOCK);