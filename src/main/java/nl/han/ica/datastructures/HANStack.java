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
        if (stack.getSize() == 0) {
            return null;
        }

        T value = stack.getFirst();
        stack.removeFirst();
        return value;
    }

    @Override
    public T peek() {
        if (stack.getSize() == 0) {
            return null;
        }

        return stack.getFirst();
    }
}
