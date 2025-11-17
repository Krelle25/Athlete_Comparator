# Athlete Comparator

A Spring Boot application for comparing athletes across different sports, currently supporting MMA fighters and NBA players. The application fetches real-time statistics from ESPN APIs and provides comprehensive comparison features.

## Features

- **MMA Fighter Comparison**: Search and compare UFC fighters with detailed statistics
- **NBA Player Comparison**: Search and compare NBA players with career statistics
- **Head-to-Head Analysis**: Direct comparison of two athletes
- **Real-time Data**: Fetches up-to-date statistics from ESPN APIs
- **RESTful API**: Well-structured REST endpoints for easy integration
- **Docker Support**: Containerized application with Docker and Docker Compose

## Tech Stack

- **Java 21**
- **Spring Boot 3.5.7**
- **Spring Data JPA**
- **MySQL / H2 Database**
- **Maven**
- **Docker**

## Prerequisites

- Java 21 or higher
- Maven 3.9+
- Docker and Docker Compose (for containerized deployment)
- MySQL (optional, H2 is available for development)

## Getting Started

### Running Locally

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd Athlete_Comparator
   ```

2. **Configure the database**
   - Create a `.env` file in the root directory with your database credentials
   - Or update `src/main/resources/application.properties`

3. **Build the application**
   ```bash
   mvnw clean install
   ```

4. **Run the application**
   ```bash
   mvnw spring-boot:run
   ```

   The application will start on `http://localhost:8080`

### Running with Docker

1. **Build and run using Docker Compose**
   ```bash
   docker-compose up --build
   ```

   The application will be available at `http://localhost:8080`

2. **Stop the application**
   ```bash
   docker-compose down
   ```

## API Endpoints

### MMA Endpoints

- `GET /api/mma/search?name={fighterName}` - Search for MMA fighters
- `GET /api/mma/fighter/{fighterId}` - Get fighter details
- `GET /api/mma/compare?fighter1={id1}&fighter2={id2}` - Compare two fighters
- `GET /api/mma/stats/{fighterId}` - Get fighter statistics

### NBA Endpoints

- `GET /api/nba/search?name={playerName}` - Search for NBA players
- `GET /api/nba/athlete/{athleteId}` - Get player details
- `GET /api/nba/compare?athlete1={id1}&athlete2={id2}` - Compare two players
- `GET /api/nba/stats/{athleteId}` - Get player statistics

### Health Check

- `GET /healthz` - Application health check endpoint

## Project Structure

```
src/
├── main/
│   ├── java/org/example/athlete_comparator/
│   │   ├── controller_api/         # REST controllers
│   │   ├── controller_healthz/     # Health check controller
│   │   ├── mma_api/                # MMA-specific controllers
│   │   ├── mma_client/             # ESPN MMA API clients
│   │   ├── mma_dto/                # MMA data transfer objects
│   │   ├── mma_service/            # MMA business logic
│   │   ├── nba_api/                # NBA-specific controllers
│   │   ├── nba_client/             # ESPN NBA API clients
│   │   ├── nba_dto/                # NBA data transfer objects
│   │   ├── nba_service/            # NBA business logic
│   │   └── AthleteComparatorApplication.java
│   └── resources/
│       ├── static/                 # Static web resources
│       ├── js/                     # JavaScript files
│       ├── stylesheet/             # CSS files
│       └── application.properties  # Application configuration
└── test/                           # Test files
```

## Configuration

The application can be configured through `application.properties` or environment variables in the `.env` file:

```properties
# Server Configuration
server.port=8080

# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/athlete_comparator
spring.datasource.username=your_username
spring.datasource.password=your_password

# JPA Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

## Building for Production

1. **Create a production build**
   ```bash
   mvnw clean package
   ```

2. **Run the JAR file**
   ```bash
   java -jar target/Athlete_Comparator-0.0.1-SNAPSHOT.jar
   ```

## Docker Build

The Dockerfile uses a multi-stage build process:
- **Build stage**: Compiles the application using Maven
- **Runtime stage**: Creates a lightweight image with only the necessary runtime dependencies

## Development

### Running Tests
```bash
mvnw test
```

### Code Style
The project follows standard Java conventions and Spring Boot best practices.

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- ESPN API for providing sports statistics
- Spring Boot team for the excellent framework
- Contributors and maintainers

## Contact

For questions or support, please open an issue in the repository.
