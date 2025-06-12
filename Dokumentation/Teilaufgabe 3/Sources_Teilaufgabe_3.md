# Netzwerk
### Kurzbeschreibung der übernommenen Teile:
- Die komplette Netzwerkkommunikation (ClientCommunicator im Paket client.networking) wurde gegenüber Teilaufgabe 2 nicht funktional verändert. Es wurden jedoch umfangreiche Logging-Ausgaben ergänzt, um Fehler, Status und wichtige Ereignisse wie Registrierung, Map-Transfer, und Zugübertragung nachvollziehbar zu machen. Alle Log-Levels wurden an relevanten Stellen im Netzwerkablauf eingefügt.

### Quellen der übernommenen Teile:
- Netzwerkprotokoll
- Technische Umsetzung Netzwerkprotokoll
- Vorlesungsfolien

# Benutzeroberfläche & MVC
### Kurzbeschreibung der übernommenen Teile:
- Für **TR-Mode** wurde die Klasse `MapVisualisator` entwickelt, die die Karte und Spielfiguren mit Unicode-Emojis (UTF-8) im Terminal (CLI) darstellt.
- Für den **GUI-Mode** wurde die Klasse `SwingMapVisualisator` neu entworfen: Sie visualisiert die Karte mit Farben und Emojis (Unicode) im Fenster und stellt darunter dynamisch die wichtigsten Spielinformationen (`SwingGameInfoVisualisator`) als Labels dar.
- Die GUI-Visualisierung basiert auf eigenen Ideen, orientiert sich aber an der Server-GUI (aus der Kursvorlage), was Layout und Spielfeldauswahl angeht. Die Auswahl und Gestaltung der Symbole (Emojis, Farben) wurde selbstständig recherchiert.
- Die Architektur (MVC-Pattern):  
    - **Model:** z. B. `GameInfo`, `TechnicalInfo`, `DiscoveryTracker`, `PlayerPositionTracker`, `GameStateTracker`
    - **View:** `MapVisualisator`, `SwingMapVisualisator`, `SwingGameInfoVisualisator`, `TechnicalInfoVisualisator`
    - **Controller:** `GameController`
- Für die **Modell-View-Kopplung** wurde zunächst ein eigenes Observer-Pattern entwickelt, später jedoch durch das Standard-Java-Interface `PropertyChangeListener` ersetzt. Die finale Lösung basiert auf dem Vorbild im Beispielprojekt (`PropertyChangeSupport`), eigenen Recherchen sowie Online-Tutorials.
- Die Trennung von Logik und Visualisierung wurde gezielt weiterentwickelt; es gibt jedoch noch Verbesserungspotenzial bezüglich “Single Responsibility” des Controllers (Refactoring vorgesehen).


### Quellen der übernommenen Teile:
- Beispielprojekt für MVC
- [Java Swing MVC - StackOverflow](https://stackoverflow.com/questions/46253722/java-swing-mvc-best-practice-to-implement-modelhow-to-display-emoji-in-java-swing)
- [Youtube Java Swing playlist](https://www.youtube.com/watch?v=4BRUmU-ETRk)
- [Emoji Search](https://emojipedia.org/)
- Vorlesungsfolien
- [Java PropertyChangeListener Explained](https://docs.oracle.com/javase/tutorial/uiswing/events/propertychangelistener.html)
- [Java Observer Pattern Tutorial](https://medium.com/@p.osinaga/using-observer-pattern-in-java-2fb6621bc0ce)
- [Swing Tutorial](https://docs.oracle.com/javase/tutorial/uiswing/index.html)

# Kartengenerierung
### Kurzbeschreibung der übernommenen Teile:
- Die Kartengenerierung (`HalfMapGenerator`) und Validierung (`HalfMapValidator`) wurden wie in Teilaufgabe 2 umgesetzt. Die Erreichbarkeit wird weiterhin mit Flood-Fill geprüft, Anpassungen wie z. B. mehr Berge orientieren sich an Beobachtungen aus dem Server Webseite.

### Quellen der übernommenen Teile:
- Spielidee
- Vorlesungsnotizen

# Wegfindung
### Kurzbeschreibung der übernommenen Teile:
- Die Wegfindung (Pfadberechnung per Dijkstra in `PathGenerator`) wurde optimiert: Korrektes Handling von Bergen, gezielte Suche im eigenen HalfMap-Bereich, laufendes Tracking von entdeckten Feldern und möglichst frühe Enemyforterkennung. Die Bewegung wird weiterhin mit `MoveCalculator` und `TargetSearcher` geplant. (und in GameController, aber muss refactroed sein)


### Quellen der übernommenen Teile:
- Vorlesungsnotizen
- Spielidee

# Datenstrukturen
### Kurzbeschreibung der übernommenen Teile:
- Die zentralen Datenstrukturen (`Coordinate`, `Field`, `HalfMap`, `ClientFullMap` etc.) sowie Enums wurden analog Teilaufgabe 2 genutzt und nicht wesentlich verändert.


### Quellen der übernommenen Teile:
- Spielidee
- Netzwerkprotokoll
- Vorlesungsfolien

# Business Logik (Logik/Algorithmen, die nicht zu den vorherigen Themen passen)
### Kurzbeschreibung der übernommenen Teile:
- Die Spiellogik (Steuerung, Zustandswechsel, Zielwahl, Zeitbegrenzung) befindet sich wie gehabt in `GameController` und wurde weiterentwickelt (bspw. Logging und Fehlerbehandlung). 

### Quellen der übernommenen Teile:
- Spielidee
- Netzwerkprotokoll
- Technische Umsetzung Netzwerkprotokoll

# Qualitätsfaktoren - Logging
### Kurzbeschreibung der übernommenen Teile:
- Logging wurde an allen kritischen Stellen ergänzt (z. B. bei Netzwerkaktionen, Spieleraktionen, Fehlerfällen, Zustandswechsel, Kartengenerierung, Pfadfindung). Es wurden verschiedene Log-Level und das Framework `slf4j` genutzt. Basis für die Implementierung waren das gegebene Projekt, die SE1-Foliensammlung, das Tutorial, sowie eigene Recherchen.

### Quellen der übernommenen Teile:
- [SLF4J Logging Framework](https://www.slf4j.org/manual.html)
- Übungsfolien SE1 SS25
- Beispielprojekt
- Notizen

# Qualitätsfaktoren - Fehlerbehandlung
### Kurzbeschreibung der übernommenen Teile:
- Fehlerbehandlung wurde systematisch an kritischen Stellen integriert (Registrierung, ungültige Moves, Netzwerkkommunikation, Kartengenerierung). Dazu wurden eigene Exceptions (`PlayerRegistrationException`, `InvalidCoordinateException`) verwendet. 

### Quellen der übernommenen Teile:
- Übungssfolien SE1 SS25 (Fehlerbehandlung)
- Beispielprojekt
- Übungsnotizen für Fehlerbehandlung

# Qualitätsfaktoren - Testing
### Kurzbeschreibung der übernommenen Teile:
- Die Tests (für Kartengenerierung, Map-Validierung, Wegfindung, Datenmodelle, Views) wurden weiter ausgebaut, um die Testabdeckung zu steigern (>65%). Es wurden verschiedene Testmethoden und -tools (JUnit, Mocking) angewandt. Als Basis dienten Vorlesung, Tutorials und das Beispielprojekt zum Thema.

### Quellen der übernommenen Teile:

- Beispielprojekt
- Übungsnotizen für Testing
- [Best Practices Mockito](https://medium.com/@keenny2543/best-practices-for-java-testing-with-mockito-2040251d4b1f)
- [Best Practices Unit Tests in Java](https://www.baeldung.com/java-unit-testing-best-practices)

# Sonstige Quellen (welche nicht zu den vorherigen Punkten eingeordnet werden können)
### Kurzbeschreibung der übernommenen Teile:
- Für spezielle Funktionalitäten wie Konsolenausgabe-Redirect (Testausgaben mit `ByteArrayOutputStream`), Threadsicherheit (`AtomicReference`, `AtomicBoolean`) und GUI-Events (Swing) wurden ergänzende Tutorials gesucht. (in den Tests genutzt)


### Quellen der übernommenen Teile:
- [Java ByteArrayOutputStream](https://www.tutorialspoint.com/java/java_bytearrayoutputstream.htm)
- [Java AtomicReference](https://stackoverflow.com/questions/3964211/when-to-use-atomicreference-in-java)
- [Java AtomicBoolean](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/atomic/AtomicBoolean.html)
- [SwingUtilities.invokeLater](https://docs.oracle.com/javase/8/docs/api/javax/swing/SwingUtilities.html)