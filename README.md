# Data Element Relation Project

## Overview

This project extends the functionality of the **Data Element Hub (DEHub)** by providing AI-supported relation recommendations between data elements.

The project consists of the following services:

* **Spring Boot** – Main backend application
* **FastAPI** – AI service for semantic similarity and relation recommendation
* **Keycloak** – Authentication and authorization

The services are orchestrated using **Docker Compose**.

---

## Project Structure

```text
project-root/
│
├── Main-Service/          # Spring Boot backend
├── AI-service/            # FastAPI AI service
├── keycloak/
│   └── import/
│       └── README.md
├── docker-compose.yml
├── .env.example
├── .gitignore
└── README.md
```

---

## Prerequisites

Before running the project, install:

* Docker Desktop
* Docker Compose
* Java 21
* Maven
* IntelliJ IDEA (recommended)

---

## External Dependency

> **Important**

This project depends on the **Data Element Hub (DEHub)** REST API.

The DEHub project is **not included** in this repository.

Before running this project, the DEHub REST API must be started separately (e.g. from IntelliJ IDEA).

The DEHub REST API is expected to be available at:

```text
http://localhost:8090/v1
```

When the Spring Boot application runs inside Docker, it accesses the locally running DEHub service through:

```text
http://host.docker.internal:8090/v1
```

instead of:

```text
http://localhost:8090/v1
```

---

## Keycloak

Authentication is provided by Keycloak.

Keycloak is included as supporting infrastructure required for accessing the external DEHub REST API. It is **not part of the thesis implementation**.

A compatible Keycloak realm is required.

The realm export is **not included** in this repository because it may contain confidential configuration and credentials.

Place the required realm export inside:

```text
keycloak/import/
```

before starting the Docker containers.

After starting Docker Compose, the Keycloak administration console is available at:

```text
http://localhost:8080
```

---

## Configuration

Create a `.env` file in the project root.

Example:

```env
KEYCLOAK_USER=admin
KEYCLOAK_PASSWORD=admin

KEYCLOAK_URL=http://keycloak:8080

AI_SERVICE_URL=http://fastapi:8000

RESTPROJECT_BASEURL=http://host.docker.internal:8090/v1
```

Modify the values if necessary for your local environment.

---

# Running the Project

### Step 1 – Start the DEHub REST API

Run the external DEHub project locally (for example from IntelliJ IDEA).

Verify that it is available:

```text
http://localhost:8090/v1
```

---

### Step 2 – Copy the Keycloak Realm

Copy the required Keycloak realm export into:

```text
keycloak/import/
```

---

### Step 3 – Build the Docker Images

```bash
docker compose build
```

---

### Step 4 – Start the Services

```bash
docker compose up
```

or

```bash
docker compose up --build
```

---

### Step 5 – Verify the Services

| Service        | URL                   |
|----------------|-----------------------|
| Spring Boot    | http://localhost:8081 |
| FastAPI        | http://localhost:8000 |
| Keycloak       | http://localhost:8080 |
| DEHub REST API | http://localhost:8090 |

---

## Stopping the Project

```bash
docker compose down
```

---

## Rebuilding the Containers

```bash
docker compose down
docker compose build --no-cache
docker compose up
```

---

## Notes

* This repository **does not include** the DEHub backend.
* The DEHub REST API must be started manually before launching this project.
* A compatible Keycloak realm is required but is **not included** because it contains confidential configuration.
* Communication with DEHub is performed exclusively through its REST API.
* Generated folders are intentionally **not included** in this repository:

```text
target/
.venv/
.idea/
__pycache__/
.git/
```

---

## Submission Notes

This repository contains only the source code developed as part of the Master's thesis.

The DEHub backend, its database, and its Keycloak realm are external components and are therefore not distributed with this repository.
