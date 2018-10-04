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
                    System.out.println("Thread: " + threadName + ", " + count);
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

    void test01() {
        try {
            int n = 10;
            add(n);
            get(n);
            Thread.sleep(10*600);
            System.out.println(taskQueue.len());
            System.out.println(emptyCount.get());
            System.out.println(taskQueue.len() == emptyCount.get());
//            Thread.sleep(10*600);
//            System.out.println(taskQueue.len());
        } catch (InterruptedException e) {

        }
    }

}
