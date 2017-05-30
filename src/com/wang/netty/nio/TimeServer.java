package com.wang.netty.nio;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;



/**
 * 使用netty框架实现Nio开发
 * Created by WangZhihao on 2017/5/30.
 */
public class TimeServer {

    public void bind(int port) throws Exception{
        //配置服务端NIO线程组
        EventLoopGroup bossGroup =new NioEventLoopGroup();
        EventLoopGroup workerGroup =new NioEventLoopGroup();
        try{
            //服务端NIO的启动类，目的降低编写Nio服务端启动类的开发复杂度
            ServerBootstrap bootstrap=new ServerBootstrap();
            bootstrap.group(bossGroup,workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG,1024)
                    .childHandler(new ChildChannelHandler());
            //绑定接口，同步等待成功
            ChannelFuture channelFuture= bootstrap.bind(port).sync();
            //阻塞作用，等待服务端监听接口关闭后main方法才能退出，和NIO的while作用类似
            channelFuture.channel().close().sync();
        }finally {
            //优雅的退出，释放资源
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }

    }
    private class ChildChannelHandler extends ChannelInitializer<SocketChannel>{

        @Override
        protected void initChannel(SocketChannel socketChannel) throws Exception {
            socketChannel.pipeline().addLast(new TimeServerHandler());
        }
    }

    /**
     * main
     * @param args
     */
    public static void main(String[] args) throws Exception {
         int port=8080;
         if(args!=null &&args.length>0){
             try{
                port=Integer.valueOf(port);
             }catch (Exception e){

             }
         }
        new TimeServer().bind(port);

    }
}
