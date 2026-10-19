# 💰 Full-Stack Personal Finance Tracker & AI Receipt Scanner

A modern, full-stack personal finance web application built with **Angular 18**, **Spring Boot 3.3.2**, **PostgreSQL / MySQL / H2 Database**, and integrated with **Google Gemini AI** for automated receipt & invoice scanning.

<p align="center">
  <a href="https://github.com/Aaditya514/My-Money/releases/latest">
    <img src="https://img.shields.io/badge/Download-Android%20APK-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Download Android APK" />
  </a>
  <a href="https://github.com/Aaditya514/My-Money/actions/workflows/ci.yml">
    <img src="https://img.shields.io/github/actions/workflow/status/Aaditya514/My-Money/ci.yml?branch=main&style=for-the-badge&label=CI%20Build" alt="CI Build Status" />
  </a>
  <img src="https://img.shields.io/badge/Angular-18-DD0031?style=for-the-badge&logo=angular&logoColor=white" alt="Angular 18" />
  <img src="https://img.shields.io/badge/Spring%20Boot-3.3.2-6DB33F?style=for-the-badge&logo=springboot&logoColor=white" alt="Spring Boot" />
</p>

---

## 📱 Mobile App Download (Android APK)

Install the Finance Tracker directly on your Android phone to track expenses on the go and scan bills with your phone camera!

### 📥 [👉 Click Here to Download Latest Android APK](https://github.com/Aaditya514/My-Money/releases/latest)

> **Direct APK File Link**: [Download `finance-tracker.apk`](https://github.com/Aaditya514/My-Money/releases/latest/download/finance-tracker.apk)

#### 📲 How to Install on Your Android Phone:
1. Tap the **Download APK** link above on your phone's browser.
2. Once downloaded, tap the file in your notification bar or **Files / Downloads** app.
3. If prompted with *"Install unknown apps"*, tap **Settings** and toggle **"Allow from this source"**.
4. Tap **Install** and open **Finance Tracker**!

> 💡 **Instant Install (PWA Alternative)**: You can also visit the web app in Chrome (Android) or Safari (iOS) and select **"Add to Home Screen"** or **"Install App"** to install the Progressive Web App with offline support without downloading an APK.

---

## 🌟 Key Features

### 📊 Financial Dashboard & Command Center
- **Executive Wealth Cards**: Real-time Net Worth (liquid cash across active accounts), Total Liquid Reserves, Savings Goals Progress, and Monthly Unallocated Buffer.
- **Monthly Spending Breakdown**: Categorized expense analysis with percentage meters.
- **Recent Activity Feed**: Color-coded transaction log (`INCOME`, `EXPENSE`, `REFUND`).

### 🏦 Multi-Account Tracking & Statement Reconciliation
- **Liquid Account Management**: Track Salary, Savings, Checking, Cash Wallet, and other liquid reserves.
- **Live Balance Synchronization**: Transactions automatically increment or decrement linked account balances.
- **Statement Reconciliation**: Reconcile system balance against physical bank statements or cash counts with timestamps and notes.
- **Account Transaction History**: 1-click modal viewing transaction history per financial account.

### 🗓️ Monthly Salary & Budget Planning
- **Configurable Income Sources**: Add multiple expected inflows (Salary, Freelance, Rental, Bonus).
- **Carry-Forward Surplus**: Carry forward unallocated surplus from prior months as additional available income.
- **Category Allocations**: Itemize allocations (`EXPENSE`, `SAVINGS`, `OTHER`) with automatic population from active categories.
- **Real-Time Safety Buffer**: Live calculation of unallocated cash cushion; transparent advisory warnings if allocations exceed income.
- **Plan Duplication & Cleanup**: Copy entire budget plans from prior months with optional surplus roll-over; safe plan deletion without altering transaction records.

### 🎯 Savings Goals & Emergency Fund Tracker
- **Milestone Goals**: Set targets, target dates, and track cumulative savings progress percentages.
- **Account-Linked Contributions**: Record dedicated contributions sourced directly from financial accounts.
- **Emergency Reserve Integration**: Seamlessly link emergency fund goals to monthly salary plans.

### 🤖 Multimodal AI Bill & Receipt Scanner (Google Gemini AI)
- **Image Upload**: Upload receipt and invoice photos (`PNG`, `JPG`, `JPEG`, `WEBP`).
- **SMS & Statement Text Analysis**: Paste bank SMS transaction alerts or raw statement text.
- **Automated Extraction**: Uses Google Gemini to detect Merchant/Title, Total Amount, Date, and Description.
- **Smart Category Matching**: Automatically maps extracted items to existing database categories.
- **Interactive Review & Edit Form**: Inspect, adjust, or edit the extracted details before confirming and saving to the database.

### 💳 Transaction Management
- Add, edit, and delete `INCOME`, `EXPENSE`, and `REFUND` transactions.
- Filter transactions by Type, Category, and keyword search.
- Direct shortcut to AI Scanner from transaction forms.

### 📂 User-Scoped Category Management
- Pre-seeded with default categories (Salary, Freelance, Bonus, Food, Transport, Shopping, Bills, Entertainment, Education, Healthcare, Other).
- Create custom categories classified as `INCOME`, `EXPENSE`, `SAVINGS`, or `OTHER`.
- Custom ordering with up/down controls and soft-archiving support.

### 🔐 Authentication & Session Guard
- User registration and login flow.
- Angular Route Guards (`authGuard`) preventing unauthorized navigation.
- Automatically seeds a ready-to-use demo account: `demo@example.com` / `password123`.

---

## 🛠️ Technology Stack

| Layer | Technologies & Tools |
| :--- | :--- |
| **Frontend** | Angular 18 (Standalone Components), TypeScript 5.4, RxJS 7.8, Reactive Forms, Router Guards |
| **Backend** | Spring Boot 3.3.2, Java 20 / 21, Spring Data JPA, Hibernate 6.5, Jakarta Validation |
| **Database** | MySQL 8.0 & Embedded H2 Database (with zero-configuration auto-fallback) |
| **AI Engine** | Google Gemini API (`gemini-1.5-flash` / `gemini-2.5-flash` via RestTemplate) |
| **Build Tools** | Apache Maven 3.9+, Node.js (v18+) & npm (v10+) |

---

## 🏗️ Architecture

```text
┌────────────────────────────────────────────────────────┐
│                   Angular 18 Frontend                  │
│       (Components ──> Services ──> HttpClient)        │
└───────────────────────────┬────────────────────────────┘
                            │ REST / JSON (HTTP)
                            ▼
┌────────────────────────────────────────────────────────┐
│               Spring Boot 3.3.2 Backend                │
│  ┌──────────────────────────────────────────────────┐  │
│  │ REST Controllers (@RestController)               │  │
│  ├──────────────────────────────────────────────────┤  │
│  │ Service Layer (@Service) - Business Logic        │  │
│  ├──────────────────────────────────────────────────┤  │
│  │ AI Service (Gemini API Integration)              │  │
│  ├──────────────────────────────────────────────────┤  │
│  │ Data Access Layer (Spring Data JPA Repositories) │  │
│  └──────────────────────────────────────────────────┘  │
└──────────────┬──────────────────────────┬──────────────┘
               │ JDBC                     │ Multimodal API Call
               ▼                          ▼
┌─────────────────────────────┐  ┌───────────────────────┐
│     Database Layer          │  │   Google Gemini AI    │
│  (H2 In-Memory or MySQL)    │  │ (Vision + Text Model) │
└─────────────────────────────┘  └───────────────────────┘
```

---

## 📁 Project Structure

```text
finance-tracker/
├── backend/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/financetracker/
│   │   │   │   ├── config/              # CORS settings & DataInitializer (auto-seeds categories & user)
│   │   │   │   ├── controller/          # REST Controllers (Auth, Transactions, Categories, Budgets, AI)
│   │   │   │   ├── dto/                 # Request & Response Data Transfer Objects
│   │   │   │   ├── entity/              # JPA Entities (User, Category, Transaction, Budget)
│   │   │   │   ├── repository/          # Spring Data JPA interfaces
│   │   │   │   └── service/             # Business Logic & Gemini AI Scanner Service
│   │   │   └── resources/
│   │   │       ├── application.properties        # Main config (default H2 with MySQL override)
│   │   │       ├── application-h2.properties     # Standalone in-memory profile
│   │   │       ├── application-mysql.properties  # Dedicated MySQL profile
│   │   │       └── mysql-schema.sql              # Standalone SQL schema script
│   │   └── test/                        # Comprehensive unit & integration tests
│   ├── pom.xml                          # Maven build dependencies & Java configuration
│   └── Dockerfile
│
└── frontend/
    ├── src/
    │   ├── app/
    │   │   ├── ai-scanner/              # Receipt photo upload & review form
    │   │   ├── budgets/                 # Budget list & spending limit tracker
    │   │   ├── categories/              # Category management
    │   │   ├── dashboard/               # Metric cards, charts & recent transactions
    │   │   ├── guards/                  # auth.guard.ts route protection
    │   │   ├── login/ & register/       # User authentication pages
    │   │   ├── models/                  # TypeScript interfaces
    │   │   ├── navbar/                  # Top navigation bar
    │   │   ├── services/                # Angular HttpClient API services
    │   │   └── transactions/            # Transaction list & add/edit forms
    │   ├── environments/                # API base URL configuration
    │   ├── index.html & styles.css      # Global layout & styles
    │   └── main.ts                      # Angular standalone bootstrap
    ├── angular.json
    └── package.json
```

---

## 🚀 Quick Start Guide

### Prerequisites
- **Java**: JDK 20 or JDK 21 installed (`java -version`)
- **Maven**: 3.9+ installed (`mvn -version`)
- **Node.js**: v18+ or v20+ (`node -v`)
- **npm**: v9+ or v10+ (`npm -v`)
- *(Optional)* **MySQL**: MySQL Server 8.0 (if using persistent MySQL instead of H2)

---

### Step 1: Start the Backend

Open a terminal in the `backend` folder:

```bash
cd backend
mvn spring-boot:run
```

- The backend will start on **`http://localhost:8080`**.
- By default, it runs with the embedded **H2 in-memory database** (zero setup needed!).
- On first boot, it automatically seeds 11 default categories and a demo user.
- **H2 Web Console**: Access tables at [http://localhost:8080/h2-console](http://localhost:8080/h2-console)  
  *(JDBC URL: `jdbc:h2:mem:finance_tracker`, Username: `sa`, Password: empty)*

---

### Step 2: Start the Frontend

Open a separate terminal in the `frontend` folder:

```bash
cd frontend
npm install
npm start
```

- The Angular application will compile and launch on **`http://localhost:4200`**.
- Open your browser and navigate to: **[http://localhost:4200](http://localhost:4200)**

---

### Step 3: Log In

You can log in immediately using the pre-seeded demo account:

| Field | Value |
| :--- | :--- |
| **Email** | `demo@example.com` |
| **Password** | `password123` |

*(Or click **Register** to create a new personal account).*

---

## 🗄️ Running with MySQL (Optional)

If you prefer to persist data in a local MySQL instance instead of H2:

1. Create the MySQL database:
   ```sql
   CREATE DATABASE finance_tracker;
   ```
2. Configure your MySQL credentials using environment variables or in `backend/src/main/resources/application.properties`:
   ```bash
   # Set environment variables (PowerShell example):
   $env:MYSQLHOST="localhost"
   $env:MYSQLPORT="3306"
   $env:MYSQLDATABASE="finance_tracker"
   $env:MYSQLUSER="root"
   $env:MYSQLPASSWORD="your_mysql_password"
   ```
3. Run with the MySQL profile:
   ```bash
   mvn spring-boot:run -Dspring-boot.run.profiles=mysql
   ```

---

## 🤖 Configuring Google Gemini AI Scanner

The AI scanner uses Google Gemini to extract data from receipt images and SMS text.

1. Get a free API key from [Google AI Studio](https://aistudio.google.com/).
2. Set the `GEMINI_API_KEY` environment variable:
   ```bash
   # Linux / macOS
   export GEMINI_API_KEY="your_actual_gemini_api_key"

   # Windows (PowerShell)
   $env:GEMINI_API_KEY="your_actual_gemini_api_key"
   ```
3. You can also specify the Gemini model in `application.properties` (defaults to `gemini-1.5-flash`).

---

## 📡 REST API Reference

### 🔐 Authentication (`/api/auth`)
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/auth/register` | Register a new user |
| `POST` | `/api/auth/login` | Login and obtain user session profile |

### 🏦 Accounts & Reconciliation (`/api/accounts`)
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/accounts?userId={id}` | List all liquid accounts for user with live balances |
| `GET` | `/api/accounts/{id}` | Get specific account details |
| `POST` | `/api/accounts` | Create a new financial account (Salary, Savings, Checking, Wallet, Other) |
| `PUT` | `/api/accounts/{id}` | Update account details |
| `POST` | `/api/accounts/{id}/reconcile` | Reconcile account balance against bank statement |
| `GET` | `/api/accounts/{id}/transactions` | View transactions linked to an account |
| `DELETE` | `/api/accounts/{id}` | Delete account (safeguarded against accounts with transactions) |

### 🗓️ Monthly Salary & Budget Plans (`/api/salary-plan`)
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/salary-plan/user/{userId}` | Get plan for month/year with live income, allocations, and buffer |
| `POST` | `/api/salary-plan` | Create or update a monthly salary plan |
| `POST` | `/api/salary-plan/copy` | Duplicate a prior month's plan with optional surplus carry-forward |
| `DELETE` | `/api/salary-plan/user/{userId}?month={m}&year={y}` | Delete plan targets for a month without affecting transaction history |

### 🎯 Savings Goals (`/api/savings-goals`)
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/savings-goals?userId={id}` | List all savings goals with progress percentages |
| `POST` | `/api/savings-goals` | Create a new target savings milestone |
| `POST` | `/api/savings-goals/{id}/contribute` | Log a contribution towards a goal from a financial account |
| `PUT` | `/api/savings-goals/{id}` | Update goal target amount, deadline, or linked account |
| `DELETE` | `/api/savings-goals/{id}` | Delete a savings goal |

### 💳 Transactions (`/api/transactions`)
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/transactions/user/{userId}` | List all transactions for a user |
| `GET` | `/api/transactions/{id}` | Retrieve single transaction details |
| `POST` | `/api/transactions` | Create a new transaction (auto-adjusts linked account balance) |
| `PUT` | `/api/transactions/{id}` | Update an existing transaction |
| `DELETE` | `/api/transactions/{id}` | Delete a transaction |

### 📂 Categories (`/api/categories`)
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/categories` | Get categories (supports filters `?userId={id}` & `?includeArchived=true`) |
| `POST` | `/api/categories` | Create custom category (`INCOME`, `EXPENSE`, `SAVINGS`, `OTHER`) |
| `PUT` | `/api/categories/{id}` | Update category name and classification |
| `PUT` | `/api/categories/{id}/archive` | Soft archive / unarchive a category |
| `PUT` | `/api/categories/reorder` | Update user custom category ordering |
| `DELETE` | `/api/categories/{id}` | Delete a custom category |

### 📊 Dashboard (`/api/dashboard`)
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/dashboard/{userId}` | Get net worth, liquid balances, goal progress, buffer, and activity feed |

### 🤖 AI Scanner (`/api/ai-scanner`)
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/ai-scanner/scan-image` | Upload receipt image (`multipart/form-data`) and extract transaction details |
| `POST` | `/api/ai-scanner/scan-text` | Parse raw text/SMS alert and extract structured transaction details |

---

## 🧪 Running Automated Tests

Run backend unit and integration tests:

```bash
cd backend
mvn test
```

---

## 💡 Roadmap & Future Enhancements
- [ ] **Spring Security 6 + JWT**: Token-based authentication with BCrypt password hashing.
- [ ] **Interactive Visual Charts**: Add Chart.js / ApexCharts for spending trends and category breakdown.
- [ ] **Export Options**: Export transaction logs to CSV, Excel, and PDF reports.
- [ ] **Advanced Filtering**: Date range pickers (e.g., "This Month", "Last 30 Days", "Custom Range").

---

## 📄 License
This project is open-source and available under the [MIT License](LICENSE).
