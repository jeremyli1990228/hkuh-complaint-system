# Netlify 部署指南

本指南将帮助您将港大医院投诉管理系统部署到Netlify。

## 部署架构

```
GitHub Repo
    │
    ├─→ complaint-h5/ ──→ Netlify (H5移动端)
    │                       https://h5-complaint.netlify.app
    │
    └─→ complaint-web/ ──→ Netlify (PC管理后台)
                            https://admin-complaint.netlify.app
```

## 方式一：手动部署（推荐首次）

### 1. 准备GitHub仓库

1. **创建GitHub仓库**
```bash
# 在GitHub上创建新仓库
# 仓库名: hkuh-complaint-system
# 选择Private或Public
```

2. **推送代码到GitHub**
```bash
cd hkuh-complaint-system
git init
git add .
git commit -m "Initial commit"
git branch -M main
git remote add origin https://github.com/your-username/hkuh-complaint-system.git
git push -u origin main
```

### 2. 部署H5移动端

1. **访问Netlify**
访问 [https://app.netlify.com](https://app.netlify.com) 并登录

2. **添加新站点**
点击 "Add new site" > "Import an existing project"

3. **连接GitHub**
选择 "Deploy with GitHub"

4. **授权Netlify访问GitHub**
点击 "Authorize Netlify"

5. **选择仓库和分支**
- Repository: `your-username/hkuh-complaint-system`
- Branch to deploy: `main`
- Build command: `npm run build`
- Publish directory: `complaint-h5/dist`

6. **配置环境变量**
点击 "Advanced" > "New variable"
```
VITE_API_BASE_URL = https://your-api-domain.com
VITE_WECHAT_APP_ID = your_wechat_app_id (可选)
```

7. **部署**
点击 "Deploy site"

8. **等待构建完成**
约2-3分钟后，站点将上线

### 3. 部署PC管理后台

重复上述步骤，但使用以下配置：
- Build command: `npm run build`
- Publish directory: `complaint-web/dist`
- 环境变量：
```
VITE_API_BASE_URL = https://your-api-domain.com
```

### 4. 配置自定义域名（可选）

1. 在Netlify站点设置中点击 "Domain management"
2. 点击 "Add custom domain"
3. 输入您的域名（如 `h5.hkuh.hk`）
4. 按照指示添加DNS记录

---

## 方式二：使用Netlify CLI

### 1. 安装Netlify CLI
```bash
npm install -g netlify-cli
```

### 2. 登录Netlify
```bash
netlify login
```

### 3. 部署H5移动端
```bash
cd complaint-h5

# 本地测试部署
netlify deploy

# 生产环境部署
netlify deploy --prod --dir=dist
```

### 4. 部署PC管理后台
```bash
cd complaint-web

# 本地测试部署
netlify deploy

# 生产环境部署
netlify deploy --prod --dir=dist
```

---

## 方式三：GitHub Actions自动部署（推荐）

### 1. 生成Netlify访问令牌

1. 访问 [https://app.netlify.com/user/applications](https://app.netlify.com/user/applications)
2. 点击 "Personal access tokens"
3. 点击 "Create new token"
4. 输入令牌名称，选择有效期
5. 点击 "Generate token"
6. **复制令牌**（只会显示一次）

### 2. 获取Netlify Site ID

1. 在Netlify站点设置中找到 "Site information"
2. 复制 "API ID"

### 3. 配置GitHub Secrets

1. 在GitHub仓库中点击 "Settings" > "Secrets and variables" > "Actions"
2. 添加以下Secrets：

| Name | Value |
|------|-------|
| `NETLIFY_AUTH_TOKEN` | 您生成的访问令牌 |
| `NETLIFY_H5_SITE_ID` | H5站点的API ID |
| `NETLIFY_WEB_SITE_ID` | PC站点的API ID |
| `VITE_API_BASE_URL` | API服务器地址 |

### 4. 启用GitHub Actions

推送代码到GitHub后，Actions将自动触发：
- 提交到 `complaint-h5/` 目录 → 自动部署H5到Netlify
- 提交到 `complaint-web/` 目录 → 自动部署PC到Netlify

---

## 配置文件说明

### netlify.toml

已在项目中创建 `netlify.toml` 配置文件：

```toml
[build]
  command = "npm run build"
  publish = "dist"

[[redirects]]
  from = "/*"
  to = "/index.html"
  status = 200
```

### 环境变量配置

#### H5移动端 (.env.production)
```env
VITE_API_BASE_URL=https://your-api-domain.com
VITE_WECHAT_APP_ID=your_wechat_app_id
VITE_ENABLE_WECHAT_SDK=false
```

#### PC管理后台 (.env.production)
```env
VITE_API_BASE_URL=https://your-api-domain.com
```

### 重要配置说明

1. **API地址**
必须将API服务器地址配置到 `VITE_API_BASE_URL`

2. **微信公众号**
如果使用微信功能，需要配置微信公众号相关参数

3. **跨域问题**
Netlify会自动处理SPA路由，所有请求会重定向到 `index.html`

---

## 后端API配置

### Nginx反向代理配置

如果您的后端API不在Netlify上，需要配置Nginx反向代理：

```nginx
server {
    listen 443 ssl http2;
    server_name api.hkuh.hk;

    ssl_certificate /etc/nginx/ssl/api.hkuh.crt;
    ssl_certificate_key /etc/nginx/ssl/api.hkuh.key;

    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

### CORS配置

后端需要配置CORS允许Netlify域名：

```java
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            .allowedOrigins(
                "https://h5-complaint.netlify.app",
                "https://admin-complaint.netlify.app"
            )
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true)
            .maxAge(3600);
    }
}
```

---

## 常见问题

### 1. 部署失败

**问题**: 构建失败
**解决**: 
- 检查 `npm run build` 本地是否成功
- 确认环境变量配置正确
- 查看Netlify构建日志

### 2. 页面空白

**问题**: 部署后页面显示空白
**解决**:
- 检查 `netlify.toml` 是否正确配置
- 确认 `publish` 目录为 `dist`

### 3. API请求失败

**问题**: 无法请求API
**解决**:
- 确认 `VITE_API_BASE_URL` 配置正确
- 检查后端CORS配置
- 确认API服务器正常运行

### 4. 路由404

**问题**: 刷新页面404
**解决**:
`netlify.toml` 已配置SPA路由重定向，如仍有问题检查文件是否正确

### 5. 微信功能不工作

**问题**: 微信分享、支付等不工作
**解决**:
- 配置正确的微信AppID
- 微信公众号后台配置JS安全域名
- 在微信开发者工具中测试

---

## 部署检查清单

### 部署前检查
- [ ] GitHub仓库已创建
- [ ] 代码已推送到GitHub
- [ ] Netlify账号已注册
- [ ] API服务器已部署并可用

### 环境变量检查
- [ ] `VITE_API_BASE_URL` 已配置
- [ ] `VITE_WECHAT_APP_ID` 已配置（如使用微信）
- [ ] 所有环境变量已在Netlify配置

### 功能测试检查
- [ ] 页面加载正常
- [ ] 登录功能正常
- [ ] API请求正常
- [ ] 路由跳转正常
- [ ] 响应式布局正常

### 安全检查
- [ ] HTTPS已启用
- [ ] 敏感信息未暴露
- [ ] CORS配置正确
- [ ] 安全性请求头已配置

---

## 监控和维护

### Netlify监控
- 访问 Netlify Dashboard 查看部署状态
- 设置构建通知（Email/Slack）
- 查看访问日志和带宽使用

### GitHub Actions
- 监控自动化部署状态
- 查看构建日志
- 配置PR预览部署

### 性能优化
- 启用Netlify Brotli压缩
- 配置CDN缓存
- 优化图片资源
- 启用HTTP/2

---

## 联系和支持

如有问题，请：
1. 查看 [Netlify文档](https://docs.netlify.com/)
2. 查看 [GitHub Actions文档](https://docs.github.com/en/actions)
3. 提交GitHub Issue
