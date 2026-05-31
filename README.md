# 港大医院投诉管理系统

现代化医院投诉管理解决方案，支持PC端管理后台和移动端H5应用。

## 项目架构

```
hkuh-complaint-system/
├── complaint-admin/      # Spring Boot 后台管理应用
├── complaint-api/       # REST API 控制器
├── complaint-common/    # 公共工具类
├── complaint-dao/      # 数据访问层
├── complaint-service/   # 业务逻辑层
├── complaint-web/       # PC端管理后台 (Vue 3 + Element Plus)
├── complaint-h5/        # 移动端H5应用 (Vue 3 + Vant)
├── nginx/               # Nginx 配置文件
├── sql/                 # 数据库脚本
└── tomcat/              # Tomcat 配置
```

## 技术栈

### 后端
- **Spring Boot 3.2.5** - 核心框架
- **MyBatis-Plus** - ORM框架
- **Spring Security** - 安全框架
- **Redis** - 缓存和会话
- **Oracle 11g** - 数据库

### 前端
- **Vue 3.4** - 渐进式框架
- **TypeScript** - 类型系统
- **Vite 5** - 构建工具
- **Element Plus** - PC端UI
- **Vant 4** - 移动端UI
- **Pinia** - 状态管理
- **ECharts 5** - 数据可视化

## 快速开始

### 环境要求
- JDK 17+
- Node.js 18+
- Oracle 11g+
- Redis 6+

### 后端部署

1. **数据库初始化**
```bash
mysql -u root -p < sql/init.sql
```

2. **配置环境变量**
```bash
cp .env.example .env
# 编辑 .env 填写数据库等信息
```

3. **启动后端服务**
```bash
cd complaint-admin
mvn clean package
java -jar target/complaint-admin.jar
```

### 前端部署（Netlify）

#### H5移动端

1. **Fork或克隆仓库**
```bash
git clone https://github.com/your-username/hkuh-complaint-system.git
cd hkuh-complaint-system/complaint-h5
```

2. **配置环境变量**
创建 `.env.production` 文件：
```env
VITE_API_BASE_URL=https://your-api-domain.com
VITE_WECHAT_APP_ID=your_wechat_app_id
```

3. **部署到Netlify**
```bash
# 方法一：使用Netlify CLI
npm install -g netlify-cli
netlify deploy --prod --dir=dist

# 方法二：连接GitHub仓库
# 1. 访问 https://netlify.com
# 2. 点击 "Add new site" > "Import an existing project"
# 3. 选择 GitHub 仓库
# 4. 配置构建命令和发布目录
# 5. 添加环境变量
# 6. 部署
```

#### PC管理后台

1. **Fork或克隆仓库**
```bash
git clone https://github.com/your-username/hkuh-complaint-system.git
cd hkuh-complaint-system/complaint-web
```

2. **配置环境变量**
创建 `.env.production` 文件：
```env
VITE_API_BASE_URL=https://your-api-domain.com
```

3. **部署到Netlify**
```bash
npm install -g netlify-cli
netlify deploy --prod --dir=dist
```

## 部署架构

```
┌─────────────────────────────────────────────────────────┐
│                        用户                               │
└──────────────┬──────────────────────────────────────────┘
               │
               ├────────────────────┐
               │                   │
               ▼                   ▼
        ┌──────────┐       ┌──────────┐
        │  Netlify  │       │  Netlify  │
        │  (H5移动端) │       │ (PC管理后台) │
        └─────┬────┘       └─────┬────┘
              │                   │
              │                   │
              └─────────┬─────────┘
                        │
                        ▼
              ┌──────────────────┐
              │   Nginx反向代理   │
              │  (HTTPS + WSS)   │
              └─────────┬─────────┘
                        │
                        ▼
              ┌──────────────────┐
              │   Spring Boot    │
              │    (API服务)      │
              └─────────┬─────────┘
                        │
              ┌─────────┴─────────┐
              │                   │
              ▼                   ▼
        ┌──────────┐       ┌──────────┐
        │  Oracle   │       │  Redis   │
        │  数据库    │       │  缓存    │
        └──────────┘       └──────────┘
```

## 功能特性

### PC管理后台
- ✅ 用户认证与授权
- ✅ 投诉工单管理
- ✅ 处理流程跟踪
- ✅ 满意度调查
- ✅ 数据统计分析
- ✅ SLA监控提醒
- ✅ 操作日志审计
- ✅ 二维码生成

### H5移动端
- ✅ 投诉建议提交
- ✅ 我的投诉查询
- ✅ 常见问题FAQ
- ✅ 满意度调查
- ✅ 微信OAuth授权
- ✅ 微信消息推送

## 安全特性

- ✅ JWT Token认证
- ✅ Spring Security权限控制
- ✅ AES-256-GCM数据加密
- ✅ 数据脱敏处理
- ✅ SQL注入防护
- ✅ XSS防护
- ✅ HTTPS强制
- ✅ 操作日志审计

## 配置说明

### 必需的环境变量

```env
# 数据库
DB_HOST=your_db_host
DB_PORT=1521
DB_SERVICE_NAME=ORCL
DB_USERNAME=complaint
DB_PASSWORD=your_password

# Redis
REDIS_HOST=your_redis_host
REDIS_PASSWORD=your_redis_password

# JWT
JWT_SECRET=your_jwt_secret_at_least_256_bits

# 微信公众号
WECHAT_MP_APP_ID=your_app_id
WECHAT_MP_APP_SECRET=your_app_secret

# 短信服务
SMS_ACCESS_KEY_ID=your_access_key
SMS_ACCESS_KEY_SECRET=your_secret_key
```

## API文档

部署后访问：
- Swagger UI: `https://your-api-domain.com/swagger-ui.html`
- API Docs: `https://your-api-domain.com/v3/api-docs`

## 开发指南

### 前端开发

```bash
# H5移动端
cd complaint-h5
npm install
npm run dev

# PC管理后台
cd complaint-web
npm install
npm run dev
```

### 后端开发

```bash
cd complaint-admin
mvn spring-boot:run
```

## 项目结构说明

```
complaint-h5/          # 移动端H5应用
├── src/
│   ├── api/          # API接口定义
│   ├── assets/       # 静态资源
│   ├── components/    # 公共组件
│   ├── router/       # 路由配置
│   ├── store/        # 状态管理
│   ├── utils/        # 工具函数
│   └── views/        # 页面组件
├── netlify.toml      # Netlify配置
└── .env.production   # 生产环境变量

complaint-web/        # PC管理后台
├── src/
│   ├── api/          # API接口定义
│   ├── assets/       # 静态资源
│   ├── components/   # 公共组件
│   ├── composables/  # 组合式函数
│   ├── router/       # 路由配置
│   ├── store/        # 状态管理
│   ├── utils/        # 工具函数
│   └── views/        # 页面组件
├── netlify.toml      # Netlify配置
└── .env.production   # 生产环境变量
```

## 许可证

本项目仅供学习和研究使用。

## 联系方式

如有问题，请提交Issue。
