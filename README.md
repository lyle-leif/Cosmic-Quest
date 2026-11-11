# Cosmic Quest

A Java study battle game that turns lesson notes into battles against alien
entities. Load a lesson file, pick an alien, and answer its questions to win.
OOP 101 project by NeuroLink.

Current version: **v0.1-console**, the full game played in the terminal. The
LibGDX screens are built on the same rules in the next milestones.

## Run the console game

You need **Java 21**. The Gradle wrapper downloads everything else.

    .\gradlew core:runConsole -q --console=plain      (Windows)
    ./gradlew core:runConsole -q --console=plain      (macOS / Linux)

The sample lesson `assets/lessons/OOP_Fundamentals.txt` loads at start. Choose
**1** for a Solo battle, pick an alien and a level, then type your answers.
Type `/help` during a battle for the commands.

How to play, step by step: [docs/CONSOLE_GUIDE.md](docs/CONSOLE_GUIDE.md).

## Use your own lesson

Write one term per line, `Term :: Definition`, and lists as
`[ENUM] Title :: item, item, item`. Then choose **3) Load lesson** and type the
file's path. Rules: [docs/LESSON_FORMAT.md](docs/LESSON_FORMAT.md).

## AI questions (optional)

Copy `config.properties.example` to `config.properties` and add a free Gemini
or Groq key. The game then asks the AI for new questions from your lesson, and
falls back to local questions whenever the AI is not available. Never commit
`config.properties`.

## Run the tests

    .\gradlew test

## Project layout

| Module | What it holds |
|---|---|
| `core` | The game: `model` (players, aliens, questions), `logic` (lessons, battles, power-ups, stats), `console` (menu and screen) |
| `ai` | Gemini and Groq clients that write questions; knows nothing about the game |
| `lwjgl3` | Desktop launcher for the LibGDX version |

Class diagram: [docs/uml/class-diagram-v2.png](docs/uml/class-diagram-v2.png)
(source `docs/uml/class-diagram.puml`). Requirements: [PRD.md](PRD.md).

## Built with

Java 21, Gradle, [libGDX](https://libgdx.com/) 1.14.2 (project created with
[gdx-liftoff](https://github.com/libgdx/gdx-liftoff)), JUnit 5, Gson.
