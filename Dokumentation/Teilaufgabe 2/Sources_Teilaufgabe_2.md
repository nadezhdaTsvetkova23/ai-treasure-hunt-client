# Netzwerk
### Kurzbeschreibung der übernommenen Teile:
- Die Klasse `ClientCommunicator` im Paket `client.networking` implementiert den gesamten Kommunikationsablauf mit dem Spielserver: Registrierung, HalfMap-Übertragung, Moves, Abfrage des Spielzustands und Empfang der vollständigen Karte. Dabei wurden die bereitgestellten Klassen wie `UniquePlayerIdentifier`, `PlayerRegistration`, `PlayerMove` etc. aus der `messagesbase`-Bibliothek berücksichtigt. Die Mapping-Logik wurde in `MapConverter` ausgelagert.
- Die Implementierung basiert auf den Vorgaben des Netzwerkprotokolls sowie den Vorlesungsnotizen

### Quellen der übernommenen Teile:
- Netzwerkprotokoll
- Technische Umsetzung Netzwerkprotokoll
- Vorlesungsfolien
- Programmierung 2 Folien aus WS22
- [WebClient in Spring Boot](https://medium.com/@ayoubseddiki132/mastering-webclient-in-spring-boot-a-complete-guide-31482263bc92)
- [Spring Boot Reference Guide](https://docs.spring.io/spring-boot/docs/2.0.3.RELEASE/reference/html/index.html)
- [Spring: ResponseEntity](https://docs.spring.io/spring-framework/reference/web/webflux/controller/ann-methods/responseentity.html)
- [Java System.currentTimeMillis() StackOverflow](https://stackoverflow.com/questions/2978598/in-java-will-system-currenttimemillis-always-return-a-value-previous-calls)


# Kartengenerierung
### Kurzbeschreibung der übernommenen Teile:
- Implementierung der Kartengenerierung (`HalfMapGenerator`) und Validierung (`HalfMapValidator`) gemäß Spielidee-Vorgaben
- Eigene Umsetzung des Flood-Fill-Algorithmus zur Erreichbarkeitsprüfung
- Mehrfache Generierungsversuche bis zur gültigen Karte
- Nutzung von Java-Standardbibliotheken (`Collections.shuffle`, `Random`) für die Zufallsverteilung

### Quellen der übernommenen Teile:
- Spielidee
- Vorlesungsnotizen (Tipps für Kartengenerierung und Validierung)
- Programmierung 2 Folien aus WS22 (Java Collections und Streams)
- ADS Folien aus SS23 (Datenstrukturen)
- [Flood-Fill Algo explained](https://www.ktbyte.com/java-tutorial/book/flood-fill)
- [Java Collections.shuffle() examples](https://www.geeksforgeeks.org/collections-shuffle-method-in-java-with-examples/) 
- [Shuffle or randomise](https://www.geeksforgeeks.org/shuffle-or-randomize-a-list-in-java/)
- [Understanding and implementing flood-fill algorithm](https://medium.com/@koray.kara98.kk/understanding-and-implementing-flood-fill-algorithm-60ab81538d54)

# Wegfindung
### Kurzbeschreibung der übernommenen Teile:
- Zur Pfadberechnung wird der Dijkstra-Algorithmus in `PathGenerator` verwendet. Dabei wurde die Bewegungslogik durch Geländearten (z. B. Mountain = 4 Schritte) realistisch implementiert. Der `MoveCalculator` generiert anschließend die Bewegungsschritte. Die Suche nach Schätzen und dem gegnerischen Fort erfolgt mit `TargetSearcher`.

### Quellen der übernommenen Teile:
- Spielidee
- Netzwerkprotokoll
- Vorlesungsnotizen (Tipps für Wegfindung, Targetfindung und Bewewgunglogik)
- ADS Folien aus SS23 
- [Dijkstra Algorithm in Java](https://medium.com/@kirti07arora/dijkstras-algorithm-in-java-a-journey-through-shortest-paths-cc2fd76104b2)

# Datenstrukturen
### Kurzbeschreibung der übernommenen Teile:
- Die zentralen Datenstrukturen wurden eigenständig entworfen:
    - `Coordinate`, `Field`, `HalfMap`, `ClientFullMap` zur Kartenrepräsentation
    - Enums wie `EGameTerrain`, `EFortPresence`, etc. zur übersichtlichen Verarbeitung
    - `GameTracker`, `DiscoveryTracker`, `PlayerPositionTracker`, `GameStateTracker` zur lokalen Spiellogik

### Quellen der übernommenen Teile:
- Spielidee
- Netzwerkprotokoll
- Technische Umsetzung Netzwerkprotokoll
- Vorlesungsfolien
- Programmierung 2 Folien aus WS22
- Abgabegespräch-Notizen

# Business Logik (Logik/Algorithmen, die nicht zu den vorherigen Themen passen)
### Kurzbeschreibung der übernommenen Teile:
- Die Spiellogik in `MainClient` führt die gesamte Spielsteuerung durch (Registrierung, Warten, Schatzsuche, Fortsuche). Die Logik wurde selbst Schritt für Schritt auf Basis der Anforderungen implementiert, inklusive Zustandswechsel, Laufzeitbegrenzung und Zielwahl.

### Quellen der übernommenen Teile:
- Spielidee
- Netzwerkprotokoll
- [Guide To Java Optional](https://www.baeldung.com/java-optional)

# Sonstige Quellen (welche nicht zu den vorherigen Punkten eingeordnet werden können)
### Kurzbeschreibung der übernommenen Teile:
- Konvertierungslogik von `Field`- und Karteninformationen in Netzwerkobjekte (`MapConverter`)
- Visualisierung der Map als Konsole-UI (`Visualisator`) zur Debug-Unterstützung

### Quellen der übernommenen Teile:
- Netzwerkprotokoll
- Technische Umsetzung Netzwerkprotokoll
- [Java console output formatting](https://stackoverflow.com/questions/45298555/formatting-the-output-printing-in-the-console-in-java)
- [Java formating](https://www.geeksforgeeks.org/java-string-format-method-with-examples/)
