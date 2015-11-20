create table backup (   
    id integer primary key autoincrement,   
    url varchar(200),   
    param varchar(500),   
    memberId integer,   
    data text,
    edit_date varchar(20)
);  