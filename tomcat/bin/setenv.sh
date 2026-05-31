# ============================================================
# 港大医院投诉管理系统 - JVM启动参数配置
# 文件: setenv.sh (Linux) 或 setenv.bat (Windows)
# 位置: $CATALINA_HOME/bin/
# ============================================================

# ==================== JVM内存配置 ====================
# 堆内存 - 根据服务器物理内存调整
JAVA_OPTS="-Xms2g -Xmx4g"

# ==================== GC配置 ====================
# G1垃圾收集器 (JDK 11+ 推荐)
JAVA_OPTS="$JAVA_OPTS -XX:+UseG1GC"
JAVA_OPTS="$JAVA_OPTS -XX:MaxGCPauseMillis=200"
JAVA_OPTS="$JAVA_OPTS -XX:InitiatingHeapOccupancyPercent=45"

# ==================== 元空间配置 ====================
JAVA_OPTS="$JAVA_OPTS -XX:MetaspaceSize=256m"
JAVA_OPTS="$JAVA_OPTS -XX:MaxMetaspaceSize=512m"

# ==================== 线程栈配置 ====================
JAVA_OPTS="$JAVA_OPTS -Xss512k"

# ==================== OOM处理 ====================
JAVA_OPTS="$JAVA_OPTS -XX:+HeapDumpOnOutOfMemoryError"
JAVA_OPTS="$JAVA_OPTS -XX:HeapDumpPath=/var/log/complaint/heapdump.hprof"

# ==================== GC日志配置 ====================
JAVA_OPTS="$JAVA_OPTS -Xlog:gc*:file=/var/log/complaint/gc.log:time,uptime,level,tags"
JAVA_OPTS="$JAVA_OPTS -XX:+UseGCLogFileRotation"
JAVA_OPTS="$JAVA_OPTS -XX:GCLogFileSize=50M"
JAVA_OPTS="$JAVA_OPTS -XX:NumberOfGCLogFiles=10"

# ==================== 性能优化 ====================
JAVA_OPTS="$JAVA_OPTS -XX:+UseStringDeduplication"
JAVA_OPTS="$JAVA_OPTS -XX:+ParallelRefProcEnabled"
JAVA_OPTS="$JAVA_OPTS -XX:MaxTenuringThreshold=6"
JAVA_OPTS="$JAVA_OPTS -XX:SurvivorRatio=8"

# ==================== 类加载优化 ====================
JAVA_OPTS="$JAVA_OPTS -XX:+ExplicitGCInvokesConcurrent"
JAVA_OPTS="$JAVA_OPTS -XX:-OmitStackTraceInFastThrow"

# ==================== 网络优化 ====================
JAVA_OPTS="$JAVA_OPTS -Djava.net.preferIPv4Stack=true"
JAVA_OPTS="$JAVA_OPTS -Dsun.net.inetaddr.ttl=60"

# ==================== 连接优化 ====================
JAVA_OPTS="$JAVA_OPTS -Dtomcat.maxThreads=200"
JAVA_OPTS="$JAVA_OPTS -Dtomcat.minSpareThreads=20"
JAVA_OPTS="$JAVA_OPTS -Dtomcat.acceptCount=100"
JAVA_OPTS="$JAVA_OPTS -Dtomcat.maxConnections=1000"

# ==================== 安全配置 ====================
JAVA_OPTS="$JAVA_OPTS -Djava.security.egd=file:/dev/./urandom"
JAVA_OPTS="$JAVA_OPTS -Djava.awt.headless=true"

# ==================== 应用配置 ====================
JAVA_OPTS="$JAVA_OPTS -Dspring.profiles.active=@spring.profiles.active@"
JAVA_OPTS="$JAVA_OPTS -Dfile.encoding=UTF-8"
JAVA_OPTS="$JAVA_OPTS -Duser.timezone=GMT+8"

# ==================== 调试配置 (生产环境建议关闭) ====================
# JAVA_OPTS="$JAVA_OPTS -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"

# ==================== 导出JVM参数 ====================
export JAVA_OPTS
export CATALINA_OPTS="$JAVA_OPTS"

# ==================== Tomcat内存配置 ====================
CATALINA_OPTS="$CATALINA_OPTS -Xms2g -Xmx4g"
CATALINA_OPTS="$CATALINA_OPTS -XX:NewRatio=2"
