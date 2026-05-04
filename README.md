# custom-data-structures-card-game

A Java implementation of the CMPE 250 Project 1 card game simulation focused on custom data structures, AVL-tree based indexing, and command-driven game state updates.

The project models a survivor card deck and a discard pile. Cards are selected for battles according to attack and health constraints, battle outcomes update scores and card states, and healing points can revive discarded cards back into the active deck.

## Highlights

- Built without Java Collections for the core deck and discard pile storage logic
- Uses custom AVL trees to keep cards searchable by attack, health, and name
- Supports battle, revive, steal, count, and winner query commands
- Maintains active deck and discard pile state across a full input file
- Applies deterministic tie-breaking for repeatable output
- Processes command-based input files and writes deterministic output files

## Data Structures

The project intentionally implements the main structures from scratch:

| Structure | File | Purpose |
| --- | --- | --- |
| Outer AVL Tree | `src/DeckOuterAVL.java` | Groups active deck cards by attack value |
| Inner AVL Tree | `src/DeckInnerAVL.java` | Orders cards inside the same attack bucket by health/name |
| Discard AVL Tree | `src/DiscardPileAVL.java` | Stores discarded cards for full and partial revive operations |
| Deck Outer Node | `src/DeckOuterNode.java` | Node model for the outer deck AVL tree |
| Deck Inner Node | `src/DeckInnerNode.java` | Node model for cards inside each attack group |
| Discard Node | `src/DiscardNode.java` | Node model for the discard pile AVL tree |

## Game Logic

Each card has:

- `name`: unique card identifier
- `attack`: base and current attack value
- `health`: base and current health value

The simulation keeps two main card pools:

- Active deck: cards available to be played in battle or stolen by the stranger
- Discard pile: defeated cards that may be revived through healing points

During a battle, the game engine chooses the best fitting card from the active deck according to the stranger's attack and health. The selected card is removed from the deck, battle damage is applied, and the card either returns to the deck or moves to the discard pile.

Healing points are then used to revive cards from the discard pile. Fully revivable cards are prioritized first; if full revive is not possible, a partial revive update can be applied to one discarded card.

## Supported Operations

The input processor supports commands for:

- drawing a new card into the active deck
- battling against a stranger card
- finding the current winner
- querying active deck size
- querying discard pile size
- stealing a card from the active deck

Command format:

```text
draw_card <name> <attack> <health>
battle <stranger_attack> <stranger_health> <heal_pool>
find_winning
deck_count
discard_pile_count
steal_card <attack_limit> <health_limit>
```

## Project Structure

```text
src/
  Main.java              File-based command runner
  GameEngine.java        Game flow, scoring, battle, steal, and revive logic
  Card.java              Card profile and derived attack/health state
  DeckOuterAVL.java      Active deck index by attack value
  DeckInnerAVL.java      Active deck index inside each attack bucket
  DiscardPileAVL.java    Discard pile index for revive operations
  DeckOuterNode.java     Outer AVL node for deck buckets
  DeckInnerNode.java     Inner AVL node for cards
  DiscardNode.java       AVL node for discarded cards
  Helper.java            Shared helper methods
testcases/
  testcase_inputs/       Sample input files
  testcase_outputs/      Expected output files
cmpe250_project1_testcases/
  testcase_inputs/       Additional assignment test inputs
  testcase_outputs/      Additional expected outputs
test_runner.py           Automated compile/run/compare helper
```

## Build and Run

Compile:

```bash
javac src/*.java
```

Run with an input and output file:

```bash
java -cp src Main <input_file> <output_file>
```

Example:

```bash
java -cp src Main testcases/testcase_inputs/type1_demo_seed1001.txt output.txt
```

## Testing

The repository includes a Python test runner that compiles the Java sources, runs the available test cases, and compares generated outputs with expected outputs.

Run all tests:

```bash
python3 test_runner.py
```

Run a specific test type:

```bash
python3 test_runner.py --type type1
python3 test_runner.py --type type2
```

Verbose comparison or benchmark mode:

```bash
python3 test_runner.py --verbose
python3 test_runner.py --benchmark
```

Manual comparison example:

```bash
javac src/*.java
java -cp src Main testcases/testcase_inputs/type1_demo_seed1001.txt my_output.txt
diff -w my_output.txt testcases/testcase_outputs/type1_demo_seed1001.txt
```

No output from `diff` means the generated result matches the expected output. The `-w` flag avoids false mismatches from whitespace or line-ending differences.

## Technical Focus

This project is mainly about applying custom data structures to a stateful card simulation:

- maintaining ranked card pools under frequent insert/delete operations
- selecting battle candidates without repeatedly sorting the full deck
- keeping discard pile revive operations efficient
- preserving deterministic command output
- separating command parsing from game-state transitions

## Notes

This was developed as a CMPE 250 data structures and algorithms project. The implementation emphasizes explicit AVL tree design, deterministic command processing, and algorithmic behavior over framework usage.
