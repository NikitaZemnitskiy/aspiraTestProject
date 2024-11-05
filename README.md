# LeonParser

## Overview

LeonParser is a Java application designed to fetch, process, and display information about various sports, their leagues, and associated events. It leverages asynchronous programming to efficiently handle data retrieval and ensure responsive performance.

## Features

- **Asynchronous Data Processing:** Utilizes `CompletableFuture` for non-blocking operations.
- **Modular Design:** Separates API interaction, data processing, and display logic into distinct components.
- **Extensible:** Easily add support for new sports and leagues by updating configurations.

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
   mvn exec:java -Dexec.mainClass="com.zemnitskiy.parser.Main"
   ```

   Or, if packaged as a JAR:

   ```bash
   java -jar target/LeonParser-1.0-SNAPSHOT.jar
   ```

## Customization

- **Adding New Sports:**

  Update the `CURRENT_DISCIPLINES` list in `Main.java` to include the new sport names.
  Update the `LEAGUE_COUNT` list in `Main.java` to change TOP leagues counting
  Update the `MATCH_COUNT` list in `Main.java` to change matches counting
  Update the `BASE_URL` list in `Main.java` to change url

- **Modifying Display Logic:**

  Adjust the `ResultPrinter` class to change how sports, leagues, and events are presented.
