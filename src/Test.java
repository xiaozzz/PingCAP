import java.util.concurrent.atomic.AtomicInteger;

public class Test {
    TaskQueue taskQueue;
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
                    Thread.sleep(101);
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
                    Thread.sleep(100);
                    // 模拟处理task，处理完后执行回调
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
    boolean test01() {
        boolean isPass = false;
        try {
            System.out.println("Starting test01...");
            taskQueue = new TaskQueue();
            int n = 100;
            add(n);
            get(n);
            Thread.sleep((n+1)*100);
            System.out.println("taskQueue len: " + taskQueue.len());
            System.out.println("Empty times:" + emptyCount.get());
            isPass = taskQueue.len() == emptyCount.get();
            System.out.println("Test01 isPass:" + isPass);
        } catch (InterruptedException e) {

        }
        return isPass;
    }

    // 开启 n 个线程同时 add 共进行 10 轮，最后判断 queue size 是否 为 10 * n
    boolean test02() {
        boolean isPass = false;
        try {
            System.out.println("Starting test02...");
            taskQueue = new TaskQueue();
            int n = 20;
            add(n);
            Thread.sleep((n+1)*100);
            System.out.println("taskQueue len: " + taskQueue.len());
            isPass = taskQueue.len() == 10 * n;
            System.out.println("Test 02 isPass:" + isPass);
        } catch (InterruptedException e) {

        }
        return isPass;
    }

    // 在 test02 基础上， 开启 n 个线程同时 get 共进行 10 轮，最后判断 queue size 是否 为 0
    boolean test03() {
        boolean isPass = false;
        try {
            System.out.println("Starting test03...");
            int n = 20;
            get(n);
            Thread.sleep((n+1)*100);
            System.out.println("taskQueue len: " + taskQueue.len());
            isPass = taskQueue.len() == 0;
            System.out.println("Test 03 isPass:" + isPass);
        } catch (InterruptedException e) {

        }
        return isPass;
    }

    boolean test04() {
        boolean isPass = false;
        taskQueue = new TaskQueue();
        // 添加一个 task
        isPass = taskQueue.add(new Task(0));
        // 关闭
        taskQueue.shutdown();
        // 关闭后不允许加
        isPass = isPass && !taskQueue.add(new Task(0));
        // 长度仍为1
        isPass = isPass && taskQueue.len() == 1;
        System.out.println("Test 04 isPass:" + isPass);
        return isPass;
    }

    void test() {
        if (test01() && test02() && test03() && test04()) {
            System.out.println("Pass all tests");
        }
    }
}
