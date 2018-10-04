import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class TaskQueue {
    // 用一个链表来表示待执行的任务队列
    private class Node {
        Task task;
        Node next;
        Node(Task task) {
            this.task = task;
        }
    }

    // 用一个集合来表示运行中的任务集合
    private Set<Task> set;

    // 表头和表尾
    private Node queueHead;
    private Node queueTail;

    // queue size
    private AtomicInteger count = new AtomicInteger();

    // 读锁和写锁
    private final ReentrantLock takeLock = new ReentrantLock();
    private final ReentrantLock putLock = new ReentrantLock();

    // 当前状态， 1表示正常， 0表示已经停止
    private int state = 1;

    TaskQueue() {
        set = new CopyOnWriteArraySet<>();
        queueHead = queueTail = new Node(null);
    }

    public boolean add(Task task)
            throws InterruptedException {

        if (isClosed()) {
            return false;
        }
        if (task == null) {
            shutdown();
            throw new NullPointerException();
        }
        final ReentrantLock putLock = this.putLock;
        final AtomicInteger count = this.count;
        putLock.lockInterruptibly();
        try {
            queueTail = queueTail.next = new Node(task);
            count.getAndIncrement();
        } finally {
            putLock.unlock();
        }
        return true;
    }

    public Task get()
            throws InterruptedException {
        Task task = null;
        final AtomicInteger count = this.count;
        final ReentrantLock takeLock = this.takeLock;
        takeLock.lockInterruptibly();
        try {
            if (queueHead.next != null) {
                Node h = queueHead;
                Node first = h.next;
                queueHead = first;
                task = first.task;
                first.task = null;
                count.getAndDecrement();
            }
        } finally {
            takeLock.unlock();
        }
        return task;
    }

    public int len() {
        return count.get() + set.size();
    }

    public void done(Task task) {
        if (set.contains(task)) {
            set.remove(task);
        }
    }

    public void shutdown() {
        state = 0;
    }

    public boolean isClosed() {
        return state == 0;
    }
}
