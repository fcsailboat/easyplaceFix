package org.uiop.easyplacefix.until;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import org.uiop.easyplacefix.EasyPlaceFix;

import java.util.Map;
import java.util.concurrent.*;

public class PlayerBlockAction {
    // 创建单线程线程池
    static ExecutorService playerActionThread = Executors.newSingleThreadExecutor();
    static ExecutorService blockActionThread = Executors.newSingleThreadExecutor();

    public static class openScreenAction {
        public static final BlockingQueue<Runnable> taskQueue = new LinkedBlockingQueue<>();
        static Future<?> f = null;

        public static boolean run(int syncId) {
            Runnable runnable = taskQueue.poll();
            if (runnable != null) {
                EasyPlaceFix.screenId = syncId;
                f = playerActionThread.submit(runnable);
            } else return true;

            return false;
        }

        public static boolean waitAction() {
            if (f == null) return true;
            if (f.isDone()) {
                f = null;
                return true;
            }
            return false;
        }

    }

    public static class openSignEditorAction {
        public static final BlockingQueue<Runnable> taskQueue = new LinkedBlockingQueue<>();

        public static boolean run() {
            Runnable runnable = taskQueue.poll();
            if (runnable != null) {
                playerActionThread.submit(runnable);
            } else return true;

            return false;
        }

    }

    public static class useItemOnAction {
        public static final BlockingQueue<Runnable> taskQueue = new LinkedBlockingQueue<>();
        public static float yawLock, pitchLock = 0;
        public static boolean notChangPlayerLook = false;
        public static boolean modifyBoolean = false;
        // 初始化线程安全的Set
        public static Map<BlockPos, Long> concurrentMap = new ConcurrentHashMap<>();
        public static BlockState pistonBlockState = null;
        public static Semaphore semaphore = new Semaphore(0); // 初始许可数为 0

        public static void run() {
            Runnable runnable = taskQueue.poll();
            if (runnable != null) {
                blockActionThread.submit(runnable);
            }
        }

        public static void upDateBlock(BlockPos pos) {
            if (concurrentMap.containsKey(pos)) {
                if (concurrentMap.get(pos) != 0) {
                    notChangPlayerLook = false;
                    concurrentMap.remove(pos);
                    PlayerRotationAction.restRotation();
                    semaphore.release();
                    concurrentMap.forEach((k, v) -> {
                        if ((System.currentTimeMillis() - v) > 300) {
                            concurrentMap.remove(k);
                        }
                    });
                }


            }

        }
    }}
