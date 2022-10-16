
Flink的window解压后没有bat文件了，导致window下无法运行集群
    可以从github flink项目的旧版本中复制flink.bat和start-cluster.bat文件，从而启动集群
    地址：https://github.com/apache/flink/tree/release-1.9/flink-dist/src/main/flink-bin/bin

直接通过start-cluster.bat启动会拉起JobManager和TaskManager，
如果下列参数不配置的话，TaskManager进程会退出，日志显示相关的参数没有配置，
在conf/flink-conf.yaml中添加下面的配置，重新启动即可：
    taskmanager.cpu.cores: 2
    taskmanager.memory.task.heap.size: 512m
    taskmanager.memory.managed.size: 512m
    taskmanager.memory.network.min: 64mb
    taskmanager.memory.network.max: 64mb

启动后会报日志错误，可以忽略，不影响使用，通过http://localhost:8081/#/overview页面可以参考flink集群信息


启动命令：
    .\bin\start-cluster.bat
    可以通过jps查看，有TaskManagerRunner（TaskManager进程） 和 StandaloneSessionClusterEntrypoint（JobManager进程）说明启动成功


如果是提交作业到集群，通过print输出（System.out），window下会输出到命令行窗口上（在网页页面看不到）

学习流程：sample -> sink -> transform -> sink -> window -> state -> checkpoint -> table -> ceb

