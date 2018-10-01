import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArraySet;

public class TaskQueue {
    // 用一个链表来表示待执行的任务队列
    private class Node {
        Task task;
        Node next;
        Node(Task task) {
            this.task = task;
        }
    }
    private Queue<Task> queue;

    // 用一个集合来表示运行中的任务集合
    private Set<Task> set;

    // 表头和表尾
    private Node queueHead;
    private Node queueTail;

    // 当前状态， 1表示正常， 0表示已经停止
    private int state = 1;

    TaskQueue() {
//        queue = new ConcurrentLinkedQueue<>();
        set = new CopyOnWriteArraySet<>();
        queueHead = null;
        queueTail = null;
    }

    public void add(Task task) {
        if (state == 1) {
            synchronized (this) {
                Node newNode = new Node(task);
                if (queueHead == null) {
                    queueTail = newNode;
                    queueHead = newNode;
                } else {
                    queueTail.next = newNode;
                    queueTail = newNode;
                }
            }
        }
    }

    public int len() {
        Node node = queueHead;
        int size = 0;
        while (node != null) {
            size++;
            node = node.next;
        }
        return size;
    }

    public Task get() {
        if (state == 1) {
            synchronized (this) {
                if (queueHead == null) {
                    return null;
                } else {
                    Task task = queueHead.task;
                    queueHead = queueHead.next;
                    set.add(task);
                    return task;
                }
            }
        } else {
            return null;
        }
    }

    public void done(Task task) {
        set.remove(task);
    }

    public void shutdown() {
        state = 0;
    }

    public boolean isClosed() {
        return state == 0;
    }
}
