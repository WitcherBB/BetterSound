# 自检用假游戏进程

`BootstrapLauncher.java` 只是为了让 `MemMonitor` 的自检可复现：它与真实开发客户端使用相同的主类
（`cpw.mods.bootstraplauncher.BootstrapLauncher`）和相同的区分参数（`forgeclientuserdev`），
但只做一件事——持续分配 256KB 数组并保留一部分，从而产生稳定的分配与 GC。

- 它**不参与**游戏构建：`build.gradle` 的 sourceSets 只有 `src/main/java` 等目录，`tools/` 不在其中。
- 跑法见任务 **看板自检(--self)**，或手动：
  ```powershell
  $jdk='D:/home/java/zulu17.58.21-ca-jdk17.0.15-win_x64'
  & "$jdk/bin/javac.exe" -encoding UTF-8 -d tools/mem-monitor/bin tools/mem-monitor/src/MemMonitor.java tools/mem-monitor/test/BootstrapLauncher.java
  # 终端 A：假游戏进程
  & "$jdk/bin/java.exe" -Xmx384m -cp tools/mem-monitor/bin cpw.mods.bootstraplauncher.BootstrapLauncher
  # 终端 B：看板（--self 只看自己；改成正常模式即可自动识别上面这个进程）
  & "$jdk/bin/java.exe" -Xmx256m -cp tools/mem-monitor/bin MemMonitor --self --interval=1 --count=6
  ```
