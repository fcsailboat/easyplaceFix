package org.uiop.easyplacefix.until;

import org.uiop.easyplacefix.EasyPlaceFix;

import java.util.concurrent.*;

public class PlayerBlockAction {
    // 创建单线程线程池
    static ExecutorService playerActionThread = Executors.newSingleThreadExecutor();
    public static class openScreenAction {
        public static final BlockingQueue<Runnable> taskQueue = new LinkedBlockingQueue<>();
        static Future<?>  f=null;
        public static boolean run(int syncId) {
            Runnable runnable = taskQueue.poll();
            if (runnable!=null){
                EasyPlaceFix.screenId =syncId;
              f= playerActionThread.submit(runnable);
            }else return true;

            return false;
        }
        public static boolean waitAction(){
            if (f==null)return true;
            if (f.isDone()){
                f=null;
                return true;
            }
            return false;
        }

    }
    public static class openSignEditorAction {
        public static final BlockingQueue<Runnable> taskQueue = new LinkedBlockingQueue<>();
        public static boolean run(){
            Runnable runnable = taskQueue.poll();
            if (runnable!=null){
               playerActionThread.submit(runnable);
            }else return true;

            return false;
        }

    }
}
