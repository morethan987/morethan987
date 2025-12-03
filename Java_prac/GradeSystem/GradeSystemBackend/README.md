# 学生成绩管理西戎后端

启动测试：

```shell
./mvnw spring-boot:run
```

使用结束记得关闭postgresql：

```shell
sudo systemctl stop postgresql.service
```

终端访问pg：

```shell
// 管理员
sudo -u postgres psql

// 本用户，直接连接到grade_sys数据库中
psql grade_sys
```
