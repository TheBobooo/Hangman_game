package com.hangman.hangman_game;

public class GameState {

    private final String word;
    private final String hint;
    private final StringBuilder maskedWord;
    private int attempts;
    private boolean lastGuessWrong = false;
    private int correctGuesses;
    private int wrongGuesses;
    private int score;

    // Constructorul standard (folosit la începutul jocului)
    public GameState(String word, String hint) {
        this.word = word;
        this.hint = hint;
        this.maskedWord = new StringBuilder("_".repeat(word.length()));
        this.attempts = 0;
        this.correctGuesses = 0;
        this.wrongGuesses = 0;
    }

    // Constructor cu scor (pentru păstrare la retry)
    public GameState(String word, String hint, int previousScore) {
        this.word = word;
        this.hint = hint;
        this.maskedWord = new StringBuilder("_".repeat(word.length()));
        this.attempts = 0;

        // Derivăm numărul de ghiciri corecte și greșite din scorul anterior
        // Exemplu: dacă scorul era 150 => poate fi 2 corecte (200) și 1 greșită (-50)
        this.correctGuesses = Math.max(0, previousScore / 100);
        this.wrongGuesses = Math.max(0, (correctGuesses * 100 - previousScore) / 50);
    }

    public boolean guess(char ch) {
        boolean found = false;
        for (int i = 0; i < word.length(); i++) {
            if (Character.toLowerCase(word.charAt(i)) == Character.toLowerCase(ch)
                    && maskedWord.charAt(i) == '_') {
                maskedWord.setCharAt(i, word.charAt(i));
                found = true;
            }
        }

        if (found) {
            correctGuesses++;
        } else {
            attempts++;
            wrongGuesses++;
        }

        lastGuessWrong = !found;
        return found;
    }

    public int getScore() {
        return correctGuesses * 100 - wrongGuesses * 50;
    }
    public void setScore(int score) {
        this.score = score;
    }
    public String getMaskedWord() {
        return maskedWord.toString();
    }

    public String getHint() {
        return hint;
    }

    public int getAttempts() {
        return attempts;
    }

    public boolean isWon() {
        return maskedWord.toString().equalsIgnoreCase(word);
    }

    public boolean isLost() {
        return attempts >= 3;
    }

    public String getWord() {
        return word;
    }

    public boolean wasLastGuessWrong() {
        return lastGuessWrong;
    }

    public int getCorrectGuesses() {
        return correctGuesses;
    }

    public int getWrongGuesses() {
        return wrongGuesses;
    }
}
