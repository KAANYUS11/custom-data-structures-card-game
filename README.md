# CMPE 250 Project 1 - Nightpass Survivor

Java implementation of the CMPE 250 Project 1 card game simulation. The project processes commands from an input file, maintains the survivor deck and discard pile with custom AVL-tree based data structures, and writes command results to an output file.

## Project Structure

```text
src/
  Main.java              Program entry point and command parser
  GameEngine.java        Game flow, scoring, battle, steal, and revive logic
  Card.java              Card model and derived attack/health state
  DeckOuterAVL.java      Deck index by attack
  DeckInnerAVL.java      Deck index by health/name inside an attack bucket
  DiscardPileAVL.java    Discard pile index for revive operations
testcases/
  testcase_inputs/       Sample input files
  testcase_outputs/      Expected output files
test_runner.py           Automated compile/run/compare helper
```

## Requirements

- Java SDK 8 or newer
- Python 3.6 or newer, only for `test_runner.py`

## Compile

```bash
cd src
javac *.java
```

## Run

```bash
java Main ../testcases/testcase_inputs/type1_demo_seed1001.txt ../output/type1_demo_seed1001.txt
```

The program expects exactly two arguments:

```text
java Main <input_file> <output_file>
```

## Test

The included `test_runner.py` compiles the Java sources and compares generated outputs against expected outputs.

```bash
python3 test_runner.py
```

## Supported Commands

- `draw_card <name> <attack> <health>`
- `battle <stranger_attack> <stranger_health> <heal_pool>`
- `find_winning`
- `deck_count`
- `discard_pile_count`
- `steal_card <attack_limit> <health_limit>`

## Notes

The implementation avoids Java collection data structures for core storage and uses custom AVL trees for deck and discard pile operations, following the project constraints.
