# 🗺️ AI Treasure Hunt Client

An autonomous Java client for a turn-based **Treasure Hunt** game, developed as part of the Software Engineering 1 course at the University of Vienna.  
The client connects to a provided server via **HTTP/XML messages** and competes against another AI client to find the treasure and capture the opponent’s fort.  

---

## 🎯 Project Goals
The project simulated the full lifecycle of a **realistic software project**:
- **Requirements Analysis** → Identifying functional & non-functional requirements from the given game description and network protocol.  
- **Architecture Design** → UML-based modeling of packages, classes, and sequence diagrams before coding.  
- **Prototype Development** → Implementing basic networking, map generation, and pathfinding.  
- **Productive Client** → Refactoring for maintainability, introducing MVC, exception handling, logging, and testing.  

The main objectives for the client were:
1. **Generate valid half-maps** according to the rules.  
2. **Communicate with the server** using the specified XML-over-HTTP protocol.  
3. **Autonomously explore the game map** and locate the treasure.  
4. **Navigate efficiently** using pathfinding (Dijkstra, extended with heuristics).  
5. **Capture the enemy fort** to win the game.  
6. **Apply software engineering best practices**:  
   - Clean package structure  
   - Model-View-Controller + Observer pattern for CLI visualization  
   - Custom exceptions for robust error handling  
   - Logging with SLF4J  
   - 65%+ test coverage with JUnit & Mockito  
   - UML-driven design and documentation  

---

<details>
  <summary><strong> Project structure</strong></summary>

- `Source/Client/`
  - `src/main/java/client/`
    - `controller/` — Orchestrates game flow (MVC Controller)
    - `exception/` — Custom checked/unchecked exceptions
    - `gamedata/` — Data models (map, treasure, fort, player state)
    - `main/` — Entry point & argument parsing (Tournament Mode)
    - `map/` — Half-map generation & validation (**OCP + Notification**)
    - `networking/` — Client–server communication (HTTP + XML)
    - `pathfinding/` — Dijkstra + extensions
    - `ui/` — Views: CLI renderer + **Swing** visualizer (MVC View)
  - `src/test/java/client/` — JUnit 5 + Mockito tests
  - `build.gradle` — Gradle build configuration
  - `settings.gradle`
</details>

## ✨ Features
- Half-map generation with validation (Flood-Fill reachability, terrain quotas, OCP-friendly rule checks)
- Pathfinding with terrain costs (mountain steps), target selection, and fallback strategies
- HTTP/XML networking (registration, map upload, moves, status polling)
- Two visualizations:
  - **CLI (UTF-8):** compact map + key state; always used in **TR** mode
  - **Swing GUI (optional):** colored grid and live labels (never started in TR)
- Quality practices: structured logging, custom checked/unchecked exceptions, 65%+ test coverage target

## 🛠️ Technologies & Tools
- **Java 21**, **Gradle**
- **JUnit 5 & Mockito** (unit tests & mocking)
- **SLF4J** (structured logging)
- **CLI visualization** with UTF-8 emojis/symbols
- **Swing GUI** (optional visualization; disabled in TR mode)
- **UML** (class + sequence diagrams for design)

## 🧱 Architecture & Best Practices
- **MVC throughout UI**: `Model` (gamedata & trackers), `View` (CLI + Swing), `Controller` (game loop/orchestration).
- **Notification pattern** for map-validation feedback (technical internals to stderr), cleanly separated from normal output.
- **OCP in map-rules**: rule checks are extensible; new rules don’t modify existing logic.
- **Pathfinding**: Dijkstra with terrain costs, target selection, and safety constraints (e.g., avoid water).
- **Network abstraction**: converter layer isolates internal models from protocol DTOs (no DTOs leaking into logic).
- **Error handling**: custom checked & unchecked exceptions; fail fast with clear messages.
- **Logging**: SLF4J with meaningful levels for registration, moves, state transitions, and errors.
- **Testing**: JUnit + Mockito, including negative, data-driven, and mock-based tests; coverage goal ≥ 65%.

## 🚀 Build & Run

### Build
```bash
# From Source/Client
./gradlew clean build
# Jar output path depends on your Gradle config, typically:
# build/libs/client-<version>.jar
```
### Run — Tournament (TR) mode (CLI only, required by course)
```bash
java -jar build/libs/client-<version>.jar TR <BaseServerUrl> <GameID>
# Example:
# java -jar build/libs/client.jar TR http://swe1.wst.univie.ac.at:18235 6aDj2
```
- TR mode uses the course protocol, never launches Swing, and prints game output to stdout; technical validation info to stderr.
- ⚠️ The client only works if a game server is running at <BaseServerUrl>.
- If no opponent joins your game, you can start a second client instance with the same GameID to play against yourself.

### Run — GUI mode (optional Swing demo)
```bash
java -jar build/libs/client-<version>.jar GUI <BaseServerUrl> <GameID>
```
- GUI mode renders a Swing window (map + info panel).
- Do not use GUI mode when connecting to the evaluation server; it is for local demos only.

### ✅ Testing
```bash
./gradlew test
```
- Includes unit tests for: map generation/validation, pathfinding, data models, and views (where applicable).
- Contains data-driven tests, negative tests, and Mockito-based mock tests.
- Coverage target: ≥ 65% (measured via IDE coverage tools in Eclipse).

### Visuals

**UML — Class Diagram**  
_Overall structure of the client (MVC, networking, map, pathfinding, UI, etc.)._  
<img width="1830" height="1277" alt="image" src="https://github.com/user-attachments/assets/37d5927d-cc9c-4d86-9cc9-1218540ef3f3" />

**UML — Sequence Diagram (Scenario 1)**  
_Generic client is on turn, our client has not yet found its treasure. Shows calculation and transmission of the next move._  
<img width="1258" height="664" alt="image" src="https://github.com/user-attachments/assets/93f61a90-3e54-4c97-bf3b-d73fed3d2f54" />

**UML — Sequence Diagram (Scenario 2)**  
_Our client accidentally sends an incorrect map; diagram shows the process from sending until termination._  
<img width="734" height="490" alt="image" src="https://github.com/user-attachments/assets/22fac908-f5cc-4de9-85cc-666c3590211c" />

**CLI Output (excerpt)**  
_Example of textual game output in TR mode (client-server interaction, status updates, moves)._  
<img width="477" height="343" alt="image" src="https://github.com/user-attachments/assets/998593e2-7af5-4a77-9c1a-ae7ce31695e3" />

**GUI Representation**  
_Game in progress: the first player has found the treasure and is moving toward the opponent’s fortress. Visualized using Swing with UTF-8 icons._  
<img width="2048" height="862" alt="image" src="https://github.com/user-attachments/assets/447cc0e6-27ce-4cef-8817-0fab0a944982" />

### Learning Outcomes

This project gave me practical experience in building a complete software system from design to production. Key takeaways:

- **Architecture & Design**  
  - Applied the **MVC pattern** for a clean separation of concerns.  
  - Modeled the system with **UML class and sequence diagrams**, validating design before coding.  
  - Used solid design principles like encapsulation and the Open/Closed Principle for maintainable code.

- **Networking & Protocols**  
  - Built a **client-server communication** workflow using REST and XML messages.  
  - Ensured full compliance with a predefined protocol so the client could play against any opponent.

- **Algorithms & Problem-Solving**  
  - Implemented **map generation** with validation rules and randomization.  
  - Integrated **pathfinding (Dijkstra + extensions)** to navigate efficiently and handle edge cases.  

- **Software Quality**  
  - Wrote **JUnit and Mockito tests**, reaching >65% coverage.  
  - Applied **logging** and **custom exception handling** for robust and debuggable execution.  
  - Practiced **refactoring** to improve readability and maintainability.

- **User Interaction**  
  - Developed both a **CLI visualization** (tournament mode) and a **Swing-based GUI**.  
  - Used **UTF-8 symbols** and structured output for an engaging player experience.  

> Overall, I strengthened my ability to design, implement, and refine complex systems — combining algorithms, architecture, and best practices into a production-ready client.
