# 数据库实验

存放数据库实验的相关文件

## 特殊说明

`setup`和`data`这两个目录是本地运行MySQL产生的文件夹，如果直接使用学校的服务器则不需要

本地运行MySQL使用官方的Docker镜像，使用`docker-compose.yml`文件进行组织，将`data`目录挂载为数据目录，密码被设置为`password`

下面是具体的操作流程：

1. 在setup目录中，使用`docker-compose up -d`来启动Docker服务

2. 使用`docker exec -it setup-mysql-1 /bin/sh`进入MySQL镜像内部

3. 在Docker镜像内部使用`mysql -u root -p`来进入数据库

进去了之后就可以正常进行数据库操作了😄