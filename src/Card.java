public class Card {
    final String name;
    final int healthInitial;
    final int attackInitial;

    int healthBase;
    int attackBase;
    int healthCurrent;
    int attackCurrent;
    int missingHealth;
    int reviveProgress;

    public Card(String name, int attack, int health) {
        this.name = name;
        this.healthInitial = health;
        this.attackInitial = attack;
        this.healthBase = health;
        this.attackBase = attack;
        this.healthCurrent = health;
        this.attackCurrent = attack;
        this.reviveProgress = 0;
    }


    public void setCurrentHealth(int health) {
        if (health < 0) {
            this.healthCurrent = 0;
        } else if (health > healthBase) {
            this.healthCurrent = healthBase;
        } else {
            this.healthCurrent = health;
        }
    }


    public void refreshAttackFromBase() {
        if (healthBase == 0) {
            this.attackCurrent = 0;
            return;
        }

        // Apply revive penalties to base attack first, then scale by current health ratio.
        double factor = 1.0;
        if (this.reviveProgress >= this.healthBase && this.healthBase > 0) {
            factor = 0.90;
        } else if (this.reviveProgress > 0) {
            factor = 0.95;
        }

        int penalizedBase = (int) Math.floor(this.attackBase * factor);
        int newAttack = (penalizedBase * this.healthCurrent) / this.healthBase;
        if (newAttack < 1) newAttack = 1;
        this.attackCurrent = newAttack;
    }
    

    public int getMissingHealth() {
        missingHealth = healthBase - reviveProgress;
        if (missingHealth < 0) {
            return 0;
        } else {
            return missingHealth;
        }
    }
}
