package com.czg;

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
public class SoketMultiplexingSingleThreadv2 {


    int port = 9090 ;
    ServerSocketChannel serverSocketChannel = null;
    Selector selector;
    public  void initServer() throws IOException {
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.bind(new InetSocketAddress(port));
        selector = Selector.open();
        serverSocketChannel.register(selector,SelectionKey.OP_ACCEPT);

    }

    @Test
    public   void  start() throws IOException {
        initServer();
        while (true){

            if(selector.select(50)>0){
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectionKeys.iterator();
                while (iterator.hasNext()){
                    SelectionKey key = iterator.next();
                    iterator.remove();
                    //如果是客户端的连接
                    if(key.isAcceptable()){
                        acceptHadler(key);
                    }else if(key.isReadable()){
                      //  readHandler(key);
                      //  key.cancel();
                        key.interestOps(key.interestOps()| ~SelectionKey.OP_READ);
                        readHandler(key);

                    }else  if(key.isWritable()){//写事件
                        key.interestOps(key.interestOps()&  ~SelectionKey.OP_WRITE);
                        writeHandler(key);
                    }

                }

            }
        }
    }

    private void writeHandler(SelectionKey key) throws IOException {
        new Thread(()->{
            SocketChannel clientChannel =(SocketChannel) key.channel();
            ByteBuffer buffer =(ByteBuffer) key.attachment();
            //buffer 翻转获取字符准备再写出去
            buffer.flip();
            while (buffer.hasRemaining()){
                try {
                    clientChannel.write(buffer);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            buffer.clear();
        }).start();



    }

    //读取
    private void readHandler(SelectionKey key) throws IOException {
        new Thread(()->{
            SocketChannel clientChannel =(SocketChannel) key.channel();
            ByteBuffer buffer =(ByteBuffer) key.attachment();
            buffer.clear();
            int read = 0 ;
            while (true){
                try {
                        read = clientChannel.read(buffer);

                    if(read>0){
                        key.interestOps(SelectionKey.OP_READ);
                        clientChannel.register(key.selector(),SelectionKey.OP_WRITE,buffer);

                    }else if(read==0){
                        break;
                    }else {
                        //关闭channel
                        clientChannel.close();
                        break;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();



    }

    private void acceptHadler(SelectionKey key) throws IOException {
        //注册客户端连接到 cannel
        ServerSocketChannel channel = (ServerSocketChannel)key.channel();
        SocketChannel client = channel.accept();
        client.configureBlocking(false);
        client.register(selector,SelectionKey.OP_READ,  ByteBuffer.allocate(8192));
        System.out.println("client:" + client.getRemoteAddress() );
    }


}
