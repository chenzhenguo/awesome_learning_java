package com.czg.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class NettyIO {

    public static void main(String[] args) throws InterruptedException {
        NioEventLoopGroup boss = new NioEventLoopGroup(2);
        NioEventLoopGroup worker = new NioEventLoopGroup(2);

        ServerBootstrap boot = new ServerBootstrap();
        boot.group(boss, worker)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY,false)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new MyInbound2());
                        pipeline.addLast(new MyInbound2());
                        pipeline.addLast(new MyInbound2());
                        pipeline.addLast(new MyInbound2());
                    }
                }).bind(9999)
                .sync()
                .channel()
                .closeFuture()
                .sync();
        System.out.println("server  start....");

    }
}
class MyInbound2 extends ChannelInboundHandlerAdapter{

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf =(ByteBuf)msg;
        int index = buf.writerIndex();
        byte[] data = new byte[index];
        buf.getBytes(0,data);
        String dd = new String(data);

        String[] split = dd.split("\n");
        for (String  str :split){
            System.out.println(str);
        }
        ctx.write(msg);
    }
}