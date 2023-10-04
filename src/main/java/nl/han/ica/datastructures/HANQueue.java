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
        if (queue.getSize() == 0) {
            return null;
        }

        T value = queue.getFirst();
        queue.removeFirst();
        return value;
    }

    @Override
    public T peek() {
        if (queue.getSize() == 0) {
            return null;
        }

        return queue.getFirst();
    }

    @Override
    public int getSize() {
        return queue.getSize();
    }
}
