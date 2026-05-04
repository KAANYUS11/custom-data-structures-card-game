public class DeckOuterNode {
    DeckInnerAVL innerDeck;
    int height = 1; // Leaf height is 1
    int healthKey;

    DeckOuterNode left;
    DeckOuterNode right;

    public DeckOuterNode(int healthKey) {
        this.healthKey = healthKey;
        this.innerDeck = new DeckInnerAVL();
        this.left = null;
        this.right = null;
    }
    public DeckOuterNode() {
        this(0); 
    }

    public void addCard(Card card) {
        innerDeck.insertCard(card);
    }

    public void removeCard(Card card) {
        // ensure inner AVL root is updated with returned subtree
        innerDeck.deleteCard(card);
    }

    public Card findMinKillerCard(int strangerHealth) {
        return innerDeck.findMinDamagingKillerCard(strangerHealth);
    }

    public int getInnerDeckHealth() {
        Card c = innerDeck.root.getCardAtPointer();
        if (c == null) {
            return 0;
        } else {
            return c.healthCurrent;
        }
    }

}
