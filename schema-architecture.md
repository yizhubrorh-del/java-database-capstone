This Spring Boot application uses both MVC and REST controllers. Thymeleaf templates are used for the Admin and Doctor dashboards, while REST APIs serve all other modules. The application interacts with two databases—MySQL (for patient, doctor, appointment, and admin data) and MongoDB (for prescriptions). All controllers route requests through a common service layer, which in turn delegates to the appropriate repositories. MySQL uses JPA entities while MongoDB uses document models.

It's called Smart Clinic Management System, it follows a three-tier web architecture, which separates the system in three layers:

1. Presentation Tier: The user interface made with Thymeleaf templates and REST API consumers.
2. Application Tier: The backend that contains controllers, services, and all the business logic.
3. Data Tier: The databases for structured data and MongoDB for flexible, document-based data.

This architecture improves scalability, maintainability, and deployment flexibility. Each tier can be independently developed, tested, or scaled without impacting the others directly.

## Data Flow

1. User accesses any of the dashboards or frontend modules.
2. The action is routed to the appropriate Thymeleaf or REST controller.
3. The controller calls the service layer to apply business rules and validations.
4. The service coordinates workflows to the respective repository, MySQL or MongoDB.
5. The repository runs the query against the database. Spring Data JPA does this for MySQL and Spring Data MongoDB for MongoDB.
6. The results are mapped to model classes: @Entity classes for MySQL, @Document classes for MongoDB
7. The models go back up through the service to the controller. The MVC controllers pass them to Thymeleaf templates, which render HTML. The REST controllers sreialize them to JSON.