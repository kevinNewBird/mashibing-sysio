package com.mashibing.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.CharsetUtil;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * description  MyNetty <BR>
 * <p>
 * author: zhao.song
 * date: created in 16:32  2021/7/20
 * company: TRS信息技术有限公司
 * version 1.0
 */
public class MyNetty {

    @Test
    public void myByteBuf() {
        // 初始大小8, 最大20
//        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer(8, 20);
//        ByteBuf buf = UnpooledByteBufAllocator.DEFAULT.heapBuffer(8, 20);
        ByteBuf buf = PooledByteBufAllocator.DEFAULT.heapBuffer(8, 20);
        print(buf);

        for (int i = 0; i < 5; i++) {
            buf.writeBytes("1234".getBytes());
            print(buf);
        }

    }

    public static void print(ByteBuf buf) {
        System.out.println("buf.isReadable()    :" + buf.isReadable());//是否可读
        System.out.println("buf.readerIndex()   :" + buf.readerIndex());//读取位置的索引
        System.out.println("buf.readableBytes() :" + buf.readableBytes());//可读取的字节大小

        System.out.println("buf.isWritable()    :" + buf.isWritable());//是否可写
        System.out.println("buf.writerIndex()   :" + buf.writerIndex());//写的位置索引
        System.out.println("buf.writableBytes() :" + buf.writableBytes());//可写的字节大小

        System.out.println("buf.capacity()      :" + buf.capacity());// 动态真实分配的大小
        System.out.println("buf.maxCapacity()   :" + buf.maxCapacity());// 最大容量大小

        System.out.println("buf.isDirect()      :" + buf.isDirect());// true堆外/false堆内

        System.out.println("-----------------------------------------------------");

    }


    @Test
    public void loopExecutor() throws Exception {
        // group 线程池
        NioEventLoopGroup selector = new NioEventLoopGroup(1);
        selector.execute(() -> {
            for (; ; ) {
                try {
                    System.out.println("hello world 001");
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        selector.execute(() -> {
            for (; ; ) {
                try {
                    System.out.println("hello world 002");
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        System.in.read();
    }

    /**
     * 客户端
     * 连接别人:
     * 1. 主动发送数据
     * 2. 别人什么时候给我发? 基于event
     */
    @Test
    public void clientMode() throws Exception {
        NioEventLoopGroup thread = new NioEventLoopGroup(1);

        // 客户端模式:
        NioSocketChannel client = new NioSocketChannel();
        thread.register(client); // 客户端注册到loop

        // 响应式
        ChannelPipeline p = client.pipeline();
        p.addLast(new MyInHandler());
        // reactor 异步的特征
        ChannelFuture future = client.connect(new InetSocketAddress("192.168.233.128", 9090));
        future = future.sync();
        ByteBuf buf = Unpooled.copiedBuffer("hello server".getBytes(StandardCharsets.UTF_8));
        ChannelFuture sendFuture = client.writeAndFlush(buf);
        sendFuture.sync();


        future.channel().closeFuture().sync();// 服务端不关, 代码会在此处阻塞
        System.out.println("client over...");
    }


    /**
     * 用户自己实现, 你能说让用户放弃属性的操作嘛?
     */
    //@ChannelHandler.Sharable//不应该被强压给coder
    public static class MyInHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
            System.out.println("client registed...");
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            System.out.println("client active");
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            ByteBuf buf = (ByteBuf) msg;
            // read一系列的方法: buf的指针会被移动, 所以如果使用ctx.writeAndFlush输出是无效的
//            CharSequence str = buf.readCharSequence(buf.readableBytes(), CharsetUtil.UTF_8);
            // get一系列的方法: buf的指针不会移动,所以buf可以复用
            CharSequence str = buf.getCharSequence(0, buf.readableBytes(), CharsetUtil.UTF_8);
            System.out.println(str.toString());
            ctx.writeAndFlush(buf);
        }
    }


    /**
     * description   服务端模式  <BR>
     *
     * @param :
     * @return
     * @author zhao.song  2021/7/21  14:14
     */
    @Test
    public void serverMode() throws InterruptedException {
        NioEventLoopGroup thread = new NioEventLoopGroup(1);

        NioServerSocketChannel server = new NioServerSocketChannel();
        thread.register(server);
        //指不定什么时候家里来人...响应式
        ChannelPipeline p = server.pipeline();
        p.addLast(new MyAcceptHandler(thread, new ChannelHandlerInitial()));// accept接收客户端,并且注册到selector
        ChannelFuture conFuture = server.bind(new InetSocketAddress(9090));

        conFuture = conFuture.sync();

        conFuture.channel().closeFuture().sync();
        System.out.println("server close...");

    }

    @ChannelHandler.Sharable
    public static class ChannelHandlerInitial extends ChannelInboundHandlerAdapter {

        @Override
        public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
            Channel client = ctx.channel();
            ChannelPipeline p = client.pipeline();
            p.addLast(new MyInHandler());//2,client::pipeline[ChannelHandlerInitial,MyInHandler]
            p.remove(this);
        }

//        @Override
//        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//            System.out.println("haha");
//            super.channelRead(ctx, msg);
//        }
    }


    public static class MyAcceptHandler extends ChannelInboundHandlerAdapter {

        private final EventLoopGroup selector;
        private final ChannelHandler handler;

        public MyAcceptHandler(EventLoopGroup thread, ChannelHandler myInHandler) {
            this.selector = thread;
            this.handler = myInHandler;// ChannelHandlerInitial
        }


        @Override
        public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
            System.out.println("server registed...");
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            System.out.println("server active");
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            // 在服务端接收的是一个客户端
            SocketChannel client = (SocketChannel) msg;// accept 无需调用,框架直接传入
            // 2.响应式的handler
            ChannelPipeline p = client.pipeline();
            p.addLast(handler);// 1,client::pipeline[ChannelHandlerInitial]

            // 1.注册
            selector.register(client);
        }
    }


    /**
     * description   官方netty客户端  <BR>
     *
     * @param :
     * @return
     * @author zhao.song  2021/7/21  17:21
     */
    @Test
    public void nettyClient() throws InterruptedException {
        NioEventLoopGroup group = new NioEventLoopGroup(1);

        Bootstrap bootstrap = new Bootstrap();

        ChannelFuture future = bootstrap
                .group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast(new MyInHandler());
                    }
                }).connect("192.168.233.128", 9090)
                .sync();
        Channel client = future.channel();
        if (future.isSuccess()) {
            System.out.println("connect success...");
        }

        ChannelFuture send = client.writeAndFlush(Unpooled.wrappedBuffer("hello server!".getBytes(StandardCharsets.UTF_8)));
        send.sync();

        client.closeFuture().sync();

    }

    @Test
    public void nettyServer() throws Exception {
        NioEventLoopGroup boss = new NioEventLoopGroup();

        ServerBootstrap bs = new ServerBootstrap();
        ChannelFuture future = bs.group(boss, boss)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast(new MyInHandler());
                    }
                }).bind(9090).sync();

        future.channel().closeFuture().sync();

    }

}
