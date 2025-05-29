**Documentation for User Stories 009, 010, and 011**

---

### 1. Overview

This documentation provides a comprehensive technical overview of the implementation of user stories 009 (Manage Team Membership), 010 (Team Management), and 011 (Create Teams) in the ToDoList application. The application is built using Spring Boot and utilizes a PostgreSQL database for data persistence. This document details the defined paths (endpoints), classes and methods, Thymeleaf templates, tests, and code explanations associated with these user stories.

### 2. User Story 009: Manage Team Membership

**Description**: As a user, I will be able to add or remove myself from any existing teams to participate or stop participating in them.

#### 2.1 Defined Paths (Endpoints)

- **Add User to Team**:
    - **Path**: `/teams/{teamId}/addUser `
    - **Method**: `POST`
    - **Description**: Endpoint to add a user to a specified team.

- **Remove User from Team**:
    - **Path**: `/teams/{teamId}/removeUser `
    - **Method**: `POST`
    - **Description**: Endpoint to remove a user from a specified team.

#### 2.2 Classes and Methods

- **Controller**: `TeamsController`
    - **Methods**:
        - `addUser ToTeam(Long teamId, Long userId)`: Adds a user to the specified team.
        - `removeUser FromTeam(Long teamId, Long userId)`: Removes a user from the specified team.

- **Service**: `EquipoService`
    - **Methods**:
        - `añadirUsuarioAEquipo(Long teamId, Long userId)`: Adds a user to the specified team.
        - `eliminarUsuarioDeEquipo(Long teamId, Long userId)`: Removes a user from the specified team.

- **Repository**: `EquipoRepository`
    - **Methods**:
        - `save(Equipo equipo)`: Saves the team entity to the database.
        - `findById(Long id)`: Retrieves a team by its ID.

#### 2.3 Thymeleaf Templates

- **Team Membership Management**: `teamsList.html`
    - Displays a list of teams the user is a member of, with options to add or remove themselves from teams.

### 3. User Story 010: Team Management

**Description**: As an administrator, I can rename and delete teams to adapt them to the projects and structure of the company.

#### 3.1 Defined Paths (Endpoints)

- **Rename Team**:
    - **Path**: `/teams/{teamId}/rename`
    - **Method**: `POST`
    - **Description**: Endpoint to rename a specified team.

- **Delete Team**:
    - **Path**: `/teams/{teamId}/delete`
    - **Method**: `POST`
    - **Description**: Endpoint to delete a specified team.

#### 3.2 Classes and Methods

- **Controller**: `TeamsController`
    - **Methods**:
        - `renameTeam(Long teamId, String newName)`: Renames the specified team.
        - `deleteTeam(Long teamId)`: Deletes the specified team.

- **Service**: `EquipoService`
    - **Methods**:
        - `renombrarEquipo(Long teamId, String newName)`: Renames the specified team.
        - `eliminarEquipo(Long teamId)`: Deletes the specified team.

- **Repository**: `EquipoRepository`
    - **Methods**:
        - `deleteById(Long id)`: Deletes a team by its ID.

#### 3.3 Thymeleaf Templates

- **Rename Team Form**: `editTeam.html`
    - Contains a form for administrators to input the new name for the team.

- **Team Management**: `teamsList.html`
    - Displays a list of teams with options for administrators to rename or delete teams.

### 4. User Story 011: Create Teams

**Description**: As a user, I will be able to create new teams to facilitate collaboration and organization within the application.

#### 4.1 Defined Paths (Endpoints)

- **Create Team**:
    - **Path**: `/teams/create`
    - **Method**: `POST`
    - **Description**: Endpoint to create a new team.

#### 4.2 Classes and Methods

- **Controller**: `TeamsController`
    - **Methods**:
        - `createTeam(TeamData teamData)`: Handles the creation of a new team.

- **Service**: `EquipoService`
    - **Methods**:
        - `crearEquipo(String nombre)`: Creates a new team and returns the created team data.

- **Repository**: `EquipoRepository`
    - **Methods**:
        - `save(Equipo equipo)`: Saves the new team entity to the database.

#### 4.3 Thymeleaf Templates

- **Create Team Form**: `create-team.html`
    - Contains a form for users to input the name of the new team they wish to create.

### 5. Tests

#### 5.1 Unit Tests

- **Test Class**: `EquipoServiceTest`
    - **Tests**:
        - `testCrearEquipo()`: Tests the creation of a new team.
        - `testAñadirUsuarioAEquipo()`: Tests adding a user to a team.
        - `testEliminarUsuarioDeEquipo()`: Tests removing a user from a team.
        - `testRenombrarEquipo()`: Tests renaming a team.
        - `testEliminarEquipo()`: Tests deleting a team.

#### 5.2 Integration Tests

- **Test Class**: `TeamsControllerTest`
    - **Tests**:
        - `testCreateTeam()`: Tests the endpoint for creating a new team.
        - `testAddUser ToTeam()`: Tests the endpoint for adding a user to a team.
        - `testRemoveUser FromTeam()`: Tests the endpoint for removing a user from a team.
        - `testRenameTeam()`: Tests the endpoint for renaming a team.
        - `testDeleteTeam()`: Tests the endpoint for deleting a team.
        - `testAddUser ToTeamNotFound()`: Tests adding a user to a non-existent team.
        - `testRemoveUser FromTeamNotFound()`: Tests removing a user from a non-existent team.
        - `testRenameTeamNotFound()`: Tests renaming a non-existent team.
        - `testDeleteTeamNotFound()`: Tests deleting a non-existent team.

### 6. Code Explanations

#### 6.1 TeamsController

The `TeamsController` class handles HTTP requests related to team management. It includes methods for creating, renaming, adding users to, and removing users from teams. Each method is annotated with appropriate HTTP method annotations (e.g., `@PostMapping`) to define the request type.

#### 6.2 EquipoService

The `EquipoService` class contains the business logic for managing teams. It interacts with the `EquipoRepository` to perform CRUD operations on team entities. The service methods ensure that the necessary validations are performed before executing any database operations.

#### 6.3 Thymeleaf Templates

The Thymeleaf templates provide the user interface for team management. They include forms for creating and renaming teams, as well as displaying lists of teams with options for users to manage their memberships.

### 7. Database Tables Screenshots

![Tabla Usuarios](assets/Captura_Tabla_Usuarios.png)
![Tabla Tareas](assets/Captura_Tabla_Tareas.png)
![Tabla Equipos](assets/Captura_Tabla_Equipos.png)
![Tabla Equipo_Usuario](assets/Captura_Tabla_Equipo_usuario.png)

### 8. Useful Links

- **GitHub Repository**: https://github.com/JMGanzar/Hands-On-Classes-3
- **Trello's Board**: https://trello.com/b/qwBdSOAF/p2-y-p3-to-do-list-app
- **Docker Image**: https://hub.docker.com/r/jmganzar/p3-todolistapp/tags