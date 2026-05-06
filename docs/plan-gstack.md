项目概述

  产品定位: 个人终身投资学习私人系统，兼顾自用与商业化
  - 核心功能: 抖音式碎片化刷学 + 多邻国式间隔重复记忆 + 体系化知识图谱 + 经典书籍联动
  - 目标用户: 2.2亿A股个人投资者、职场中年人(35-50岁)、投资新手
  - MVP周期: 8-10周
  - 技术栈: Flutter/uni-app (前端) + Java SpringBoot (后端) + MySQL + Redis

  商业模式

  - 基础免费: 入门知识点 + 基础书籍拆解
  - 会员付费: 进阶知识点 + 高阶书籍 + 专属学习路径 + 高级功能

  ---
  CEO Review 接受的功能 (SELECTIVE EXPANSION)

  ┌─────────────────────┬──────────────────────────────────────────┬────────┐
  │        功能         │                   描述                   │ 优先级 │
  ├─────────────────────┼──────────────────────────────────────────┼────────┤
  │ 学习连续签到 + 徽章 │ 每日刷学连续签到，链式激励               │ P1     │
  ├─────────────────────┼──────────────────────────────────────────┼────────┤
  │ 个人数据中心        │ 模块掌握雷达图、周学习热力图、薄弱点列表 │ P1     │
  ├─────────────────────┼──────────────────────────────────────────┼────────┤
  │ 自适应间隔重复算法  │ 基于SM-2的个性化复习调度                 │ P1     │
  ├─────────────────────┼──────────────────────────────────────────┼────────┤
  │ 暗色模式 + 护眼模式 │ 通勤/睡前场景适配                        │ P2     │
  └─────────────────────┴──────────────────────────────────────────┴────────┘

  ---
  数据库设计

  核心表结构

  users                    # 用户表
  ├── id (PK)
  ├── username
  ├── avatar
  ├── level
  ├── streak_count          # 当前连续签到天数
  ├── longest_streak       # 最长连续签到
  ├── last_active_date     # 最后活跃日期
  ├── theme_preference      # light/dark/sepia
  ├── created_at
  └── updated_at

  knowledge_nodes         # 知识图谱节点
  ├── id (PK)
  ├── parent_id            # 父节点ID
  ├── title
  ├── content_json         # 内容(JSON格式)
  ├── infographic_url     # 信息图URL ← 新增
  ├── difficulty           # 难度等级
  ├── node_type            # topic/subtopic/point
  ├── prerequisite_ids     # 前置节点(JSON数组)
  ├── related_node_ids     # 相关节点(JSON数组)
  └── created_at

  knowledge_edges         # 知识图谱边
  ├── id (PK)
  ├── from_node_id
  ├── to_node_id
  └── edge_type            # prerequisite/related/similar

  questions               # 题目表
  ├── id (PK)
  ├── node_id (FK)         # 关联知识点
  ├── question_type        # choice/judgment/fill
  ├── content_json         # 题目内容
  ├── answer_json          # 答案+解析
  ├── explanation          # AI解释(可选)
  └── created_at

  user_progress          # 用户学习进度
  ├── id (PK)
  ├── user_id (FK)
  ├── node_id (FK)
  ├── mastery_level        # 掌握等级 0-5
  ├── next_review_date    # 下次复习日期
  ├── review_count        # 复习次数
  ├── last_reviewed_at
  └── updated_at

  user_answers           # 用户答题记录
  ├── id (PK)
  ├── user_id (FK)
  ├── question_id (FK)
  ├── answer
  ├── is_correct
  ├── answered_at
  └── time_spent_ms

  streaks                # 签到表
  ├── id (PK)
  ├── user_id (FK)
  ├── current_streak
  ├── longest_streak
  ├── last_streak_date
  └── updated_at

  badges                 # 徽章表
  ├── id (PK)
  ├── name
  ├── description
  ├── icon_url
  └── criteria_json       # 获取条件

  user_badges           # 用户徽章
  ├── user_id (FK)
  ├── badge_id (FK)
  └── earned_at

  books                 # 书籍表
  ├── id (PK)
  ├── title
  ├── author
  ├── cover_url
  ├── chapters_json      # 章节列表
  ├── linked_node_ids    # 关联知识点
  └── created_at

  user_book_progress    # 用户阅读进度
  ├── id (PK)
  ├── user_id (FK)
  ├── book_id (FK)
  ├── current_chapter
  ├── total_chapters
  ├── started_at
  └── finished_at

  analytics_sessions    # 分析会话(用于热力图)
  ├── id (PK)
  ├── user_id (FK)
  ├── session_date
  ├── topics_studied
  ├── questions_answered
  ├── accuracy_rate
  └── study_duration_minutes

  ---
  后端API设计

  认证模块

  POST /api/auth/register     # 注册
  POST /api/auth/login        # 登录
  POST /api/auth/refresh      # 刷新Token

  用户模块

  GET  /api/users/me          # 获取当前用户
  PUT  /api/users/me          # 更新用户信息(含主题偏好)

  知识图谱模块

  GET  /api/nodes                    # 获取知识图谱结构
  GET  /api/nodes/:id                # 节点详情
  GET  /api/nodes/:id/children       # 子节点
  GET  /api/nodes/:id/feed           # 获取该节点相关的刷学内容
  POST /api/nodes                    # 创建节点(管理员)
  POST /api/nodes/:id/infographic    # 生成信息图 ← 新增

  刷学Feed模块

  GET  /api/feed                     # 每日刷学队列(基于SM-2调度)
  POST /api/nodes/:id/progress        # 更新节点掌握程度

  题目模块

  GET  /api/questions?node_id=X      # 获取题库
  POST /api/questions/:id/answer       # 提交答案

  错题本模块

  GET  /api/mistakes                 # 全部错题
  GET  /api/mistakes/review           # 复习队列(基于间隔重复)
  POST /api/mistakes/:id/relearn      # 重新学习标记

  书籍模块

  GET  /api/books                    # 书籍列表
  POST /api/books                    # 上传书籍
  GET  /api/books/:id                # 书籍详情(含章节)
  GET  /api/books/:id/chapters/:cid  # 章节内容+关联知识点

  签到模块

  GET  /api/streaks                  # 签到信息
  POST /api/streaks/heartbeat         # 每日签到心跳

  徽章模块

  GET  /api/badges                   # 全部徽章(含获得状态)

  数据分析模块

  GET  /api/analytics/dashboard      # 仪表盘数据
  GET  /api/analytics/heatmap         # 周热力图数据
  GET  /api/analytics/weak-topics     # 薄弱知识点

  ---
  前端项目结构

  lib/
  ├── main.dart
  ├── app.dart
  ├── config/
  │   └── theme.dart                 # 主题配置
  ├── models/
  │   ├── user.dart
  │   ├── knowledge_node.dart
  │   ├── question.dart
  │   ├── book.dart
  │   └── badge.dart
  ├── providers/
  │   ├── auth_provider.dart
  │   ├── theme_provider.dart         # 暗色+护眼模式
  │   ├── learning_provider.dart      # 学习进度状态
  │   └── streak_provider.dart
  ├── services/
  │   ├── api_service.dart
  │   ├── auth_service.dart
  │   ├── spaced_repetition_service.dart  # SM-2算法
  │   └── analytics_service.dart
  ├── screens/
  │   ├── home_screen.dart           # 刷知识点(TikTok式)
  │   ├── question_screen.dart       # 刷题
  │   ├── mistake_screen.dart        # 错题本
  │   ├── review_screen.dart         # 复习
  │   ├── graph_screen.dart          # 知识图谱
  │   ├── book_screen.dart           # 书籍模块
  │   ├── book_detail_screen.dart    # 书籍章节
  │   ├── profile_screen.dart        # 个人中心
  │   └── analytics_screen.dart       # 数据中心
  ├── components/
  │   ├── swipe_card.dart            # 刷学卡片(含信息图)
  │   ├── question_card.dart
  │   ├── node_graph.dart            # 知识图谱可视化
  │   ├── streak_badge.dart
  │   ├── mastery_radar.dart          # 掌握度雷达图
  │   ├── weekly_heatmap.dart         # 周热力图
  │   └── topic_card.dart
  └── utils/
      ├── sm2_algorithm.dart         # SM-2间隔重复算法
      └── date_utils.dart

  ---
  实施阶段

  Phase 1: 基础架构 (第1-2周)

  ┌──────────────────────┬───────────────────────────────────────────┬────────┐
  │         任务         │                   文件                    │  依赖  │
  ├──────────────────────┼───────────────────────────────────────────┼────────┤
  │ Flutter项目初始化    │ flutter/                                  │ -      │
  ├──────────────────────┼───────────────────────────────────────────┼────────┤
  │ SpringBoot项目初始化 │ backend/                                  │ -      │
  ├──────────────────────┼───────────────────────────────────────────┼────────┤
  │ 数据库表设计+迁移    │ backend/src/main/resources/db/migration/  │ -      │
  ├──────────────────────┼───────────────────────────────────────────┼────────┤
  │ JWT认证              │ backend/.../api/auth/                     │ 数据库 │
  ├──────────────────────┼───────────────────────────────────────────┼────────┤
  │ 用户API              │ backend/.../api/users/                    │ 认证   │
  ├──────────────────────┼───────────────────────────────────────────┼────────┤
  │ ThemeProvider骨架    │ flutter/lib/providers/theme_provider.dart │ -      │
  └──────────────────────┴───────────────────────────────────────────┴────────┘

  交付物: 前后端骨架，认证流程，主题切换骨架

  ---
  Phase 2: 核心刷学闭环 (第3-4周)

  ┌──────────────────┬──────────────────────────────────────────────────┬───────────────┐
  │       任务       │                       文件                       │     依赖      │
  ├──────────────────┼──────────────────────────────────────────────────┼───────────────┤
  │ 知识图谱API      │ backend/.../api/nodes/                           │ Phase 1       │
  ├──────────────────┼──────────────────────────────────────────────────┼───────────────┤
  │ 知识节点数据填充 │ -                                                │ PRD知识点清单 │
  ├──────────────────┼──────────────────────────────────────────────────┼───────────────┤
  │ SM-2间隔重复服务 │ backend/.../service/SpacedRepetitionService.java │ Phase 1       │
  ├──────────────────┼──────────────────────────────────────────────────┼───────────────┤
  │ 刷学Feed API     │ backend/.../api/feed/                            │ SM-2服务      │
  ├──────────────────┼──────────────────────────────────────────────────┼───────────────┤
  │ 题目API          │ backend/.../api/questions/                       │ 节点          │
  ├──────────────────┼──────────────────────────────────────────────────┼───────────────┤
  │ 进度跟踪API      │ backend/.../api/progress/                        │ 题目          │
  ├──────────────────┼──────────────────────────────────────────────────┼───────────────┤
  │ 前端首页(刷卡片) │ flutter/lib/screens/home_screen.dart             │ Feed API      │
  ├──────────────────┼──────────────────────────────────────────────────┼───────────────┤
  │ 前端刷题页       │ flutter/lib/screens/question_screen.dart         │ 题目API       │
  └──────────────────┴──────────────────────────────────────────────────┴───────────────┘

  交付物: 可刷知识点，看题答题，即时反馈

  ---
  Phase 3: 错题本+复习 (第5周)

  ┌──────────────┬───────────────────────────────────────────────┬─────────┐
  │     任务     │                     文件                      │  依赖   │
  ├──────────────┼───────────────────────────────────────────────┼─────────┤
  │ 错题本API    │ backend/.../api/mistakes/                     │ Phase 2 │
  ├──────────────┼───────────────────────────────────────────────┼─────────┤
  │ 复习队列API  │ backend/.../api/mistakes/review               │ SM-2    │
  ├──────────────┼───────────────────────────────────────────────┼─────────┤
  │ 答题记录服务 │ backend/.../service/AnswerLoggingService.java │ Phase 2 │
  ├──────────────┼───────────────────────────────────────────────┼─────────┤
  │ 前端错题本页 │ flutter/lib/screens/mistake_screen.dart       │ 错题API │
  ├──────────────┼───────────────────────────────────────────────┼─────────┤
  │ 前端复习页   │ flutter/lib/screens/review_screen.dart        │ 复习API │
  └──────────────┴───────────────────────────────────────────────┴─────────┘

  交付物: 错题本，间隔重复复习

  ---
  Phase 4: 知识图谱可视化 (第5-6周)

  ┌──────────────────┬──────────────────────────────────────────────┬─────────┐
  │       任务       │                     文件                     │  依赖   │
  ├──────────────────┼──────────────────────────────────────────────┼─────────┤
  │ 图节点懒加载API  │ backend/.../api/nodes/                       │ Phase 2 │
  ├──────────────────┼──────────────────────────────────────────────┼─────────┤
  │ 学习路径推荐服务 │ backend/.../service/LearningPathService.java │ 图API   │
  ├──────────────────┼──────────────────────────────────────────────┼─────────┤
  │ 前端图谱组件     │ flutter/lib/components/node_graph.dart       │ 图API   │
  ├──────────────────┼──────────────────────────────────────────────┼─────────┤
  │ 前端图谱页       │ flutter/lib/screens/graph_screen.dart        │ 图组件  │
  └──────────────────┴──────────────────────────────────────────────┴─────────┘

  交付物: 可视化知识图谱，学习路径

  ---
  Phase 5: 书籍模块 (第6-7周)

  ┌──────────────┬─────────────────────────────────────────────┬─────────┐
  │     任务     │                    文件                     │  依赖   │
  ├──────────────┼─────────────────────────────────────────────┼─────────┤
  │ 书籍API      │ backend/.../api/books/                      │ Phase 2 │
  ├──────────────┼─────────────────────────────────────────────┼─────────┤
  │ 章节关联服务 │ backend/.../service/BookLinkingService.java │ 书籍API │
  ├──────────────┼─────────────────────────────────────────────┼─────────┤
  │ 阅读进度API  │ backend/.../api/books/progress/             │ 书籍API │
  ├──────────────┼─────────────────────────────────────────────┼─────────┤
  │ 前端书籍列表 │ flutter/lib/screens/book_screen.dart        │ 书籍API │
  ├──────────────┼─────────────────────────────────────────────┼─────────┤
  │ 前端书籍详情 │ flutter/lib/screens/book_detail_screen.dart │ 章节API │
  └──────────────┴─────────────────────────────────────────────┴─────────┘

  交付物: 书籍上传，章节浏览，知识点联动

  ---
  Phase 6: 签到+徽章 (第7周)

  ┌──────────────┬──────────────────────────────────────────┬──────────┐
  │     任务     │                   文件                   │   依赖   │
  ├──────────────┼──────────────────────────────────────────┼──────────┤
  │ 签到API      │ backend/.../api/streaks/                 │ Phase 2  │
  ├──────────────┼──────────────────────────────────────────┼──────────┤
  │ 徽章API      │ backend/.../api/badges/                  │ Phase 2  │
  ├──────────────┼──────────────────────────────────────────┼──────────┤
  │ 签到计算服务 │ backend/.../service/StreakService.java   │ 签到API  │
  ├──────────────┼──────────────────────────────────────────┼──────────┤
  │ 徽章授予逻辑 │ backend/.../service/BadgeService.java    │ 签到服务 │
  ├──────────────┼──────────────────────────────────────────┼──────────┤
  │ 前端签到显示 │ flutter/lib/components/streak_badge.dart │ 签到API  │
  ├──────────────┼──────────────────────────────────────────┼──────────┤
  │ 前端个人中心 │ flutter/lib/screens/profile_screen.dart  │ 徽章API  │
  └──────────────┴──────────────────────────────────────────┴──────────┘

  交付物: 连续签到，徽章系统

  ---
  Phase 7: 数据中心 (第8周)

  ┌──────────────┬────────────────────────────────────────────┬───────────┐
  │     任务     │                    文件                    │   依赖    │
  ├──────────────┼────────────────────────────────────────────┼───────────┤
  │ 分析API      │ backend/.../api/analytics/                 │ Phase 2+3 │
  ├──────────────┼────────────────────────────────────────────┼───────────┤
  │ 数据聚合服务 │ backend/.../service/AnalyticsService.java  │ 答题记录  │
  ├──────────────┼────────────────────────────────────────────┼───────────┤
  │ 前端雷达图   │ flutter/lib/components/mastery_radar.dart  │ 分析API   │
  ├──────────────┼────────────────────────────────────────────┼───────────┤
  │ 前端热力图   │ flutter/lib/components/weekly_heatmap.dart │ 分析API   │
  ├──────────────┼────────────────────────────────────────────┼───────────┤
  │ 前端数据中心 │ flutter/lib/screens/analytics_screen.dart  │ 图表组件  │
  └──────────────┴────────────────────────────────────────────┴───────────┘

  交付物: 个人学习数据中心

  ---
  Phase 8: 收尾+主题 (第8周)

  ┌──────────────────┬──────────────────────────────────┬──────────────┐
  │       任务       │               文件               │     依赖     │
  ├──────────────────┼──────────────────────────────────┼──────────────┤
  │ 暗色模式完整实现 │ flutter/lib/config/theme.dart    │ Phase 1      │
  ├──────────────────┼──────────────────────────────────┼──────────────┤
  │ 护眼模式实现     │ flutter/lib/config/theme.dart    │ Phase 1      │
  ├──────────────────┼──────────────────────────────────┼──────────────┤
  │ 主题持久化       │ backend/.../api/users/ + Flutter │ 主题Provider │
  ├──────────────────┼──────────────────────────────────┼──────────────┤
  │ 性能优化         │ 前端各组件                       │ -            │
  └──────────────────┴──────────────────────────────────┴──────────────┘

  交付物: 完整主题系统，性能优化

  ---
  信息图生成流程

  每个知识点需要配套信息图(infographic)，流程如下:

  知识节点创建
      ↓
  调用 picture.js 生成信息图
      ↓
  图片存储到项目文件系统 (backend/uploads/infographics/)
      ↓
  infographic_url 存入 knowledge_nodes 表
      ↓
  前端刷学卡片显示图片

  picture.js 脚本位置: /picture.js
  图片存储目录: backend/uploads/infographics/
  支持格式: PNG/JPG

  ---
  测试策略

  单元测试

  - SM-2算法 (SpacedRepetitionServiceTest)
  - 签到计算 (StreakServiceTest)
  - 进度更新 (ProgressServiceTest)

  集成测试

  - 认证流程
  - Feed API调度正确性
  - 答题→错题本→复习完整流程

  E2E测试

  - 刷学完整流程: 打开APP → 刷10张卡 → 验证进度更新
  - 答题流程: 答5题 → 检查错题本

  ---
  风险管理

  ┌──────────┬──────────────────────┬────────────────────────────┐
  │   风险   │         描述         │          缓解方案          │
  ├──────────┼──────────────────────┼────────────────────────────┤
  │ 内容空白 │ 知识图谱无初始数据   │ 基于PRD 170个知识点填充    │
  ├──────────┼──────────────────────┼────────────────────────────┤
  │ 算法调优 │ SM-2参数需适配用户群 │ 管理后台可调整间隔参数     │
  ├──────────┼──────────────────────┼────────────────────────────┤
  │ 图谱性能 │ 低配设备图谱渲染卡顿 │ Canvas渲染+列表降级        │
  ├──────────┼──────────────────────┼────────────────────────────┤
  │ 用户流失 │ 第1周后参与度下降    │ 推送通知+签到激励+周报邮件 │
  └──────────┴──────────────────────┴────────────────────────────┘

  ---
  下一步行动

  1. 确认技术选型: Flutter 还是 uni-app？(WeChat集成需求影响选择)
  2. 确认picture.js: 当前脚本内容为"ttt"，需要明确生成逻辑
  3. 启动Phase 1: 项目脚手架搭建

  ---
  成功标准

  - 用户可注册登录
  - TikTok式刷知识点(含信息图)
  - 刷题即时反馈
  - 错题本间隔重复复习
  - 知识图谱可视化
  - 书籍上传+章节联动
  - 连续签到+徽章
  - 数据中心(雷达图+热力图+薄弱点)
  - 暗色模式+护眼模式
  - 后端单元测试覆盖率 >= 80%