package com.wang.netty.nio;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.Future;

/**
 * 客户端代码
 * Created by WangZhihao on 2017/5/31.
 */
public class TimeClient {

    public void connect(int port, String host)throws Exception{
        EventLoopGroup group =new NioEventLoopGroup();
        try {
            Bootstrap bootstrap=new Bootstrap();
            bootstrap.group(group).channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY,true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new TimeClientHandler());
                        }
                    });
            ChannelFuture channelFuture= bootstrap.connect(host,port).sync();
            channelFuture.channel().closeFuture().sync();

        }finally {
            group.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        int port=8080;
        new TimeClient().connect(port,"127.0.0.1");
    }






}
