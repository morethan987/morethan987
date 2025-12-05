# 学生成绩管理后端

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

## 设计思路

### 演化思路：

1. 初始时，前端请求直接发送到后端，最高层次的抽象：

```text
Frontend Query -> Backend System
```

2. 前端请求需要经过权限鉴定，于是后端独立分化出一个“安全基础设施”模块：

```text
Frontend Query -> Security -> Main Service Providing a Web Interface
```

3. 为了应对多变的前端请求条件（前端请求可能是HTTP，也可能是消息队列，定时任务），将前端请求的多样性消化掉：

```text
Frontend Query -> Security -> Controller -> Main Service
```

4. 主服务部分需要将业务逻辑与数据分离，这一层将主要业务逻辑的复杂性消化掉：

```text
Frontend Query -> Security -> Controller -> Service -> Data Acess
```

5. Service 中对数据的操作大致可以概括为：拉取一批数据，修改数据，写回数据库；因此又开一层用于数据批处理：

```text
Frontend Query -> Security -> Controller -> Service -> Repository -> DataBase
```

6. 为了弥合 Repository 和 DataBase 之间的信息差异(Repository 差一个可操作的单条数据对象，DataBase 缺一个数据表的定义)，我们定义单条数据的格式，当作他们之间的接口：

```text
Frontend Query -> Security -> Controller -> Service -> Repository -> Entity -> DataBase
```

7. 锦上添花，为了业务扩展的需要，我们按照语义将实体类分包：

```text
Frontend Query -> Security -> Controller -> Service -> Repository ->Domain -> Packages -> Entity -> DataBase
```

8. 貌似遗漏了什么？主逻辑与DB之间有实体类作为接口，那么前后端之间的接口呢？因此使用DTO：

```text
Frontend Query -> Security -> DTO -> Controller -> Service -> Repository ->Domain -> Packages -> Entity -> DataBase
```

当然，实体类有时候为了方便也会参与到这一步来

最终，调用依赖：

```text
Frontend Query -> Security -> DTO -> Controller -> Service -> Repository ->Domain -> Packages -> Entity -> DataBase
```


### 目录结构

对标上面的设计，有了下面这个目录结构

```text
.
├── GradeSystemBackendApplication.java // 主类
├── controller // 控制器类，处理web逻辑
│   ├── AuthController.java
│   └── StudentController.java
├── domain // 核心业务建模，内部按功能分包
│   ├── auth
│   │   ├── Permission.java
│   │   ├── Role.java
│   │   └── User.java
│   ├── course
│   │   └── Course.java
│   ├── grade
│   │   └── Grade.java
│   ├── info
│   │   ├── Gender.java
│   │   └── UserProfile.java
│   ├── student
│   │   ├── Student.java
│   │   └── StudentStatus.java
│   ├── teacher
│   │   ├── Teacher.java
│   │   ├── TeacherStatus.java
│   │   └── TeacherTitle.java
│   └── teachingclass
│       └── TeachingClass.java
├── dto // 传输类，用作前后端接口
│   ├── StudentDTO.java
│   └── UserDTO.java
├── repository // 数据仓库
│   ├── CourseRepository.java
│   ├── GradeRepository.java
│   ├── PermissionRepository.java
│   ├── RoleRepository.java
│   ├── StudentRepository.java
│   ├── TeacherRepository.java
│   ├── TeachingClassRepository.java
│   ├── UserProfileRepository.java
│   └── UserRepository.java
├── security // 安全基础设施
│   ├── CustomUserDetailsService.java
│   └── SecurityConfig.java
└── service // 服务类
    ├── StudentService.java
    └── TeacherService.java
```
