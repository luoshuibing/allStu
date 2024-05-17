# 不屈日记

# Docker

Docker是一个快速交付应用、运行应用的技术
* 可以将程序及其依赖、运行环境一起打包为一个镜像，可以迁移到任意Linux操作系统
* 运行时利用沙箱机制形成隔离容器，各个应用互不干扰
* 启动、移除都可以通过一行命令完成

Docker和虚拟机的差异
* docker是一个系统进程；虚拟机是在操作系统中的操作系统
* docker体积小，启动快，性能好，虚拟机体积大，启动速度慢性能一般

镜像和容器
* Docker将应用程序及其所需的依赖、函数库、环境、配置等文件打包在一起，成为镜像。
* 镜像中的应用程序运行后形成的进程就是容器，只是docker会给容器做隔离，对外不可见。

DockerHub
* DockerHub是一个Docker镜像的托管平台。这样的平台成为Docker registgry

Docker是一个CS架构的程序
* 服务端：Docker守护进程，负责处理Docker指令，管理镜像、容器等
* 客户端：通过命令或者RestApi向Docker服务端发送指令。可以在本地或远程向服务端发送指令

镜像相关命令
* 镜像名称一般分两部分组成：[repository]:[tag]
* 在没有指定tag时，默认是latest，代表最新版本的镜像

```
docker build
docker images
docker rmi
docker pull
docker push
docker save 保存为一个包
docker load 加载压缩包

docker --help

```

[dockerhub官网](https://hub.docker.com/)

容器相关命令
```
docker run
docker pause
docker unpause
docker stop
docker start
docker ps
docker logs
docker exec 进入容器
docker rm 删除容器
```

docker run命令
* --name:容器名称
* -p:端口映射
* -d:容器后台运行

查看容器日志
* docker logs
* 添加-f参数可以持续查看日志

查看容器状态
* docker ps

查看容器状态
* docker ps
* 添加-a参数查看所有状态的容器

删除容器：
* docker run
* 不能删除运行中的容器

进入容器：
* 命令式docker exec -it
* exec命令可以进入容器修改文件

数据卷：

数据卷是一个虚拟目录，指向宿主机文件系统中的某个目录

docker volume [command]
* create 创建一个volume
* inspect 显示一个或多个volume的信息
* ls 列出所有的volume
* prune 删除未使用的volume
* rm 删除一个或多个指定的volume

`docker run --name mn -p 80:80 -v html:/usr/share/nginx/html -d nginx`


DOCKER file

```
FROM centos:6
ENV key value
copy ./mysql-5.7.rpm /tmp
run yum install gcc
expose 8080
entrypoint java -jar xx.jar
```


dockercompose分布式应用部署 
* 基于comopse实现集群服务快速部署


docker tag命令
* 推送本地镜像到仓库前必须重命名镜像，以镜像仓库地址为前缀
* 镜像仓库推送前需要把仓库地址配置到docker服务的daemon.json文件中，被docker新人
* 推送使用docker push命令
* 拉取使用docker pull命令


开启启动设置
docker update --restart=always tracker
docker update --restart=always storage
































































