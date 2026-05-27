# CLAUDE.md — 我的助理 Android App

## 项目概要
Android 原生 App，使用 Kotlin + Jetpack Compose。
核心功能：截图自动监控 → DeepSeek API 解析 → 信息卡片管理。

## 构建命令
```bash
./gradlew assembleDebug          # 构建 Debug APK
./gradlew test                   # 运行单元测试
./gradlew connectedAndroidTest   # 运行仪器测试（需连接设备）
./gradlew lint                   # 代码检查
```

## 架构原则
- Clean Architecture：data / domain / presentation 三层严格分离
- MVI 模式：UI 只消费 UiState，通过 Intent 驱动 ViewModel
- 单 Activity：所有页面为 Composable，使用 Compose Navigation
- Repository 模式：ViewModel 只依赖 UseCase，UseCase 只依赖 Repository 接口

## 代码规范
- 所有 suspend 函数必须有错误处理（runCatching 或 Result）
- Flow 在 ViewModel 中使用 stateIn(SharingStarted.WhileSubscribed(5000))
- Composable 函数名以大写开头，参数超过 3 个时换行对齐
- 每个 UseCase 只做一件事，文件名 = 动词 + 名词 + UseCase

## 关键约束
- API Key 绝对不能硬编码，只能从 EncryptedSharedPreferences 读取
- 截图文件路径只能临时使用，解析完成后不复制原文件
- 所有数据库操作必须在非主线程执行（Room 已强制，WorkManager 保证）
- 折叠屏布局：使用 WindowSizeClass 判断，不能用硬编码 dp 值

## 已知外部依赖
- DeepSeek API：https://api.deepseek.com/v1（需要 API Key）
- 模型：deepseek-chat（文字）/ 视觉模型按文档最新名称
