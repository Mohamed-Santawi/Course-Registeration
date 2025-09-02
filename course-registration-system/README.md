# Course Registration System

A Spring Boot web application for managing college course registrations, built with Thymeleaf and MySQL.

---

## Prerequisites

- **Java JDK 17+** (or the version specified in `pom.xml`)
- **Maven** (for building the project)
- **MySQL Server** (running locally or accessible remotely)

---

## Setup Instructions

### 1. Clone or Extract the Project

- Download and extract the project ZIP, or clone the repository if provided.
- Open a terminal and navigate to the `course-registration-system` directory.

### 2. Set Up the MySQL Database

1. **Install MySQL** if not already installed.
2. **Create a new database** (e.g., `course_registration`).
3. **Create a user** and grant privileges, or use the default root user.
4. **Import the provided SQL file** (if any):

   ```sh
   mysql -u root -p course_registration < database_migration.sql
   ```

   (You may need to adjust the username and database name as needed.)

### 3. Configure Database Connection

- Open `src/main/resources/application.properties`.
- Update the following properties to match your MySQL setup:

  ```properties
  spring.datasource.url=jdbc:mysql://localhost:3306/course_registration
  spring.datasource.username=YOUR_MYSQL_USERNAME
  spring.datasource.password=YOUR_MYSQL_PASSWORD
  ```

### 4. Build the Project

In the terminal, run:

```sh
mvn clean package
```

### 5. Run the Application

After building, run:

```sh
mvn spring-boot:run
```

**OR**

```sh
java -jar target/course-registration-system-0.0.1-SNAPSHOT.jar
```

### 6. Access the Application

- Open your browser and go to: [http://localhost:8080](http://localhost:8080)

---

## Troubleshooting

- **Port already in use?** Change the server port in `application.properties`:
  ```properties
  server.port=8081
  ```
- **Database connection errors?** Double-check your MySQL credentials and that the database exists.
- **Missing tables?** Ensure you have run the SQL migration file.

---

## Notes

- If you need to change the database name, username, or password, update both the MySQL setup and `application.properties`.
- For any issues, please contact the developer.