package nio;

/**
 * Created by WangZhihao on 2017/5/30.
 */
public class TimeClient {

    public static void main(String[] args) {
        int port=8080;
        if(args!=null && args.length>0){
            try{
                port=Integer.parseInt(args[0]);
            }catch(Exception e){

            }
        }
        new Thread(new TimeClientHandle(null,port),"TimeClient-001").start();
    }
}
