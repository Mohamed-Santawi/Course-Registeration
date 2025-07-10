HEAD
# Course-Registeration

# Course Registration System

A Spring Boot MVC web application for university course registration and management.

## Features

- Student and admin roles
- Course registration, schedule management, and enrollment conflict detection
- Admin dashboard for managing courses, programs, users, and schedules
- Responsive, RTL-friendly UI (Arabic support)
- REST API for SPA-style enrollment
- MySQL database, JPA, Spring Security, Thymeleaf

## Setup

1. **Clone the repository**
2. **Configure MySQL**
   - Create a database named `ex4`
   - Update `src/main/resources/application.properties` with your MySQL username and password
3. **Build and run**
   ```sh
   mvn clean package
   java -jar target/course-registration-system-0.0.1-SNAPSHOT.jar
   ```
4. **Access the app**
   - Visit [http://localhost:8080](http://localhost:8080)

## Testing

- **Unit and integration tests:**
  ```sh
  mvn test
  ```

## Docker Deployment

1. **Build the Docker image:**
   ```sh
   docker build -t course-registration-system .
   ```
2. **Run the container:**
   ```sh
   docker run -p 8080:8080 --env SPRING_DATASOURCE_URL=jdbc:mysql://host.docker.internal:3306/ex4 --env SPRING_DATASOURCE_USERNAME=root --env SPRING_DATASOURCE_PASSWORD=your_mysql_password course-registration-system
   ```
   - Adjust MySQL connection details as needed.

## Notes

- Default admin user: `admin` (set password hash in DataInitializer)
- Profile image uploads are stored in the `uploads` directory
- For production, set secure passwords and review security settings
 2bac376 (first)
