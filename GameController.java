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
                "Crocodil", "Balena","Elefant", "Tigru", "Girafa", "Rinocer", "Leu", "Pantera", "Zebra", "Urs",
                "Vultur", "Pinguin", "Cameleon", "Vidra", "Lup", "Vulpe", "Bursuc", "Iepure",
                "Cangur", "Koala", "Delfin", "Calmar", "Caracatita", "Papagal", "Strut", "Hipopotam",
                "Gazela", "Antilopa", "Pelican", "Raton", "Foca", "Naparca", "Berbec", "Caprioara",
                "Cocostar", "Ciocanitoare", "Bufnita", "Ursulet", "Cartita", "Soparla"));

        hints = new LinkedList<>(Arrays.asList(
                "Reptila mare", "Cel mai mare mamifer", "Are trompa lunga si urechi mari", "Felina cu dungi portocalii si negre", "Are gat foarte lung si mananca frunze",
                "Are un corn mare pe nas", "Este regele junglei", "Felina neagra si agila", "Animal cu dungi albe si negre",
                "Animal mare, iubitor de miere", "Pasare rapitoare cu vedere excelenta", "Pasare care nu zboara, dar inoata",
                "Se schimba in functie de mediu", "Traieste in apa si adora sa se joace", "Animal care urla la luna",
                "Cunoscuta pentru viclenie", "Are dungi albe pe fata si sapa vizuini", "Are urechi lungi si sare repede",
                "Poarta puiul intr-un buzunar", "Traieste in Australia si iubeste eucaliptul", "Mamifer marin foarte inteligent",
                "Are tentacule si cerneala", "Are opt brate si traieste in mare", "Pasare colorata care poate imita vorbele",
                "Pasare mare care nu zboara", "Animal mare care sta mult in apa", "Alerga foarte repede prin savana",
                "Ruda gazelei, cu coarne lungi", "Pasare cu cioc urias pentru peste", "Animal cu masca neagra pe fata",
                "Traieste la pol si face trucuri in circ", "Pare un sarpe, dar are picioare", "Are coarne rasucite",
                "Animal gratios din padure", "Pasare cu picioare lungi", "Bate in copaci cu ciocul",
                "Pasare nocturna foarte inteleapta", "Forma de plus preferata a copiilor", "Sapa galerii sub pamant",
                "Mica si se catarara pe pereti"));
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
