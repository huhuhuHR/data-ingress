# Linux 系统监控工具

## 目的

1. **找出系统瓶颈**
2. 磁盘（存储）瓶颈
3. CPU和内存瓶颈
4. 网络瓶颈

#### vmstat

命令报告关于内核线程、虚拟内存、磁盘、陷阱和 CPU 活动的统计信息

```
Usage:
 vmstat [options] [delay [count]]

Options:
 -a, --active           显示活跃和非活跃内存active/inactive memory
 -f, --forks            显示从系统启动至今的fork数量number of forks since boot
 -m, --slabs            显示slabinfo
 -n, --one-header       只在开始时显示一次各字段名称do not redisplay header
 -s, --stats            显示内存相关统计信息及多种系统活动数量event counter statistics
 -d, --disk             显示磁盘相关统计信息disk statistics
 -D, --disk-sum         summarize disk statistics
 -p, --partition <dev>  partition specific statistics
 -S, --unit <char>      使用指定单位显示define display unit
 -w, --wide             wide output
 -t, --timestamp        show timestamp

 -h, --help     display this help and exit
 -V, --version  output version information and exit
```

#### 参数讲解

``` 
procs   -----------memory----------   ---swap-- -----io----  --system--  ----cpu----
r b     swpd  free    buff   cache    si   so    bi     bo   in   cs     us  sy  id  wa
0 0     160   122976  28668  4337136  0    0     261    315  1    3      1   0   97   2
```

| **类别**            | **项目**                                                 | **含义**                                                     | **说明**                                                     |
| ------------------- | -------------------------------------------------------- | ------------------------------------------------------------ | ------------------------------------------------------------ |
| Procs（进程）       | r                                                        | 等待执行的任务数                                             | 展示了正在执行和等待cpu资源的任务个数。当这个值超过了cpu个数，就会出现cpu瓶颈。 |
| B                   | 等待IO的进程数量                                         |                                                              |                                                              |
| Memory(内存)        | swpd                                                     | 正在使用虚拟的内存大小，单位k                                |                                                              |
| free                | 空闲内存大小                                             |                                                              |                                                              |
| buff                | 已用的buff大小，对块设备的读写进行缓冲                   |                                                              |                                                              |
| cache               | 已用的cache大小，文件系统的cache                         |                                                              |                                                              |
| inact               | 非活跃内存大小，即被标明可回收的内存，区别于free和active | 具体含义见：概念补充（当使用-a选项时显示）                   |                                                              |
| active              | 活跃的内存大小                                           | 具体含义见：概念补充（当使用-a选项时显示）                   |                                                              |
| Swap                | si                                                       | 每秒从交换区写入内存的大小（单位：kb/s）                     |                                                              |
| so                  | 每秒从内存写到交换区的大小                               |                                                              |                                                              |
| IO                  | bi                                                       | 每秒读取的块数（读磁盘）                                     | 现在的Linux版本块的大小为1024bytes                           |
| bo                  | 每秒写入的块数（写磁盘）                                 |                                                              |                                                              |
| system              | in                                                       | 每秒中断数，包括时钟中断                                     | 这两个值越大，会看到由内核消耗的cpu时间会越多                |
| cs                  | 每秒上下文切换数                                         |                                                              |                                                              |
| CPU（以百分比表示） | Us                                                       | 用户进程执行消耗cpu时间(user time)                           | us的值比较高时，说明用户进程消耗的cpu时间多，但是如果长期超过50%的使用，那么我们就该考虑优化程序算法或其他措施了 |
| Sy                  | 系统进程消耗cpu时间(system time)                         | sys的值过高时，说明系统内核消耗的cpu资源多，这个不是良性的表现，我们应该检查原因。 |                                                              |
| Id                  | 空闲时间(包括IO等待时间)                                 |                                                              |                                                              |
| wa                  | 等待IO时间                                               | Wa过高时，说明io等待比较严重，这可能是由于磁盘大量随机访问造成的，也有可能是磁盘的带宽出现瓶颈。 |                                                              |

#### 问题分析

1. r经常大于4，且id经常少于40，表示cpu的负荷很重。
2. disk经常不等于0，且在b中的队列大于3，表示io性能不好。
3. r是连续的大于在系统中的CPU的个数表示系统现在运行比较慢,有多数的进程等待CPU。
4. 如果r的输出数大于系统中可用CPU个数的4倍的话,则系统面临着CPU短缺的问题,或者是CPU的速率过低,系统中有多数的进程在等待CPU,造成系统中进程运行过慢。
5. 如果空闲时间(cpu id)持续为0并且系统时间(cpu sy)是用户时间的两倍(cpu us)系统则面临着CPU资源的短缺。

#### 内存的buffer和cache区别？

​		cache 是为了弥补高速设备和低速设备的鸿沟而引入的中间层，最终起到**加快访问速度**的作用。
​		buffer 的主要目的进行流量整形，把突发的大数量较小规模的 I/O 整理成平稳的小数量较大规模的 I/O，以**减少响应次数**（比如从网上下电影，你不能下一点点数据就写一下硬盘，而是积攒一定量的数据以后一整块一起写，不然硬盘都要被你玩坏了）。

​		1、**Buffer**（缓冲区）是系统两端处理**速度平衡**（从长时间尺度上看）时使用的。它的引入是为了减小短期内突发I/O的影响，起到**流量整形**的作用。比如生产者——消费者问题，他们产生和消耗资源的速度大体接近，加一个buffer可以抵消掉资源刚产生/消耗时的突然变化。
​		2、**Cache**（缓存）则是系统两端处理**速度不匹配**时的一种**折衷策略**。因为CPU和memory之间的速度差异越来越大，所以人们充分利用数据的局部性（locality）特征，通过使用存储系统分级（memory hierarchy）的策略来减小这种差异带来的影响。
​		3、假定以后存储器访问变得跟CPU做计算一样快，cache就可以消失，但是buffer依然存在。比如从网络上下载东西，瞬时速率可能会有较大变化，但从长期来看却是稳定的，这样就能通过引入一个buffer使得OS接收数据的速率更稳定，进一步减少对磁盘的伤害。
​		4、TLB（Translation Lookaside Buffer，翻译后备缓冲器）名字起错了，其实它是一个cache.

#### 如何释放buffer和cache？

drop_caches的值可以是0-3之间的数字，代表不同的含义：

1. 0不释放（系统默认值）
2. 1释放页缓存
3. 2释放dentries和inodes
4. 3释放所有缓存

```
echo 3 > /proc/sys/vm/drop_caches
```

