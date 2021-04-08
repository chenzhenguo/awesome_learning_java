package com.czg.netty;


import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.EventExecutorGroup;
import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Calendar;


//netty 的基础用法
public class MyNetty {

    @Test
    public  void  myBytebuf(){
     //   ByteBuf byteBuf = UnpooledByteBufAllocator.DEFAULT.heapBuffer(8, 20);
/**
 * initialCapacity就是初始申请的内存大小，也就是用户需要的内存大小。
 * maxCapacity是指内存最大允许大小，默认传进来是Integer.MAX_VALUE。
 *
 *
 * Pooled 和 Unpooled

 *

 * Pooled池化内存分配每次从预先分配好的一块内存取一段连续内存封装成ByteBuf提供给应用程序,
 * Pooled[预先分配好一整块内存,分配的时候用一定算法从一整块内存取出一块连续内存]
 * UnPooled[每次分配内存申请内存]
 * Unpooled非池化每次进行内存分配的时候调用系统API向操作系统申请一块内存
 *
 *
 * Unsafe 和 非Unsafe
 * Unsafe直接获取ByteBuf在JVM内存地址调用JDK的Unsafe进行读写操作,通过ByteBuf分配内存首地址和当前指针基于内存偏移地址获取值,
 *
 * 非Unsafe不依赖JDK的Unsafe对象,通过内存数组和索引获取值
 *
 * Heap和Direct
 * Heap在堆上进行内存分配,分配内存需要被GC管理,无需手动释放内存,依赖底层byte数组,
 *
 * Direct调用JDK的API进行内存分配,分配内存不受JVM控制最终不会参与GC过程,需要手动释放内存避免造成内存无法释放,依赖DirectByteBuffer对象内存

 如何减少多线程内存分配之间的竞争关系？
 PooledByteBufAllocator内存分配器结构维护Arena数组,所有的内存分配都在Arena上进行,

 通过PoolThreadCache对象将线程和Arena进行一一绑定, 默认情况一个Nio线程管理一个Arena实现多线程内存分配相互不受影响减少多线程内存分配之间的竞争

 不同大小的内存是如何进行分配的？
 Page级别的内存分配通过完全二叉树的标记查找某一段连续内存,

 Page级别以下的内存分配首先查找到Page然后把此Page按照SubPage大小进行划分最后通过位图的方式进行内存分配

 不同大小的内存是如何进行分配的？2
 Netty一次向系统申请16M的连续内存空间，这块内存通过PoolChunk对象包装，进一步的把这16M内存分成了2048个页（pageSize=8k）。页作为Netty内存管理的最基本的单位 ，所有的内存分配首先必须申请一块空闲页。
 对于小内存（小于4096）的分配还会将Page细化成更小的单位Subpage。Subpage按大小分有两大类，36种情况：Tiny：小于512的情况，最小空间为16，对齐大小为16，区间为[16,512)，所以共有32种情况。Small：大于等于512的情况，总共有四种，512,1024,2048,4096。

 作者：石家志远
 链接：https://www.jianshu.com/p/2498db9c91fe
 */
        //堆内缓冲区
        ByteBuf byteBuf = PooledByteBufAllocator.DEFAULT.heapBuffer(8, 20);
        byteBuf.writeBytes(new byte[]{1,2,3,4});
        print(byteBuf);
        byteBuf.writeBytes(new byte[]{1,2,3,4});
        print(byteBuf);
        byteBuf.writeBytes(new byte[]{1,2,3,4});
        print(byteBuf);
        byteBuf.writeBytes(new byte[]{1,2,3,4});
        print(byteBuf);
        byteBuf.writeBytes(new byte[]{1,2,3,4});
        print(byteBuf);
        byteBuf.writeBytes(new byte[]{1,2,3,4});
        print(byteBuf);
    }

    public static void print(ByteBuf byteBuf) {
        System.out.println("是否可读：byteBuf.isReadable():"+byteBuf.isReadable());
        System.out.println("读开始字节：byteBuf.readerIndex():"+byteBuf.readerIndex());
        System.out.println("可读字节数byteBuf.readableBytes():"+byteBuf.readableBytes());
        System.out.println("是否可写byteBuf.isWritable():"+byteBuf.isWritable());
        System.out.println("可写字节数byteBuf.writerIndex():"+byteBuf.writerIndex());
        System.out.println("可写字节数 ：byteBuf.writableBytes():"+byteBuf.writableBytes());

        System.out.println("返回此缓冲区已包含的字节数 byteBuf.capacity():"+byteBuf.capacity());

        System.out.println("此缓冲区可以包含的最大字节数 byteBuf.maxCapacity():"+byteBuf.maxCapacity());
        System.out.println("是否在堆外byteBuf.isDirect():"+byteBuf.isDirect());
        System.out.println("-------------------------------------");


    }

    @Test
    public void  loopExecutor() throws IOException {
        //创建运行线程池
        NioEventLoopGroup loopGroup = new NioEventLoopGroup(1);
        loopGroup.execute(()->{

            while (true){
                System.out.println(111111);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        loopGroup.execute(()->{

            while (true){
                System.out.println(222);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        System.in.read();//保证循环不停
    }

    //使用netty 原生方法创建客户端连接
    @Test
    public  void clintMode() throws InterruptedException {
        //运行线程池
        NioEventLoopGroup eventLoopGroup =new NioEventLoopGroup();

        //客户端注册到线程池中开始运行
        NioSocketChannel socketChannel = new NioSocketChannel();
                //epoll_ctl(5,ADD,3)
                eventLoopGroup.register(socketChannel);

        ChannelPipeline pipeline = socketChannel.pipeline();
            pipeline.addLast(new MyInHandler());
        ChannelFuture connect = pipeline.connect(new InetSocketAddress("127.0.0.1", 9090));
        ChannelFuture sync = connect.sync();
        ChannelFuture channelFuture = pipeline.writeAndFlush(Unpooled.copiedBuffer("hi server".getBytes()));
        channelFuture.sync();
        sync.channel().closeFuture().sync();
        System.out.println("client over");

    }

    //使用netty  的bootsrap  创建客户端连接
    @Test
    public void nettyClient() throws InterruptedException {
        NioEventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        Bootstrap bs =  new Bootstrap();
        ChannelFuture connect = bs.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new MyInHandler());

                    }
                })
                .connect(new InetSocketAddress("127.0.0.1", 9090));
        Channel channel = connect.sync().channel();
        ChannelFuture hellow_server = channel.writeAndFlush(Unpooled.copiedBuffer("hellow server".getBytes())).sync();
        channel.closeFuture().sync();


    }

    class MyInHandler extends ChannelInboundHandlerAdapter {

         @Override
         public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
             System.out.println("clinet Registered....");

         }

         @Override
         public void channelActive(ChannelHandlerContext ctx) throws Exception {
             System.out.println("clinet active....");
         }

         @Override
         public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
             ByteBuf buf = (ByteBuf) msg;
             CharSequence charSequence = buf.getCharSequence(0, buf.readableBytes(), CharsetUtil.UTF_8);
             System.out.println(charSequence);
             ctx.writeAndFlush(buf);
         }
     }


        @Test
         public      void serverModel(){
            NioEventLoopGroup eventLoopGroup = new NioEventLoopGroup();
            NioServerSocketChannel serverSocketChannel = new NioServerSocketChannel();
            eventLoopGroup.register(serverSocketChannel);

            ChannelPipeline pipeline = serverSocketChannel.pipeline();
            pipeline.addLast(new MyacceptHandler(eventLoopGroup,new ChannelInit()));

        }

    private class MyacceptHandler extends ChannelInboundHandlerAdapter {
        private  final  EventLoopGroup selector;
        private  final  ChannelHandler handler;

        public MyacceptHandler(EventLoopGroup thread,ChannelHandler handler){
                this.selector = thread;
                this.handler = handler;
        }

        @Override
        public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
            System.out.println("server registerd");
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            SocketChannel channel = (SocketChannel) msg;
            ChannelPipeline pipeline = channel.pipeline();
            pipeline.addLast(handler);

            selector.register(channel);

        }
    }
    //这个注解没啥用，知识装饰，需要手动实现单例模式
    @ChannelHandler.Sharable
     class ChannelInit extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
            ctx.channel().pipeline().addLast(new MyInHandler());
            ctx.pipeline().remove(this);
        }
    }
}
