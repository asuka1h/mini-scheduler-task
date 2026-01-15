# How to Run Mini-Scheduler

## Prerequisites

Before running the project, make sure you have:

1. **Java 17 or higher** ✅ (You have Java 24 installed)
2. **Maven 3.6+** (or we'll use Maven wrapper)
3. **Node.js 18+ and npm** (for frontend)

## Quick Start (Recommended)

### Step 1: Install Node.js (if not installed)

If Node.js is not installed, you can install it using:

**macOS (using Homebrew):**
```bash
brew install node
```

**Or download from:** https://nodejs.org/

### Step 2: Setup Maven Wrapper

```bash
cd backend
mvn wrapper:wrapper
cd ..
```

### Step 3: Run the Startup Script

```bash
chmod +x start.sh
./start.sh
```

This will start:
- Master node on port 8080
- Worker node on port 8081  
- Frontend on port 3000

Access the frontend at: **http://localhost:3000**

---

## Manual Setup (Step by Step)

If you prefer to run each component manually:

### Step 1: Start Master Node

```bash
cd backend

# If Maven wrapper exists:
./mvnw spring-boot:run -Dspring-boot.run.profiles=master

# Or if using system Maven:
mvn spring-boot:run -Dspring-boot.run.profiles=master
```

Master will start on **http://localhost:8080**

### Step 2: Start Worker Node (New Terminal)

```bash
cd backend

# If Maven wrapper exists:
./mvnw spring-boot:run -Dspring-boot.run.profiles=worker

# Or if using system Maven:
mvn spring-boot:run -Dspring-boot.run.profiles=worker
```

Worker will start on **http://localhost:8081** and automatically register with Master.

### Step 3: Start Frontend (New Terminal)

```bash
cd frontend

# Install dependencies (first time only)
npm install

# Start development server
npm run dev
```

Frontend will start on **http://localhost:3000**

---

## Verify Installation

### Check Master API:
```bash
curl http://localhost:8080/api/workers
```

### Check if Worker is registered:
```bash
curl http://localhost:8080/api/workers
```

You should see the worker node in the response.

### Submit a Test Task:
```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "command": "echo Hello World && sleep 2 && echo Task completed",
    "cpuRequired": 1,
    "memRequired": 100
  }'
```

---

## Troubleshooting

### Issue: Maven wrapper not found
**Solution:**
```bash
cd backend
mvn wrapper:wrapper
```

### Issue: Port already in use
**Solution:** 
- Check if ports 8080, 8081, or 3000 are already in use
- Kill existing processes or change ports in `application.yml`

### Issue: Node.js not found
**Solution:**
- Install Node.js from https://nodejs.org/
- Or use Homebrew: `brew install node`

### Issue: Frontend dependencies error
**Solution:**
```bash
cd frontend
rm -rf node_modules package-lock.json
npm install
```

### Issue: Worker not connecting to Master
**Solution:**
- Make sure Master is running first
- Check `application.yml` for correct Master URL
- Check logs: `tail -f worker.log`

---

## Stopping Services

### If using start.sh script:
The script will show the PIDs. Stop them with:
```bash
kill <MASTER_PID> <WORKER_PID> <FRONTEND_PID>
```

### Manual stop:
- Press `Ctrl+C` in each terminal
- Or find and kill processes:
```bash
# Find Java processes
jps | grep MiniScheduler

# Kill by PID
kill <PID>
```

---

## Development Mode

### Backend (with hot reload):
```bash
cd backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=master -Dspring-boot.run.jvmArguments="-Dspring.devtools.restart.enabled=true"
```

### Frontend (with hot reload):
```bash
cd frontend
npm run dev
```
Vite automatically supports hot module replacement.

---

## Production Build

### Build Backend:
```bash
cd backend
./mvnw clean package
java -jar target/mini-scheduler-1.0.0.jar --spring.profiles.active=master
```

### Build Frontend:
```bash
cd frontend
npm run build
# Static files will be in dist/ directory
```

---

## Next Steps

1. Open **http://localhost:3000** in your browser
2. You should see the cluster heatmap with your Worker node
3. Submit a task using the form on the right
4. Click on a running task to see live logs!
