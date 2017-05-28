package nio;

import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;

/**
 * jdk with 1.8
 * Created by WangZhihao on 2017/5/28.
 */
public class MultiplexerTimeServer  implements  Runnable{

    private Selector selector;

    private ServerSocketChannel serverSocketChannel;

    private volatile boolean  stop;







    @Override
    public void run() {

    }
}
