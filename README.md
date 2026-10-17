# 薄肌预备役

> 还在努力，还没到位，但一直在路上。
> 一个帮你把「多喝水 / 早睡 / 去健身」三件事坚持下去的 Android App。

![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-7F52FF?logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Compose%20BOM-2026.02.01-4285F4?logo=jetpackcompose&logoColor=white)
![minSdk](https://img.shields.io/badge/minSdk-26%20(Android%208.0)-3DDC84?logo=android&logoColor=white)
![targetSdk](https://img.shields.io/badge/targetSdk-37-3DDC84?logo=android&logoColor=white)
![License](https://img.shields.io/badge/license-unspecified-lightgrey)

三个模块分别叫 **大水理论 / 大睡理论 / 薄肌理论**。语气是轻松自嘲的，像朋友之间互相督促——
不说教、不制造焦虑、不打卡排名。所有数据只存在你手机里，不联网、不要账号。

---

## 功能

### 总览
顶部一句随机开场白，下面三张卡片分别汇总喝水进度、昨晚是否按时睡、今天是不是训练日。
**整张卡片点击直达对应模块**。

### 大水理论
- 每日目标（默认 2000ml）、提醒时间段（默认 08:00–22:00）、提醒间隔（默认 60 分钟）都可调
- 一键 `+200ml` / `+500ml` / 自定义，支持撤销上次、清空今日
- 进度是**圆环**，中间显示百分比和已喝毫升
- **达成目标后当天不再提醒**，闹钟直接排到第二天

### 大睡理论
- 目标就寝时间（默认 23:00）+ 提前提醒分钟数（默认 30 分钟）
- 可选「睡前再追加一次」，追加提醒落在目标就寝时刻
- 次日手动记一笔「昨晚按时睡了 / 没忍住」，记错了可以改
- 进度是**七个小方块**：绿=按时，半透明白=没忍住，空心=还没记录

### 薄肌理论
**纯激励页。** 不做任何训练记录、打卡、统计——练什么、练多重，交给你已经在用的健身软件。

- 30 条以上大字激励语，每次打开随机展示，**点卡片任意处换一句**
- 保留提醒：可选每周训练日（默认周一/三/五）+ 提醒时间（默认 19:00）
- 通知文案从激励语库随机抽取，**通知上没有「完成 / 跳过」按钮**，点了直接进这一页

### 通用
- 底部四个 Tab，每个模块的提醒可单独开关
- 设置页：昵称、深色模式（跟随系统 / 浅色 / 深色）、数据清空
- 内置国产 ROM 后台保活引导（通知权限 / 精确闹钟 / 电池优化白名单 / 自启动，按品牌列了路径）

---

## 界面结构

总览页和三个模块页用的是**同一个卡片组件**，所以样式天然统一：

```
┌────────────────────────────────┐
│ 模块名                    角标   │
│                                │
│        进 度（主体，最大）       │   大水=圆环 / 大睡=七方块 / 薄肌=无
│                                │
│ 激励语（点一下换一句）           │
│ 一行状态                        │
└────────────────────────────────┘
        ↕ 同一条滚动轴，一起上滑
  快捷记录 / 昨晚记录 / 训练日
        ↕
  提醒设置
```

- 卡片是模块色渐变 + 白字（品牌卡），右侧有纯代码绘制的水印剪影：
  大水=透明玻璃杯、大睡=枕头、薄肌=哑铃
- 卡片以外的部分跟随系统主题，**浅色 / 深色都支持**
- 配色：深色基调 + 橙红强调 `#FF7A33`；大水 `#38B6FF`、大睡 `#B07CFF`、薄肌 `#FF5A36`

---

## 技术栈

| | |
| --- | --- |
| 语言 | Kotlin 2.2.10 |
| UI | Jetpack Compose + Material3（100% 声明式，图标和插画全是矢量 / Canvas 代码绘制） |
| 架构 | 单 Activity + Compose 导航，无额外框架 |
| 存储 | DataStore Preferences |
| 提醒 | AlarmManager 精确闹钟 + BroadcastReceiver |
| 构建 | Gradle 9.6.0 + AGP 9.4.1，版本目录 `gradle/libs.versions.toml` |

没有网络请求、没有依赖注入框架、没有第三方 SDK。

---

## 快速开始

### 环境要求

- JDK 17 以上
- Android SDK（项目 `targetSdk 37`，编译需要 `android-37` 平台）
- Android 8.0（API 26）及以上的真机或模拟器

### 构建

```bash
export JAVA_HOME="/path/to/your/jdk"      # 或 Android Studio 自带的 jbr
export ANDROID_HOME="$HOME/Android/Sdk"

./gradlew assembleDebug      # 调试包
./gradlew assembleRelease    # 发布包
```

产物：

- `app/build/outputs/apk/debug/app-debug.apk`
- `app/build/outputs/apk/release/app-release.apk`

> 当前 release 包用的是 **debug keystore** 签名，方便直接装真机。上架前请在
> `app/build.gradle.kts` 里换成自己的签名配置。
>
> 项目路径含中文时，AGP 默认会拒绝构建。仓库里已通过 `gradle.properties` 的
> `android.overridePathCheck=true` 放行；如果你 clone 到纯英文路径，可以删掉这一行。

也可以直接用 Android Studio：`File → Open` 选中目录，等 Gradle Sync 完成后
`Build → Build Bundle(s)/APK(s) → Build APK(s)`。

### 安装

```bash
adb install -r app/build/outputs/apk/release/app-release.apk
```

---

## 目录结构

```
.
├── app/src/main/
│   ├── java/com/example/boji/
│   │   ├── MainActivity.kt            入口 + 通知权限 + 通知跳转
│   │   ├── BojiApplication.kt         建通知渠道 + 启动时兜底重排提醒
│   │   ├── data/BojiRepository.kt     DataStore（设置 + 记录）
│   │   ├── reminder/
│   │   │   ├── AlarmScheduler.kt      精确闹钟排期
│   │   │   ├── ReminderReceiver.kt    闹钟触发 → 发通知 → 排下一次
│   │   │   ├── BootReceiver.kt        开机 / 升级 / 时间变更 重注册
│   │   │   └── Notifier.kt            通知渠道 + 三条通知
│   │   ├── ui/
│   │   │   ├── AppController.kt       UI ↔ 数据层
│   │   │   ├── BojiRoot.kt            启动页 + 底部导航 + 路由
│   │   │   ├── components/ModuleCard.kt  ★ 总览与三个子页共用的卡片
│   │   │   ├── screen/                5 个页面
│   │   │   └── theme/                 配色 / 字号 / 主题
│   │   └── util/                      Copy(文案) / TimeUtil / Permissions
│   └── res/values/copy_library.xml    ★ 全部文案
├── gradle/libs.versions.toml
└── README.md
```

改样式看 `ui/components/ModuleCard.kt`（一处改动同时作用于总览和三个子页）；
改文案只看 `res/values/copy_library.xml`。

---

## 权限

| 权限 | 用途 | 必须? |
| --- | --- | --- |
| `POST_NOTIFICATIONS` | Android 13+ 弹提醒通知 | 必须，不给收不到任何提醒 |
| `SCHEDULE_EXACT_ALARM` / `USE_EXACT_ALARM` | 精确闹钟，保证准点 | 建议；未授权会退化成「不早于设定时间」，可能晚几分钟 |
| `RECEIVE_BOOT_COMPLETED` | 开机后重新注册提醒 | 必须（配合自启动权限） |
| `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` | 引导加入电池优化白名单 | 国产 ROM 上强烈建议 |
| `VIBRATE` | 通知震动 | 可选 |

不申请位置、通讯录、存储权限，也没有任何联网行为。

---

## 自定义文案

全部文案集中在 **`app/src/main/res/values/copy_library.xml`**，增删 `<item>` 即可，不需要动 Kotlin 代码。

可用占位符（自动替换）：

| 占位符 | 含义 |
| --- | --- |
| `{nick}` | 昵称（默认「预备役」） |
| `{done}` / `{goal}` / `{ml}` | 已喝 / 目标 / 剩余毫升 |
| `{time}` | 目标就寝时间 |
| `{min}` | 距目标就寝还剩多少分钟 |
| `{week}` | 今天是周几 |

| 数组 | 用途 | 条数 |
| --- | --- | --- |
| `water_reminders` | 喝水提醒通知 | 20 |
| `water_quotes` | 大水理论页激励语 | 18 |
| `water_done` / `water_empty` | 达成 / 空状态 | 10 / 4 |
| `sleep_reminders` | 早睡提醒通知 | 19 |
| `sleep_quotes` | 大睡理论页激励语 | 18 |
| `sleep_followup` | 睡前追加提醒 | 6 |
| `sleep_done` / `sleep_miss` / `sleep_empty` | 记录反馈 | 4 / 4 / 2 |
| `muscle_quotes` | 薄肌理论页激励语（通知也从这里抽） | 38 |
| `muscle_reminders` | 训练日提醒通知 | 5 |
| `overview_openers` | 总览页开场白 | 14 |

---

## 已知限制

- **存储用 DataStore 而非 Room**：数据量很小（日饮水 + 睡眠记录），少一层 KSP 更稳。
- **提醒依赖系统闹钟**：国产 ROM 会杀后台，必须按设置页里的引导开权限，否则夜里提醒可能迟到甚至不来。
  兜底机制：即使收不到开机广播，App 每次启动都会重新注册一次全部提醒。
- **「昨晚」需次日手动记一笔**：手机上无法可靠判断真实入睡时间，不做自动判定。
- **只保留最近 60 天记录**，超出自动裁剪。
- **目标饮水量步进 100ml，提醒间隔步进 15 分钟，时间选择器分钟步进 5**。
- **APK 约 13MB**：未开代码压缩 / 资源缩减，且用了 `material-icons-extended`。对体积敏感可启用 R8 并自绘图标。
- **没有单元测试 / UI 测试**，也没有跑过真机自动验证。
- 追加提醒的时刻 = 目标就寝时刻；提前量设为 0 时不会重复发。

---

## 提醒自检

装好并打开一次 App 后：

```bash
# 应能看到 com.example.boji 的几条闹钟
adb shell dumpsys alarm | grep -A 3 com.example.boji

# 重启后是否还在（国产机需先给自启动权限）
adb reboot
adb shell dumpsys alarm | grep -A 3 com.example.boji
```

想立刻验证提醒是否触发：把喝水间隔设成 15 分钟，或把早睡提醒设到「当前时间 +2 分钟」等通知弹出。

---

## 许可

尚未指定开源协议。需要的话可以加一个 MIT LICENSE（说一声即可）。
