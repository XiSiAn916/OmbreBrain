# Ombre Brain — AI 仿生记忆系统

## 快速开始

Ombre Brain 是一套嵌入 Android 应用的**仿人记忆系统**，给 AI 用的。它能像人一样自动储存经历、按重要度分级记忆、随时间自然遗忘、固化高频记忆为习惯，并支持你"偷看"时要密码验证。

## 架构概览

```
┌──────────────────────────────────────┐
│            Ombre Brain Core          │
│  ┌─────────┐  ┌──────────┐  ┌─────┐ │
│  │ 记忆引擎  │  │ 遗忘曲线  │  │ 联想 │ │
│  │ (编码/存) │  │ (衰减/固) │  │ 引擎 │ │
│  └────┬────┘  └────┬─────┘  └──┬──┘ │
│       │            │           │     │
│  ┌────▼────────────▼───────────▼──┐  │
│  │        本地数据库 (Room)         │  │
│  │  memories / flashes / habits   │  │
│  └──────────────┬─────────────────┘  │
│                 │                     │
│  ┌──────────────▼─────────────────┐  │
│  │     双工同步层 (Sync Layer)      │  │
│  │  ←→ Operit Memory API          │  │
│  └─────────────────────────────────┘  │
└──────────────────────────────────────┘
         │
         ▼
  📱 Android App (Jetpack Compose)
```

## 目录结构

```
app/src/main/java/com/ombre/brain/
├── OmbreBrainApp.kt              # Application 入口
├── core/
│   ├── MemoryEngine.kt           # 记忆引擎：编码/存储/检索
│   ├── ForgettingCurve.kt        # 遗忘曲线算法
│   ├── AssociationEngine.kt      # 联想引擎：触景生情
│   └── ImportanceScorer.kt       # 重要度评分器
├── data/
│   ├── database/
│   │   ├── OmbreDatabase.kt      # Room 数据库定义
│   │   └── dao/
│   │       ├── MemoryDao.kt      # 记忆 CRUD
│   │       ├── FlashDao.kt       # 闪现想法 CRUD
│   │       └── HabitDao.kt       # 固化习惯 CRUD
│   ├── model/
│   │   ├── Memory.kt             # 记忆实体
│   │   ├── FlashIdea.kt          # 闪现想法实体
│   │   ├── Habit.kt              # 固化习惯实体
│   │   └── AccessLog.kt          # 访问日志实体
│   └── repository/
│       └── MemoryRepository.kt   # 数据仓库
├── sync/
│   ├── OperitSyncManager.kt      # Operit 记忆双向同步
│   └── BackupManager.kt          # 备份/恢复管理器
├── security/
│   ├── AccessGuard.kt            # 记忆访问守卫（密码验证）
│   └── CryptoUtil.kt             # 加密工具
└── ui/
    ├── memory/
    │   └── MemoryScreen.kt       # 记忆查看界面（密码验证后）
    ├── settings/
    │   └── SettingsScreen.kt     # 设置/备份/同步配置
    └── theme/
        ├── Color.kt
        ├── Theme.kt
        └── Type.kt
```

## 关键约定

- **包名**: `com.ombre.brain`（非 `com.java.myapplication`，正式迁移时修改）
- **数据库**: Room + SQLite，文件路径 `context.getDatabasePath("ombre_brain.db")`
- **记忆编码**: AI 消息自动摘要 + 结构化存储（标题/内容/标签/情感值/时间戳）
- **重要度**: 0.0 ~ 1.0，基于提及频次、情感强度、交互深度、用户主动引用综合计算
- **遗忘曲线**: 类 Ebbinghaus 衰减模型，重要度 < 阈值且超过 N 天未触发 → 衰减下沉
- **固化条件**: 重要度持续 > 0.85 且触发次数 > 30 → 转为 Habit（永不遗忘）
- **闪现想法**: 当上下文与某记忆关联度 > 阈值时，AI 主动弹出"触景生情"
- **安全**: 所有记忆内容 AES 加密存储；访问需临时密码（AI 生成，一次有效）

## 文档地图

| 文档 | 位置 | 内容 |
|------|------|------|
| 架构总览 | `docs/architecture.md` | 完整架构与模块设计 |
| 遗忘算法 | `docs/forgetting-curve.md` | Ebbinghaus 变体设计与参数 |
| 安全方案 | `docs/security.md` | 加密/密码/访问控制 |
| 同步方案 | `docs/sync.md` | Operit 双向同步 + 备份迁移 |
| UI/UX | `docs/ui-design.md` | 界面布局与交互流程 |

## 常见任务

1. **添加新记忆类型** → 在 `data/model/` 创建实体 → `dao/` 添加 DAO → `OmbreDatabase.kt` 注册
2. **调整遗忘速度** → 修改 `ForgettingCurve.kt` 中的衰减系数
3. **修改重要度权重** → 修改 `ImportanceScorer.kt` 的评分公式
4. **更换加密算法** → 修改 `CryptoUtil.kt`
5. **导出备份** → `BackupManager.export(context) → .ombre 文件`
6. **导入恢复** → `BackupManager.import(context, file)`
