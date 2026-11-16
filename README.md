# 🏥 Real-Time Patient Health Monitoring Dashboard

A web-based patient health monitoring system designed to display and track important health parameters through a centralized dashboard. The application allows patients and healthcare professionals to access health readings, monitor vital signs, and view alerts through a simple and user-friendly interface.

\<p align="center">   \<strong>Built with Java, Spring Boot, PostgreSQL, HTML, CSS and JavaScript\</strong> \</p>

---

## 🌐 Live Demo

- **Frontend:** [https://patient-health-monitoring-frontend.onrender.com](https://patient-health-monitoring-frontend.onrender.com)
- **Backend API:** [https://patient-health-monitoring.onrender.com](https://patient-health-monitoring.onrender.com)
- **GitHub Repository:** [https://github.com/anjumpatel725-boop/patient-health-monitoring](https://github.com/anjumpatel725-boop/patient-health-monitoring)

*Note: The application is hosted on Render. The first request may take some time if the free service has been inactive.*

---

## 📌 Project Overview

The Real-Time Patient Health Monitoring Dashboard is an internship project developed to demonstrate how web technologies can be used to manage and visualize patient health information.

The system provides a centralized platform for displaying vital health parameters, including:

- Heart Rate
- Blood Pressure (Systolic and Diastolic)
- Body Temperature
- Blood Oxygen Level (SpO₂)

The dashboard periodically retrieves health readings from the backend and displays updated information to users.

The project also includes user authentication, role-based access, health data management, and visual alerts for readings outside configured demo ranges.

---

## ✨ Key Features

- **User Authentication:** Secure login using JWT-based authentication.
- **Role-Based Access:** Separate access for supported user roles.
- **Patient Dashboard:** Centralized view of patient health information.
- **Vital Signs Monitoring:** Displays heart rate, blood pressure, temperature, and oxygen saturation.
- **Periodic Data Updates:** Automatically refreshes dashboard readings.
- **Health Data Visualization:** Presents readings in an organized and understandable format.
- **Alert Indicators:** Highlights readings outside configured demo thresholds.
- **Doctor Information:** Displays assigned doctor information where available.
- **Responsive Interface:** Designed to work across desktop and mobile screen sizes.
- **REST API Integration:** Connects the frontend with the Spring Boot backend.

---

## 🛠️ Technology Stack

| Category            | Technologies               |
| ------------------- | -------------------------- |
| Frontend            | HTML5, CSS3, JavaScript    |
| Backend             | Java 21, Spring Boot 3.5.5 |
| API                 | REST APIs                  |
| Database            | PostgreSQL                 |
| ORM                 | Spring Data JPA, Hibernate |
| Authentication      | Spring Security / JWT      |
| Build Tool          | Maven                      |
| Frontend Build Tool | Vite, npm                  |
| Deployment          | Render                     |
| Version Control     | Git and GitHub             |

---

## 🏗️ System Architecture

The application follows a client-server architecture.

```text
       USER
         |
         v
  FRONTEND APPLICATION
   HTML, CSS, JavaScript
         |
         | REST API Requests
         v
    SPRING BOOT
      BACKEND
         |
    Authentication
    Business Logic
    Data Processing
         |
         v
     POSTGRESQL
      DATABASE
         |
         v
   HEALTH DATA RESPONSE
         |
         v
   PATIENT DASHBOARD
```

---

## 📂 Project Structure

```text
Patient-Health-Monitoring/
│
├── backend/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/healthcare/
│   │   │   │       ├── controller/
│   │   │   │       ├── service/
│   │   │   │       ├── repository/
│   │   │   │       ├── model/
│   │   │   │       ├── dto/
│   │   │   │       └── config/
│   │   │   └── resources/
│   │   └── test/
│   └── pom.xml
│
├── frontend/
│   ├── public/
│   ├── login.html
│   ├── patient-dashboard.html
│   ├── doctor-dashboard.html
│   ├── admin-dashboard.html
│   ├── script.js
│   ├── style.css
│   └── package.json
│
└── README.md
```

*The structure above is a simplified overview; exact files may vary.*

---

## ⚙️ Installation and Setup

### Prerequisites

Make sure the following tools are installed:

- Java JDK 21
- Apache Maven
- Node.js and npm
- PostgreSQL
- Git

### 1. Clone the Repository

```bash
git clone https://github.com/anjumpatel725-boop/patient-health-monitoring.git
```

### 2. Navigate to the Project

```bash
cd patient-health-monitoring
```

### 3. Configure PostgreSQL

Create a PostgreSQL database:

```sql
CREATE DATABASE patient_health_monitoring;
```

Configure the database connection using your local environment variables or the backend application configuration.

Do not commit database passwords, JWT secrets, or other sensitive credentials.

### 4. Run the Backend

```bash
mvn -f backend/pom.xml spring-boot:run
```

The backend will run on:

```text
http://localhost:8080
```

### 5. Run the Frontend

Open another terminal:

```bash
cd frontend
npm install
npm run dev
```

Open the local URL displayed by Vite in your terminal.

---

## 🔄 How the Application Works

1. The user logs into the application using valid credentials.
2. The backend verifies the user's credentials and generates an authentication token.
3. The frontend uses REST APIs to retrieve authorized patient information.
4. Health readings are stored and retrieved from the PostgreSQL database.
5. The dashboard periodically requests updated readings.
6. The interface displays vital signs and configured alert indicators.

---

## 🧪 Testing and Build

Backend verification:

```bash
mvn -B -f backend/pom.xml clean verify
```

Frontend production build:

```bash
cd frontend
npm install
npm run build
```

These commands verify the backend build and frontend production build. They do not replace complete manual application testing.

---

## ⚠️ Important Project Limitations

- Health readings are generated using a software-based simulator for demonstration purposes.
- The current implementation does not directly integrate with physical medical sensors.
- The application is an educational prototype, not a certified medical monitoring system.
- Dashboard alerts are based on configured demonstration thresholds.
- The system must not be used for medical diagnosis or emergency decision-making.

---

## 🚀 Future Enhancements

- Integration with real-time IoT health monitoring devices.
- Live ECG waveform visualization.
- WebSocket-based real-time communication.
- Advanced health analytics and reporting.
- Email and SMS notifications.
- Historical health trend analysis.
- Enhanced patient-doctor communication.
- Automated anomaly detection.

---

## 🎯 Project Objectives

- To develop a centralized patient health monitoring platform.
- To understand frontend and backend integration.
- To implement REST API-based communication.
- To manage health-related data using PostgreSQL.
- To implement JWT-based authentication.
- To gain practical experience with full-stack application development and cloud deployment.

---

## 👩‍💻 Developed By

**Anjum Patel**

B.Tech – Computer Science and Engineering

**Project:** Real-Time Patient Health Monitoring Dashboard

**Project Type:** Internship Project

---

## 📄 License

This project was developed for educational and internship purposes. All rights reserved by the project author unless otherwise specified.
