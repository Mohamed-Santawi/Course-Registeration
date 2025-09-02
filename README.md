# College Course Registration System

## Prerequisites

- Java 17+ (recommended: Java 21)
- Maven
- Node.js & npm (for frontend)
- MySQL (e.g., via XAMPP/phpMyAdmin)

## Backend Setup

1. **Configure Database**

   - Create a database named `college_db` in MySQL.
   - Update `src/main/resources/application.properties` with your MySQL username and password if needed.

2. **Database Initialization**

   - On first run, the application will automatically create an **admin account** if it does not exist:
     - Username: `admin`
     - Password: `admin` (please change after first login)
     - Role: `ADMIN`
   - **No personal or sensitive data is included.**
   - The app will not create duplicate admin accounts if one already exists.

3. **(Optional) Import SQL Snapshot for Demo/Testing**

   - To populate the database with demo data, import the provided SQL snapshot file (e.g., `college_db_snapshot.sql`) using phpMyAdmin or the MySQL CLI:
     - In phpMyAdmin: Select your database > Import > Choose file > Start import.
   - This is **not required** for the app to start, but is useful for testing with lots of data.

4. **Build and Run Backend**
   ```sh
   cd course-registration-system
   mvn clean install
   mvn spring-boot:run
   ```

## Frontend Setup

1. **Install dependencies and run**
   ```sh
   cd frontend
   npm install
   npm start
   ```
   - The frontend will run on [http://localhost:3000](http://localhost:3000) by default.

## Security & Privacy

- **Do not include any personal details** (e.g., passwords, national IDs, API keys) in your code or submissions.
- After the course, remove any API keys or sensitive data from your project.

## Notes

- The backend will always start with at least the admin account if the DB is empty.
- For a full demo, use the SQL snapshot to pre-populate the database.
- If you have questions or issues, check the logs or contact the project maintainer.
