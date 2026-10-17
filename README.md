# 薄肌预备役

一个帮你养成「多喝水 / 早睡 / 去健身」三个习惯的 Android App。
名字取自网络「xx 预备役」的自嘲梗——还在努力，还没到位，但一直在路上。

语气是轻松自嘲的，像朋友之间互相督促，不说教、不制造焦虑。

- 包名：`com.example.boji`
- 最低支持：Android 8.0（API 26）
- 目标 SDK：37（本机 SDK 只装了 android-37，详见「已知限制」）
- 版本：1.0

---

## 一、功能说明

### 通用
- 底部四个 Tab：**总览 / 大水理论 / 大睡理论 / 薄肌理论**
- 总览页顶部随机一句「预备役日报」式开场白，汇总喝水进度 + 昨晚是否按时睡 + 今天是不是健身日（只显示是否为健身日，不显示任何完成状态）
- 每个模块可**单独开关**提醒
- 设置页：昵称、深色模式（跟随系统/浅色/深色）、数据清空
- 启动页：App 名 + 一句随机激励语，900ms

### 统一的视觉语言：总览与三个子页是同一张卡
`ui/components/ModuleCard.kt` 里的 `ModuleCard` 是**总览页和三个子页共用的同一个组件**，
所以「总览里的小卡片」和「子页顶部的大卡片」天然长一样，不存在两套样式：

```
┌────────────────────────────────┐
│ 模块名（字距 4sp）        角标   │
│                                │
│        进 度（主体，最大）       │  大水=圆环 / 大睡=七个小方块 / 薄肌=无
│                                │
│ 激励语（点击整张卡换一句）        │
│ 一行状态                        │
└────────────────────────────────┘
        ↓ 同一条滚动轴，一起上滑
  快捷记录 / 昨晚记录 / 训练日
        ↓
  提醒设置（跟随浅色 / 深色主题）
```

- **进度形态**：大水 = 圆环（Canvas 绘制）、大睡 = 7 个小方块（绿=按时 / 半透明白=没忍住 / 空心=未记录）、
  薄肌 = 没有进度（总览里传空槽占位，保证三张卡高度对齐）。总览和子页用的是同一套，不再有横条。
- **主次关系**：子页里进度占绝大部分（圆环 152dp / 方块 48dp + 40sp 数字），激励语小一号（20sp）；
  薄肌没有进度，激励语本身就是主体，用 30sp。
- **卡片背景剪影**（纯 Canvas 代码绘制，无图片依赖，画在卡片右侧做水印）：
  大水 = 蓝色透明玻璃杯 / 大睡 = 枕头 / 薄肌 = 哑铃。
- **子页是一体的**：整页只有一个滚动容器，顶部卡片会跟着一起上滑，不再有「永远置顶的激励语」。
- **支持浅色 / 深色**：卡片本身是模块色渐变 + 白字（品牌卡，深浅模式一致）；
  卡片以外的快捷操作、提醒设置、提示文案全部走 `MaterialTheme.colorScheme`，跟随系统主题。
  模块色的文字色用 `waterInk()` / `sleepInk()` / `muscleInk()` / `successInk()` 自动取深浅两版，
  避免浅色底上出现「浅蓝字看不清」的情况。
- 总览三张卡**整张可点击**直达子模块，不写「去 X 理论」这类提示文案。
- 闪屏页跟随主题底色，浅色模式下是浅底深字。

### 大水理论
- 可设置每日目标（默认 2000ml，步进 100ml，范围 200–10000）
- 可设置提醒时间段（默认 08:00–22:00）与提醒间隔（默认 60 分钟，步进 15，范围 15–480）
- 一键记录：`+200ml` / `+500ml` / 自定义；支持「撤销上次」「清空今日」
- 进度展示：**圆环** 152dp（中间是百分比 + 已喝毫升，下方一行「目标 X ml · 还差 Y ml」）
- 激励语 18 条，点卡片任意处切换
- **达成目标后当天不再提醒**（闹钟直接排到第二天开始时间）

### 大睡理论
- 可设置目标就寝时间（默认 23:00）和提前提醒分钟数（默认 30 分钟，步进 5）
- 到点提醒；可选「睡前再追加一次」，追加提醒落在**目标就寝时刻**
- 次日可记录「昨晚是否按时睡」（两个按钮：按时睡了 / 没忍住），记错了可以改
- 进度展示：**7 个小方块** 48dp（绿=按时，半透明白=没忍住，空心=未记录），上方是 40sp 的「X / 7 天达标」
- 激励语 18 条，点卡片任意处切换

### 薄肌理论（纯激励页）
- **不做任何训练记录、打卡、统计**，没有相关数据表和输入界面
- 大字激励语（38 条，30sp）每次打开随机展示，**点卡片任意处切换**，切换时淡入 + 轻微缩放
- 保留提醒：可选每周健身日（默认周一/三/五）+ 提醒时间（默认 19:00），通知文案从激励语库随机抽取，**通知上没有「完成/跳过」按钮**，点击直接打开本页
- 因为薄肌没有进度，激励语就是它卡片的主体（30sp），卡片最小高度 220dp

---

## 二、目录结构

```
薄肌预备役/
├── app/
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/example/boji/
│       │   ├── MainActivity.kt            入口 + 通知权限申请 + Tab 跳转
│       │   ├── BojiApplication.kt         建通知渠道 + 启动时兜底重排提醒
│       │   ├── data/BojiRepository.kt     DataStore 存储（设置 + 记录）
│       │   ├── reminder/
│       │   │   ├── AlarmScheduler.kt      精确闹钟排期算法
│       │   │   ├── ReminderReceiver.kt    闹钟触发 -> 发通知 -> 排下一次
│       │   │   ├── BootReceiver.kt        开机/升级/时间变更 重注册
│       │   │   └── Notifier.kt            通知渠道 + 三条通知
│       │   ├── ui/
│       │   │   ├── AppController.kt       UI ↔ 数据层，改设置后自动重排
│       │   │   ├── BojiRoot.kt            启动页 + 底部导航 + 页面路由
│       │   │   ├── components/            模块卡/进度环/周方块/卡片/选择器/对话框
│       │   │   ├── screen/                5 个页面
│       │   │   └── theme/                 配色 / 字号 / 主题
│       │   └── util/                      Copy(文案) / TimeUtil / Permissions
│       └── res/
│           ├── values/copy_library.xml    ★ 全部文案库
│           ├── drawable/                  矢量图标
│           └── mipmap-anydpi-v26/         自适应图标
├── gradle/libs.versions.toml              版本目录
├── settings.gradle.kts / build.gradle.kts
└── README.md
```

技术栈：Kotlin 2.2.10 + AGP 9.4.1 + Gradle 9.6.0 + Jetpack Compose（BOM 2026.02.01）+ Material3 + DataStore。

---

## 三、如何构建 APK

### 方式 A：命令行（已验证可用）

```bash
# Windows Git Bash / PowerShell 通用
export JAVA_HOME="D:/Android Studio/jbr"          # 或你自己安装的 JDK 17+
export ANDROID_HOME="C:/Users/pc/AppData/Local/Android/Sdk"

./gradlew assembleDebug      # 调试包
./gradlew assembleRelease    # 发布包（已用 debug keystore 签名，可直接装真机）
```

产物：
- `app/build/outputs/apk/debug/app-debug.apk`
- `app/build/outputs/apk/release/app-release.apk`

> 注意：项目路径含中文，AGP 默认会拒绝构建。已在 `gradle.properties` 里加了
> `android.overridePathCheck=true` 放行。若换到纯英文路径，可删掉这一行。

### 方式 B：Android Studio
1. File → Open → 选择 `薄肌预备役` 目录
2. 等待 Gradle Sync 完成（首次会下载依赖）
3. Build → Build Bundle(s)/APK(s) → Build APK(s)

### 安装到真机
```bash
adb install -r app/build/outputs/apk/release/app-release.apk
```

---

## 四、权限说明

| 权限 | 用途 | 是否必须 |
| --- | --- | --- |
| `POST_NOTIFICATIONS` | Android 13+ 弹提醒通知 | 必须（不给就收不到任何提醒） |
| `SCHEDULE_EXACT_ALARM` / `USE_EXACT_ALARM` | 精确闹钟，保证准点提醒 | 建议开启；未授权时会退化为「不早于设定时间」，可能晚几分钟 |
| `RECEIVE_BOOT_COMPLETED` | 开机后重新注册提醒 | 必须（配合自启动权限） |
| `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` | 引导加入电池优化白名单 | 国产 ROM 上强烈建议 |
| `VIBRATE` | 通知震动 | 可选 |

App 不联网、不要账号、不申请位置/通讯录/存储权限。所有数据只写在本机 DataStore 里，卸载即清空。

**国产手机必看**：设置页 → 「查看后台保活引导」，里面有 4 步（通知权限 / 精确闹钟 / 电池优化白名单 / 自启动），
自启动部分按品牌列出了具体路径（小米、华为、OPPO、vivo、三星）。不做这一步，夜里提醒很可能收不到。

> 兜底机制：即使收不到开机广播，App 每次启动（以及从设置页返回时）都会重新注册一次全部提醒。

---

## 五、改文案

全部文案集中在 **`app/src/main/res/values/copy_library.xml`**，改完重新编译即可，不需要动 Kotlin 代码。

可用的占位符（代码会自动替换）：

| 占位符 | 含义 |
| --- | --- |
| `{nick}` | 用户昵称（默认「预备役」） |
| `{done}` / `{goal}` / `{ml}` | 已喝 / 目标 / 剩余毫升 |
| `{time}` | 目标就寝时间，如 23:00 |
| `{min}` | 距目标就寝还剩多少分钟 |
| `{week}` | 今天是周几 |

当前条目数：

| 数组 | 用途 | 条数 |
| --- | --- | --- |
| `water_reminders` | 喝水提醒通知 | 20 |
| `water_quotes` | 大水理论页大字激励语 | 18 |
| `water_done` / `water_empty` | 达成 / 空状态 | 10 / 4 |
| `sleep_reminders` | 早睡提醒通知 | 19 |
| `sleep_quotes` | 大睡理论页大字激励语 | 18 |
| `sleep_followup` | 睡前追加提醒 | 6 |
| `sleep_done` / `sleep_miss` / `sleep_empty` | 记录反馈 | 4 / 4 / 2 |
| `muscle_quotes` | 薄肌理论页大字激励语（通知也从这里抽） | 38 |
| `muscle_reminders` | 健身日提醒通知 | 5 |
| `overview_openers` | 总览页日报开场白 | 14 |

直接增删 `<item>` 即可，数量不限。

---

## 六、已知限制 & 我替你拍板的默认方案

需求里没写清楚的地方，我按「最简单合理」处理了，列在这里方便你改：

1. **存储用 DataStore 而不是 Room**：数据量很小（日饮水 + 睡眠记录），DataStore 少一层 KSP，构建更稳。若之后要做复杂统计再换 Room。
2. **targetSdk 用 37**：本机 SDK 只装了 android-37，没有 35/36。有需要可在 `app/build.gradle.kts` 里改，同时装对应 platform。
3. **只保留最近 60 天记录**：超出自动裁剪，避免无限增长。
4. **「昨晚」= 前一天日期**，需你在次日手动点一下；不做自动判定（手机上无法可靠判断真实入睡时间）。
5. **进度形态按模块区分**：大水用圆环、大睡用 7 个方块，都是 Canvas 纯代码绘制；薄肌不显示进度但占同样高度。
6. **追加提醒的时刻 = 目标就寝时刻**（首次提醒 = 目标 − 提前量）。提前量设为 0 时不会重复发。
7. **目标饮水量步进 100ml，提醒间隔步进 15 分钟，时间选择器分钟步进 5**。
8. **默认训练日 = 周一/三/五，默认提醒 19:00**。
9. **深色模式给了三档**（跟随系统/浅色/深色），比需求多一档，方便你白天截图。
10. **模块卡本身恒为模块色渐变 + 白字**（品牌卡，深浅模式一致）；卡片以外的部分全部跟随系统主题，
    所以浅色模式下「下面的设置」是浅底深字，不再是固定的黑色。
11. **Release 包用 debug keystore 签名**：为了让你能直接 `adb install` 到真机。上架前请在 `app/build.gradle.kts` 里换成自己的签名配置。
12. **APK 约 13MB**：没有开代码压缩/资源缩减（AGP 9 的 `optimization` 关掉了，避免破坏 Compose），另外用了 `material-icons-extended`（Bedtime / WaterDrop / FitnessCenter 这几个图标只在扩展包里）。对体积敏感可以启用 R8 + 换成 icons-core 并自绘图标。
13. **没有做单元测试/UI 测试**，也没有跑真机：当前环境没有连接设备，也没有可用的 AVD。功能验证方式见下一节。

---

## 七、自检验收清单（请在真机上跑一遍）

### 提醒能触发
```bash
# 装好 App 并打开一次后，确认闹钟已注册（应能看到 com.example.boji 的 4 条）
adb shell dumpsys alarm | grep -A 3 com.example.boji
```
- 想立刻验证：把喝水间隔设成 15 分钟，或把早睡提醒设到「当前时间 +2 分钟」，等通知弹出。
- 手动触发（等价于闹钟到点）：
  ```bash
  adb shell am broadcast -n com.example.boji/.reminder.ReminderReceiver \
      -a android.intent.action.BOOT_COMPLETED   # 这条走 BootReceiver，会重排
  ```
  更直接的方式是把提醒时间调到下一分钟，等系统自己触发。

### 重启后仍有效
```bash
adb reboot
# 开机后重新插线，再确认闹钟还在
adb shell dumpsys alarm | grep -A 3 com.example.boji
```
国产机需要先给「自启动」权限（设置页引导里有路径）。

### 数据能保存
- 点几次 `+200ml`，杀掉进程重进，进度还在
- 记录昨晚睡眠，切到其他 Tab 再切回来，7 天条还在
- 设置页改昵称/深色模式，重启 App 后保持

### 其它
- 关掉某个模块的开关 → 该模块闹钟应被取消（`dumpsys alarm` 里少一条）
- 喝水达标后 → 当天不再弹喝水提醒（闹钟被排到次日开始时间）
- 通知点击 → 直接跳到对应 Tab
