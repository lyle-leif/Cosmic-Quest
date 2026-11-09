# Cosmic Quest — Product Requirements Document
 
| | |
|---|---|
| **Product** | Cosmic Quest |
| **Course** | OOP 101 (Java), Batangas State University |
| **Team** | NeuroLink: Lyle (repo owner), Jhon Moises, Angelo |
| **Repository** | github.com/lyle-leif/Cosmic-Quest (default branch `dev`) |
| **Timeline sheet** | Google Sheet "OOP Timeline" (Timeline, Console Files, Guide, Progress) |
| **Status** | S01–S03 merged on `dev` (as of Nov 9, 2026) |
| **Version** | 1.0 |
 
---
 
## 1. Summary
 
Cosmic Quest is a desktop study game written in Java. A player loads their lesson notes from a `.txt` file, and the game turns them into quiz questions. An astronaut hero battles alien entities: a correct answer damages the alien, a wrong answer lets the alien strike back. The game is built console-first in plain Java, then given a LibGDX graphical interface.
 
## 2. Problem
 
Reviewing notes by rereading them is passive and easy to quit. Students need active recall (answering questions from memory), but writing their own quizzes takes time. Cosmic Quest turns any notes that follow a simple format into a quiz automatically and wraps it in a battle so practice feels like a game.
 
## 3. Goals
 
| # | Goal | Measured by |
|---|---|---|
| G1 | Turn a lesson file into playable quiz battles with no manual question writing | A valid `.txt` lesson produces questions of all four types |
| G2 | Demonstrate all four OOP pillars with real, explainable code | Each pillar mapped to classes in the README and slides |
| G3 | Ship a working game even if the GUI runs late | Console version complete and tagged `v0.1-console` |
| G4 | Work offline | Every mode playable with Wi-Fi off |
| G5 | Every member contributes equally and visibly | One named commit per member per session on `dev` |
 
## 4. Non-goals
 
- No online multiplayer or accounts (Squad mode is local, on one computer).
- No mobile or web build (desktop only, LWJGL3).
- No in-game lesson editor (lessons are written in any text editor).
- No grading or saving progress to a server.
- AI question generation is optional; the game never depends on it.
## 5. Users
 
| User | Need |
|---|---|
| Student reviewing alone | Quick, engaging practice from their own notes (Solo Boss Battle) |
| Study group of up to 3 | Take turns against one boss on one laptop (Squad Boss Battle) |
| Instructor | A clear demonstration of OOP concepts in a working program |
 
## 6. Core gameplay loop
 
1. Load a lesson file (`assets/lessons/*.txt`).
2. Pick a mode: Solo or Squad.
3. An alien appears. Its type decides the question type.
4. The player answers before the timer runs out.
5. Correct: the hero attacks; damage grows with the answer streak. Every 3rd streak grants a power-up.
6. Wrong or timed out: the alien attacks; the missed question returns later in the battle.
7. The battle ends when the alien or the hero reaches 0 HP.
8. Debrief: stars, score, and every missed question with its correct answer.
## 7. Functional requirements
 
Priority: **P0** = required for the console gate, **P1** = required for v1.0, **P2** = cut first if time runs short.
 
### 7.1 Lesson files — P0
 
| ID | Requirement |
|---|---|
| L1 | A term line is `Term :: Definition`. |
| L2 | A list line is `[ENUM] Title :: item1, item2, item3`. |
| L3 | Lines starting with `#` and blank lines are ignored. |
| L4 | A line is split at the first `::` only; spaces at the edges are trimmed. |
| L5 | A line with no `::`, an empty term, definition, title or list item is rejected with `InvalidLessonFormatException` naming its line number. |
| L6 | List items cannot contain commas; each term should appear once per file. |
 
Full rules: `docs/LESSON_FORMAT.md`. Sample: `assets/lessons/OOP_Fundamentals.txt` (20 terms, 3 lists).
 
### 7.2 Questions — P0
 
| Type | Asked by | How it is checked |
|---|---|---|
| Identification | Void Brute | Shows a definition; the typed term must match after normalizing (trim, lower case, single spaces) |
| Multiple choice | Nebula Lich | The correct term plus 3 other terms, shuffled; accepts A–D or the option text |
| Enumeration | Lunar Beast | List every item of an `[ENUM]` line; partial damage by match ratio (3 of 4 = 0.75) |
| Reasoning | Star Weaver | "Explain <term> in your own words"; passes when at least 60% of the keywords appear (`KEYWORD_PASS_RATIO = 0.6`) |
 
- Questions are generated offline from the lesson by `QuestionGenerator` (P0).
- Missed questions go to the back of the `QuestionPool` and return later (P0).
- Difficulty is 1 to 3.
### 7.3 Hero — P0
 
| Stat | Value |
|---|---|
| Hull (HP) | 100 |
| Base damage | 20 |
| Difficulty multiplier | 1.0 / 1.25 / 1.5 |
| Streak bonus | +10% per correct answer in a row, capped at +50% |
| Damage formula | `BASE_DAMAGE × multiplier × (1 + min(streak × 0.10, 0.50))`, rounded |
| Power-up slots | 3 |
 
### 7.4 Alien entities — P0
 
| Alien | Question type | HP (Lv 1 / 2 / 3) | ATK (Lv 1 / 2 / 3) | Special move |
|---|---|---|---|---|
| Void Brute | Identification | 60 / 100 / 160 | 8 / 12 / 18 | Ground Slam: a heavy hit every 3rd turn |
| Star Weaver | Reasoning | 50 / 90 / 140 | 10 / 14 / 20 | Thread Bind: locks one power-up for 2 turns |
| Nebula Lich | Multiple choice | 70 / 120 / 180 | 9 / 13 / 19 | Hex Bolt: reshuffles the choices at level 3 |
| Lunar Beast | Enumeration | 80 / 130 / 200 | 11 / 15 / 22 | Moon Pounce: damage grows with each item missed |
 
Levels outside 1–3 are rejected. Level 3 aliens act as bosses.
 
### 7.5 Power-ups — P0 (logic), P1 (visuals)
 
| Power-up | Effect |
|---|---|
| Shield | Blocks the next hit |
| Repair | Heals 20 hull (never above max) |
| Double Damage | The next hit deals double |
| Hint | Removes 2 wrong options, or reveals the first letter |
 
### 7.6 Battle modes — P0
 
| Mode | Rules |
|---|---|
| Solo Boss Battle | 1 player vs 1 alien; states QUESTION → PLAYER_ATTACK / ENEMY_ATTACK → WON / LOST |
| Squad Boss Battle | 2–3 players take turns vs a level 3 boss; defeated players are skipped; the team loses when all are at 0 HP |
 
### 7.7 Match stats and debrief — P0
 
- Track correct and total answers, damage dealt and taken, missed questions and the player's answers.
- Stars: 1 for winning, 2 if hull is above 50%, 3 if nothing was missed.
- The debrief lists every missed question with its correct answer.
### 7.8 AI question generation — P1
 
- `QuestionProvider` interface with `LocalProvider` (offline, uses `QuestionGenerator`) and `GeminiProvider` (Gemini Flash via `java.net.http.HttpClient`, JSON response).
- The API key is read from `config.properties`, which is git-ignored; only `config.properties.example` is committed.
- Any error, timeout or HTTP 429 falls back to `LocalProvider` with the message "Offline: using local questions".
- In the GUI, generation runs on a background thread and never freezes the game.
### 7.9 Console version — P0
 
- Main menu: Solo, Squad, Load lesson, How to play, Exit; invalid input never crashes the game.
- HUD with name, level, HP bars and streak; all printing lives in `ConsoleRenderer`.
### 7.10 GUI (LibGDX) — P1 unless marked
 
| Screen / feature | Priority |
|---|---|
| Loading screen with progress bar | P1 |
| Main menu | P1 |
| Lesson select with error dialog showing the line number | P1 |
| Solo battle screen (hero and enemy panels, question panel, timer, power-ups) | P1 |
| Victory and Game Over screens with stats and debrief | P1 |
| Settings (music and sound volume, mute, timer length) saved with Preferences | P1 |
| How to Play and credits | P1 |
| Attack, hit, hurt, shield and idle animations; screen shake | P1 |
| Audio: music per environment and sound effects | P1 |
| Squad battle screen with turn-order bar | P1 |
| Parallax galaxy backgrounds | P1 |
| Environment hazards | P2 |
| Suit Locker (6 outfits with unlock conditions) | P2 |
| Particle effects | P2 |
| AI generation inside the GUI | P2 |
 
**Cut order if time runs short:** particles → Suit Locker → hazards → AI in the GUI.
 
#### Environment hazards (P2)
 
| Environment | Hazard |
|---|---|
| Event Horizon | Timer 10% shorter (Squad boss arena) |
| Loom Cluster | Every 4th turn locks a power-up |
| Crimson Necropolis | A wrong answer costs 3 extra hull |
| Shattered Moon | Every 5 turns, both sides take 5 damage |
 
Other backgrounds: Andromeda (menu), Deep Field, Supernova (Victory), Wreckage (Game Over).
 
#### Outfits (P2)
 
| Outfit | Unlock condition |
|---|---|
| Rookie Cadet | Starter |
| Nebula Ranger | Clear 3 missions |
| Lunar Scout | Answer 50 questions |
| Void Breaker | Defeat a level 3 Void Brute |
| Solar Flare | Perfect score on a mission |
| Star Commander | Earn 15 stars |
 
Outfits are drawn by tinting sprite layers with `batch.setColor`, so no extra art per outfit is needed.
 
## 8. Non-functional requirements
 
| Area | Requirement |
|---|---|
| Platform | Windows desktop; Eclipse Temurin JDK 21; LibGDX with the LWJGL3 backend; window 1280×720 (`FitViewport`) |
| Offline | Every mode works with no internet |
| Robustness | Bad lesson files, empty answers, missing files and API failures never crash the game |
| Performance | No visible frame drops during animations or particles; AI calls never block rendering |
| Security | No API keys or passwords in the repository |
| Code quality | Private fields, constants instead of magic numbers, Javadoc on public classes and methods, methods under 30 lines in `logic` |
| Testability | `model` and `logic` contain no LibGDX imports and are covered by JUnit 5 tests (`.\gradlew core:test`) |
| Distribution | A runnable JAR from `.\gradlew lwjgl3:jar` that works on a computer that never opened the project |
 
## 9. Architecture
 
```
core/src/main/java/com/neurolink/cosmicquest/
  model/    Combatant, Entity + 4 aliens, Player, Question + 4 types, QuestionType, Team, Outfit
  logic/    Term, LessonData, TxtParser, QuestionGenerator, QuestionPool, DamageCalculator,
            BattleState, TurnResult, BattleMode, SoloBossBattle, SquadBossBattle, MatchStats,
            PowerUp + 4 types, PowerUpInventory, hazards
  ai/       QuestionProvider, LocalProvider, GeminiProvider
  console/  ConsoleMain, ConsoleRenderer               (Weeks 1–2)
  screens/  LibGDX screens and BattleController        (Weeks 3–5)
  render/   AstronautActor, EnemyActor, backgrounds, effects
  audio/    AudioManager
lwjgl3/     Desktop launcher only
```
 
Rule: `console` and `screens` call `model` and `logic`; never the other way round. The same battle logic powers both versions.
 
### OOP pillars
 
| Pillar | Where |
|---|---|
| Encapsulation | Private fields in `Entity`, `Player`, `Question`; HP changes only through `takeDamage` and `heal`; no `setHp` |
| Inheritance | 4 aliens extend `Entity`; 4 question types extend `Question`; battle modes extend `BattleMode`; power-ups extend `PowerUp` |
| Polymorphism | `Combatant` holds a player or an alien; each question overrides `checkAnswer`; each mode overrides `submitAnswer` |
| Abstraction | `Combatant`, `QuestionProvider` and `UnlockCondition` interfaces; abstract `Entity`, `Question`, `BattleMode`, `PowerUp` |
 
Class diagram: `docs/uml/class-diagram.puml` (v1, updated to v2 in S08 and v3 in S18).
 
## 10. Milestones
 
| Gate | Session | Delivers | Tag on `main` |
|---|---|---|---|
| Gate 1 | S04 | All model classes and the parser compile; 10+ tests pass | — |
| Gate 2 | S08 | Console game playable from a lesson file, solo and squad; AI provider with fallback | `v0.1-console` |
| Gate 3 | S12 | One full solo battle playable in the GUI | `v0.2-gui-alpha` |
| Gate 4 | S16 | Feature freeze: animation, audio, hazards, outfits, squad | `v0.3-feature-complete` |
| Gate 5 | S20 | Tested, documented, runnable JAR, slides, demo video | `v1.0` |
 
20 sessions, 3 tasks each (one per member). Session dates and every task's steps and "Done when" are in the Timeline sheet.
 
## 11. Team and process
 
| Item | Rule |
|---|---|
| Ownership | One task per member per session, from the Timeline sheet |
| Review rotation | Lyle reviews Jhon Moises, Angelo reviews Lyle, Jhon Moises reviews Angelo |
| Branches | `main` = releases only; `dev` = finished work (protected); one `feature/`, `fix/`, `test/` or `docs/` branch per task |
| Commits | PR title = the sheet's commit message, `[Sxx] type(scope): summary`; task PRs use Squash and merge |
| Releases | PR from `dev` into `main` with a merge commit, then a tag |
| Protection | PR and 1 approval required on `dev` and `main`; no force pushes or deletions |
| Tools | VS Code only; Live Share for short pairing; Claude reviews work and guides step by step |
 
## 12. Acceptance criteria for v1.0
 
- [ ] Any valid lesson file produces questions of all four types; an invalid one shows its line number.
- [ ] Solo and Squad battles can be played start to finish in the console and in the GUI.
- [ ] Damage, streaks, power-ups and stars follow the numbers in this document.
- [ ] The debrief shows every missed question with its correct answer.
- [ ] The game runs with Wi-Fi off; AI failures fall back silently to local questions.
- [ ] Settings persist after restarting.
- [ ] All JUnit tests pass; the manual test checklist passes on two laptops.
- [ ] The JAR runs on a fresh computer.
- [ ] README, user guide, final UML, slides and demo video are complete.
- [ ] `v1.0` is tagged on `main` with a GitHub Release containing the JAR.
## 13. Risks
 
| Risk | Impact | Mitigation |
|---|---|---|
| Schedule slip (S03 merged about 2 weeks after its planned date) | GUI features cut | Keep console-first; protect Gates 2 and 3; apply the cut order early |
| AI free-tier limits or outages | No AI questions | `LocalProvider` fallback is P0; AI is optional |
| Merge conflicts on shared files (`BattleController`, `BattleMode`) | Lost time | Pull `dev` before starting; owners merge first; separate controller from screen |
| Art and audio take longer than code | Unpolished GUI | Layered, tinted sprites; free assets with licenses in `CREDITS.md` |
| A member falls behind | Uneven contribution | Review within 24 hours; "(part 1)" commits; reassign in the sheet when needed |
 
## 14. Current status (Nov 9, 2026)
 
| Session | Merged on `dev` |
|---|---|
| S01 | LibGDX project setup; lesson format + sample lesson; `QuestionType`, `Combatant`, `Entity` |
| S02 | `Player`; 4 alien subclasses; UML v1 |
| S03 | `TxtParser`, `LessonData`, `Term`, `InvalidLessonFormatException`; abstract `Question`; `IdentificationQuestion`, `MultipleChoiceQuestion` |
| Next (S04) | `QuestionGenerator` (Jhon Moises), `EnumerationQuestion` + `ReasoningQuestion` (Lyle), JUnit 5 tests (Angelo) |