# 网关服务技术说明

## 1. 概述

网关服务使用 **Spring Cloud Gateway**，配合 **Nacos** 作为注册中心，统一对外暴露 API，并转发请求至后端微服务。

- **网关端口**：9000
- **后端服务名**：kindergarten-backend
- **路由规则**：`/api/**` → `lb://kindergarten-backend`

## 2. 技术栈

| 组件 | 版本 |
|------|------|
| Spring Cloud Gateway | 4.x（随 Spring Cloud 2023.0） |
| Spring Cloud Alibaba Nacos Discovery | 2023.0.1.0 |
| Nacos Server | 2.2.3 |

## 3. 配置说明

### 3.1 网关配置（gateway/application.yml）

```yaml
server:
  port: 9000

spring:
  application:
    name: kindergarten-gateway
  cloud:
    nacos:
      discovery:
        server-addr: ${NACOS_SERVER_ADDR:127.0.0.1:8848}
        namespace: ${NACOS_NAMESPACE:}
    gateway:
      discovery:
        locator:
          enabled: true
          lower-case-service-id: true
      routes:
        - id: kindergarten-backend
          uri: lb://kindergarten-backend
          predicates:
            - Path=/api/**
```

- `lb://kindergarten-backend`：通过 Nacos 服务发现 + 负载均衡转发；**必须引入 `spring-cloud-starter-loadbalancer`**，否则会报 503（无法解析服务实例）
- 环境变量 `NACOS_SERVER_ADDR`：Nacos 地址，默认 `127.0.0.1:8848`
- 环境变量 `NACOS_NAMESPACE`：Nacos 命名空间，为空则使用 public；**网关与后端需一致**
- `fail-fast: false`：Nacos 未就绪时不阻塞启动，后台重试注册

### 3.2 后端配置（backend/application.yml）

后端需注册到 Nacos，供网关发现：

```yaml
spring:
  application:
    name: kindergarten-backend
  cloud:
    nacos:
      discovery:
        server-addr: ${NACOS_SERVER_ADDR:127.0.0.1:8848}
        namespace: ${NACOS_NAMESPACE:}
```

## 4. 启动方式

### 4.1 使用 Nacos（生产/联调）

1. **启动 Nacos**
   ```bash
   # Docker 方式（Apple Silicon 需加 --platform linux/amd64）
   docker run -d --platform linux/amd64 \
     -p 8848:8848 -p 9848:9848 -p 9849:9849 \
     -e MODE=standalone -e SPRING_DATASOURCE_PLATFORM=derby \
     -v /Users/wxp/database/nacos/logs:/home/nacos/logs \
     -v /Users/wxp/database/nacos/conf:/home/nacos/conf \
     nacos/nacos-server:v2.2.3
   ```

2. **启动后端**
   ```bash
   cd backend && mvn spring-boot:run
   ```

3. **启动网关**
   ```bash
   cd gateway && mvn spring-boot:run
   ```

4. **启动前端**（开发时通过 Vite 代理将 `/api` 指向 `http://localhost:9000`）
   ```bash
   cd frontend && npm run dev
   ```

## 5. 前端接入

开发环境（`vite.config.ts`）已将 `/api` 代理到网关：

```typescript
proxy: {
  '/api': {
    target: 'http://localhost:9000',
    changeOrigin: true,
  },
}
```

生产环境：前端部署后，API 请求应指向网关域名或 IP（如 `https://api.example.com`），由 Nginx 或负载均衡将 `/api` 转发至网关 9000 端口。

## 6. 路由说明

| 前端请求 | 网关转发 | 后端实际接口 |
|----------|----------|--------------|
| GET /api/sessions | lb://kindergarten-backend/api/sessions | GET /api/sessions |
| POST /api/chat/stream | lb://kindergarten-backend/api/chat/stream | POST /api/chat/stream |

路径不变，仅入口由直连后端改为经网关转发。

## 7. 常见问题

### 503 Service Unavailable（lb 路由）

前端经网关访问时若一直返回 **503**，且 Nacos 上已能看到 `kindergarten-backend` 实例，多半是网关缺少 **LoadBalancer** 依赖，无法把 `lb://kindergarten-backend` 解析成实际主机和端口。

**解决**：在 `gateway/pom.xml` 中确保存在：

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-loadbalancer</artifactId>
</dependency>
```

添加后重新编译并重启网关即可。

### Nacos 版本与 nacos-client 匹配

当前项目使用 **Spring Cloud Alibaba 2023.0.1.0**（nacos-client 2.x），需配合 **Nacos Server 2.x**。

**Nacos 1.x** 仅支持 HTTP（8848），与 nacos-client 2.x 不兼容。**Nacos 3.x** 需升级到 Spring Cloud Alibaba 2025.x。

启动 Nacos 2.2.3 时需同时暴露 8848（HTTP）、9848、9849（gRPC），并使用 `SPRING_DATASOURCE_PLATFORM=derby` 启用内置 Derby。

### Client not connected, current status:STARTING

该错误表示 Nacos 客户端的 **gRPC 连接**（端口 9848）未建立。8848 仅用于 HTTP，Java 客户端主要使用 gRPC（9848）。

**排查步骤：**

1. **验证 9848 端口是否可达**
   ```bash
   nc -zv 127.0.0.1 9848
   # 或
   telnet 127.0.0.1 9848
   ```
   若连接失败，说明 9848 未监听或被拦截。

2. **Docker 部署**：必须同时暴露 9848 和 9849
   ```bash
   docker run -d -p 8848:8848 -p 9848:9848 -p 9849:9849 nacos/nacos-server:v3.0.2
   ```

3. **本地/standalone 部署**：检查防火墙是否放行 9848；确认 Nacos 启动日志中有 gRPC 相关输出。

4. **延迟注册（临时缓解）**：若 gRPC 连接较慢，可增加延迟
   ```yaml
   kindergarten:
     nacos:
       registration-delay-ms: 8000  # 默认 5000
   ```
   或环境变量 `NACOS_REGISTRATION_DELAY_MS=8000`。

5. **JVM 参数（已内置）**：`mvn spring-boot:run` 时自动注入以下参数，缓解 IPv6/gRPC 连接问题：
   - `-Djava.net.preferIPv4Stack=true`：强制 IPv4
   - `-Dnacos.remote.client.grpc.timeout=15000`
   - `-Dnacos.remote.client.grpc.server.check.timeout=15000`
   
   **IDE 或 java -jar 启动时**需手动添加上述 JVM 参数。
