# Cosmic Quest Console Guide

This guide explains how to run and play the console version of Cosmic Quest
(v0.1). The console version has the full game rules; the LibGDX screens come
later and use the same rules.

## 1. Run the game

You need Java 21. Gradle downloads everything else the first time.

From the project folder:

    .\gradlew core:runConsole -q --console=plain      (Windows)
    ./gradlew core:runConsole -q --console=plain      (macOS / Linux)

The game loads the sample lesson `assets/lessons/OOP_Fundamentals.txt` at
start. To leave, choose **Exit** from the main menu, or press Ctrl+Z then Enter
(Windows) or Ctrl+D (macOS / Linux).

## 2. Main menu

    MAIN MENU  (lesson: OOP_Fundamentals.txt)
      1) Solo battle
      2) Squad battle
      3) Load lesson
      4) How to play
      5) Exit

Type the number and press Enter. Anything else just asks again; the game never
crashes on bad input.

| Option | What it does |
|---|---|
| Solo battle | You pick a pilot name, an alien and a level (1, 2 or 3 boss), then fight alone. |
| Squad battle | 2 or 3 pilots take turns against a level 3 boss. |
| Load lesson | Type the path to your own lesson `.txt` file. Quotes around the path are fine. A blank line cancels. |
| How to play | Shows the rules and the battle commands. |
| Exit | Closes the game. |

## 3. Your own lesson

A lesson is a plain text file with one term per line:

    # Comment lines start with #
    Encapsulation :: Bundling data and the methods that use it into one unit.
    [ENUM] Pillars of OOP :: encapsulation, inheritance, polymorphism, abstraction

- `Term :: Definition` makes identification, multiple choice and reasoning questions.
- `[ENUM] Title :: item, item, item` makes enumeration questions.
- A bad line is reported with its line number, and the old lesson stays loaded.

Multiple choice needs at least 4 different terms. An alien whose question type
the lesson cannot make is left out of the alien list. Full rules:
[LESSON_FORMAT.md](LESSON_FORMAT.md).

## 4. Aliens

Each alien asks one type of question.

| Alien | Asks | Special move |
|---|---|---|
| Void Brute | Identification: type the term for a definition | Ground Slam: every 3rd turn its strike deals 1.5 times its attack |
| Nebula Lich | Multiple choice: answer A–D or type the option | Hex Bolt (level 3): a missed question comes back with its options reshuffled |
| Lunar Beast | Enumeration: list every item, separated by commas | Moon Pounce: its strike deals 2 extra damage for each item you missed |
| Star Weaver | Reasoning: explain a term in your own words | Thread Bind: each strike locks one of your power-ups for 2 turns |

Answers ignore capital letters and extra spaces. Enumeration items can be in
any order; a partial list still deals partial damage. A reasoning answer passes
when it uses at least 60% of the key words.

## 5. A turn

    --------------------------------------------------------
     Lyle               Hull [####################] 100/100
                        Streak 0 (+0% damage)
     Void Brute Lv 1    Hull [####################]  60/60
    --------------------------------------------------------

    [Identification] Difficulty 2/3
    A feature that allows a class to acquire fields and methods from another class.
    (Type the term.)
    Power-ups: none (earn one with every 3 right answers in a row)
    > 

1. Read the question and type your answer within **30 seconds**. A late answer
   counts as a miss.
2. **Right:** you hit the alien. Each right answer in a row adds 10% damage, up
   to +50%.
3. **Wrong:** the alien strikes back, your streak resets, and the question comes
   back later. The correct answer is shown.
4. You win when the alien's HP reaches 0. You lose when your hull reaches 0.

## 6. Commands during a battle

| Command | Effect |
|---|---|
| `/1`, `/2`, `/3` | Use the power-up in that slot, before you answer |
| `/help` | Show the rules again |
| `/quit` | Give up the battle and return to the menu |

## 7. Power-ups

Every 3 right answers in a row earn a random power-up. You can hold 3.

| Power-up | Effect |
|---|---|
| Shield | Blocks the next hit |
| Repair | Heals 20 hull (never above the maximum) |
| Double Damage | Your next hit deals double damage |
| Hint | Multiple choice: hides 2 wrong options. Enumeration: shows the first letter of each item. Reasoning: suggests a key word. Identification: shows the first letter. |

## 8. Squad battle

- Choose 2 or 3 pilots. Names must be different (capital letters do not count).
- Pilots answer in turn. A wrong answer hurts only the pilot who answered.
- Each pilot keeps their own streak, score and power-ups.
- A pilot at 0 hull is skipped. The squad loses only when every pilot is down.
- The HUD marks the current pilot with `>` and a fallen pilot with `DOWN`.

## 9. Debrief and stars

After each battle the debrief shows the result, your stars, right answers,
accuracy, damage dealt and taken, and every missed question with the correct
answer and what you typed.

| Stars | How to earn them |
|---|---|
| `[*] [ ] [ ]` | Win |
| `[*] [*] [ ]` | Win with more than half your hull left (Squad: the whole team's hull) |
| `[*] [*] [*]` | Win with more than half your hull left and no missed questions |

## 10. AI questions (optional)

By default the questions come from the lesson on your computer. The game can
also ask an AI to write new questions from the same lesson.

1. Copy `config.properties.example` and name the copy `config.properties`,
   in the project folder.
2. Add a free key after `gemini.api.key=` (from aistudio.google.com) and/or
   `groq.api.key=` (from console.groq.com).
3. Start the game. It shows `AI questions: on (Gemini, Groq)`.

Before each battle the game tries Gemini, then Groq. You will see one of:

| Message | Meaning |
|---|---|
| `AI questions from Gemini` | The AI wrote the questions. |
| `... (topped up with local questions)` | Some AI questions failed the game's checks and were replaced. |
| `Offline: using local questions` | No AI answered. The reason is shown below it, for example `check the key in config.properties` or `rate limit reached`. The battle still plays normally. |

`config.properties` is git-ignored. Never commit it or share your keys.

## 11. Troubleshooting

| Problem | Fix |
|---|---|
| `Default lesson not found` | Run the game from the project folder, or use Load lesson. |
| `No file found at ...` | Check the path. Drag the file into the terminal to paste its full path. |
| `Bad lesson file. Line N: ...` | Fix that line (see section 3). |
| `Load a lesson first (menu 3).` | No lesson is loaded yet; load one. |
| AI says `HTTP 400` / `401` / `403` | The key is wrong or missing a character. Copy it again. |
| AI says `HTTP 404` | The model name in `config.properties` is wrong or retired. |
| AI says `HTTP 429` | The free limit is used up for now; local questions are used. |
