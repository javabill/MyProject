下载openjdk：http://hg.openjdk.java.net/

重载的方法在编译过程中即可完成识别，具体到每一个方法的调用，java编译器会根据传入参数的声明类型来选取重载方法，
选取过程：
1. 在不考虑对基本类型自动拆箱，以及可变长参数的情况下选取重载方法
2. 如果在第一个阶段中没找到适配的方法，那么在允许自动装拆箱，但不允许可变长参数的情况下
3. 在第二阶段没找到适配的方法，在允许自动拆装箱以及可变长参数的情况下选取重载方法

小知识：这个限制可以通过字节码工具绕开。也就是说，在编译完成之后，我们可以再向 class 文件中添加方法名和参数类型相同，而返回类型不同的方法。当这种包括多个方法名相同、参数类型相同，而返回类型不同的方法的类，出现在 Java 编译器的用户类路径上时，它是怎么确定需要调用哪个方法的呢？当前版本的 Java 编译器会直接选取第一个方法名以及参数类型匹配的方法。并且，它会根据所选取方法的返回类型来决定可不可以通过编译，以及需不需要进行值转换等。

java字节码中与调用相关的指令共有5种：
   invokestatic:调用静态方法
   invokespecial：调用私有实例方法，构造器，以及使用super关键字调用父类的实例方法或构造器，和所实现的接口的默认方法
   invokevirtual：调用非私有实例方法
   invokeinterface：调用接口方法
   Invokedynamic：调用动态方法

java虚拟机识别方法的关键在于类名、方法名和方法描述符

java虚拟机关于重写的判断同样基于方法描述，即子类定义了与父类中非私有、非静态方法同名的方法，只有当两个方法的参数类型和返回类型一致，java虚拟机才会判定为重写

java语言中重写而java虚拟机中非重写的情况，编译器会通过生成桥接来实现java中的重写语义

重载(编译阶段完成)被称为静态绑定  重写被称为动态绑定

唯一的例外在于，如果虚拟机能够确定目标方法有且仅有一个，比如说目标方法被标记为 final[3][4]，那么它可以不通过动态类型，直接确定目标方法。

javap -v Object.class

对于非接口符号的引用：假定该符号引用所指向的类为C，则java虚拟机会按照步骤：
1. 在C中查找符合名字及描述符的方法
2. 如果没有找到，在C的父类中继续搜索，直至Object类
3. 如果没找到，在C所直接实现或间接实现的接口中搜索，这一步搜索得到的目标方法必须是非私有、非静态的，并且目标方法在间接实现的接口中，需满足C与该接口之间没有其他符合条件的目标方法，如果有多个符合条件的目标方法，则任意返回其中一个
静态方法可以通过子类来调用，子类的静态方法会隐藏(与重写区分)父类中的同名、同描述符的静态方法

对于接口符号的引用，假定该符号引用所指向的接口为I，则java虚拟机会按照步骤：
1. 在 I 中查找符合名字及描述符的方法
2. 如果没有找到，在Object类中的公有实例方法中搜索
3. 如果没有找到，则在 I 的超接口中搜索，这一步的搜索结果的要求与非接口符号引用步骤3的要求一致

符号引用会被解析成实际引用，对于可以静态绑定的方法调用而言，实际引用是一个指向方法的指针，
对于需要动态绑定的方法调用而言，实际引用则是一个方法表的索引

对象头：
Java对象头和monitor是实现synchronized的基础
Hotspot虚拟机的对象头主要包括两部分数据：Mark Word（标记字段）、Klass Pointer（类型指针）。其中Klass Point是是对象指向它的类元数据的指针，虚拟机通过这个指针来确定这个对象是哪个类的实例，Mark Word用于存储对象自身的运行时数据，它是实现轻量级锁和偏向锁的关键

Mark Word用于存储对象自身的运行时数据，如哈希码（HashCode）、GC分代年龄、锁状态标志、线程持有的锁、偏向线程 ID、偏向时间戳等等。Java对象头一般占有两个机器码（在32位虚拟机中，1个机器码等于4字节，也就是32bit），但是如果对象是数组类型，则需要三个机器码，因为JVM虚拟机可以通过Java对象的元数据信息确定Java对象的大小，但是无法从数组的元数据来确认数组的大小，所以用一块来记录数组长度

对象头信息是与对象自身定义的数据无关的额外存储成本，但是考虑到虚拟机的空间效率，Mark Word被设计成一个非固定的数据结构以便在极小的空间内存存储尽量多的数据，它会根据对象的状态复用自己的存储空间，也就是说，Mark Word会随着程序的运行发生变化，变化状态如下（32位虚拟机）：

Monitor 是线程私有的数据结构，每一个线程都有一个可用monitor record列表，同时还有一个全局的可用列表。每一个被锁住的对象都会和一个monitor关联（对象头的MarkWord中的LockWord指向monitor的起始地址），同时monitor中有一个Owner字段存放拥有该锁的线程的唯一标识，表示该锁被这个线程占用。
Owner：初始时为NULL表示当前没有任何线程拥有该monitor record，当线程成功拥有该锁后保存线程唯一标识，当锁被释放时又设置为NULL；
EntryQ:关联一个系统互斥锁（semaphore），阻塞所有试图锁住monitor record失败的线程。
RcThis:表示blocked或waiting在该monitor record上的所有线程的个数。
Nest:用来实现重入锁的计数。
HashCode:保存从对象头拷贝过来的HashCode值（可能还包含GC age）。
Candidate:用来避免不必要的阻塞或等待线程唤醒，因为每一次只有一个线程能够成功拥有锁，如果每次前一个释放锁的线程唤醒所有正在阻塞或等待的线程，会引起不必要的上下文切换（从阻塞到就绪然后因为竞争锁失败又被阻塞）从而导致性能严重下降。Candidate只有两种可能的值0表示没有需要唤醒的线程1表示要唤醒一个继任线程来竞争锁。
自旋锁、适应性自旋锁、锁消除、锁粗化、偏向锁、轻量级锁等技术来减少锁操作的开销。
锁主要存在四中状态，依次是：无锁状态、偏向锁状态、轻量级锁状态、重量级锁状态，他们会随着竞争的激烈而逐渐升级。注意锁可以升级不可降级，这种策略是为了提高获得锁和释放锁的效率。

1. 编译器优化的重排序。编译器在不改变单线程程序语义的前提下，可以重新安排语句的执行顺序。
2. 指令级并行的重排序。现代处理器采用了指令级并行技术来将多条指令重叠执行。如果不存在数据依赖性，处理器可以改变语句对应机器指令的执行顺序。
3. 内存系统的重排序。由于处理器使用缓存和读／写缓冲区，这使得加载和存储操作看上去可能是在乱序执行。

关于主内存与工作内存之间具体的交互协议，Java内存模型定义了以下8种具体的操作来完成：
1. lock，锁定，作用于主内存的变量，它把主内存中的变量标识为一条线程独占状态；
2. unlock，解锁，作用于主内存的变量，它把锁定的变量释放出来，释放出来的变量才可以被其它线程锁定；
3. read，读取，作用于主内存的变量，它把一个变量从主内存传输到工作内存中，以便后续的load操作使用；
4. load，载入，作用于工作内存的变量，它把read操作从主内存得到的变量放入工作内存的变量副本中；
5. use，使用，作用于工作内存的变量，它把工作内存中的一个变量传递给执行引擎，每当虚拟机遇到一个需要使用到变量的值的字节码指令时将会执行这个操作；
6. assign，赋值，作用于工作内存的变量，它把一个从执行引擎接收到的变量赋值给工作内存的变量，每当虚拟机遇到一个给变量赋值的字节码指令时使用这个操作；
7. store，存储，作用于工作内存的变量，它把工作内存中一个变量的值传递到主内存中，以便后续的write操作使用；
8. write，写入，作用于主内存的变量，它把store操作从工作内存得到的变量的值放入到主内存的变量中；

Java内存模型还定义了执行上述8种操作的基本规则：
1. 不允许read和load、store和write操作之一单独出现，即不允许出现从主内存读取了而工作内存不接受，或者从工作内存回写了但主内存不接受的情况出现；
2. 不允许一个线程丢弃它最近的assign操作，即变量在工作内存变化了必须把该变化同步回主内存；
3. 不允许一个线程无原因地（即未发生过assign操作）把一个变量从工作内存同步回主内存；
4. 一个新的变量必须在主内存中诞生，不允许工作内存中直接使用一个未被初始化（load或assign）过的变量，换句话说就是对一个变量的use和store操作之前必须执行过load和assign操作；
5. 一个变量同一时刻只允许一条线程对其进行lock操作，但lock操作可以被同一个线程执行多次，多次执行lock后，只有执行相同次数的unlock操作，变量才能被解锁。
6. 如果对一个变量执行lock操作，将会清空工作内存中此变量的值，在执行引擎使用这个变量前，需要重新执行load或assign操作初始化变量的值；
7. 如果一个变量没有被lock操作锁定，则不允许对其执行unlock操作，也不允许unlock一个其它线程锁定的变量；
8. 对一个变量执行unlock操作之前，必须先把此变量同步回主内存中，即执行store和write操作；
unsafe
https://tech.meituan.com/2019/02/14/talk-about-java-magic-class-unsafe.html
Lambda
JDK使用invokedynamic及VM Anonymous Class结合来实现Java语言层面上的Lambda表达式
* invokedynamic： invokedynamic是Java 7为了实现在JVM上运行动态语言而引入的一条新的虚拟机指令，它可以实现在运行期动态解析出调用点限定符所引用的方法，然后再执行该方法，invokedynamic指令的分派逻辑是由用户设定的引导方法决定。
* VM Anonymous Class：可以看做是一种模板机制，针对于程序动态生成很多结构相同、仅若干常量不同的类时，可以先创建包含常量占位符的模板类，而后通过Unsafe.defineAnonymousClass方法定义具体类时填充模板的占位符生成具体的匿名类。生成的匿名类不显式挂在任何ClassLoader下面，只要当该类没有存在的实例对象、且没有强引用来引用该类的Class对象时，该类就会被GC回收。故而VM Anonymous Class相比于Java语言层面的匿名内部类无需通过ClassClassLoader进行类加载且更易回收。

