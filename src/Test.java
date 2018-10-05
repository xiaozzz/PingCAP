import java.util.concurrent.atomic.AtomicInteger;

public class Test {
    TaskQueue taskQueue = null;
    Test() {
        taskQueue = new TaskQueue();
    }
    private AtomicInteger count = new AtomicInteger();
    private AtomicInteger emptyCount = new AtomicInteger();


    class TaskQueueAdd implements Runnable {
        private Thread t;
        private String threadName;

        TaskQueueAdd(String name) {
            threadName = name;
        }

        public void run() {
            try {
                for(int i = 0; i < 10; i++) {
                    Task task = new Task(count.get());
                    count.getAndIncrement();
                    taskQueue.add(task);
//                    System.out.println("Thread: " + threadName + ", " + count);
                    Thread.sleep(500);
                }
            }catch (InterruptedException e) {
                System.out.println("Thread " +  threadName + " interrupted.");
            }
        }

        public void start () {
            if (t == null) {
                t = new Thread (this, threadName);
                t.start ();
            }
        }
    }

    class TaskQueueGet implements Runnable {
        private Thread t;
        private String threadName;

        TaskQueueGet(String name) {
            threadName = name;
        }

        public void run() {
            try {
                for(int i = 0; i < 10; i++) {
                    Task task = taskQueue.get();
                    Thread.sleep(500);
                    if (task != null) {
                        taskQueue.done(task);
                    } else {
                        emptyCount.getAndIncrement();
                        System.out.println("Empty queue");
                    }
                }
            }catch (InterruptedException e) {
                System.out.println("Thread " +  threadName + " interrupted.");
            }
        }

        public void start () {
            if (t == null) {
                t = new Thread (this, threadName);
                t.start ();
            }
        }
    }

    void add(int n) {
        for (int i = 0; i < n; i++) {
            TaskQueueAdd R1 = new TaskQueueAdd( "Thread-add");
            R1.start();
        }
    }

    void get(int n) {
        for (int i = 0; i < n; i++) {
            TaskQueueGet R2 = new TaskQueueGet( "Thread-get");
            R2.start();
        }
    }

    // 开启 n 个线程同时 add 和 get，共进行 10 轮，最后判断 get empty数量和最后 queue size 是否一致
    void test01() {
        try {
            int n = 20;
            add(n);
            get(n);
            Thread.sleep((n+1)*500);
            System.out.println("taskQueue len: " + taskQueue.len());
            System.out.println("Empty times:" + emptyCount.get());
            System.out.println("isPass:" + (taskQueue.len() == emptyCount.get() ? "true" : "false"));
        } catch (InterruptedException e) {

        }
    }

}
