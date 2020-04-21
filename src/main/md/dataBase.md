http://benjaminwhx.com/2018/02/28/%E7%94%B1%E4%B8%80%E6%AC%A1%E7%BA%BF%E4%B8%8A%E9%97%AE%E9%A2%98%E5%B8%A6%E6%9D%A5%E7%9A%84MySQL%E6%AD%BB%E9%94%81%E9%97%AE%E9%A2%98%E5%88%86%E6%9E%90/
数据库sql优化 explain看哪些参数  B+树  为什么使用B+树  mvcc结构
建立数据库索引的原则：
1. 最左匹配原则，mysql会一直向右匹配直到遇到范围查询(< > between、like) 就停止匹配
2. =和in可以乱序
3. 选择区分度高的列作为索引 区分度count(distinct col)/count(*)
4. 索引列不参与计算，应将from_unixtime(create_time) = ’2014-05-29’ 改为create_time = unix_timestamp(’2014-05-29’)
5. 尽量扩展索引不新建索引
慢查询优化基本步骤
1. 先运行看看是否真的很慢，注意设置SQL_NO_CACHE
2. where条件单表查，锁定最小返回记录表。这句话的意思是把查询语句的where都应用到表中返回的记录数最小的表开始查起，单表每个字段分别查询，看哪个字段的区分度最高
3. explain查看执行计划，是否与1预期一致（从锁定记录较少的表开始查询）
4. order by limit 形式的sql语句让排序的表优先查
5. 了解业务方使用场景
6.  加索引时参照建索引的几大原则
7. 观察结果，不符合预期继续从1分析
Mvcc：
MVCC，Multi-Version Concurrency Control，多版本并发控制。MVCC 是一种并发控制的方法，一般在数据库管理系统中，实现对数据库的并发访问；在编程语言中实现事务内存。
MVCC的实现，是通过保存数据在某一个时间点的快照来实现的。因此每一个事务无论执行多长时间看到的数据，都是一样的。所以MVCC实现可重复读。
* 快照读：select语句默认，不加锁，MVCC实现可重复读，使用的是MVCC机制读取undo中的已经提交的数据。所以它的读取是非阻塞的
* 当前读：select语句加S锁或X锁；所有的修改操作加X锁，在select for update 的时候，才是当前读。
行记录隐藏字段：
* db_row_id，行ID，用来生成默认聚簇索引（聚簇索引，保存的数据在物理磁盘中按顺序保存，这样相关数据保存在一起，提高查询速度）
* db_trx_id，事务ID，新开始一个事务时生成，实例内全局唯一
* db_roll_ptr，undo log指针，指向对应记录当前的undo log
* deleted_bit，删除标记位，删除时设置
1. update
    * 行记录数据写入undo log,事务的回滚操作就需要undo log
    * 更新行记录数据，当前事务ID写入db_trx_id，undo log指针写入db_roll_ptr
2. delete
    * 和update一样，只增加deleted_bit设置
3. insert
    * 生成undo log
    * 插入行记录数据，当前事务ID写入db_trx_id， db_roll_ptr为空
这样设计使得读操作很简单，性能很好，并且也能保证只会读到符合标准的行，不足之处是每行记录都需要额外的储存空间，需要做更多的行检查工作，以及额外的维护工作
数据库锁：
Latch一般称为闩锁（轻量级的锁），因为其要求锁定的时间必须非常短。若持续的时间长，则应用的性能会非常差，在InnoDB引擎中，Latch又可以分为mutex（互斥量）和rwlock（读写锁）。其目的是用来保证并发线程操作临界资源的正确性，并且通常没有死锁检测的机制。
Lock的对象是事务，用来锁定的是数据库中的对象，如表、页、行。并且一般lock的对象仅在事务commit或rollback后进行释放（不同事务隔离级别释放的时间可能不同）。

InnoDB 实现了标准的行级锁，也就是共享锁（Shared Lock）和互斥锁（Exclusive Lock）。
* 共享锁（读锁），允许事务读一行数据。
* 排他锁（写锁），允许事务删除或更新一行数据。
锁的粒度一般分为：表锁、页锁、行级锁
表锁：
1. 表级别的锁定是MySQL各存储引擎中最大颗粒度的锁定机制。该锁定机制最大的特点是实现逻辑非常简单，带来的系统负面影响最小。所以获取锁和释放锁的速度很快。由于表级锁一次会将整个表锁定，所以可以很好的避免困扰我们的死锁问题。
2. 当然，锁定颗粒度大所带来最大的负面影响就是出现锁定资源争用的概率也会最高，致使并发度大打折扣。
3. 使用表级锁定的主要是MyISAM，MEMORY，CSV等一些非事务性存储引擎。
页锁：
1. 页级锁定是MySQL中比较独特的一种锁定级别，在其他数据库管理软件中也并不是太常见。页级锁定的特点是锁定颗粒度介于行级锁定与表级锁之间，所以获取锁定所需要的资源开销，以及所能提供的并发处理能力也同样是介于上面二者之间。另外，页级锁定和行级锁定一样，会发生死锁。
2. 在数据库实现资源锁定的过程中，随着锁定资源颗粒度的减小，锁定相同数据量的数据所需要消耗的内存数量是越来越多的，实现算法也会越来越复杂。不过，随着锁定资源颗粒度的减小，应用程序的访问请求遇到锁等待的可能性也会随之降低，系统整体并发度也随之提升。
3. 使用页级锁定的主要是BerkeleyDB存储引擎。
行锁：
1. 行级锁定最大的特点就是锁定对象的粒度很小，也是目前各大数据库管理软件所实现的锁定颗粒度最小的。由于锁定颗粒度很小，所以发生锁定资源争用的概率也最小，能够给予应用程序尽可能大的并发处理能力而提高一些需要高并发应用系统的整体性能。
2. 虽然能够在并发处理能力上面有较大的优势，但是行级锁定也因此带来了不少弊端。由于锁定资源的颗粒度很小，所以每次获取锁和释放锁需要做的事情也更多，带来的消耗自然也就更大了。此外，行级锁定也最容易发生死锁。
3. 使用行级锁定的主要是InnoDB存储引擎。

总结
* 表级锁：开销小，加锁快；不会出现死锁；锁定粒度大，发生锁冲突的概率最高，并发度最低。
* 行级锁：开销大，加锁慢；会出现死锁；锁定粒度最小，发生锁冲突的概率最低，并发度也最高。
* 页面锁：开销和加锁时间界于表锁和行锁之间；会出现死锁；锁定粒度界于表锁和行锁之间，并发度一般。
InnoDB中的锁：
意向锁：那什么是意向锁呢？我们在这里可以举一个例子：如果没有意向锁，当已经有人使用行锁对表中的某一行进行修改时，如果另外一个请求要对全表进行修改，那么就需要对所有的行是否被锁定进行扫描，在这种情况下，效率是非常低的；不过，在引入意向锁之后，当有人使用行锁对表中的某一行进行修改之前，会先为表添加意向互斥锁（IX），再为行记录添加互斥锁（X），在这时如果有人尝试对全表进行修改就不需要判断表中的每一行数据是否被加锁了，只需要通过等待意向互斥锁被释放就可以了。

意向锁也分为两种：
* 意向共享锁（IS）：事务想要在获得表中某些记录的共享锁，需要在表上先加意向共享锁。
* 意向互斥锁（IX）：事务想要在获得表中某些记录的互斥锁，需要在表上先加意向互斥锁。
InnoDB存储引擎有3种行锁的算法，其分别是：
* Record Lock：单个行记录上的锁。
* Gap Lock：间隙锁，锁定一个范围，但不包含记录本身。
* Next-Key Lock：Gap Lock+Record Lock，锁定一个范围，并且锁定记录本身。
Record Lock总是会去锁住索引记录，如果InnoDB存储引擎表在建立的时候没有设置任何一个索引，那么这时InnoDB存储引擎会使用隐式的主键来进行锁定。

Next-Key Lock是结合了Gap Lock和Record Lock的一种锁定算法，在Next-Key Lock算法下，InnoDB对于行的查询都是采用这种锁定算法。例如有一个索引有10，11，13和20这4个值，那么该索引可能被Next-Key Locking的区间为：


除了Next-Key Locking，还有Previous-Key Locking技术。同样上述的值，使用Previous-Key Locking技术，那么可锁定的区间为：

但是不是所有索引都会加上Next-key Lock的，在查询的列是唯一索引（包含主键索引）的情况下，Next-key Lock会降级为Record Lock。
用户可以通过以下两种方式来显示的关闭Gap Lock：
* 将事务的隔离级别设为 READ COMMITED。
* 将参数innodb_locks_unsafe_for_binlog设置为1。
Gap Lock的作用是为了阻止多个事务将记录插入到同一个范围内，设计它的目的是用来解决Phontom Problem（幻读问题）。在MySQL默认的隔离级别（Repeatable Read）下，InnoDB就是使用它来解决幻读问题。
幻读是指在同一事务下，连续执行两次同样的SQL语句可能导致不同的结果，第二次的SQL可能会返回之前不存在的行，也就是第一次执行和第二次执行期间有其他事务往里插入了新的行。
一致性非锁定读（consistent nonlocking read）是指InnoDB存储引擎通过多版本控制（MVCC）的方式来读取当前执行时间数据库中行的数据。如果读取的这行正在执行DELETE或UPDATE操作，这时读取操作不会向XS锁一样去等待锁释放，而是会去读一个快照数据。

在事务隔离级别RC和RR下，InnoDB存储引擎使用非锁定的一致性读。然而对于快照数据的定义却不同，在RC级别下，对于快照数据，非一致性读总是读取被锁定行的最新一份快照数据。而在RR级别下，对于快照数据，非一致性读总是读取事务开始时的行数据版本。
一致性锁定读：
前面说到，在默认隔离级别RR下，InnoDB存储引擎的SELECT操作使用一致性非锁定读。但是在某些情况下，用户需要显式地对数据库读取操作进行加锁以保证数据逻辑的一致性。InnoDB存储引擎对于SELECT语句支持两种一致性的锁定读（locking read）操作。
* SELECT … FOR UPDATE （X锁）
* SELECT … LOCK IN SHARE MODE （S锁）


1. InnoDB的RC存在幻读，并且不存在gap锁。在有索引的时候，会对索引命中行加上X锁，在无索引时，会对表的所有行加上X锁（RC里锁全部行和锁表不同，因为锁全部行还可以insert，但是锁表不可以）
2. InnoDB的RR不存在幻读（注意：在一般概念里，会强调RR隔离级别不允许脏读和不可重复读，但是可以幻读，但是这里却特别强调RR不存在幻读现象，是因为MySQL InnoDB引擎的实现和标准有所不同），在非唯一索引以及无索引时，会先对命中行加上gap锁来防止幻读，然后再对命中行加上X锁。在无索引时，会先对表的全部行加上gap锁来防止缓存，然后再对全部行加上X锁（RR里锁全部行和锁表功能上类似，但不是锁表）
聚集索引(聚簇索引)：数据行的物理顺序与列值（一般是主键的那一列）的逻辑顺序相同，一个表中只能拥有一个聚集索引。mysql中主键就是聚集索引
非聚集索引：该索引中索引的逻辑顺序与磁盘上行的物理存储顺序不同，一个表中可以拥有多个非聚集索引。
mysql的回表查询：

B+树叶子节点的数据存储是有序的，底层相当于链表，存储下个节点的链接，比如查询id> 10 and id < 100，先查询id > 10 的，再next知道有id>100的则返回
数据库并发控制：
https://mp.weixin.qq.com/s/SVo0eTBaTvneLImJA54Flg