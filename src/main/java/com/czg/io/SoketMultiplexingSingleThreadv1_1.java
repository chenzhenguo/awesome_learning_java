package com.czg.io;

import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;

/**
 * socekt  soket io多路复用
 *
 */
public class SoketMultiplexingSingleThreadv1_1 {


    int port = 9090 ;
    ServerSocketChannel server = null;
    Selector selector;
    public  void initServer() throws IOException {
         server = ServerSocketChannel.open();
        server.configureBlocking(false);
         selector = Selector.open();

        server.bind(new InetSocketAddress(port));
        server.register(selector,SelectionKey.OP_ACCEPT);

    }

    @Test
    public   void  start() throws IOException {
        initServer();
        while (true){

            if(selector.select()>0){
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectionKeys.iterator();
                while (iterator.hasNext()){
                    SelectionKey key = iterator.next();
                    if(key.isAcceptable()){//如果是客户端的连接
                        acceptHadler(key);
                    }else if(key.isReadable()){
                        readHandler(key);
                    }

                }

            }
        }
    }

    //读取
    private void readHandler(SelectionKey key) throws IOException {
        SocketChannel clientChannel =(SocketChannel) key.channel();
        ByteBuffer buffer =(ByteBuffer) key.attachment();
        buffer.clear();

        int read = clientChannel.read(buffer);
        if(read>0){
         clientChannel.register(key.selector(),SelectionKey.OP_WRITE,buffer);
        }

    }

    private void acceptHadler(SelectionKey key) throws IOException {
        //注册客户端连接到 cannel
        ServerSocketChannel channel = (ServerSocketChannel)key.channel();
        SocketChannel client = channel.accept();
        client.configureBlocking(false);
        client.register(selector,SelectionKey.OP_READ,  ByteBuffer.allocate(8192));

    }


}
