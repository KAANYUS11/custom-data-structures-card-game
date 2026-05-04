public class GameEngine {
    DeckOuterAVL outerDeck;
    DiscardPileAVL discardPile;
    Card bestfittingCard1; 
    Card bestfittingCard2;
    Card bestfittingCard3;
    int survivorPoints;
    int strangerPoints;
    int revivedCardNumber = 0;

    public void initializeGame() {
        this.outerDeck = new DeckOuterAVL();
        this.discardPile = new DiscardPileAVL();
        this.bestfittingCard1 = null; 
        this.bestfittingCard2 = null;
        this.bestfittingCard3 = null;
        this.survivorPoints = 0;
        this.strangerPoints = 0;
        this.revivedCardNumber = 0;
    }

    // DRAW FUNCTIONS
    public String drawCard(String name, int attackInitial, int healthInitial) {
        Card newCard = new Card(name, attackInitial, healthInitial );
        this.outerDeck.insertCard(newCard); // update the root
        return "Added " + name + " to the deck";
    }

    // BATTLE FUNCTIONS
        public String battle(int strangerAttack, int strangerHealth, int healPoolPoints){
        revivedCardNumber = 0;
        StringBuilder result = new StringBuilder();

        Card played = this.outerDeck.findBestFittingCard(strangerAttack, strangerHealth);
        if (played == null) {
            int k = this.revivedCardNumber;
            return "No card to play, " + k + " cards revived";
        }

        // Remove chosen card from deck
        this.outerDeck.deleteCard(played);

        played.refreshAttackFromBase();
        int attackBefore = played.attackCurrent;
        int healthBefore = played.healthCurrent;

        boolean isSurvivor = healthBefore > strangerAttack;
        boolean isKiller = attackBefore >= strangerHealth;
        int priority;
        if (!isSurvivor) {
            priority = 4;          // cannot survive
        } else if (isKiller) {
            priority = 1;          // survive and kill
        } else {
            priority = 2;          // survive, not kill
        }

  
        int finalStrangerHealth = strangerHealth - attackBefore;
        int newCardHealth = healthBefore - strangerAttack;

        // Apply card health change and refresh attack for post-battle state
        played.setCurrentHealth(newCardHealth);
        played.refreshAttackFromBase();

        // Scoring
        if (finalStrangerHealth <= 0) {
            this.survivorPoints += 2;
        } else {
            this.survivorPoints += 1;
        }

        boolean cardSurvived = played.healthCurrent > 0;
        if (cardSurvived) {
            this.strangerPoints += 1;
            this.outerDeck.insertCard(played);
        } else {
            this.strangerPoints += 2;
            played.reviveProgress = 0;
            played.setCurrentHealth(0);
            played.refreshAttackFromBase();
            this.discardPile.insertCard(played);
        }

        if (healPoolPoints > 0) {
            healingPhase(healPoolPoints);
        }
        int k = this.revivedCardNumber;

        if (cardSurvived) {
            result.append("Found with priority ").append(priority)
                  .append(", Survivor plays ").append(played.name)
                  .append(", the played card returned to deck, ").append(k).append(" cards revived");
        } else {
            result.append("Found with priority ").append(priority)
                  .append(", Survivor plays ").append(played.name)
                  .append(", the played card is discarded, ").append(k).append(" cards revived");
        }   

        return result.toString();
    }
        
    private void healingPhase(int healPoints) {
        // Fully revive loop: repeatedly pick the largest missingHealth <= healPoints
        Card candidate = this.discardPile.findMaxFullyRevivable(healPoints);
        while (candidate != null && healPoints > 0) {
            // compute missing using healthBase - healthCurrent (defensive)
            int missing = candidate.getMissingHealth();
            if (missing <= 0) break;

            // consume heal points
            healPoints -= missing;

            // remove from discard
            this.discardPile.deleteCard(candidate);

            // mark as fully revived
            candidate.reviveProgress = candidate.healthBase; // so getMissingHealth() -> 0
            candidate.setCurrentHealth(candidate.healthBase);
            candidate.refreshAttackFromBase();

            // insert back to outer deck
            this.outerDeck.insertCard(candidate);
            revivedCardNumber++;

            // next candidate
            candidate = this.discardPile.findMaxFullyRevivable(healPoints);
        }

        // If no full-revive possible, partially revive the single smallest-missing card (leftmost node)
        if (healPoints > 0) {
            Card partial = this.discardPile.findMinPartiallyRevivable(); // leftmost
            if (partial != null) {
                // remove from discard, apply partial healing, re-insert into discard
                this.discardPile.deleteCard(partial);

                int missingBefore = partial.getMissingHealth();
                int healed = Math.min(healPoints, Math.max(0, missingBefore));
                partial.reviveProgress += healed;
                if (partial.reviveProgress > partial.healthBase) partial.reviveProgress = partial.healthBase;

                // update current health consistently and attack
                partial.setCurrentHealth(partial.healthBase - (partial.getMissingHealth()));
                partial.refreshAttackFromBase();

                // re-insert (still in discard) and update discard size
                this.discardPile.insertCard(partial);
            }
        }
    }
    
    // STEAL FUNCTIONS
    public String stealCard(int attackLimit, int healthLimit){
        StringBuilder result = new StringBuilder();
        Card stolenCard = this.outerDeck.findWorstStealableCard(attackLimit, healthLimit);
        if (stolenCard != null) {
            this.outerDeck.deleteCard(stolenCard);
            result.append("The Stranger stole the card: " + stolenCard.name);
        } else {
            result.append("No card to steal");
        }
        return result.toString();
    }
        

    // COUNT FUNCTIONS
    public String deckCount() {
        int k = 0;
        if (this.outerDeck != null) k = this.outerDeck.deckSize;
        return "Number of cards in the deck: " + k;
    }

    public String discardPileCount() {
        return "Number of cards in the discard pile: " + this.discardPile.discardSize;
    }

    public String findWinning() {
        // Apply discard penalty as specified (subtract number of discard cards from survivor score)
        int survivorAdjusted = this.survivorPoints - this.discardPile.discardSize;
        if (survivorAdjusted < 0) survivorAdjusted = 0;
        String result = "";
        if (survivorAdjusted >= this.strangerPoints) {
            result = "The Survivor, Score: " + survivorAdjusted;
        } else {
            result = "The Stranger, Score: " + this.strangerPoints;
        }
        return result; 
    }
        
    ///////DEBUG


}
