import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class TaskQueue {

    // 用一个链表来表示[待执行]的任务队列
    private class Node {
        Task task;
        Node next;
        Node(Task task) {
            this.task = task;
        }
    }

    // 表头和表尾，其中 queueHead.task 为 null，第一个 task 实际为 queueHead.next.task
    private Node queueHead;
    private Node queueTail;

    // 用一个集合来表示[运行中]的任务集合
    private Set<Task> set;

    // queue 大小
    private AtomicInteger count = new AtomicInteger();

    // 读锁和写锁
    private final ReentrantLock takeLock = new ReentrantLock();
    private final ReentrantLock putLock = new ReentrantLock();

    // 当前状态， 1表示正常， 0表示已经停止
    private int state = 1;

    TaskQueue() {
        set = Collections.synchronizedSet(new HashSet<>());
        queueHead = new Node(null);
        queueTail = queueHead;
    }

    public boolean add(Task task) {
        try {
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
                // 入队尾
                queueTail.next = new Node(task);
                queueTail = queueTail.next;
                // 加长度
                count.getAndIncrement();
            } finally {
                putLock.unlock();
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Task get() {
        try {
            Task task;
            final AtomicInteger count = this.count;
            final ReentrantLock takeLock = this.takeLock;
            takeLock.lockInterruptibly();
            try {
                if (queueHead.next != null) {
                    // 非空
                    // 出队头
                    Node h = queueHead;
                    Node first = h.next;
                    queueHead = first;
                    task = first.task;
                    first.task = null;
                    // 减长度
                    count.getAndDecrement();
                } else {
                    // 空
                    task = null;
                }
            } catch (Exception e) {
                shutdown();
                task = null;
            } finally {
                takeLock.unlock();
            }
            return task;
        } catch (Exception e) {
            return null;
        }
    }

    public int len() {
        return count.get() + set.size();
    }

    public void done(Task task) {
        try {
            boolean isRemoved = set.remove(task);
            // 异常处理，待移除的task不在running状态
            if (!isRemoved) {
                throw new Exception();
            }
        } catch (Exception e) {
            // 处理异常
        }
    }

    public void shutdown() {
        state = 0;
    }

    public boolean isClosed() {
        return state == 0;
    }
}
