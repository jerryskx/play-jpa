play-jpa
========

Test github repository, and JPA with Playframework

## References

* [Akka 2.0](http://akka.io/)
* [Akka Couch](https://raw.github.com/m242/akka-couch)
* [MySql](http://dev.mysql.com/downloads/mysql/)


Required:  MySql DB server v5.5.25

http://dev.mysql.com/doc/refman/5.5/en/macosx-installation.html  (Instructions)
http://dev.mysql.com/downloads/mysql/              (Community Server)
Install Server, MySQLStartupItem.pkg, and MySQL.prefPane (All inside the downloaded .dmg file)

http://dev.mysql.com/downloads/workbench/5.2.html  (GUI tools)


http://dev.mysql.com/doc/workbench/en/wb-getting-started-tutorial-creating-a-model.html



## MySql DB Script

select * from order_log
go
select * from order_items
go

insert into order_log (shipto_first_name, shipto_last_name, shipto_address1, shipto_city, shipto_state, shipto_zip, order_timestamp) values ('Jerry', 'Wang', '225 S. Sepulveda Blvd', 'Manhattan Beach', 'CA', '90266', now())
go


--insert into order_items (order_id, sku, price, status) values (1000000, '123abc', 14.99,'NEW')
insert into order_items (order_id, sku, price, status) values (1000000, '345def', 18.99,'NEW')
go