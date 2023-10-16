package nl.han.ica.datastructures;

public class HANQueue<T> implements IHANQueue<T> {

    private HANLinkedList<T> queue;

    @Override
    public void clear() {
        queue = new HANLinkedList<>();
    }

    @Override
    public boolean isEmpty() {
        return queue.getSize() == 0;
    }

    @Override
    public void enqueue(T value) {
        queue.insert(queue.getSize(), value);
    }

    @Override
    public T dequeue() {
        T value = queue.getFirst();
        queue.removeFirst();
        return value;
    }

    @Override
    public T peek() {
        return queue.getFirst();
    }

    @Override
    public int getSize() {
        return queue.getSize();
    }
}
