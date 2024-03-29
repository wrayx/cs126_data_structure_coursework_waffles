package uk.ac.warwick.cs126.structures;

public class KeyValuePairLinkedList<K extends Comparable<K>, V> {

    protected ListElement<KeyValuePair<K, V>> head;
    protected int size;

    public KeyValuePairLinkedList() {
        head = null;
        size = 0;
    }

    
    /** 
     * @param key
     * @param value
     */
    public void add(K key, V value) {
        this.add(new KeyValuePair<>(key, value));
    }

    
    /** 
     * @param kvp
     */
    public void add(KeyValuePair<K, V> kvp) {
        ListElement<KeyValuePair<K, V>> new_element =
                new ListElement<>(kvp);
        new_element.setNext(head);
        head = new_element;
        size++;
    }

    
    /** 
     * @return int
     */
    public int size() {
        return size;
    }

    
    /** 
     * @return ListElement<KeyValuePair<K, V>>
     */
    public ListElement<KeyValuePair<K, V>> getHead() {
        return head;
    }

    
    /** 
     * @param key
     * @return KeyValuePair<K, V>
     */
    public KeyValuePair<K, V> get(K key) {
        ListElement<KeyValuePair<K, V>> temp = head;

        while (temp != null) {
            if (temp.getValue().getKey().equals(key)) {
                return temp.getValue();
            }

            temp = temp.getNext();
        }

        return null;
    }
}
