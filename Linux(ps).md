# Linux 系统监控工具（PS）

## 目的

1. **找出系统瓶颈**
2. 磁盘（存储）瓶颈
3. CPU和内存瓶颈
4. 网络瓶颈

#### ps

```
命令参数：
a  	显示所有进程
-a 	显示同一终端下的所有程序
-A 	显示所有进程
c  	显示进程的真实名称
-N 	反向选择
-e 	等于“-A”
e		显示环境变量
f  	显示程序间的关系
-H 	显示树状结构
r  	显示当前终端的进程
T  	显示当前终端的所有程序
u  	指定用户的所有进程
-au 显示较详细的资讯
-aux 显示所有包含其他使用者的行程 
-C<命令> 列出指定命令的状况
--lines<行数> 每页显示的行数
--width<字符数> 每页显示的字符数
--help 显示帮助信息

linux上进程有5种状态:
1. 运行(正在运行或在运行队列中等待)
2. 中断(休眠中, 受阻, 在等待某个条件的形成或接受到信号)
3. 不可中断(收到信号不唤醒和不可运行, 进程必须等待直到有中断发生)
4. 僵死(进程已终止, 但进程描述符存在, 直到父进程调用wait4()系统调用后释放)
5. 停止(进程收到SIGSTOP, SIGSTP, SIGTIN, SIGTOU信号后停止运行运行)

ps工具标识进程的5种状态码:
D 不可中断 uninterruptible sleep (usually IO)
R 运行 runnable (on run queue)
S 中断 sleeping
T 停止 traced or stopped
Z 僵死 a defunct (”zombie”) process
```

#### Linux下ps命令用于显示当前进程 (process) 的状态

##### **显示所有当前进程**

```
ps -ax
```

##### **根据用户过滤进程**

```
 ps -u 用户
```

##### **通过cpu和内存使用来过滤进程**

```
#按照CPU或者内存用量来筛选，这样你就找到哪个进程占用了你的资源
ps -aux
# CPU 使用来升序排序
ps -aux --sort -pcpu
# 内存使用 来升序排序
ps -aux --sort -pmem
# 查看cpu内存消耗前十的进程
ps -aux --sort -pcpu,+pmem | head -n 10
```

##### **返回含义**

```
USER：该 process 属于那个使用者账号的

PID ：该 process 的号码

%CPU：该 process 使用掉的 CPU 资源百分比

%MEM：该 process 所占用的物理内存百分比

VSZ ：该 process 使用掉的虚拟内存量 (Kbytes)

RSS ：该 process 占用的固定的内存量 (Kbytes)

TTY ：该 process 是在那个终端机上面运作，若与终端机无关，则显示 ?，另外， tty1-tty6 是本机上面的登入者程序，若为 pts/0 等等的，则表示为由网络连接进主机的程序。

STAT：该程序目前的状态，主要的状态有

R ：该程序目前正在运作，或者是可被运作

S ：该程序目前正在睡眠当中 (可说是 idle 状态)，但可被某些讯号 (signal) 唤醒。

T ：该程序目前正在侦测或者是停止了

Z ：该程序应该已经终止，但是其父程序却无法正常的终止他，造成 zombie (疆尸) 程序的状态

START：该 process 被触发启动的时间

TIME ：该 process 实际使用 CPU 运作的时间

COMMAND：该程序的实际指令
```

##### **通过进程名和PID过滤**

```
ps -f -C 进程名/PID
```

##### **根据线程来过滤进程**

```
#知道特定进程的线程，可以使用-L 参数，后面加上特定的PID
ps -L PID
```

##### **树形显示进程**

```
 #树形结构显示进程，可以使用 -axjf 参数
 ps -axjf
 #树形结构显示进程
 pstree
```

##### **显示安全信息**

```
#参数 -e 显示所有进程信息，-o 参数控制输出。Pid,User 和 Args参数显示PID，运行应用的用户和该应用
#-e 参数 一起使用的关键字是args, cmd, comm, command, fname, ucmd, ucomm, lstart, bsdstart 和 start
ps -eo pid,user,args
```

##### **使用PS实时监控进程状态**

```
#通过CPU和内存的使用率来筛选进程，并且我们希望结果能够每秒刷新一次。为此，我们可以将ps命令和watch命令结合起来
watch -n 1 ‘ps -aux --sort -pmem, -pcpu’|head 20’
```

