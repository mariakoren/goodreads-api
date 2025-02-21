# Goodreads-like API

## Overview
This is a REST API application inspired by Goodreads, built using **Java Spring Boot** and **MySQL**. It provides endpoints for managing books, users, and comments, with secure authentication via **OAuth2 and Keycloak**.

## Features
### User Features:
- User registration and login (OAuth2 via Keycloak)
- Edit personal details
- Delete account
- Browse books
- Leave comments on books
- Mark books as "To Read"

### Admin Features:
- Add and remove books
- Manage user accounts
- Delete inappropriate comments
- View statistics

## Technologies Used
- **Backend**: Java Spring Boot
- **Database**: MySQL
- **Authentication**: OAuth2 with Keycloak
- **Build Tool**: Maven

## Installation & Setup
### Prerequisites
- Java 17+
- MySQL database
- Maven
- Keycloak server configured

### Steps to Run the Application
1. Clone the repository:
   ```sh
   git clone https://github.com/mariakoren/goodreads-api.git
   cd goodreads-api
   ```
2. Configure MySQL database:
   - Update `application.properties` with your MySQL credentials.
3. Set up Keycloak:
   - Configure Keycloak realm, clients, and roles.
4. Build and run the project:
   ```sh
   mvn spring-boot:run
   ```


