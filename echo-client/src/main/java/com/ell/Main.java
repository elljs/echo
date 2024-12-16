package com.ell;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class Main {
    public static void main(String[] args) throws Exception {
        // 打开客户端通道
        SocketChannel socketChannel = SocketChannel.open();
        // 设置为非阻塞模式
        socketChannel.configureBlocking(false);
        // 连接到服务器
        socketChannel.connect(new InetSocketAddress("localhost", 8080));

        // 打开选择器
        Selector selector = Selector.open();
        // 注册通道到选择器
        socketChannel.register(selector, SelectionKey.OP_CONNECT);

        while (true) {
            // 阻塞直到有事件就绪
            selector.select();

            // 获取所有就绪的事件
            for (SelectionKey key : selector.selectedKeys()) {
                if (key.isConnectable()) {
                    SocketChannel channel = (SocketChannel) key.channel();
                    // 完成连接
                    if (channel.finishConnect()) {
                        // 发送数据
                        String message = "Hello, Echo Server!";
                        ByteBuffer buffer = ByteBuffer.wrap(message.getBytes());
                        while (buffer.hasRemaining()) {
                            channel.write(buffer);
                        }
                        // 注册为可读事件
                        channel.register(selector, SelectionKey.OP_READ);
                    }
                } else if (key.isReadable()) {
                    // 接收数据
                    SocketChannel channel = (SocketChannel) key.channel();
                    ByteBuffer buffer = ByteBuffer.allocate(256);
                    int read = channel.read(buffer);
                    if (read > 0) {
                        buffer.flip();
                        byte[] data = new byte[buffer.remaining()];
                        buffer.get(data);
                        System.out.println("Received: " + new String(data));
                    }
                }
            }
            // 清除已处理的事件
            selector.selectedKeys().clear();
        }
    }
}