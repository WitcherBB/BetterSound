# BetterSound 开发期 JVM 内存看板（VSCode）

在 VSCode 里调试 Minecraft 客户端时，实时查看堆占用、各内存池、GC 次数/停顿与分配趋势。
只用 JDK 自带能力（Attach API + 本地 JMX + jcmd/jmap），不引入任何第三方依赖，监控本身开销可以忽略。

> 为什么需要这个：VSCode 的 Java 调试器（vscode-java-debug）只通过 JDWP 提供断点、变量、调用栈，
> **没有内存/分配视图**。JVM 的内存数据必须从 JVM 侧取，所以这里用「外挂监控终端 + 独立启动配置」的方式解决。

## 一、快速开始（3 步）

1. 启动游戏：按 F5 选 **runClient+Mem**（和 runClient 完全一样，只多了内存诊断参数）。
   > 也可以用原来的 runClient，监控会自动找到它：它会按 `cpw.mods.bootstraplauncher.BootstrapLauncher` + `forgeclientuserdev` 自动识别游戏进程。
2. 打开命令面板（Ctrl+Shift+P）→ **Tasks: Run Task** → **实时内存看板**。
3. 终端里就会每 2 秒刷新一次；Ctrl+C 结束。

也可以在 `runClient+Mem` 启动前就先运行看板任务，它会一直等游戏出现并自动连接。

## 二、看板读法

```
堆  [█████████████░░░░░░░░░░░░░]  49.5%  190.0MB / 384.0MB   已提交 279.0MB   非堆 11.3MB   已提交 14.4MB
GC  次数 29（本轮 +1）   累计停顿 0.07s   平均/次 2.0ms   本轮最长 2.0ms   历史最长 3.0ms
分配 堆净增长 -766 KB/s（两次采样之间的堆增量；被 GC 回收的后台垃圾分配不会计入）   运行 0:00:55   线程 15   类 2085
堆外 direct 0.0MB
```

| 区域 | 含义 | 怎么看 |
| --- | --- | --- |
| **堆** | 已用 / 上限（`-Xmx1G`）、已提交量、非堆（元空间等） | 绿色 <65%，黄色 ≥65%，红色 ≥85%；长期贴着红区说明 `-Xmx` 偏小或有泄漏 |
| **GC** | 累计次数、累计/平均/最长停顿 | 平均停顿几十毫秒以上、次数持续快速上涨 = GC 压力大；`本轮 +N` 用来观察某个操作触发了几次 GC |
| **分配** | 两次采样之间的堆净增量 | 正数表示堆在涨（持续正增长最值得警惕）；负数是 GC 回收多于新增。JDK 17 的 JMX 不提供累计分配字节数（`TotalAllocatedBytes` 是 JDK 21+），所以这里用堆增量表示 |
| **堆外** | NIO 直接缓冲区用量 | 音频/文件 IO 相关，能看出堆外是否在涨 |
| **趋势图** | 每列 = 一次采样的堆占用率，纵轴 0~100% | 锯齿状且底部稳定 = 正常；整体逐格抬升且不回落 = 泄漏 |

内存池区块按占堆比例排序，显示每个池的已用、已提交与近 30 秒变化速率
（`G1 Eden` 增长快是正常的，`G1 Old Gen` 持续正增长要重点关注——通常是长生命周期对象/泄漏）。

## 三、任务与命令

| 任务（Ctrl+Shift+P → Tasks: Run Task） | 作用 |
| --- | --- |
| **实时内存看板** | 附加到游戏进程，每 2 秒刷新堆/内存池/GC/分配 |
| **编译内存监控工具** | 用 JDK17 重新编译 `tools/mem-monitor`（改过源码后运行） |
| **导出堆快照(.hprof)** | 对运行中的客户端执行 `jmap -dump:live`，产出 `run/heapdump-<时间>.hprof` |
| **类直方图(GC.class_histogram)** | 对运行中的客户端执行 `jcmd GC.class_histogram -all`，按实例数/字节数排序 |
| **看板自检(--self)** | 监控本进程 6 秒，用来确认工具本身工作正常（不依赖游戏） |

命令行形式（在 VSCode 集成终端里直接跑也行）：

```powershell
$jdk = 'D:/home/java/zulu17.58.21-ca-jdk17.0.15-win_x64'
# 每 1 秒刷新；只跑 30 轮后退出，便于脚本化
& "$jdk/bin/java.exe" -cp tools/mem-monitor/bin MemMonitor --interval=1 --count=30
# 指定进程号
& "$jdk/bin/java.exe" -cp tools/mem-monitor/bin MemMonitor --pid=12345
# 只取一次快照（脚本里用）
& "$jdk/bin/java.exe" -cp tools/mem-monitor/bin MemMonitor --once
```

参数：`--pid=N`（默认自动查找游戏进程）、`--interval=秒`（默认 2）、`--count=轮数`（默认一直刷新）、
`--wait=秒`（等待游戏出现的上限，默认 600）、`--port=端口`（本地 JMX 端口退路）、`--match=片段`（自动识别的命令行片段，默认 `--gameDir .`）、`--self`（自测模式）。

## 四、runClient+Mem 比 runClient 多了什么

`-Xmx1G` 与原来一致，新增的都是诊断参数：

| 参数 | 说明 |
| --- | --- |
| `-Dmemsound.tag=runClientMem` | 给进程打标记，让看板精确定位游戏进程 |
| `-Dcom.sun.management.jmxremote` + `port=9011` + `authenticate=false` + `ssl=false` | 本地回环的 JMX 端口，Attach 不可用时作为退路（仅监听本机，调试用） |
| `-XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=heapdump.hprof` | OOM 时自动把堆快照写到 `run/heapdump.hprof` |
| `-Xlog:gc*:file=gc-%p.log:time,uptime,level,tags:filecount=3,filesize=8M` | GC 详情写到 `run/` 下带 PID 的日志，滚动保留 3 个 × 8MB，事后可对照卡顿时间点 |

这些参数**不会**进正式构建产物（`build.gradle` 的 run 配置与 jar 都没动）。

## 五、抓不到问题时的下一步

- **看具体是哪些对象在涨**：运行任务 **导出堆快照(.hprof)**，用 VisualVM / Eclipse MAT / JProfiler 打开，
  看「对象直方图」与「支配树（GC Roots 引用链）」。这是定位泄漏最可靠的手段。
- **看某次操作前后的差异**：操作前跑一次 **类直方图**，操作后（必要时手动触发一次 `/gc` 或等自动 GC）再跑一次，比较实例数变化。
- **看卡顿**：对照 `run/gc-<pid>.log` 里的时间戳与游戏内卡顿时刻，确认是不是 GC 停顿导致。
- **想把堆调大**：改 `.vscode/launch.json` 里对应配置的 `-Xmx1G`（`build.gradle` 的 client 运行配置里也有同一个值，用 Gradle 启动时需要同步改）。

## 六、常见问题

**附加失败：拒绝访问 / AccessDeniedException**
Attach API 走的是 Windows 命名管道，某些受限环境（沙箱、以管理员身份运行的 VSCode、安全软件）会拦截。
看板会自动退到 `--port=9011`（只要游戏是 `runClient+Mem` 启动的）。若仍不行，请用普通 Windows 终端运行，
或把游戏和看板放到同一权限级别。

**端口 9011 被占用**
换一个端口：游戏侧改 `launch.json` 里的 `-Dcom.sun.management.jmxremote.port=9011`，
看板侧改成 `--port=新端口`。

**“分配”显示“采样中…”**
第一轮采样没有前值，等一个刷新周期即可。

**趋势图全是点或很稀疏**
说明采样点还不够（每列 = 一次采样），或者堆占用一直低于 3%（图的最下面一行）。

**为什么不用 VSCode 扩展做这件事**
Oracle 官方的 VisualVM 类 VSCode 扩展已停止维护，且 VSCode 的 Java 调试协议（JDWP）本身不传内存数据。
图形化分析建议直接用独立的 VisualVM / JDK Mission Control 附加到同一进程（JDK 21 才有 `jcmd <pid> JFR.start` 的分配采样；
本机游戏跑在 JDK 17 上，因此这里用 JMX + jcmd/jmap 组合）。

## 七、文件说明

```
tools/mem-monitor/
  src/MemMonitor.java          # 看板本体（单文件，JDK9+ 可编译）
  test/BootstrapLauncher.java  # 自检用的假游戏进程（同主类、持续分配内存，不参与游戏构建）
  bin/                         # 编译产物（被 .gitignore 忽略）
```

改过 `MemMonitor.java` 后运行任务 **编译内存监控工具**（或 **看板自检(--self)**，它会自动先编译）。
