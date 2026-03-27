  Currency Exchange REST API
A lightweight Java backend REST API for managing currencies and exchange rates, built with Servlet API + JDBC and optimized for performance and clean architecture.

  Overview
This project implements a simple but robust currency exchange service with the following capabilities:
Manage currencies (create, retrieve)
Manage exchange rates (create, update, retrieve)
Convert data into structured JSON responses
Optimized data access with reduced database overhead

  The project demonstrates strong fundamentals in backend development, including:
REST API design
JDBC-based data access
DTO pattern
Input validation
Error handling
Performance optimization

  Tech Stack
Java (Servlet API)
JDBC
Oracle Database
HikariCP (connection pooling)
Jackson (JSON serialization)
Maven

  Architecture
The project follows a layered structure:
Servlet Layer  →  Service/Validation  →  DAO (DbConnector)  →  Database
Key components:
BaseServlet
Centralized JSON response handling
Unified error responses
Shared configuration
DAO Layer (DbConnector)
Encapsulates all DB access
Uses Optional for safer null handling
DTO Layer
Separates internal models from API responses
Validation Layer
Centralized input validation logic

  Error Handling
Consistent JSON error responses:
{
  "error": "error description"
}
Proper HTTP status codes:
400 – Bad request
404 – Not found
409 – Conflict
500 – Server error

  API Endpoints
Currency
GET /currency/{code}
POST /currencies
Exchange Rates
GET /exchangeRates
POST /exchangeRates
GET /exchangeRate/{pair}
PATCH /exchangeRate/{pair}

  Key Features
✅ Clean and readable code structure
✅ Centralized response handling (BaseServlet)
✅ Transition to Optional for null safety
✅ Optimized database access
✅ DTO-based API design
✅ Input validation layer
✅ Connection pooling (HikariCP)

  How to Run
git clone https://github.com/toshikator/currency_exchange.git
Deploy WAR to your servlet container (e.g. Tomcat).
