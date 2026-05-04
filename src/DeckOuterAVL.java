public class DeckOuterAVL {
    DeckOuterNode root; 
    int deckSize; 
    public DeckOuterAVL() {
        this.root = null;
    }


    // INSERT FUNCTIONS
    // public wrapper function to return the updated root
    public void insertCard(Card card) {
        this.root = insertCard(card, this.root);
        deckSize ++;

    }
    
    private DeckOuterNode insertCard(Card card, DeckOuterNode node) {
        int cardHealth = card.healthCurrent;
        if (node == null) {
            DeckOuterNode newNode = new DeckOuterNode(cardHealth);
            newNode.addCard(card);
            return newNode;
        }
        int cmp = cardHealth - node.healthKey;
        if (cmp < 0) {
            node.left = insertCard(card, node.left);
        } else if (cmp > 0) {
            node.right = insertCard(card, node.right);
        } else {
            node.addCard(card);
            return node;
        }
        updateHeight(node);
        return rebalance(node);
    }

    // DELETE FUNCTIONS

    public void deleteCard(Card card){
        this.root = deleteCard(card, root);
        deckSize --;
    }

    private DeckOuterNode deleteCard(Card card, DeckOuterNode node) {
        if (node == null) return null;
        int cardHealth = card.healthCurrent;
        int cmp = cardHealth - node.healthKey;
        if (cmp < 0) {
            node.left = deleteCard(card, node.left);
        } else if (cmp > 0) {
            node.right = deleteCard(card, node.right);
        } else {
            // Found health node: remove card in inner deck
            node.innerDeck.deleteCard(card);
            if (node.innerDeck.root == null) { // inner AVL emptied, remove this outer node
                if (node.left == null || node.right == null) {
                    node = (node.left != null ? node.left : node.right);
                } else {
                    DeckOuterNode successor = findMin(node.right);
                    node.healthKey = successor.healthKey;
                    node.innerDeck = successor.innerDeck;
                    node.right = deleteEntireKey(successor.healthKey, node.right);
                }
            }
        }
        if (node == null) return null;
        updateHeight(node);
        return rebalance(node);
    }

    // Consistent argument order: key, node
    private DeckOuterNode deleteEntireKey(int healthKey, DeckOuterNode node) {
        if (node == null) return null;
        int cmp = healthKey - node.healthKey;
        if (cmp < 0) {
            node.left = deleteEntireKey(healthKey, node.left);
        } else if (cmp > 0) {
            node.right = deleteEntireKey(healthKey, node.right);
        } else {
            // remove this node
            if (node.left == null || node.right == null) {
                node = (node.left != null ? node.left : node.right);
            } else {
                DeckOuterNode successor = findMin(node.right);
                node.healthKey = successor.healthKey;
                node.innerDeck = successor.innerDeck;
                node.right = deleteEntireKey(successor.healthKey, node.right);
            }
        }
        if (node == null) return null;
        updateHeight(node);
        return rebalance(node);
    }

    public DeckOuterNode findMin(DeckOuterNode node) {
        while (node != null && node.left != null) node = node.left;
        return node;
    }

    // FIND FUNCTIONS
    public Card findBestFittingCard(int strangerAttack, int strangerHealth){
        // best[0]=survivor killer (min attack that still kills)
        // best[1]=survivor non-killer (max attack among survivors)
        // best[2]=non-survivor MAX attack (killer/non-killer not distinguished)
        Card[] best = new Card[4];

        // 1) Prefer survivors
        findBestFittingSurvivorCards(strangerAttack, strangerHealth, best);
        if (best[0] != null){
            return best[0];
        } else if (best[1] != null) {
            return best[1];
        }

        // 2) Otherwise, pick the non-survivor with the highest attack
        findBestFittingNonSurvivorCards(strangerAttack, strangerHealth, best);
        if (best[2] != null){
            return best[2];
        }

        return null;
    }

    private Card[] findBestFittingSurvivorCards(int strangerAttack, int strangerHealth, Card[] best) {
        findBestSurvivorRecursive(root, strangerAttack, strangerHealth, best);
        return best;
    }

    private Card[] findBestFittingNonSurvivorCards(int strangerAttack, int strangerHealth, Card[] best) {
        // Fill only best[2] = max attack among all non-survivors (healthKey <= strangerAttack)
        findBestNonSurvivorRecursive(root, strangerAttack, best);
        return best;
    }

        private void findBestSurvivorRecursive(DeckOuterNode node, int strangerAttack, int strangerHealth, Card[] best) {
        if (node == null) return;
        if (node.healthKey > strangerAttack) {
            findBestSurvivorRecursive(node.left, strangerAttack, strangerHealth, best);

            Card minKiller = safeMinKiller(node, strangerHealth);
            if (minKiller != null) {
                minKiller.refreshAttackFromBase();
                // prefer lower attack; if equal attack, prefer LOWER BASE health
                if (best[0] == null
                    || minKiller.attackCurrent < best[0].attackCurrent
                    || (minKiller.attackCurrent == best[0].attackCurrent
                        && minKiller.healthBase < best[0].healthBase)) {
                    best[0] = minKiller;
                }
            } else if (best[0] == null) {
                Card maxNonKiller = safeMaxDamaging(node);
                if (maxNonKiller != null) {
                    maxNonKiller.refreshAttackFromBase();
                    // strictly less: Acur < Hstranger (equal should be killer)
                    if (maxNonKiller.healthCurrent > strangerAttack && maxNonKiller.attackCurrent < strangerHealth) {
                        if (best[1] == null || maxNonKiller.attackCurrent > best[1].attackCurrent) {
                            best[1] = maxNonKiller;
                        }
                    }
                }
            }

            findBestSurvivorRecursive(node.right, strangerAttack, strangerHealth, best);
        } else {
            findBestSurvivorRecursive(node.right, strangerAttack, strangerHealth, best);
        }
    }

    private void findBestNonSurvivorRecursive(DeckOuterNode node, int strangerAttack, Card[] best) {
        if (node == null) return;

        if (node.healthKey <= strangerAttack) {
            // Candidate from this node: max attack in its inner deck
            Card maxAttack = safeMaxDamaging(node);
            if (maxAttack != null) {
                maxAttack.refreshAttackFromBase();
                if (best[2] == null || maxAttack.attackCurrent > best[2].attackCurrent) {
                    best[2] = maxAttack;
                }
            }
            // Explore both sides (there may be more non-survivor nodes)
            findBestNonSurvivorRecursive(node.left, strangerAttack, best);
            findBestNonSurvivorRecursive(node.right, strangerAttack, best);
        } else {
            // Only the left subtree can have non-survivors
            findBestNonSurvivorRecursive(node.left, strangerAttack, best);
        }
    }

    // Safe wrappers: avoid NPEs from innerDeck during find
    private Card safeMinKiller(DeckOuterNode node, int strangerHealth) {
        try {
            if (node.innerDeck == null || node.innerDeck.root == null) return null;
            return node.innerDeck.findMinDamagingKillerCard(strangerHealth);
        } catch (Exception ex) {
            System.err.println("WARN safeMinKiller failed at key=" + node.healthKey + " -> " + ex.getMessage());
            return null;
        }
    }

    private Card safeMaxDamaging(DeckOuterNode node) {
        try {
            if (node.innerDeck == null || node.innerDeck.root == null) return null;
            return node.innerDeck.findMaxDamagingCard();
        } catch (Exception ex) {
            System.err.println("WARN safeMaxDamaging failed at key=" + node.healthKey + " -> " + ex.getMessage());
            return null;
        }
    }


    // Finds the card Stranger should steal: min attackCurrent > attackLimit among all cards with health > healthLimit.
    public Card findWorstStealableCard(int attackLimit, int healthLimit) {
        return findWorstStealableCard(attackLimit, healthLimit, root, null);
    }

    public Card findWorstStealableCard(int attackLimit, int healthLimit, DeckOuterNode node, Card currentBest) {
        if (node == null) return currentBest;
        if (node.healthKey > healthLimit) {
            // Left subtree may have smaller health
            currentBest = findWorstStealableCard(attackLimit, healthLimit, node.left, currentBest);
            // Try this node's inner AVL for minimum attack card > attackLimit
            Card candidate = node.innerDeck.findMinAttackAbove(attackLimit);
            if (candidate != null && candidate.attackCurrent > attackLimit) {
                if (currentBest == null
                    || candidate.attackCurrent < currentBest.attackCurrent
                    || (candidate.attackCurrent == currentBest.attackCurrent && candidate.healthCurrent < currentBest.healthCurrent)) {
                    currentBest = candidate;
                }
            }
            // Right subtree may also have other survivor nodes
            currentBest = findWorstStealableCard(attackLimit, healthLimit, node.right, currentBest);
        } else {
            // Only right subtree could hold nodes with enough health
            currentBest = findWorstStealableCard(attackLimit, healthLimit, node.right, currentBest);
        }
        return currentBest;
    }

    // AVL BALANCING FUNCTIONS
    private int height(DeckOuterNode n) {
        if (n == null) {
            return 0;
        } else {
            return n.height;
        }
    }
    private void updateHeight(DeckOuterNode n) { 
        n.height = 1 + Math.max(height(n.left), height(n.right));
     }
    private int balanceFactor(DeckOuterNode n) {
        if (n == null) {
            return 0;
        } else {
            return height(n.left) - height(n.right);
        }
    }

    private DeckOuterNode rotateRight(DeckOuterNode y) {
        DeckOuterNode x = y.left;
        DeckOuterNode T2 = x.right;
        x.right = y;
        y.left = T2;
        updateHeight(y);
        updateHeight(x);
        return x;
    }

    private DeckOuterNode rotateLeft(DeckOuterNode y) {
        DeckOuterNode x = y.right;
        DeckOuterNode T2 = x.left;
        x.left = y;
        y.right = T2;
        updateHeight(y);
        updateHeight(x);
        return x;
    }

    private DeckOuterNode rebalance(DeckOuterNode node) {
        int bf = balanceFactor(node);
        if (bf > 1) {
            if (balanceFactor(node.left) < 0) {
                node.left = rotateLeft(node.left);
            }
            return rotateRight(node);
        }
        if (bf < -1) {
            if (balanceFactor(node.right) > 0) {
                node.right = rotateRight(node.right);
            }
            return rotateLeft(node);
        }
        return node;
    }

    
}

