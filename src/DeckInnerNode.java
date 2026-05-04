import java.util.ArrayList;

public class DeckInnerNode {
    ArrayList<Card> cards = new ArrayList<Card>();
    // AVL bookkeeping
    public int height = 1; // leaf height = 1
    private int pointer = 0;
    public DeckInnerNode left;
    public DeckInnerNode right;

    public DeckInnerNode() {
        this.left = null;
        this.right = null;
        this.cards = new java.util.ArrayList<>();
        this.pointer = 0;
    }

    public void addCard(Card card) {
        // recover if someone set cards to null, and reset pointer on empty
        if (this.cards == null) {
            this.cards = new java.util.ArrayList<>();
            this.pointer = 0;
        }
        if (this.cards.isEmpty()) {
            this.pointer = 0;
        }
        this.cards.add(card);
    }   

    // returns true if node became empty after removal
    public boolean removeCard() {
        // advance logical pointer (FIFO over duplicates)
        this.pointer++;
        // when pointer reaches size, node is empty
        if (this.cards == null || this.pointer >= this.cards.size()) {
            return true; // inner node is empty - delete node
        } else {
            return false; 
        }
    }
    
    public Card getCardAtPointer() { // first existing card
        if (this.cards == null) return null;
        if (pointer >= 0 && pointer < cards.size()) {
            return cards.get(pointer);
        } else {
            return null;
        }
    }

    public int getKeyAttack() {
        Card c = getCardAtPointer();
        return (c == null) ? 0 : c.attackCurrent;
    }
}