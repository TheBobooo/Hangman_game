package com.hangman.hangman_game;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpSession;
import java.util.*;

@Controller
public class GameController {

    private List<String> words;
    private List<String> hints;

    @PostConstruct
    public void init() {
        words = new LinkedList<>(Arrays.asList(
                "Algorithm", "Bias", "Chatbot", "MachineLearning", "TrainingData", "NeuralNetwork",
                "Blog", "Presentation", "Automation", "Virtual", "Augmented", "Podcast",
                "Infographic", "Webinar", "Interactive", "Environment", "Animation", "Mixed",
                "Immersion", "Simulation"
        ));

        hints = new LinkedList<>(Arrays.asList(
                "A step-by-step procedure used by AI to learn and adapt",
                "A systematic error in AI that can affect fairness",
                "AI-based assistant that can respond to human queries",
                "A subfield of AI that enables computers to learn from data",
                "The dataset used to teach AI systems before validation",
                "A model inspired by the human brain with layered nodes",
                "An online journal format often used for personal or professional content",
                "A visual and verbal content type used to present ideas",
                "Performing repetitive tasks using technology without human input",
                "Fully computer-generated environment used in simulations or games",
                "Enhances the real world with digital overlays using cameras or AR glasses",
                "An audio program you can subscribe to and listen to on demand",
                "A graphic visual representation of information or data",
                "A live or recorded online seminar, often educational or promotional",
                "Content that allows user interaction for engagement",
                "The surroundings or context where digital content is used",
                "Graphics that move, often used in videos or presentations",
                "A blend of virtual and real elements in one environment",
                "The sense of being fully involved in a virtual environment",
                "A digital recreation of real-world experiences for training or learning"
        ));

    }

    @ModelAttribute("game")
    public GameState getGame(HttpSession session) {
        GameState game = (GameState) session.getAttribute("game");
        if (game == null) {
            int score = session.getAttribute("score") != null ? (int) session.getAttribute("score") : 0;
            int index = new Random().nextInt(words.size());
            game = new GameState(words.get(index), hints.get(index), score);
            session.setAttribute("game", game);
        }
        return game;
    }

    @GetMapping("/")
    public String showStartPage() {
        return "start";
    }

    @GetMapping("/start")
    public String start(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    @PostMapping("/start")
    public String submitName(@RequestParam String name, HttpSession session) {
        session.setAttribute("playerName", name);
        session.setAttribute("score", 0);
        return "redirect:/game";
    }

    @GetMapping("/game")
    public String game(@ModelAttribute("game") GameState game, Model model, HttpSession session) {
        String playerName = (String) session.getAttribute("playerName");
        model.addAttribute("playerName", playerName);
        model.addAttribute("masked", game.getMaskedWord());
        model.addAttribute("maskedChars", game.getMaskedWord().toCharArray());
        model.addAttribute("hint", game.getHint());
        model.addAttribute("attempts", game.getAttempts());
        model.addAttribute("showWrongImage", game.wasLastGuessWrong());
        model.addAttribute("score", game.getScore());

        boolean gameOver = game.isWon() || game.isLost();
        model.addAttribute("gameOver", gameOver);
        model.addAttribute("won", game.isWon());
        model.addAttribute("word", game.getWord());

        String message = (String) session.getAttribute("message");
        if (message != null) {
            model.addAttribute("message", message);
            session.removeAttribute("message");
        }

        return "game";
    }

    @PostMapping("/guess")
    public String guess(@RequestParam String letter,
                        @ModelAttribute("game") GameState game,
                        HttpSession session,
                        RedirectAttributes redirectAttributes) {
        if (letter.length() == 1 && !game.isWon() && !game.isLost()) {
            boolean correct = game.guess(letter.charAt(0));
            int score = game.getScore();
            if (correct) {
                score += 100;
            } else {
                score -= 50;
                redirectAttributes.addFlashAttribute("showWrongGuessImage", true);
            }
            game.setScore(score);
            session.setAttribute("score", score);
        }
        return "redirect:/game";
    }

    @GetMapping("/restart")
    public String restart(HttpSession session) {
        session.invalidate();
        return "redirect:/start";
    }

    @PostMapping("/reset")
    public String reset(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        String playerName = null;
        int score = 0;
        List<String> usedWords = null;

        if (session != null) {
            playerName = (String) session.getAttribute("playerName");
            GameState previousGame = (GameState) session.getAttribute("game");
            if (previousGame != null) {
                score = previousGame.getScore();
            }
            usedWords = (List<String>) session.getAttribute("usedWords");
            session.invalidate();
        }

        HttpSession newSession = request.getSession(true);
        newSession.setAttribute("playerName", playerName);
        newSession.setAttribute("score", score);
        if (usedWords != null) {
            newSession.setAttribute("usedWords", usedWords);
        } else {
            newSession.setAttribute("usedWords", new LinkedList<>());
        }

        List<String> availableWords = new ArrayList<>(words);
        if (usedWords != null) availableWords.removeAll(usedWords);

        if (availableWords.isEmpty()) {
            return "redirect:/end";
        }

        int index = new Random().nextInt(availableWords.size());
        String newWord = availableWords.get(index);
        String newHint = hints.get(words.indexOf(newWord));
        ((List<String>) newSession.getAttribute("usedWords")).add(newWord);

        GameState newGame = new GameState(newWord, newHint, score);
        newSession.setAttribute("game", newGame);

        return "redirect:/game";
    }
}