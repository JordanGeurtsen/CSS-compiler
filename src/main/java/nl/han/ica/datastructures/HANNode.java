package nl.han.ica.datastructures;

public class HANNode<T> {
    private T value;
    private HANNode<T> next;

    public HANNode(T value) {
        this.value = value;
    }

    public HANNode<T> getNext() {
        return next;
    }

    public void setNext(HANNode<T> next) {
        this.next = next;
    }

    public T getValue() {
        return value;
    }
}
