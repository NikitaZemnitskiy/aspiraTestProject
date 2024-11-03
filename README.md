# LeonParser

## Overview

LeonParser is a Java application designed to fetch, process, and display information about various sports, their leagues, and associated events. It leverages asynchronous programming to efficiently handle data retrieval and ensure responsive performance.

## Features

- **Asynchronous Data Processing:** Utilizes `CompletableFuture` for non-blocking operations.
- **Custom Sorting:** Implements configurable comparators to sort leagues based on predefined priorities.
- **Modular Design:** Separates API interaction, data processing, and display logic into distinct components.
- **Extensible:** Easily add support for new sports and leagues by updating configuration maps.

## Usage

1. **Clone the Repository:**

   ```bash
   git clone https://github.com/yourusername/LeonParser.git
   cd LeonParser
   ```

2. **Build the Project:**

   Ensure you have Java and Maven installed. Then run:

   ```bash
   mvn clean install
   ```

3. **Run the Application:**

   ```bash
   mvn exec:java -Dexec.mainClass="com.zemnitskiy.parser.LeonParser"
   ```

   Or, if packaged as a JAR:

   ```bash
   java -jar target/LeonParser-1.0-SNAPSHOT.jar
   ```

## Customization

- **Adding New Sports:**

  Update the `CURRENT_DISCIPLINES` list in `LeonParser.java` to include the new sport names.

- **Configuring Sort Priorities:**

  Modify the `ComparatorUtils` class to define sorting priorities for regions and leagues based on the sport.

  ```java
  private static final Map<String, Integer> NEW_SPORT_LEAGUE_PRIORITY = Map.ofEntries(
      Map.entry("New League 1", 0),
      Map.entry("New League 2", 1)
  );

  public static Comparator<League> getLeagueComparator(String sportName) {
      return switch (sportName) {
          case "NewSport" ->
              Comparator.comparingInt(league ->
                  NEW_SPORT_LEAGUE_PRIORITY.getOrDefault(league.getName(), Integer.MAX_VALUE)
              );
          // existing cases...
          default -> Comparator.comparingInt(_ -> 0);
      };
  }
  ```

- **Modifying Display Logic:**

  Adjust the `DisplayService` class to change how sports, leagues, and events are presented.

## Contributing

Contributions are welcome! Please fork the repository and create a pull request with your changes.

## License

This project is licensed under the MIT License.

## Contact

For any questions or suggestions, please contact [your.email@example.com](mailto:your.email@example.com).