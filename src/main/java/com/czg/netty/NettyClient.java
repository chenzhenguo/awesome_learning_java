package com.czg.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.SwappedByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class NettyClient {
    public static void main(String[] args) throws InterruptedException {
         NioEventLoopGroup eventExecutors = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(eventExecutors).channel(NioSocketChannel.class).
                remoteAddress("127.0.0.1",9999).
                handler(new ChannelInitializer<SocketChannel>(){

                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        System.out.println("init clieny");
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        pipeline.addLast(new MyInbound1());
                    }
                });
        ChannelFuture sync = bootstrap.connect().sync();
        Channel channel = sync.channel();
        System.out.println(channel);
        ByteBuf byteBuf = Unpooled.copiedBuffer("hi --->".getBytes());
        channel.writeAndFlush(byteBuf);

    }
}

class MyInbound1 extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        int size = buf.writerIndex();
        byte[] data = new byte[size];
        buf.getBytes(0, data);

        String s = new String(data);
        String[] split = s.split("\n");
        for (String str: split){
            System.out.println(str);
        }

    }
}