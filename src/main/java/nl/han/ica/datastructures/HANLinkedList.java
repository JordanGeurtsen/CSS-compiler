package nl.han.ica.datastructures;

public class HANLinkedList<T> implements IHANLinkedList<T>{
    private HANNode<T> head;
    private int size;

    public HANLinkedList() {
        head = new HANNode<>(null);
        size = 0;
    }

    public HANLinkedList(HANNode<T> head) {
        this.head = head;
        size = 1;
    }

    @Override
    public void addFirst(T value) {
        HANNode<T> node = new HANNode<>(value);

        if (head != null) {
            node.setNext(head);
        }
        head = node;
        size++;
    }

    @Override
    public void clear() {
        head = null;
        size = 0;
    }

    @Override
    public void insert(int index, T value) {
        if (index == 0) {
            addFirst(value);
            return;
        }

        HANNode<T> node = new HANNode<>(value);
        HANNode<T> current = head;
        int i = 0;

        while (current != null) {
            if (i == index - 1) {
                node.setNext(current.getNext());
                current.setNext(node);
                return;
            }
            current = current.getNext();
            i++;
        }

        size++;
    }

    @Override
    public void delete(int pos) {
        if (pos == 0) {
            removeFirst();
            return;
        }

        HANNode<T> current = head;
        int i = 0;

        while (current != null) {
            if (i == pos - 1) {
                current.setNext(current.getNext().getNext());
                return;
            }
            current = current.getNext();
            i++;
        }

        size--;
    }

    @Override
    public T get(int pos) {
        HANNode<T> current = head;
        int i = 0;

        while (current != null) {
            if (i == pos) {
                return current.getValue();
            }
            current = current.getNext();
            i++;
        }

        return null;
    }

    @Override
    public void removeFirst() {
        if (head == null) {
            return;
        }

        head = head.getNext();
        size--;
    }

    @Override
    public T getFirst() {
        if (head == null) {
            return null;
        }

        return head.getValue();
    }

    @Override
    public int getSize() {
        return size;
    }
}
