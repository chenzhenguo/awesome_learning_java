package com.czg.io;

import sun.rmi.transport.Connection;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class ChatServer {

    public static void main(String[] args) throws IOException {
            Selector selector = Selector.open();
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.bind(new InetSocketAddress(8888));
            serverSocketChannel.configureBlocking(false);
            //将accept 时间绑定selector上
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            while (true){
                //阻塞在select 上了，等待有 key返回
                selector.select();
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                //遍历select key
                Iterator<SelectionKey> iterator = selectionKeys.iterator();
                while (iterator.hasNext()){
                    SelectionKey selectionKey = iterator.next();
                    if(selectionKey.isAcceptable()) {
                        ServerSocketChannel ssc = (ServerSocketChannel) selectionKey.channel();
                        SocketChannel socketChannel = ssc.accept();
                        System.out.println("accept new connect :" + socketChannel.getRemoteAddress());
                        socketChannel.configureBlocking(false);
                        socketChannel.register(selector, SelectionKey.OP_READ);
                        //加入群聊
                        ChatHolder.join(socketChannel);
                    }else if(selectionKey.isReadable()){
                        SocketChannel channel = (SocketChannel)selectionKey.channel();
                        //将数据读入到buffer
                        ByteBuffer allocate = ByteBuffer.allocate(1024);
                        int length = channel.read(allocate);
                        if(length>0){
                            allocate.flip();
                            byte[] bytes = new byte[allocate.remaining()];
                            //将buffer的数据读入到 bytes中
                            allocate.get(bytes);
                            String content = new String(bytes,"UTF-8").replace("\r\n","");
                            if(content.equalsIgnoreCase("quite")){
                                //退出
                                ChatHolder.quit(channel);
                                selectionKey.channel();
                                serverSocketChannel.close();


                            }else {
                                ChatHolder.propagate(channel,content);
                            }
                        }

                    }
                    iterator.remove();
                }
            }


    }

    private static class ChatHolder {
        private static  final Map<SocketChannel,String> USER_MAP= new ConcurrentHashMap<>();
        public static void join(SocketChannel socketChannel) {
            //有人加入就分配一个id
            String id ="用户"+ ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE);
            send(socketChannel,"您的id为"+id);
            //给其他的channel 连接的用户发送消息，提示加入群聊信息
            for(SocketChannel channel:USER_MAP.keySet()){
                send(channel,id+"加入了群聊");
            };
            //将当前用户的channel 放入map
            USER_MAP.put(socketChannel,id);



        }

        public static  void quit(SocketChannel socketChannel){
            String id = USER_MAP.get(socketChannel);
            send(socketChannel,"您已退出了群聊"+"\n\r");
            USER_MAP.remove(socketChannel);
            for (SocketChannel channel : USER_MAP.keySet()){
                if(channel != socketChannel){
                    send(channel,id+"退出了群聊"+"\n\r");
                }
            }

        }

        public  static  void  propagate(SocketChannel socketChannel ,String content){
            String id= USER_MAP.get(socketChannel);
            for(SocketChannel channel :USER_MAP.keySet()){
                if(channel!=socketChannel){
                    send(channel,id+":"+content+"\n\r");
                }
            }
        }



        private static void send(SocketChannel socketChannel, String msg)  {
            ByteBuffer writeBuffer = ByteBuffer.allocate(1024);
                writeBuffer.put(msg.getBytes());
                writeBuffer.flip();
            try {
                socketChannel.write(writeBuffer);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}
