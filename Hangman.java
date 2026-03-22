import java.util.*;

public class Hangman {

    static final String[] WORDS = {
        "java", "programming", "computer", "keyboard", "developer",
        "algorithm", "variable", "function", "exception", "inheritance",
        "polymorphism", "encapsulation", "abstraction", "interface", "compiler",
        "database", "framework", "recursion", "iteration", "debugging"
    };

    static final String[] HANGMAN_STAGES = {
        """
                
                
                
                
                
                
        ==========""",
        """
                |
                |
                |
                |
                |
                |
        ==========""",
        """
          +-----+
                |
                |
                |
                |
                |
        ==========""",
        """
          +-----+
          |     |
                |
                |
                |
                |
        ==========""",
        """
          +-----+
          |     |
          O     |
                |
                |
                |
        ==========""",
        """
          +-----+
          |     |
          O     |
          |     |
                |
                |
        ==========""",
        """
          +-----+
          |     |
          O     |
         /|     |
                |
                |
        ==========""",
        """
          +-----+
          |     |
          O     |
         /|\\    |
                |
                |
        ==========""",
        """
          +-----+
          |     |
          O     |
         /|\\    |
         /      |
                |
        ==========""",
        """
          +-----+
          |     |
          O     |
         /|\\    |
         / \\    |
                |
        ==========""",
    };

    static final int MAX_WRONG = HANGMAN_STAGES.length - 1;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        boolean playAgain = true;

        System.out.println("╔══════════════════════════════╗");
        System.out.println("║       HANGMAN  GAME          ║");
        System.out.println("╚══════════════════════════════╝");

        while (playAgain) {
            playGame(scanner);
            System.out.print("\nPlay again? (yes/no): ");
            String response = scanner.next().trim().toLowerCase();
            playAgain = response.equals("yes") || response.equals("y");
        }

        System.out.println("\nThanks for playing! Goodbye.");
        scanner.close();
    }

    static void playGame(Scanner scanner) {
        String word = WORDS[new Random().nextInt(WORDS.length)];
        Set<Character> guessedLetters = new LinkedHashSet<>();
        int wrongGuesses = 0;

        System.out.println("\n--- New Game Started ---");
        System.out.println("Guess the word! It has " + word.length() + " letters.\n");

        while (wrongGuesses < MAX_WRONG) {
            System.out.println(HANGMAN_STAGES[wrongGuesses]);
            System.out.println("\nWord: " + displayWord(word, guessedLetters));
            System.out.println("Wrong guesses left: " + (MAX_WRONG - wrongGuesses));
            System.out.println("Guessed letters: " + formatGuessed(guessedLetters));

            if (isWordGuessed(word, guessedLetters)) {
                System.out.println("\n✅ YOU WIN! The word was: " + word.toUpperCase());
                return;
            }

            System.out.print("\nEnter a letter: ");
            String input = scanner.next().trim().toLowerCase();

            if (input.length() != 1 || !Character.isLetter(input.charAt(0))) {
                System.out.println("⚠ Please enter a single valid letter.");
                continue;
            }

            char guess = input.charAt(0);

            if (guessedLetters.contains(guess)) {
                System.out.println("⚠ You already guessed '" + guess + "'. Try another.");
                continue;
            }

            guessedLetters.add(guess);

            if (word.indexOf(guess) >= 0) {
                System.out.println("✅ Good guess! '" + guess + "' is in the word.");
            } else {
                wrongGuesses++;
                System.out.println("❌ Wrong! '" + guess + "' is not in the word.");
            }
        }

        System.out.println(HANGMAN_STAGES[MAX_WRONG]);
        System.out.println("\n💀 GAME OVER! The word was: " + word.toUpperCase());
    }

    static String displayWord(String word, Set<Character> guessedLetters) {
        StringBuilder sb = new StringBuilder();
        for (char c : word.toCharArray()) {
            sb.append(guessedLetters.contains(c) ? c : '_');
            sb.append(' ');
        }
        return sb.toString().trim();
    }

    static boolean isWordGuessed(String word, Set<Character> guessedLetters) {
        for (char c : word.toCharArray()) {
            if (!guessedLetters.contains(c)) return false;
        }
        return true;
    }

    static String formatGuessed(Set<Character> guessedLetters) {
        if (guessedLetters.isEmpty()) return "None";
        StringBuilder sb = new StringBuilder();
        for (char c : guessedLetters) {
            sb.append(c).append(' ');
        }
        return sb.toString().trim();
    }
}

