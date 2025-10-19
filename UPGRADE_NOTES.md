# MC QQ Chat 插件升级说明

## 更新内容

### 版本 1.2-SNAPSHOT

本次更新主要解决了以下问题：

1. **添加 Velocity 代理服务器支持**
   - 原项目只支持 BungeeCord，现在同时支持 Velocity
   - Velocity 使用不同的插件消息系统，已完全适配

2. **升级到 Paper 1.21 API**
   - 更新了聊天事件处理，使用新的 `AsyncChatEvent` 替代已弃用的 `AsyncPlayerChatEvent`
   - 使用 Adventure Component API 替代旧的 BungeeCord Chat API
   - 解决了 1.21 版本的兼容性问题

3. **修复聊天格式化问题**
   - 使用新的 Adventure API 实现聊天消息格式化
   - 保留了悬停和点击事件功能
   - 确保消息在不同服务器间正确同步

## 项目结构

```
mc_qq_chat/
├── bukkit/          # Bukkit/Spigot/Paper 后端插件
├── bungee/          # BungeeCord 代理插件（兼容保留）
├── velocity/        # Velocity 代理插件（新增）
└── util/            # 工具类和共享代码
```

## 使用方法

### 对于 BungeeCord 代理服务器

1. 将编译好的 jar 文件放入 BungeeCord 的 `plugins` 文件夹
2. 将 jar 文件也放入每个 Bukkit/Spigot/Paper 后端服务器的 `plugins` 文件夹
3. 配置 `config.yml` 文件，设置 QQ 机器人令牌等信息
4. 重启服务器

### 对于 Velocity 代理服务器（新增）

1. 将编译好的 jar 文件放入 Velocity 的 `plugins` 文件夹
2. 将 jar 文件也放入每个 Bukkit/Spigot/Paper 后端服务器的 `plugins` 文件夹
3. 配置 `config.yml` 文件，设置 QQ 机器人令牌等信息
4. 重启服务器

**注意**：Velocity 和 BungeeCord 使用相同的插件 jar 文件，会自动检测并加载对应的模块。

## 配置文件说明

`config.yml` 配置示例：

```yaml
# 配置日志级别，可选的是 info warning fine finest
log-level: info
logFile: "plugins/mc_qq_chat/run.log"

# 机器人的令牌，格式是 Bot APPID.Token
botToken: "Bot 102.abcdxxxx"

# 如果机器人内有多个主频道，这里输入频道索引
guildsIndex: 0

# 机器人主频道的下面的子频道名称
channelName: "服务器内部聊天(与游戏内聊天同步)"

# 频道的加入链接，用于格式化消息时处理点击事件
channelUrl: "https://pd.qq.com/s/8tjjog2zh"

# 推送消息事件，AT_MESSAGE_CREATE代表当收到@机器人的消息时
# MESSAGE_CREATE代表频道内的全部消息，而不只是at
intents: "MESSAGE_CREATE"
```

## 功能特性

1. **跨服聊天同步**
   - 支持多个 Bukkit 后端服务器之间的聊天同步
   - 通过 BungeeCord/Velocity 代理服务器转发消息

2. **QQ 频道集成**
   - 游戏内聊天自动同步到 QQ 频道
   - QQ 频道消息自动同步到游戏内
   - 支持 QQ 机器人命令（如 `/glist` 查看在线玩家）

3. **富文本消息**
   - 消息支持悬停显示详细信息（如玩家延迟、服务器人数）
   - 支持点击事件（如点击玩家名传送、点击服务器名切换服务器）
   - 支持点击查看玩家统计数据

4. **日志系统**
   - 可配置的日志级别
   - 独立的日志文件

## 编译方法

```bash
mvn clean package
```

编译后的 jar 文件位于 `target/` 目录。

## 依赖要求

- **Java**: 17+
- **Minecraft**: 1.21.x
- **服务端**: Paper 1.21+ (推荐)
- **代理**: Velocity 3.3+ 或 BungeeCord 1.20+

## 技术细节

### 插件消息通道

- 通道标识符: `mc_qq_chat:bungeecord`
- 子通道: `mc_qq_chat:chat`

### API 变化

1. **聊天事件**
   - 旧版: `org.bukkit.event.player.AsyncPlayerChatEvent`
   - 新版: `io.papermc.paper.event.player.AsyncChatEvent`

2. **文本组件**
   - 旧版: `net.md_5.bungee.api.chat.TextComponent`
   - 新版: `net.kyori.adventure.text.Component`

3. **消息序列化**
   - 旧版: `net.md_5.bungee.chat.ComponentSerializer`
   - 新版: `net.kyori.adventure.text.serializer.gson.GsonComponentSerializer`

## 已知问题

- 无

## 问题反馈

如有问题，请提交 Issue。

## 许可证

[请根据您的项目添加许可证信息]

