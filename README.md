# Micro-Service Application

## Overview

This project is part of my CI/CD Engineer internship at 10 000 Codeurs.

The objective is to design and implement an end-to-end CI/CD pipeline for a microservices-based application using Jenkins, SonarQube, Docker, Kubernetes and ArgoCD.

---

## Technologies

- Jenkins
- Maven
- SonarQube
- Docker
- Docker Hub
- Kubernetes
- ArgoCD
- Argo Image Updater
- Git
- GitHub
- Bash
- YAML

---

## CI/CD Workflow

1. Developer pushes code to GitHub.
2. Jenkins pipeline is automatically triggered.
3. Maven builds the application.
4. SonarQube performs code quality analysis.
5. Automated tests are executed.
6. Docker images are built and pushed to Docker Hub.
7. Argo Image Updater updates Kubernetes manifests.
8. ArgoCD deploys the application to Kubernetes.

---

## Project Structure

```text
Micro-Service-Application
│
├── ensitech-angular ( Frontend )
├── ensitech-spring-microservice ( Backend )

```

Frontend:
- Angular

Backend:
- Spring Boot Microservices

---

## Current Progress

Completed:

- Jenkins integration
- SonarQube integration
- Docker integration
- Kubernetes deployment
- ArgoCD configuration
- Argo Image Updater configuration

In Progress:

- Remaining microservices automation
- Frontend automation

Planned:

- GitHub Actions migration
- Azure cloud integration

---

## Author

Louis-Pierre Gilbert

CI/CD Engineer Intern

CESI Lyon

LinkedIn:
https://www.linkedin.com/in/louis-pierre-gilbert-4597972a8

GitHub:
https://github.com/LPG2323
