import java.util.ArrayList;

public class DiscardNode {
    public ArrayList<Card> cards;
    public int height;
    private int pointer;
    public DiscardNode left;
    public DiscardNode right; // For AVL usage if needed

    public DiscardNode() {
        this.left = null;
        this.right = null;
        this.cards = new ArrayList<Card>();
        this.pointer = 0;
    }

    public void addCard(Card c) {
        if (this.cards == null) this.cards = new ArrayList<>();
        this.cards.add(c);
    }

    // Lazy FIFO removes one card; returns true if node is logically empty
    public boolean removeCard() {
        this.pointer++;
        if (this.cards == null || this.pointer > this.cards.size()) {
            return true; // inner node is empty - delete node
        } else {
            return false;
        }
    }

    public boolean isEmpty() {
        return this.cards == null || this.cards.isEmpty();
    }
    // Returns the first logically alive card, or null if empty
    public Card getCardAtPointer() {
        if (this.cards == null || this.cards.isEmpty()) return null;
        return this.cards.get(0);
    }

    public int getKeyMissingHealth() {
        Card c = getCardAtPointer();
        return c.getMissingHealth();
    }
}
