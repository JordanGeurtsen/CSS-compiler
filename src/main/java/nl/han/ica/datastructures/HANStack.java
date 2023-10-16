package nl.han.ica.datastructures;

public class HANStack<T> implements IHANStack<T> {

    private HANLinkedList<T> stack;

    public HANStack() {
        stack = new HANLinkedList<>();
    }

    @Override
    public void push(T value) {
        stack.addFirst(value);
    }

    @Override
    public T pop() {
        T value = stack.getFirst();
        stack.removeFirst();
        return value;
    }

    @Override
    public T peek() {
        return stack.getFirst();
    }
}
