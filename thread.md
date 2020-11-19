     12    java.lang.Thread.State: RUNNABLE
      3    java.lang.Thread.State: TIMED_WAITING (on object monitor)
     28    java.lang.Thread.State: TIMED_WAITING (parking)
      2    java.lang.Thread.State: TIMED_WAITING (sleeping)
      2    java.lang.Thread.State: WAITING (on object monitor)
     27    java.lang.Thread.State: WAITING (parking)
     线程状态
     RUNNABLE 线程处于执行中
     BLOCKED 线程被阻塞
     # 调用了sleep、wait(interval)、join(interval)方法，有限等待
     TIMED_WAITING (on object monitor)
     TIMED_WAITING (parking)
     TIMED_WAITING (sleeping)
     # 调用了wait或join方法，无限等待
     WAITING (on object monitor)
     WAITING (parking)
     
     1死锁-Found one Java-level deadlock
     
     2Waiting on condition：在等待一个条件的发生，来把自己唤醒，或者调用了sleep方法
     此时线程状态：
     WAITING(parking)：一直等待那个条件发生
     TIMED_WAITING(parking或sleeping)：定时等待，即使条件不发生，时间到了也可以自己唤醒
     如果发现大量线程处于此状态，并且从线程的堆栈上查看到是正在执行网络读写，这可能是一个网络瓶颈问题或者第三方响应慢的问题
     
     3Blocked
     线程所需要的资源长时间等待却一直无法获取，被标识为阻塞状态，可以理解为等待资源超时的线程。线程堆栈中一般存在Waiting to Lock
     
     
     4 Waiting for monitor entry 和 in Object.wait()
     每个 Monitor在某个时刻，只能被一个线程拥有，该线程就是Active Thread，而其它线程都是Waiting Thread，分别在两个队列 Entry Set和Wait Set里面等候。 在Entry Set中等待的线程状态是Waiting for monitor entry，而在Wait Set中等待的线程状态是in Object.wait()。当被调用notify或notifyAll时，只有在Wait Set中的线程会被唤醒
     Waiting for monitor entry：等待进入一个临界区 ，所以它在Entry Set队列中等待。此时线程状态一般都是Blocked，如果存在大量线程在此状态，可能是一个全局锁阻塞住了大量线程。随着时间流逝，waiting for monitor entry的线程越来越多，没有减少的趋势，可能意味着某些线程在临界区里呆的时间太长了，以至于越来越多新线程迟迟无法进入临界区
     
     当线程获得了Monitor，如果发现线程继续运行的条件没有满足，它则调用对象（一般就是被 synchronized 的对象）的 wait() 方法，放弃了Monitor，进入Wait Set队列。此时线程状态大致为以下几种：TIMED_WAITING (on object monitor)和 WAITING (on object monitor)
     
    等待IO
    有时候线程状态是Runnable，但却是在等待IO
    "socketReadThread" prio=6 tid=0x0000000006a0d800 nid=0x1b40 runnable
    [0x00000000089ef000] java.lang.Thread.State: RUNNABLE
        at java.net.SocketInputStream.socketRead0(Native Method) 
    
    如果cpu使用率不高，但性能低下，一般都是由锁或IO阻塞造成，这时要注意查看状态为BLOCKED或者Waiting的线程，看它们需要等待什么锁或者是否出现了死锁，再考虑如何优化并发
    如果发现有大量的线程都在处在 Wait on condition，从线程 stack看，正等待网络读写，这可能是一个网络瓶颈的征兆。因为网络阻塞导致线程无法执行。一种情况是网络非常忙，几乎消耗了所有的带宽，仍然有大量数据等待网络读写；另一种情况也可能是网络空闲，但由于路由等问题，导致包无法正常的到达
   
    
     java定位cpu线程思路
     1 top -p pid -H
     2 printf “%x” nid
     3 jstack -l pid|grep nid -A10
     
     https://www.cnblogs.com/zhengyun_ustc/archive/2013/01/06/dumpanalysis.html
     https://blog.csdn.net/rachel_luo/article/details/8920596
     https://www.jianshu.com/p/4598a4c0fea3
     https://www.cnblogs.com/kabi/p/5169383.html
     http://byteliu.com/2019/01/27/%E7%BA%BF%E4%B8%8A%E9%97%AE%E9%A2%98%E6%8E%92%E6%9F%A5%E5%B7%A5%E5%85%B7/
     https://github.com/cjunn/script_tool/blob/master/java/java-thread-top.sh
     https://www.shuzhiduo.com/A/GBJr7WLa50/
     https://jishuin.proginn.com/p/763bfbd29679
     