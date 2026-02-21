# JSON to CSV Converter

A Spring Boot backend application that allows user to upload a JSON file, convert it into CSV format, and download the generated file.
The application supports nested JSON flattening and uses JWT authentication for security.

---

## 🚀 Features

* Upload JSON file
* Validate JSON format
* Convert JSON → CSV
* Flatten nested JSON (objects & arrays)
* Download generated CSV
* JWT-based authentication
* Global exception handling

---

---

## 📌 How It Works

1. Upload a JSON file.
2. The system validates and flattens nested JSON.
3. CSV file is generated and stored.
4. User can download the processed CSV.

---

## ▶ Run the Project

```bash
./gradlew bootRun
```

---

## 🔐 Authentication

All protected APIs require JWT token:

```
Authorization: Bearer <token>
```

---


