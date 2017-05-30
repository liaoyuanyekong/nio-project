package nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * 客户端
 * Created by WangZhihao on 2017/5/30.
 */
public class TimeClientHandle implements Runnable{
    private String host;
    private int port;
    private Selector selector;
    private SocketChannel socketChannel;
    private volatile boolean stop;

    public TimeClientHandle(String host,int port){
        this.host=host==null?"127.0.0.1":host;
        this.port=port;
        try {
            selector=Selector.open();
            socketChannel=SocketChannel.open();
            socketChannel.configureBlocking(false);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }




    @Override
    public void run() {
        //建立服务端连接
        try {
            doConnect();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }


        while(!stop){
            try {
                selector.select(1000);
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectionKeys.iterator();
                SelectionKey selectionKey=null;
                while(iterator.hasNext()){
                    selectionKey=iterator.next();
                    iterator.remove();
                    try{
                        handleInput(selectionKey);
                    }catch (IOException e){
                        if(selectionKey!=null){
                            selectionKey.cancel();
                            if(selectionKey.channel()!=null)
                                selectionKey.channel().close();
                        }

                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
        if(selector!=null){
            try {
                selector.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private void  handleInput(SelectionKey key) throws IOException{
        if(key.isValid()){
            SocketChannel socketChannel= (SocketChannel) key.channel();
            //与服务端的差别，判断是否连接成功。如果成功的话，从服务端读取数据，
            if(key.isConnectable()){
                if(socketChannel.finishConnect()){
                    socketChannel.register(selector, SelectionKey.OP_READ);
                    //连接建立以后向服务端发送请求
                    dowrite(socketChannel);
                }else{
                    System.exit(1);
                }
            }
            if(key.isReadable()){
                ByteBuffer byteBuffer=ByteBuffer.allocate(1024);
                int readBytes= socketChannel.read(byteBuffer);
                if(readBytes>0){
                    byteBuffer.flip();
                    byte[] bytes=new byte[byteBuffer.remaining()];
                    byteBuffer.get(bytes);
                    String body=new String(bytes,"UTF-8");
                    System.out.println("NOW IS : "+body);
                    this.stop=true;
                }else if(readBytes<0){
                    key.cancel();
                    socketChannel.close();
                }
            }
        }
    }

    private void dowrite(SocketChannel socketChannel) throws IOException {
        byte[] reg="QUERY TIME ORDER".getBytes();
        ByteBuffer byteBuffer=ByteBuffer.allocate(reg.length);
        byteBuffer.put(reg);
        byteBuffer.flip();
        socketChannel.write(byteBuffer);
        if(!byteBuffer.hasRemaining()){
            System.out.println(" send order succeed");
        }
    }

    private void doConnect() throws IOException{
        if(socketChannel.connect(new InetSocketAddress(host,port))){
            socketChannel.register(selector,SelectionKey.OP_READ);
            dowrite(socketChannel);
        }else{
            socketChannel.register(selector,SelectionKey.OP_CONNECT);
        }
    }


}
