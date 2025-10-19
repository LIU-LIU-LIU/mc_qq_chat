# MC QQ Chat - Velocity 版本

> 🚀 Minecraft 1.21 + Velocity 3.3+ 跨服聊天与 QQ 频道同步插件

## ⚠️ 重要说明

**本分支只支持 Velocity 代理服务器！**

- 如果你还在使用 **BungeeCord**，请切换到 [`legacy-bungeecord`](../../tree/legacy-bungeecord) 分支
- Velocity 是现代化的代理服务器，性能更好，更安全
- BungeeCord 已停止更新，建议迁移到 Velocity

## ✨ 功能特性

### 1. 跨服聊天同步
- ✅ 多个 Paper 后端服务器之间的聊天自动同步
- ✅ 显示消息来源服务器
- ✅ 支持悬停查看服务器信息
- ✅ 点击服务器名可切换服务器

### 2. QQ 频道集成
- ✅ 游戏聊天自动同步到 QQ 频道
- ✅ QQ 频道消息自动同步到游戏
- ✅ 支持 QQ 机器人命令（`@机器人 /glist`）
- ✅ 显示玩家头像和昵称

### 3. 富文本消息
- ✅ 悬停显示玩家延迟
- ✅ 悬停显示服务器玩家列表
- ✅ 点击玩家名执行命令
- ✅ 完整的颜色和格式支持

## 📦 依赖要求

- **Java**: 17+
- **Minecraft**: 1.21.x
- **服务端**: Paper 1.21+
- **代理**: Velocity 3.3+

## 🚀 安装方法

### 1. 编译插件

```bash
mvn clean package
```

### 2. 部署

1. 将编译好的 jar 文件放入 **Velocity** 的 `plugins` 文件夹
2. 将同一个 jar 文件放入**所有 Paper 后端服务器**的 `plugins` 文件夹
3. 配置 `config.yml` 文件
4. 重启所有服务器

## ⚙️ 配置文件

在 Velocity 的 `plugins/mc_qq_chat/config.yml`：

```yaml
# 日志级别: info, warning, fine, finest
log-level: info
logFile: "plugins/mc_qq_chat/run.log"

# QQ 机器人令牌，格式：Bot APPID.Token
botToken: "Bot 102.abcdxxxx"

# 频道索引（如果有多个主频道）
guildsIndex: 0

# 子频道名称
channelName: "服务器内部聊天(与游戏内聊天同步)"

# 频道加入链接
channelUrl: "https://pd.qq.com/s/8tjjog2zh"

# 消息事件类型：MESSAGE_CREATE 或 AT_MESSAGE_CREATE
intents: "MESSAGE_CREATE"
```

## 🏗️ 技术架构

```
┌─────────────────────┐
│   QQ 频道机器人      │
└──────────┬──────────┘
           │ WebSocket
           │
┌──────────▼──────────┐
│     Velocity        │
│   (代理服务器)       │
│                     │
│  - VelocityMain     │
│  - VelocityFun      │
│  - WebSocketApi     │
│  - ProxyAdapter     │
└──────────┬──────────┘
           │ Plugin Message
           │ (mc_qq_chat:bungeecord)
           │
    ┌──────┴──────┬──────────┬──────────┐
    │             │          │          │
┌───▼───┐   ┌────▼────┐ ┌───▼───┐ ┌───▼───┐
│ Paper │   │  Paper  │ │ Paper │ │ Paper │
│ 1.21  │   │  1.21   │ │ 1.21  │ │ 1.21  │
└───────┘   └─────────┘ └───────┘ └───────┘
```

## 🔄 从 BungeeCord 迁移

如果你正在从 BungeeCord 迁移到 Velocity：

1. **备份数据**：备份所有服务器配置和数据
2. **安装 Velocity**：下载并安装 Velocity
3. **迁移配置**：将 BungeeCord 的服务器列表迁移到 Velocity
4. **安装本插件**：按照上述安装方法部署
5. **测试**：在测试环境验证功能正常

## 🐛 常见问题

### 无法接收与发送 QQ 频道的消息
- 检查配置文件中的 APPID 与 Token 是否填写正确
- 确认网络连接正常，防火墙未阻止
- 查看日志文件定位问题

### 无法接收跨服消息
- 确认在所有子服和 Velocity 端均已安装插件
- 检查插件消息通道是否正确注册
- 将日志级别改为 `finest` 观察详细日志

### 插件无法加载
- 确认 Java 版本为 17+
- 检查是否使用 Velocity 3.3+
- 查看 `plugins/mc_qq_chat/run.log`

## 📝 更新日志

### v2.0-SNAPSHOT (当前版本 - master 分支)
- 🚀 完全重写以支持 Velocity
- 🗑️ 移除 BungeeCord 支持
- ⬆️ 升级到 Paper 1.21 API
- 🔄 使用 Adventure Component API
- 🏗️ 采用代理适配器模式

### v1.x (legacy-bungeecord 分支)
- 支持 BungeeCord 1.20
- 支持 Paper 1.20.2

## 💡 相关链接

- [Velocity 官网](https://papermc.io/software/velocity)
- [Paper 官网](https://papermc.io/)
- [QQ 机器人文档](https://bot.q.qq.com/wiki/)
