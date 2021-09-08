
# lanproxy
基于lan-proxy开源代理工具，改造让其首先读取lanproxy.home，然后才读取user.home  
已调试完成，打包后运行Dockerfile第一行，然后运行proxy.yml即可
打包成容器的逻辑为：拷贝server中的内容，放到指定目录，设置正确的权限
k8s部署逻辑为：通过挂载本地目录覆盖config内容，并通过节点上的proxydata标签选择有数据的节点（你也可以通过其他方式进行数据挂载）
端口对外暴露方式为：把不同端口映射到节点端口上，由于我推荐使用ssl，所以我只暴露了ssl对应的端口。
而公网实际端口也是通过映射方式暴露的，所以管理端填的虽然是80，但是实际对外的端口不是。
进行验证的限制是为了更安全

## 说明
tips: 
- 任何过公网的东西建议使用https增加信息安全性
- 内网穿透建议自己使用，不建议公用   
- 为了服务安全，建议限制端口范围，规范管理

## 改动项：
- 代码：不只是读``user.home``，会更优先地读取``lanproxy.home``作为工作目录（[代码地址](https://github.com/zafir-zhong/lanproxy/proxy-server/src/main/java/org/fengfei/lanproxy/server/config/ProxyConfig.java)）
- 优化：加入了docker和k8s对应的模板，[模板地址](https://github.com/zafir-zhong/public/tree/master/tool/lanproxy/proxy-server/k8s)

# 部署步骤
## 服务端

- 直接部署

```shell script
mvn clean package
cd distribution/proxy-server-0.1/bin
bash  startup.sh
```

说明：
1. 服务端配置文件位于``distribution/proxy-server-0.1/conf``
2. 请检查服务端配置与客户端一致，例如设置为开启ssl，则需要连接服务端的ssl端口

---

- 打包成镜像
```shell script
mvn clean package
cd distribution
docker build -t tool/lan-proxy:1.0.00-release .
```

说明：
1. 服务端配置文件位于``distribution/proxy-server-0.1/conf``
2. 请检查服务端配置与客户端一致，例如设置为开启ssl，则需要连接服务端的ssl端口
3. 建议正确修改配置后再运行镜像构建命令
4. 请注意宿主机端口与容器端口的区别，确认端口配置正确

---

- k8s部署
```shell script
# 请注意，容器化部署需要修改对外暴露的端口，例如我把4993映射成30993，所以实际连接的是30993
bash build.sh
```

说明：
1. 请核对``distribution``目录下的yml文件，在``proxy-base.yml``中需要挂载主机目录
2. 请核对端口设置，在k8s是进行了端口映射的，所以访问宿主机端口有所区别：例如4993映射成了30993；80映射成30008；8090映射成30809
3. 服务端配置文件位于``distribution/proxy-server-0.1/conf``，在生成``configmap``或挂载前请进行核对
4. 请检查服务端配置与客户端一致，例如设置为开启ssl，则需要连接服务端的ssl端口
5. 若不打算用filebeat整理日志，只需根据注释修改``distribution/buildServer.sh``



