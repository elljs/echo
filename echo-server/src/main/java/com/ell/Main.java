package com.ell;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class Main {
    public static void main(String[] args) throws Exception {
        // 打开服务器通道
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        // 绑定端口
        serverChannel.bind(new InetSocketAddress(8080));
        // 设置为非阻塞模式
        serverChannel.configureBlocking(false);

        // 打开选择器
        Selector selector = Selector.open();
        // 注册通道到选择器
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);

        while (true) {
            // 阻塞直到有事件就绪
            selector.select();

            // 获取所有就绪的事件
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectedKeys.iterator();

            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();

                // 处理接受事件
                if (key.isAcceptable()) {
                    ServerSocketChannel server = (ServerSocketChannel) key.channel();
                    SocketChannel client = server.accept();
                    client.configureBlocking(false);
                    client.register(selector, SelectionKey.OP_READ);
                }

                // 处理读事件
                if (key.isReadable()) {
                    SocketChannel client = (SocketChannel) key.channel();
                    ByteBuffer buffer = ByteBuffer.allocate(256);
                    int read = client.read(buffer);
                    if (read == -1) {
                        key.cancel();
                        client.close();
                    } else {
                        buffer.flip();
                        client.write(buffer);
                        buffer.clear();
                    }
                }

                // 移除当前已经处理的事件
                iterator.remove();
            }
        }
    }
}