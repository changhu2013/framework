
=================================================================

                  打豆豆    mobid.dadoudou.framework
  
=================================================================  

         该框架是基于OSGI实现的一套动态模块化的服务管理框架，用于简化后台应用服务

的开发和服务发布。

         该框架提供一个maven插件来完成OSGI Bundle的安装
         
         该插件的地址在：https://github.com/changhu2013/maven-osgi-plugin

=================================================================


1. 如何使用该框架
   
   1.1 下载该架构架构源码。注意该项目为Maven Project，在eclipse里需要
   
 安装Maven插件。
 
   1.2 运行Maven命令 jetty:run 。 该项目已经配置了jetty插件，可直接使用。
   
 注意：该项目还配置了maven-osgi-plugin插件，该插件在test阶段运行deployBundle
 
 命令以安装配置指定的OSGI Bundles 。
 
   1.3 服务器起来之后在控制台可运行ss命令查看已经安装好的OSGI Bundles 。 



=================================================================




=================================================================




=================================================================




=================================================================