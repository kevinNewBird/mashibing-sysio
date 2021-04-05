package com.mashibing.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/***********************
 * @Description: 网络通讯Socket的非阻塞IO<BR>
 * @author: zhao.song
 * @since: 2021/4/5 19:31
 * @version: 1.0
 ***********************/
public class SocketNIO {

    public static void main(String[] args) throws IOException, InterruptedException {
        //Java层面: New IO
        ServerSocketChannel socket = ServerSocketChannel.open(); // new
        socket.bind(new InetSocketAddress(9090));// bind -> listen

        socket.configureBlocking(false); // 重点:OS(操作系统)层面的非阻塞NONBlocking


        while (true) {
            //接受客户端的连接
            Thread.sleep(1000);
            SocketChannel client = socket.accept();//不会阻塞? -1 NULL
            if (client == null) {
                System.out.println("null...");
            }else{
                client.configureBlocking(false);// 重点 socket()
            }
        }

    }
}
