import random

WORDS = [
    {"word": "PYTHON",  "hint": "a snake-named language"},
    {"word": "MATRIX",  "hint": "rows and columns"},
    {"word": "BINARY",  "hint": "0s and 1s"},
    {"word": "SYNTAX",  "hint": "code grammar rules"},
    {"word": "CLUSTER", "hint": "group of nodes"},
    {"word": "RENDER",  "hint": "draw to screen"},
    {"word": "LAMBDA",  "hint": "anonymous function"},
    {"word": "FILTER",  "hint": "keep matching items"},
    {"word": "SOCKET",  "hint": "network endpoint"},
    {"word": "BUFFER",  "hint": "temporary data store"},
    {"word": "PARSER",  "hint": "reads structure"},
    {"word": "PROMPT",  "hint": "AI input text"},
    {"word": "MODULE",  "hint": "reusable code unit"},
    {"word": "KERNEL",  "hint": "OS core"},
    {"word": "SCRIPT",  "hint": "run-this-file code"},
]

ROUNDS = 10


def scramble(word: str) -> str:
    letters = list(word)
    while True:
        random.shuffle(letters)
        if "".join(letters) != word:
            return "".join(letters)


def play():
    print("\n╔══════════════════════════════╗")
    print("║       WORD  SCRAMBLE         ║")
    print("╚══════════════════════════════╝")
    print(f"  Unscramble {ROUNDS} tech words.\n")
    print("  Commands: type your answer, or")
    print("  press Enter to skip a word.\n")

    pool = random.sample(WORDS, ROUNDS)
    score = 0

    for i, item in enumerate(pool, 1):
        word = item["word"]
        hint = item["hint"]
        jumbled = scramble(word)

        print(f"  Round {i}/{ROUNDS}  •  Score: {score}")
        print(f"  ─────────────────────────────")
        print(f"  Scrambled : {jumbled}")
        print(f"  Hint      : {hint}")

        guess = input("  Your answer (Enter to skip): ").strip().upper()

        if guess == word:
            score += 1
            print(f"  ✓ Correct!\n")
        elif guess == "":
            print(f"  → Skipped. It was: {word}\n")
        else:
            print(f"  ✗ Wrong. It was: {word}\n")

    print("══════════════════════════════")
    print(f"  Final score: {score}/{ROUNDS}")
    pct = round((score / ROUNDS) * 100)
    if pct == 100:
        verdict = "Perfect score — you crushed it!"
    elif pct >= 70:
        verdict = "Nice work — solid round."
    elif pct >= 40:
        verdict = "Not bad — keep practicing."
    else:
        verdict = "Rough round — the words fought back."
    print(f"  {verdict}")
    print("══════════════════════════════\n")


def main():
    while True:
        play()
        again = input("  Play again? (y/n): ").strip().lower()
        if again != "y":
            print("\n  See you next time!\n")
            break


if __name__ == "__main__":
    main()
