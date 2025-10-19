# Git 提交指南

## 当前状态

- ✅ `legacy-bungeecord` 分支：保留旧代码（支持 BungeeCord）
- ✅ `master` 分支：新代码（只支持 Velocity）

## 提交步骤

### 1. 查看修改
```bash
git status
```

### 2. 添加所有修改
```bash
git add .
```

### 3. 提交到本地
```bash
git commit -m "v2.0: 完全迁移到 Velocity，移除 BungeeCord 支持

主要变更：
- 移除 BungeeCord 支持，旧代码保存在 legacy-bungeecord 分支
- 完全重写以支持 Velocity 3.3+
- 升级到 Paper 1.21.1 API
- 使用 Adventure Component API 替代 BungeeCord Chat API
- 使用 AsyncChatEvent 替代已弃用的 AsyncPlayerChatEvent
- 创建代理适配器模式，提高代码可维护性
- 改进错误处理，即使 QQ 连接失败插件也能正常运行
- 更新文档和 README

技术栈：
- Velocity 3.3+
- Paper 1.21.1+
- Adventure API
- Java 17+
"
```

### 4. 推送 legacy-bungeecord 分支到远程
```bash
git push origin legacy-bungeecord
```

### 5. 推送 master 分支到远程
```bash
git push origin master
```

如果 master 分支之前已经有提交，可能需要强制推送：
```bash
git push origin master --force
```

## 注意事项

⚠️ **在强制推送 master 之前，确保：**
1. 已经创建并推送了 `legacy-bungeecord` 分支作为备份
2. 团队成员知道 master 分支将被强制更新
3. 在 GitHub 上创建一个 Release 标记旧版本（可选但推荐）

## 在 GitHub 上的后续操作

1. **创建 Release**：
   - 为 `legacy-bungeecord` 分支创建一个 `v1.x` 的 Release
   - 为新的 `master` 分支创建 `v2.0` 的 Release

2. **更新分支保护规则**（如果有）

3. **在 README 中添加徽章**显示支持的版本

4. **考虑在 Issues 中添加迁移指南**

