public class DeckInnerAVL {

    DeckInnerNode root ; 
    
    public DeckInnerAVL() {
        this.root = null;
    }

    // INSERT FUNCTIONS
    public void insertCard(Card card) {
        this.root = insertCard(card, this.root);
    }

    private DeckInnerNode insertCard(Card card, DeckInnerNode node) {
        if (node == null) {
            DeckInnerNode node1 = new DeckInnerNode();
            node1.addCard(card);
            return node1;
        }

        int cmp = card.attackCurrent - node.getKeyAttack();
        if (cmp < 0) {
            node.left = insertCard(card, node.left);
        } else if (cmp > 0) {
            node.right = insertCard(card, node.right);
        } else {
            node.addCard(card);
            return node; // no structure change when adding duplicate
        }

        updateHeight(node);
        return rebalance(node); //return node
    }

    // DELETE FUNCTIONS
    public void deleteCard(Card card) {
        this.root = deleteCard(this.root, card);
    }
    
    // actual deletion function 
    private DeckInnerNode deleteCard(DeckInnerNode node, Card card) {
        if (node == null) {
            return null;
        }
        int cmp = card.attackCurrent - node.getKeyAttack();
        if (cmp < 0) {
            node.left = deleteCard(node.left, card);
        } else if (cmp > 0) {
            node.right = deleteCard(node.right, card);
        } else {
            // same attack key; remove one instance. If removeCard returns true, the node is empty
            if (node.removeCard()) {
                // node has no more cards; remove from AVL structure
                if (node.left == null) {
                    node = node.right;
                }else if (node.right == null){
                    node = node.left; 
                }else {
                    DeckInnerNode successor = findMin(node.right);
                    node.cards = successor.cards;
                    node.right = deleteEntireNode(successor.getKeyAttack(), node.right );
                }
            }
        }
        if (node == null) return null;
        updateHeight(node);
        return rebalance(node);
    }

    // classical node deletion process
    private DeckInnerNode deleteEntireNode(int attackKey, DeckInnerNode node) {
        if (node == null) {
            return null;
        }
        int cmp = attackKey - node.getKeyAttack();
        if (cmp < 0) {
            node.left = deleteEntireNode(attackKey, node.left);
        } else if (cmp > 0) {
            node.right = deleteEntireNode( attackKey, node.right);
        } else {
            // remove this node entirely
            if (node.left == null) {
                node = node.right;
            }else if (node.right == null) {
                node = node.left;
            } else {
                DeckInnerNode successor = findMin(node.right);
                node.cards = successor.cards;
                node.right = deleteEntireNode(successor.getKeyAttack(), node.right);
            }
        }
        if (node == null) return null;
        updateHeight(node);
        return rebalance(node);
    }

    private DeckInnerNode findMin(DeckInnerNode node) {
        DeckInnerNode cur = node;
        while (cur != null && cur.left != null) cur = cur.left;
        return cur;
    }



    // FIND FUNCTIONS
    //killers (1-3)
    public Card findMaxDamagingCard() {
        DeckInnerNode n = root;
        if (n == null) return null;
        while (n.right != null) n = n.right;
        return n.getCardAtPointer(); // ensure pointer is never null in a valid node
    }

    // Return the minimal attack card whose attack >= strangerHealth (or null if none)
    public Card findMinDamagingKillerCard(int strangerHealth) {
        DeckInnerNode n = root;
        DeckInnerNode bestNode = null;
        while (n != null) {
            int atkKey = n.getKeyAttack();
            if (atkKey >= strangerHealth) {
                bestNode = n;       // candidate with minimal attack so far
                n = n.left;         // try to find a smaller killer
            } else {
                n = n.right;        // need larger attack
            }
        }
        return (bestNode == null) ? null : bestNode.getCardAtPointer();
    }

    // Optional: lower_bound for any threshold
    public Card lowerBoundAttack(int threshold) {
        DeckInnerNode n = root;
        Card res = null;
        while (n != null) {
            int atk = n.getKeyAttack();
            if (atk >= threshold) {
                res = n.getCardAtPointer();
                n = n.left;
            } else {
                n = n.right;
            }
        }
        return res;
    }
    

    // wrapper funct
    // Finds the card with the smallest attack strictly greater than attackLimit
    public Card findMinAttackAbove(int attackLimit) {
        return findMinAttackAbove(this.root, attackLimit, null);
    }

    private Card findMinAttackAbove(DeckInnerNode node, int attackLimit, Card currentBest) {
        if (node == null) return currentBest;
        int nodeAttack = node.getKeyAttack();
        if (nodeAttack > attackLimit) {
            // Check all non-deleted cards here (should all have this key)
            Card card = node.getCardAtPointer();
            if (card.attackCurrent > attackLimit) {
                if (currentBest == null ||
                    card.attackCurrent < currentBest.attackCurrent ||
                    (card.attackCurrent == currentBest.attackCurrent && card.healthCurrent < currentBest.healthCurrent)) {
                    currentBest = card;
                }
            }
            // There could be lower attack in the left
            return findMinAttackAbove(node.left, attackLimit, currentBest);
        } else {
            // This node (and all left) are <= attackLimit, go right
            return findMinAttackAbove(node.right, attackLimit, currentBest);
        }
    }

    // AVL BALANCING FUNCTIONS
    private int height(DeckInnerNode n) { 
        if (n == null) {
            return 0;
        } else {
            return n.height;
        }
    }

    private void updateHeight(DeckInnerNode n) { 
        n.height = 1 + Math.max(height(n.left), height(n.right)); 
    }
    
    private int balanceFactor(DeckInnerNode n) { 
        if (n == null) {
            return 0;
        } else {
            return height(n.left) - height(n.right);
        }
     }

    private DeckInnerNode rotateRight(DeckInnerNode y) {
        DeckInnerNode x = y.left;
        DeckInnerNode T2 = x.right;
        x.right = y;
        y.left = T2;
        updateHeight(y);
        updateHeight(x);
        return x;
    }

    private DeckInnerNode rotateLeft(DeckInnerNode y) {
        DeckInnerNode x = y.right;
        DeckInnerNode T2 = x.left;
        x.left = y;
        y.right = T2;
        updateHeight(y);
        updateHeight(x);
        return x;
    }

    private DeckInnerNode rebalance(DeckInnerNode node) {
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

    
        
}
