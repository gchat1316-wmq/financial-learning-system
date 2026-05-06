前置条件

  - Java 17+ (后端运行)
  - Maven 3.8+ (后端构建)
  - Node.js 18+ (前端构建)
  - MySQL 8.0+ (数据库)
  - Redis (缓存)

  1. 数据库准备

  CREATE DATABASE investment_learning DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

  Flyway 会在后端首次启动时自动执行 migrations。

  2. 后端启动

  cd backend

  # 配置环境变量 (Linux/macOS)
  export DB_PASSWORD=root123456
  export REDIS_HOST=localhost
  export JWT_SECRET=your-256-bit-secret-key-change-in-production

  # 启动
  mvn spring-boot:run

  后端地址: http://localhost:8080

  3. 前端启动

  cd frontend
  npm install          # 首次运行需要
  npm run dev:h5       # H5 开发模式

  访问: http://localhost:5173 (或 HBuilderX 控制台显示的地址)

  4. 一键启动脚本

  ./start-dev.sh

  5. 配置修改

  前端 API 地址在 manifest.json 中配置（需要自行在 uni-app 项目中修改）：

  - H5 配置 publicPath 和 API 代理
  - 开发时后端接口需支持 CORS 或配置代理

  常见问题

  - MySQL 连接失败: 检查 DB_PASSWORD 和 MySQL 服务状态
  - Redis 连接失败: 检查 REDIS_HOST 和 Redis 服务状态
  - 前端接口 404: 确认后端已启动，H5 需配置 API 代理或 CORS