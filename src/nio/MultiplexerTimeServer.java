package nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

/**
 * jdk with 1.8
 * 使用nio进行编程服务端代码
 * Created by WangZhihao on 2017/5/28.
 */
public class MultiplexerTimeServer  implements  Runnable{

    private Selector selector;

    private ServerSocketChannel serverSocketChannel;

    private volatile boolean  stop;

    public  MultiplexerTimeServer(int port){
        try {
            //使用工厂类初始化selector 和serverSocketChannel两个对象
            selector=Selector.open();
            serverSocketChannel=ServerSocketChannel.open();
            //设置阻塞模式为非阻塞
            serverSocketChannel.configureBlocking(false);
            //设置端口号和 backlog(The maximum number of pending connections(最大等待连接数)) 的大小
            serverSocketChannel.bind(new InetSocketAddress(port),1024);
            //将channel注册到selector
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println(" This server is running in port "+port);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * 终止线程
     */
    public void stop(){
        this.stop=true;
    }

    @Override
    public void run() {
        while(!stop){
            try {
                //每一秒轮询一次 获取就绪的channel
                selector.select(1000);
                //获得此时就绪含有该channel的selectionKeys(管道集合)
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectionKeys.iterator();
                SelectionKey selectionKey=null;
                //取出管道中内容放入缓存并读取
                while(iterator.hasNext()){
                    selectionKey= iterator.next();
                    //从集合中移除next返回的元素，且此方法只能调用一次
                    iterator.remove();
                    //处理数据
                    try {
                        handleInput(selectionKey);
                    }catch (Exception e){
                        if(selectionKey!=null){
                            selectionKey.cancel();
                            if(selectionKey.channel()!=null){
                                selectionKey.channel().close();
                            }
                        }
                    }

                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //关闭多路复用器，释放资源
        if(selector!=null)
            try{
                selector.close();
            }catch(IOException e){
            }

    }

    private void handleInput(SelectionKey key) throws IOException{
        if(key.isValid()){
            //Tests whether this key's channel is ready to accept a new socket connection
            if(key.isAcceptable()){
                //accept the new connection
                ServerSocketChannel serverSocketChannel= (ServerSocketChannel) key.channel();
                //相当于三次握手，客户端服务器连接正式建立
                SocketChannel socketChannel=serverSocketChannel.accept();
                socketChannel.configureBlocking(false);
                //将channel注册到selector
                socketChannel.register(selector,SelectionKey.OP_READ);
            }
            if(key.isReadable()){
                //读取channel中数据
                //从缓存中取出数据
                SocketChannel socketChannel= (SocketChannel) key.channel();
                //声明缓冲区，将数据从channel读入缓冲区
                ByteBuffer byteBuffer=ByteBuffer.allocate(1024);
                //Reads a sequence of bytes from this channel into the given buffer.
                int readBytes= socketChannel.read(byteBuffer);
                //大于0 读到了字节;等于0 没读取到字节，不做处理正常情况;返回-1 链路已经关闭释放资源
                if(readBytes>0){
                    /*Flips this buffer.  The limit is set to the current position and then
                    * the position is set to zero.  If the mark is defined then it is discarded
                    */
                    byteBuffer.flip();
                    //Returns the number of elements between the current position and the limit.
                    byte[] bytes =new byte[byteBuffer.remaining()];
                    //This method transfers bytes from this buffer into the given  destination array
                    byteBuffer.get(bytes);
                    String body =new String(bytes,"UTF-8");
                    System.out.println("this time server receive order is "+body);
                    String currentTime="QUERY TIME ORDER".equalsIgnoreCase(body)?
                            new Date(System.currentTimeMillis()).toString():"BAD ORDER";
                    //消息写回客户端
                    doWrite(socketChannel,body);
                }else if(readBytes<0){
                    key.channel();
                    socketChannel.close();
                }else ;
            }
        }
    }

    /**
     * 反馈消息到客户端的方法
     * @param channel
     * @param response
     */
    private void doWrite(SocketChannel channel,String response) throws IOException {
        if(response!=null && response.trim().length()>0){
            //创建缓冲区
            byte[] bytes=response.getBytes();
            ByteBuffer byteBuffer=ByteBuffer.allocate(bytes.length);
            //数据放入缓冲区
            byteBuffer.put(bytes);
            //添加缓冲区数据到管道
            byteBuffer.flip();
            channel.write(byteBuffer);
        }
    }


}
