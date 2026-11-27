# Cosmic Quest — Migration and Handoff Guide
 
This file hands the project over so it can be finished by one person (Lyle) from S04 to S20, with or without an AI assistant. Read it once from top to bottom, then use it as a checklist. Product rules and numbers live in `docs/PRD.md`; task steps live in the Timeline sheet.
 
| | |
|---|---|
| **Snapshot date** | Nov 9, 2026 |
| **Repository** | github.com/lyle-leif/Cosmic-Quest |
| **Default branch** | `dev`, last commit `2f2663f` `[S03] feat(model): add IdentificationQuestion and MultipleChoiceQuestion (#14)` |
| **`main`** | `e7ad2c9` (untouched until the S08 release; no tags yet) |
| **Open branches** | None besides `dev` and `main` |
| **Done** | S01, S02, S03 (9 of 60 tasks) |
| **Remaining** | S04 to S20 (51 tasks) |
| **Timeline sheet** | docs.google.com/spreadsheets/d/1jvS93DGnE-Jj2XWtLfAHs1KAP9UI9iiShteQm3_UNT8 |
 
---
 
## 1. Working solo: what changes
 
| Area | Before (team) | Now (solo) | Action |
|---|---|---|---|
| PR approval | Ruleset "Rules" requires 1 approval on `dev` and `main`, nobody can bypass | You can't approve your own PR, so every merge is blocked | Settings → Rules → Rulesets → **Rules** → Require a pull request → set **Required approvals to 0**. Keep "Require a pull request" on so every task stays one squashed commit. |
| Review | Rotation (Lyle → JM → Angelo → Lyle) | No second reviewer | Self-review with the PR checklist (section 7) before merging; run `.\gradlew core:test` every time |
| Sheet ownership | Member column = who does it | You do the rest | Change the **Member** column for every task you take over, so the sheet matches the commit authors. Reviewer column: write `self`. |
| Commit author | Each member | You | Commit as yourself only; no co-author lines |
| Session pace | 3 tasks per session (one each) | 51 tasks for one person | Follow the cut order in section 9 early; protect Gate 2 (console) and Gate 3 (GUI solo battle) first |
 
Repo merge settings stay as they are: **only squash merging is allowed**. For the 4 releases into `main`, turn on "Allow merge commits" (Settings → General → Pull Requests), merge the release, then turn it off again.
 
---
 
## 2. Environment
 
| Tool | Version / setting |
|---|---|
| JDK | Eclipse Temurin 21; `JAVA_HOME` points to the JDK 21 folder |
| Build | Gradle wrapper 9.8.0 (always `.\gradlew` in PowerShell) |
| LibGDX | 1.14.2, LWJGL3 3.4.3 (`gradle.properties`) |
| Editor | VS Code with Extension Pack for Java and Gradle for Java |
| Package | `com.neurolink.cosmicquest` |
| Source root | `core/src/main/java/com/neurolink/cosmicquest/` |
| Test root (from S04) | `core/src/test/java/com/neurolink/cosmicquest/` |
 
| Command | Use |
|---|---|
| `.\gradlew core:compileJava` | Compile the game code (no output = success) |
| `.\gradlew core:test` | Run all JUnit tests (from S04) |
| `.\gradlew lwjgl3:run` | Open the game window |
| `.\gradlew lwjgl3:jar` | Build the runnable JAR into `lwjgl3/build/libs` (S19) |
 
---
 
## 3. Decisions already made (keep them)
 
These differ from, or add to, the original sheet text. New code must fit them.
 
| Topic | Decision |
|---|---|
| Negative numbers | `Entity.takeDamage`, `Player.takeDamage`, `heal`, `activateShield`, `addScore` throw `IllegalArgumentException` on negative input |
| Shield | `Player.activateShield(int turns)` exists; the S06 Shield power-up calls `activateShield(1)` |
| Level check | Aliens validate the level with a `private static int checkLevel(int)` called inside `super(...)`, because Java 21 forbids statements before `super(...)`. Reuse this pattern whenever a constructor must validate before calling `super`. |
| Question fields | `Question` fields are `final`; no setters; difficulty 1–3 validated; blank text or answer rejected |
| Normalizing | `Question.normalize(s)`: trim, lower case, collapse whitespace; `null` becomes `""` |
| Multiple choice | Exactly 4 non-blank options, `correctIndex` 0–3, options stored read-only; accepts `a`–`d` in any case or the option text |
| Lesson parsing | Split at the first `::` with `indexOf`; `[ENUM]` checked before terms; empty term, definition, title or item rejected |
| Exception type | `InvalidLessonFormatException` is a **checked** exception with `getLineNumber()`; callers must catch it and show the message |
| Read-only data | `LessonData` returns unmodifiable lists and map, in file order (`LinkedHashMap`) |
| Lesson sample | `Instance` was replaced with `Static` (Object and Instance were ambiguous) |
| UML | `docs/uml/class-diagram.puml` uses packages, `<<Sxx>>` tags for planned classes, `left to right direction` (keeps the PNG under PlantUML's 4,096 px limit). v2 drops the `<<Sxx>>` tags because every class is built. Render without Graphviz: `java -jar plantuml.jar -Playout=smetana -tpng docs/uml/class-diagram.puml` (the jar ships with the VS Code PlantUML extension) |
| Merging | Repo allows squash only; PR title = the sheet's commit message |
 
---
 
## 4. Code that exists now (public API)
 
New classes must call these exactly as written.
 
| Class | Public members |
|---|---|
| `model.QuestionType` (enum) | `IDENTIFICATION`, `REASONING`, `MULTIPLE_CHOICE`, `ENUMERATION` |
| `model.Combatant` (interface) | `takeDamage(int)`, `isDefeated()`, `getHp()`, `getMaxHp()` |
| `model.Entity` (abstract) | `protected Entity(String name, int level, int hp, int maxHp, int attackPower)`; `getName()`, `getLevel()`, `getAttackPower()`, `attack(Combatant target)`; abstract `getQuestionType()`, `getSpecialMove()` |
| `VoidBrute`, `StarWeaver`, `NebulaLich`, `LunarBeast` | `new X(int level)` (1–3) |
| `model.Player` | `new Player(String name)`; `getName()`, `getScore()`, `getStreak()`, `getShieldTurns()`, `heal(int)`, `activateShield(int)`, `addScore(int)`, `increaseStreak()`, `resetStreak()` |
| `model.Question` (abstract) | `protected Question(String questionText, String correctAnswer, int difficulty)`; `getQuestionText()`, `getCorrectAnswer()`, `getDifficulty()`; abstract `checkAnswer(String)`, `getType()`; `protected static normalize(String)` |
| `model.IdentificationQuestion` | `new IdentificationQuestion(String questionText, String correctAnswer, int difficulty)` |
| `model.MultipleChoiceQuestion` | `new MultipleChoiceQuestion(String questionText, List<String> options, int correctIndex, int difficulty)`; `getOptions()`, `getCorrectIndex()` |
| `logic.Term` | `new Term(String term, String definition)`; `getTerm()`, `getDefinition()` |
| `logic.LessonData` | `new LessonData(List<Term>, Map<String, List<String>>)`; `getTerms()`, `getEnumerations()` |
| `logic.TxtParser` | `parseFile(Path) throws IOException, InvalidLessonFormatException` |
| `logic.InvalidLessonFormatException` | `new InvalidLessonFormatException(int lineNumber, String message)`; `getLineNumber()`; message starts with `Line N: ` |
 
Note: `Question` is in `model`, `Term` and `LessonData` are in `logic`. `logic` may import `model`; `model` must never import `logic`.
 
---
 
## 5. Remaining work, in build order
 
Do tasks top to bottom: each one only needs what is above it. Full steps and "Done when" for every task are in the Timeline sheet (column "How to do it").
 
### Week 1–2: console game (Gate 1 at S04, Gate 2 at S08)
 
| # | Session | Task | Commit message | Key notes |
|---|---|---|---|---|
| 1 | S04 | Enumeration and Reasoning questions | `[S04] feat(model): add EnumerationQuestion and ReasoningQuestion` | See section 6.1 for constructors |
| 2 | S04 | QuestionGenerator | `[S04] feat(logic): add QuestionGenerator` | Needs #1; see section 6.2 |
| 3 | S04 | JUnit 5 and first tests | `[S04] test: add JUnit 5 tests for parser and answer checks` | See section 6.3 for Gradle setup. **Gate 1:** 10+ tests pass |
| 4 | S05 | QuestionPool | `[S05] feat(logic): add QuestionPool with shuffle and recycle` | `ArrayDeque`; missed questions go to the back |
| 5 | S05 | DamageCalculator | `[S05] feat(logic): add DamageCalculator with streak bonus` | Static utility class with a private constructor; constants from PRD 7.3 |
| 6 | S05 | ConsoleRenderer | `[S05] feat(console): add ConsoleRenderer for HUD and questions` | Only class that calls `System.out` |
| 7 | S06 | BattleState, TurnResult, abstract BattleMode | `[S06] feat(logic): add BattleState and abstract BattleMode` | See section 6.4 |
| 8 | S06 | PowerUp classes | `[S06] feat(logic): add PowerUp classes` | In `logic`, with `PowerUpInventory`; see section 6.4 (decided) |
| 9 | S06 | SoloBossBattle | `[S06] feat(logic): add SoloBossBattle turn logic` | Every 3rd streak grants a random power-up |
| 10 | S07 | MatchStats and debrief | `[S07] feat(logic): add MatchStats and mission debrief` | Add `protected MatchStats stats` to BattleMode; one `stats.recordAnswer` call in SoloBossBattle |
| 11 | S07 | Team and SquadBossBattle | `[S07] feat(logic): add Team and SquadBossBattle` | Max 3 players; skip defeated players |
| 12 | S07 | Console main menu | `[S07] feat(console): add main menu and lesson loading` | Catch `NumberFormatException` and `InvalidLessonFormatException` |
| 13 | S08 | AI provider with offline fallback | `[S08] feat(ai): add QuestionProvider with Gemini and offline fallback` | Key in `config.properties` (git-ignored); commit only `config.properties.example` |
| 14 | S08 | UML v2 and console guide | `[S08] docs: update UML to v2 and add console guide` | Also rename `class-diagram-v1_1.png` to `class-diagram-v1.png` |
| 15 | S08 | Console gate and release | `[S08] test: console integration tests and bug fixes` | **Gate 2:** release `dev` → `main`, tag `v0.1-console` (section 8) |
 
### Week 3: GUI foundation (Gate 3 at S12)
 
| # | Session | Task | Commit message |
|---|---|---|---|
| 16 | S09 | Game class, Assets, LoadingScreen | `[S09] feat(screens): add CosmicQuestGame, Assets and LoadingScreen` |
| 17 | S09 | UI Skin and fonts | `[S09] feat(ui): add Skin, fonts and UI assets` |
| 18 | S09 | Sprites and TextureAtlas | `[S09] feat(assets): add sprite PNGs and TextureAtlas` |
| 19 | S10 | MainMenuScreen | `[S10] feat(screens): add MainMenuScreen` |
| 20 | S10 | LessonSelectScreen | `[S10] feat(screens): add LessonSelectScreen` |
| 21 | S10 | Parallax backgrounds | `[S10] feat(render): add ParallaxBackground for each environment` |
| 22 | S11 | BattleScreen layout | `[S11] feat(screens): add BattleScreen layout` |
| 23 | S11 | BattleController | `[S11] feat(screens): add BattleController connecting UI to SoloBossBattle` |
| 24 | S11 | AstronautActor | `[S11] feat(render): add AstronautActor with tinted layers` |
| 25 | S12 | EnemyActor | `[S12] feat(render): add EnemyActor with level variants` |
| 26 | S12 | Victory and Game Over screens | `[S12] feat(screens): add VictoryScreen and GameOverScreen` |
| 27 | S12 | GUI gate and release | `[S12] test: GUI solo battle playtest and fixes` → tag `v0.2-gui-alpha` |
 
### Week 4: features (Gate 4 at S16)
 
| # | Session | Task | Commit message |
|---|---|---|---|
| 28 | S13 | Idle animations | `[S13] feat(anim): add idle bob and hover animations` |
| 29 | S13 | Attack and hit animations | `[S13] feat(anim): add attack beam, hit flash and damage numbers` |
| 30 | S13 | Shake, hurt and shield effects | `[S13] feat(anim): add screen shake, hurt and shield effects` |
| 31 | S14 | Settings screen | `[S14] feat(screens): add SettingsScreen with volume sliders` |
| 32 | S14 | AudioManager | `[S14] feat(audio): add AudioManager with music and sound effects` |
| 33 | S14 | Particles (P2) | `[S14] feat(fx): add particle effects for stars, meteors and supernova` |
| 34 | S15 | How to Play and credits | `[S15] feat(screens): add HowToPlayScreen and credits` |
| 35 | S15 | Environment hazards (P2) | `[S15] feat(logic): add BattleEnvironment hazards` |
| 36 | S15 | Outfits and progress (P2) | `[S15] feat(model): add Outfit, UnlockCondition and PlayerProgress` |
| 37 | S16 | Suit Locker (P2) | `[S16] feat(screens): add SuitLockerScreen` |
| 38 | S16 | Squad Battle screen | `[S16] feat(screens): add SquadBattleScreen with turn order` |
| 39 | S16 | Week 4 merge and release | `[S16] test: tests for hazards, unlocks and squad; merge week 4` → tag `v0.3-feature-complete` |
 
### Week 5: polish and ship (Gate 5 at S20)
 
| # | Session | Task | Commit message |
|---|---|---|---|
| 40 | S17 | Screen transitions and UI sounds | `[S17] feat(ui): add screen fade transitions and UI sounds` |
| 41 | S17 | Async AI generation (P2) | `[S17] feat(ai): generate questions asynchronously on the loading screen` |
| 42 | S17 | Edge cases | `[S17] fix: handle bad lessons, no internet, empty answers and API limits` |
| 43 | S18 | Final UML and user guide | `[S18] docs: add final UML v3 and user guide` |
| 44 | S18 | Refactor and Javadoc | `[S18] refactor: clean up BattleMode and controllers; add Javadoc` |
| 45 | S18 | Regression tests | `[S18] test: full regression tests for model and logic` |
| 46 | S19 | Presentation slides | `[S19] docs: add presentation slides` |
| 47 | S19 | Runnable JAR | `[S19] build: create runnable desktop JAR` |
| 48 | S19 | Playtest fixes | `[S19] fix: bugs from classmate playtest` |
| 49 | S20 | README and demo video | `[S20] docs: finalize README with screenshots and demo video link` |
| 50 | S20 | Balance pass | `[S20] chore: tune HP, damage and timers in GameConfig` |
| 51 | S20 | Release v1.0 | `[S20] release: merge dev into main and tag v1.0` |
 
---
 
## 6. Design notes for the next tasks
 
These fill gaps the sheet leaves open, so later classes fit together. They are proposals; change them if you have a better reason, but decide once and keep it.
 
### 6.1 EnumerationQuestion and ReasoningQuestion (S04)
 
| Class | Constructor | `checkAnswer(input)` | Extra |
|---|---|---|---|
| `EnumerationQuestion` | `(String questionText, List<String> items, int difficulty)`; pass `String.join(", ", items)` as the correct answer to `super` | `true` only when every item is named (ratio 1.0) | `getMatchRatio(input)`: split input on commas, normalize, count distinct items matched ÷ item count (3 of 4 = 0.75); `getItems()` read-only |
| `ReasoningQuestion` | `(String questionText, String correctAnswer, List<String> keywords, int difficulty)`; `correctAnswer` = the definition | `true` when matched keywords ÷ keyword count ≥ `KEYWORD_PASS_RATIO` (0.6) | `getKeywords()` read-only; compare on normalized text |
 
Validate lists in a static helper inside `super(...)` (same pattern as `checkLevel`).
 
### 6.2 QuestionGenerator (S04)
 
| Method | Rule |
|---|---|
| `QuestionGenerator(LessonData data)` | Keep `data` in a field |
| `generateIdentification()` | Random term; question text = its definition; answer = its term |
| `generateMultipleChoice()` | Needs at least 4 terms. Correct term + 3 different terms, `Collections.shuffle`, record the index after shuffling |
| `generateEnumeration()` | One per `[ENUM]` entry; question text "List the <title>." |
| `generateReasoning()` | Question text "Explain <term> in your own words."; keywords = 3–5 words from the definition after removing punctuation, words shorter than 4 letters and common words (the, and, that, with, from, which…) |
| `generateQuestions(int count)` | Mixed list; skip a type the lesson can't support (no lists → no enumeration; fewer than 4 terms → no multiple choice) |
 
Pass a `Random` into the constructor (or a second constructor) so tests can use a fixed seed.
 
### 6.3 JUnit 5 setup (S04)
 
Add to `core/build.gradle`:
 
```gradle
dependencies {
  testImplementation platform('org.junit:junit-bom:5.11.3')
  testImplementation 'org.junit.jupiter:junit-jupiter'
  testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
}
 
test {
  useJUnitPlatform()
}
```
 
- Gradle 9 needs the `junit-platform-launcher` line; without it the tests won't start.
- Tests run with `core/` as the working directory, so `Path.of("assets/...")` fails there. Write small lesson files in each test with `Files.createTempFile` + `Files.writeString`, or put copies in `core/src/test/resources/lessons/` and load them with `getClass().getResource(...)`.
### 6.4 Battle core (S05–S07)
 
| Topic | Proposal |
|---|---|
| Package cycle (**decided**, PRD D1) | `PowerUp`, its 4 subclasses and `PowerUpInventory` (3 slots: `add`, `use`, `isFull`, `getPowerUps`) are in `logic`. `Player` holds no power-ups; `BattleMode` keeps one `PowerUpInventory` per player (`Map<Player, PowerUpInventory>`, or one field in Solo). The UML already shows this. |
| BattleMode fields | `protected` so subclasses can use them: `questionPool`, `entity`, `state`, `currentTurn`, `currentQuestion`, `stats` (S07), `doubleDamageNext` (for Double Damage) |
| Hint | Store hidden option indexes in BattleMode (for multiple choice) or the revealed first letter; never change the `Question` object |
| DamageCalculator | `playerDamage(int difficulty, int streak)`, `enemyDamage(Entity)`, `enumerationDamage(int difficulty, int streak, double ratio)`; all static |
| TurnResult | Immutable: `correct`, `damageDealt`, `damageTaken`, `newState` |
| Randomness | Inject `Random` wherever something is random (power-up rewards, alien choice) so tests are repeatable |
 
### 6.5 GUI and AI (S08 onward)
 
- Lessons in the GUI come from `Gdx.files.local("lessons/")`; copy the sample there on first run (S10).
- `config.properties` and `lessons/` must load from next to the JAR (S19).
- Check the current Gemini model name, endpoint and free-tier limits at ai.google.dev before S08; they change often. (Checked in S08: default `gemini-3.5-flash-lite`; GitHub Models was retired on July 30, 2026, so the second provider is Groq. Both model names can be changed in `config.properties`.)
- Never call the AI inside `render()`; use a background thread and `Gdx.app.postRunnable` (S17).
---
 
## 7. Solo workflow per task
 
```powershell
cd "$HOME\Projects\Cosmic-Quest"
git switch dev
git pull
git switch -c <branch from the sheet>
# ... code ...
.\gradlew core:compileJava
.\gradlew core:test            # from S04
git status
git add <only this task's files>
git commit -m "<commit message from the sheet>"
git push -u origin <branch>
```
 
Then on GitHub: PR into **dev** → self-review checklist → **Squash and merge** → check the title box says the exact commit message → Confirm. Clean up:
 
```powershell
git switch dev
git pull
git branch -D <branch>
git log --oneline -1          # copy the hash into the sheet
```
 
**Self-review checklist (before every merge)**
 
- [ ] Title = the sheet's commit message; base = `dev`
- [ ] Only this task's files; no `*Check.java`, `build/`, `.vscode/`, `config.properties`
- [ ] "Done when" from the sheet is true
- [ ] `.\gradlew core:test` passes (from S04)
- [ ] No LibGDX imports in `model` or `logic`; no `System.out` outside `ConsoleRenderer`
- [ ] Javadoc on public classes and methods; Shift + Alt + F to format
- [ ] Sheet updated: Status = Done, Commit hash
Never use `git push --force`. If a PR needs changes, add a commit and push normally.
 
---
 
## 8. Release procedure (S08, S12, S16, S20)
 
| # | Step |
|---|---|
| 1 | All of that gate's tasks merged; `.\gradlew core:test` passes; play the game once |
| 2 | Settings → General → Pull Requests → check **Allow merge commits** |
| 3 | PR from `dev` into `main`, title e.g. `[S08] release: v0.1-console` |
| 4 | Merge with **Create a merge commit** (not squash) |
| 5 | Uncheck **Allow merge commits** again |
| 6 | `git switch main` → `git pull` → `git tag v0.1-console` → `git push origin v0.1-console` |
| 7 | S20 only: create a GitHub Release from `v1.0` and attach the JAR |
 
Tags: `v0.1-console` (S08), `v0.2-gui-alpha` (S12), `v0.3-feature-complete` (S16), `v1.0` (S20).
 
---
 
## 9. Scope control
 
Protect, in order: **Gate 2 (console game)** → **Gate 3 (GUI solo battle)** → Gate 5 deliverables (JAR, README, slides).
 
Cut order if time runs short: **particles → Suit Locker and outfits → environment hazards → AI generation in the GUI**. The offline `LocalProvider` is never cut.
 
---
 
## 10. Known loose ends
 
| Item | Fix |
|---|---|
| UML picture is named `docs/uml/class-diagram-v1_1.png` | Done in S08: renamed to `class-diagram-v1.png`; v2 is `class-diagram-v2.png` |
| UML commit on `dev` is titled `Docs/uml v1 (#11)` | Can't change history; note `556a82e` in the sheet |
| S03 rows in the sheet may still say Not started | Done: `cee9904` (TxtParser), `b56a343` (Question), `2f2663f` (Identification + Multiple Choice) |
| Several files end without a newline | VS Code setting **Files: Insert Final Newline** = on |
| `CosmicQuestGame` still draws the LibGDX placeholder | Replaced in S09 |
| Session Guides doc has tabs S01–S03 only; AI Tutor Briefing PDF shows the old S02 assignment | Treat this file, the PRD and the sheet as the current sources |
 
---
 
## 11. Starting prompt for an AI assistant
 
Paste this together with `docs/PRD.md` and this file when you start a session with an AI tool:
 
```text
I'm continuing Cosmic Quest alone: a Java 21 + LibGDX OOP 101 game in VS Code on Windows (PowerShell, .\gradlew).
Read PRD.md and MIGRATION.md first and follow the decisions in MIGRATION.md sections 3, 4 and 6.
Today's task is [Sxx]: [paste the task row from the Timeline sheet].
Use the exact branch name and commit message from the row. No LibGDX in model or logic.
Explain each step so I understand it, and give me a temporary check I can run before committing.
Do not add yourself as a co-author or contributor in any commit.
```