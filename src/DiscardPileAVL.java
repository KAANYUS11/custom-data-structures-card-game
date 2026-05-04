
public class DiscardPileAVL {
    public DiscardNode root;
    int discardSize;
    public DiscardPileAVL() {
        this.root = null;
    }

    // INSERT FUNCTION
    public void insertCard(Card card) {
        this.root = insertCard(card, this.root);
        discardSize++;
    }

    private DiscardNode insertCard(Card card, DiscardNode node) {
        int cardMissingHealth = card.getMissingHealth();
        if (node == null) {
            DiscardNode node1 = new DiscardNode();
            node1.addCard(card);
            return node1;
        }

        int cmp = cardMissingHealth - node.getKeyMissingHealth();
        if (cmp < 0) {
            node.left = insertCard(card, node.left);
        } else if (cmp > 0) {
            node.right = insertCard(card, node.right);
        } else {
            node.addCard(card);
            return node; // no structure change when adding duplicate
        }
        updateHeight(node);
        return rebalance(node);
    }

    // DELETE FUNCTION (lazy pointer, physical removal when empty)
    public void deleteCard(Card card) {
        this.root = deleteCard(this.root, card);
        discardSize--;  
    }
    private DiscardNode deleteCard(DiscardNode node, Card card) {
        if (node == null) {
            return null;
        }
        int cmp = card.getMissingHealth() - node.getKeyMissingHealth();
        if (cmp < 0) {
            node.left = deleteCard(node.left, card);
        } else if (cmp > 0) {
            node.right = deleteCard(node.right, card);
        } else {
            // lazy pointer delete
            if (node.removeCard()) {
                // node has no more cards; remove from AVL structure
                if (node.left == null) {
                    node = node.right;
                }else if (node.right == null){
                    node = node.left; 
                }else {
                    DiscardNode successor = findMin(node.right);
                    node.cards = successor.cards;
                    node.right = deleteEntireNode(successor.getKeyMissingHealth(), node.right );
                }
            }
        }
        updateHeight(node);
        return rebalance(node);
    }

    private DiscardNode deleteEntireNode(int missinghealth, DiscardNode node) {
        if (node == null) {
            return null;
        }
        int cmp = missinghealth - node.getKeyMissingHealth();
        if (cmp < 0) {
            node.left = deleteEntireNode(missinghealth, node.left);
        } else if (cmp > 0) {
            node.right = deleteEntireNode( missinghealth, node.right);
        } else {
            // remove this node entirely
            if (node.left == null) {
                node = node.right;
            }else if (node.right == null) {
                node = node.left;
            } else {
                DiscardNode successor = findMin(node.right);
                node.cards = successor.cards;
                node.right = deleteEntireNode(successor.getKeyMissingHealth(), node.right);
            }
        }
        if (node == null) return null;
        updateHeight(node);
        return rebalance(node);
    }

    private DiscardNode findMin(DiscardNode node) {
        DiscardNode cur = node;
        while (cur != null && cur.left != null) cur = cur.left;
        return cur;
    }


    // FIND FUNCTIONS 
    


    // AVL BALANCING FUNCTIONS
    private int height(DiscardNode n) { 
        if (n == null) {
            return 0;
        } else {
            return n.height;
        }
    }

    private void updateHeight(DiscardNode n) { 
        n.height = 1 + Math.max(height(n.left), height(n.right)); 
    }
    
    private int balanceFactor(DiscardNode n) { 
        if (n == null) {
            return 0;
        } else {
            return height(n.left) - height(n.right);
        }
     }

    private DiscardNode rotateRight(DiscardNode y) {
        DiscardNode x = y.left;
        DiscardNode T2 = x.right;
        x.right = y;
        y.left = T2;
        updateHeight(y);
        updateHeight(x);
        return x;
    }

    private DiscardNode rotateLeft(DiscardNode y) {
        DiscardNode x = y.right;
        DiscardNode T2 = x.left;
        x.left = y;
        y.right = T2;
        updateHeight(y);
        updateHeight(x);
        return x;
    }

    private DiscardNode rebalance(DiscardNode node) {
        int bf = balanceFactor(node);
        if (bf > 1) {
            if (balanceFactor(node.left) < 0) {
                node.left = rotateLeft(node.left); // left right  inbalance
            }
            return rotateRight(node); //left left inbalance
        }
        if (bf < -1) {
            if (balanceFactor(node.right) > 0) {
                node.right = rotateRight(node.right); // right left  inbalance
            }
            return rotateLeft(node); // right right  inbalance
        }
        return node;
    }

    // Finds the card with the largest missingHealth <= healPoints (best full revive)
    public Card findMaxFullyRevivable(int healPoints) {
        return findMaxFullyRevivable(this.root, healPoints, null);
    }
    private Card findMaxFullyRevivable(DiscardNode node, int healPoints, Card currentBest) {
        if (node == null) return currentBest;
        int nodeMissing = node.getKeyMissingHealth();
        if (nodeMissing <= healPoints) {
            Card candidate = node.getCardAtPointer();
            if (candidate != null) {
                if (currentBest == null || nodeMissing > currentBest.getMissingHealth()) {
                    currentBest = candidate;
                }
            }
            // Larger keys might also qualify -> search right subtree
            return findMaxFullyRevivable(node.right, healPoints, currentBest);
        } else {
            // only left subtree can have keys <= healPoints
            return findMaxFullyRevivable(node.left, healPoints, currentBest);
        }
    }

    // Finds the card with the smallest missingHealth > healPoints (partial revive)
    public Card findMinPartiallyRevivable() {
        DiscardNode cur = this.root;
        if (cur == null) return null;
        while (cur.left != null) cur = cur.left;
        return cur.getCardAtPointer(); // earliest-discarded card in that node
    }

}
