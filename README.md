# Vacancy Tracker Bot 🤖💼

A production-ready Telegram bot that acts as a personal job search assistant. It automatically aggregates, filters, and delivers relevant job vacancies from public REST APIs directly to users based on their custom preferences.

Built with **pure Java 21** using **Clean Architecture** principles without heavy frameworks, demonstrating a deep understanding of core programming patterns, multithreading, and systems design.

---

## 🚀 Key Features

*   **Smart Filtering:** Filter vacancies by Region, Minimum Experience, Minimum Salary, and Keywords.
*   **Scheduled Notifications:** Automate daily vacancy scanning and delivery at a user-defined time and time zone.
*   **State Persistence:** Full data recovery. User profiles, settings, and job history are saved to disk and restored instantly upon bot reboots.
*   **Interactive UI:** Seamless user experience using a combination of text commands and dynamic Telegram inline keyboards.
*   **Data Privacy (GDPR-ready):** A `/stop` command initiates a complete wipe of user data, logs, and scheduled tasks from the system.

---

## 🛠 Tech Stack

*   **Language:** Java 21 (LTS)
*   **Build System:** Gradle
*   **API Integration:** Native `HttpClient` (REST API integration)
*   **JSON Processing:** Jackson `ObjectMapper`
*   **Task Scheduling:** `ScheduledExecutorService` (concurrent multi-user thread pool)
*   **Telegram Framework:** `org.telegram:telegrambots-longpolling`
*   **Testing:** JUnit & Mockito (80%+ code coverage)

---

## 🏗 Architectural Design

The project strictly follows the **Three-Tier (Clean) Architecture** pattern to guarantee modularity and loose coupling:

1.  **Presentation Layer:** Handles Telegram updates via `LongPollingSingleThreadUpdateConsumer`. Uses a **Dispatcher (Router)** and the **Command Pattern** to map requests to isolated `BotCommand` handlers.
2.  **Business Logic Layer:** Coordinates job alerts, executes core algorithms, handles multi-user time zone offsets, and performs custom post-filtering on REST API payloads.
3.  **Data Access Layer:** Abstracted via the `UserRepository` interface. Implements data serialization into persistent JSON files (`JsonUserRepository`). Allows swapping the file system for a SQL database by changing a single line of code.

---

## 🏁 Getting Started

### Prerequisites
*   Java 21 JDK or higher
*   A Telegram Bot Token (obtained from [@BotFather](https://t.me))

### Local Setup

1. **Clone the repository:**
   ```bash
   git clone github.com
   cd vacancy-tracker-bot
   ```

2. **Configure Environment Variables:**
   Create a `config.properties` or `application.yaml` file in the root directory (or use environment variables):
   ```env
   BOT_NAME=@vacancyFinalWork_bot
   BOT_TOKEN=8722477529:AAHm6DKjQP8UY8vwXalgSz5SCO7A9Atg1hs
   ```

3. **Build the Project:**
   ```bash
   ./gradlew build
   ```

4. **Run the Application:**
   ```bash
   ./gradlew run
   ```

---

## 🤖 Bot Commands Reference

*   `/start` - Initialize registration and detect user time zone.
*   `/menu` - Open the interactive main settings panel.
*   `/region [code]` - Select target region for job hunting.
*   `/experience [years]` - Set your minimum work experience.
*   `/salary [amount]` - Define minimum acceptable salary.
*   `/keyword [text]` - Specify job title or tech stack keywords.
*   `/notify [HH MM]` - Schedule your daily alert delivery time.
*   `/ready` - Confirm filters and start background tracking.
*   `/stop` - Terminate tracking and wipe all your personal data.
