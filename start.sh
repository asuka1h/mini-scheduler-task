#!/bin/bash

echo "🚀 Starting Mini-Scheduler System"
echo ""

# 检查Java是否安装
if ! command -v java &> /dev/null; then
    echo "❌ Java is not installed. Please install Java 17 or higher."
    exit 1
fi

# 检查Node.js是否安装
if ! command -v node &> /dev/null; then
    echo "❌ Node.js is not installed. Please install Node.js 18 or higher."
    exit 1
fi

echo "✅ Prerequisites check passed"
echo ""

# 启动Master节点（后台运行）
echo "📦 Starting Master node..."
cd backend
if [ ! -f "mvnw" ]; then
    echo "⚠️  Maven wrapper not found. Please run: mvn wrapper:wrapper"
    exit 1
fi

chmod +x mvnw
./mvnw spring-boot:run -Dspring-boot.run.profiles=master > ../master.log 2>&1 &
MASTER_PID=$!
echo "Master started with PID: $MASTER_PID"
cd ..

# 等待Master启动
echo "⏳ Waiting for Master to start..."
sleep 10

# 启动Worker节点（后台运行）
echo "👷 Starting Worker node..."
cd backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=worker > ../worker.log 2>&1 &
WORKER_PID=$!
echo "Worker started with PID: $WORKER_PID"
cd ..

# 等待Worker启动
echo "⏳ Waiting for Worker to start..."
sleep 5

# 启动前端
echo "🎨 Starting Frontend..."
cd frontend
if [ ! -d "node_modules" ]; then
    echo "📦 Installing frontend dependencies..."
    npm install
fi

npm run dev > ../frontend.log 2>&1 &
FRONTEND_PID=$!
echo "Frontend started with PID: $FRONTEND_PID"
cd ..

echo ""
echo "✨ All services started!"
echo ""
echo "📊 Master: http://localhost:8080"
echo "👷 Worker: http://localhost:8081"
echo "🎨 Frontend: http://localhost:3000"
echo ""
echo "📝 Logs:"
echo "   - Master: master.log"
echo "   - Worker: worker.log"
echo "   - Frontend: frontend.log"
echo ""
echo "To stop all services, run: kill $MASTER_PID $WORKER_PID $FRONTEND_PID"
