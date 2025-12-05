# 前后端接口文档

## 一、系统总览

### 基础信息

| 项目       | 内容                    |
| -------- | --------------------- |
| API 前缀   | `/api/v1`             |
| 数据格式     | JSON                  |
| 编码       | UTF-8                 |
| 认证方式     | Session               |
| 鉴权模型     | RBAC（基于角色）            |
| 时间格式     | `YYYY-MM-DD HH:mm:ss` |
| 会话传输   | Cookie                |

---

## 二、Session 认证机制

### 认证方式说明

- 使用 Session-Cookie 认证机制
- 登录成功后，服务器自动设置 Session Cookie
- 后续请求自动携带 Cookie 进行认证
- 无需前端手动管理 Token

### Cookie 设置规范

```http
Set-Cookie: sessionId=abc123; HttpOnly; Secure; SameSite=Strict; Path=/
```

### 权限模型说明

| 角色      | 接口范围          |
| ------- | ------------- |
| student | `/student/**` |
| teacher | `/teacher/**` |
| admin   | `/admin/**`   |
| 公共      | `/auth/**`    |

---

## 三、统一响应结构

### 成功

```json
{
  "code": 0,
  "msg": "ok",
  "data": {}
}
```

### 失败

```json
{
  "code": 401,
  "msg": "Token 已失效"
}
```

### 状态码规范

| code | 含义    |
| ---- | ----- |
| 0    | 成功    |
| 400  | 参数错误  |
| 401  | 未授权   |
| 403  | 无权限   |
| 404  | 数据不存在 |
| 500  | 服务错误  |

---

## 四、认证接口 Auth

---

### 1. 用户登录

**POST** `/api/v1/auth/login`

#### 请求

```json
{
  "username": "2023001",
  "password": "123456"
}
```

#### 响应

```json
{
  "code": 0,
  "msg": "ok",
  "data": {
    "user": {
      "id": "stu_001",
      "role": "student",
      "name": "张三",
      "number": "2023001"
    }
  }
}
```

#### HTTP 响应头

```http
Set-Cookie: sessionId=abc123; HttpOnly; Secure; SameSite=Strict; Path=/
```

| 字段           | 含义                  |
| ------------ | ------------------- |
| user         | 当前用户信息              |
| sessionId    | 服务器设置的 Session ID      |

---

### 2. 用户登出

**POST** `/api/v1/auth/logout`

#### 响应

```json
{
  "code": 0,
  "msg": "ok"
}
```

#### HTTP 响应头

```http
Set-Cookie: sessionId=; Expires=Thu, 01 Jan 1970 00:00:00 GMT; Path=/
```

---

### 3. 查询当前用户

**GET** `/api/v1/auth/me`

---

## 五、学生接口 Student

---

### 1. 个人信息

**GET** `/api/v1/student/profile`

响应：

```json
{
  "code": 0,
  "data": {
    "stuNo": "2023001",
    "name": "张三",
    "gender": "男",
    "grade": "2023",
    "major": "计算机科学",
    "email": "zs@example.com"
  }
}
```

---

### 2. 修改信息

**PUT** `/api/v1/student/profile`

请求：

```json
{
  "name": "张三",
  "email": "new@example.com"
}
```

---

### 3. 我的成绩

**GET** `/api/v1/student/scores`

参数：

| 参数       | 说明 |
| -------- | -- |
| semester | 可选 |
| courseId | 可选 |

响应：

```json
{
  "code": 0,
  "data": [
    {
      "courseId": "C001",
      "courseName": "高等数学",
      "teacher": "李老师",
      "classId": "T202301",
      "daily": 85,
      "mid": 80,
      "lab": 88,
      "final": 90,
      "total": 87
    }
  ]
}
```

---

### 4. 成绩概览

**GET** `/api/v1/student/dashboard`

```json
{
  "courseCount": 3,
  "avg": 81.2,
  "passed": 3,
  "failed": 0,
  "distribution": {
    "excellent": 1,
    "good": 2,
    "pass": 0,
    "fail": 0
  }
}
```

---

## 六、教师接口 Teacher

---

### 1. 教学班列表

**GET** `/api/v1/teacher/classes`

响应：

```json
[
  {
    "classId": "T202302",
    "courseName": "数据结构",
    "studentCount": 45,
    "semester": "2023-2024-1"
  }
]
```

---

### 2. 班级成绩

**GET** `/api/v1/teacher/class/{classId}/scores`

响应：

```json
[
  {
    "stuNo": "2023001",
    "name": "张三",
    "daily": 80,
    "mid": 70,
    "lab": 85,
    "final": 90,
    "total": 84
  }
]
```

---

### 3. 录入成绩

**POST** `/api/v1/teacher/class/{classId}/score`

```json
{
  "stuNo": "2023001",
  "daily": 80,
  "mid": 70,
  "lab": 85,
  "final": 90
}
```

---

### 4. 查询学生成绩

**GET** `/api/v1/teacher/student/{stuNo}/scores`

---

### 5. 教师 Dashboard

**GET** `/api/v1/teacher/dashboard`

```json
{
  "studentCount": 120,
  "classCount": 4,
  "avgScore": 76.4,
  "passRate": 0.92,
  "distribution": {
    "0-59": 5,
    "60-69": 20,
    "70-79": 40,
    "80-89": 30,
    "90+": 10
  }
}
```

---

## 七、公共接口

---

### 1. 课程列表

**GET** `/api/v1/courses`

---

### 2. 教学班列表

**GET** `/api/v1/classes`

---

# 八、前端使用示例

---

## Axios Client（src/client.ts）

```ts
import axios from "axios"

export const client = axios.create({
  baseURL: "/api/v1",
  timeout: 5000,
  withCredentials: true, // 携带 Cookie
})
```

---

## Modules 层（src/modules/student.ts）

```ts
import { client } from "@/client"

export const getStudentProfile = () => client.get("/student/profile")
export const updateProfile = (data) => client.put("/student/profile", data)
export const getScores = () => client.get("/student/scores")
export const getDashboard = () => client.get("/student/dashboard")
```

---

## Hooks 层（src/hooks/useStudent.ts）

```ts
import { useQuery } from "@tanstack/react-query"
import { getDashboard } from "@/modules/student"

export const useStudentDashboard = () => {
  return useQuery(["dashboard"], async () => {
    const res = await getDashboard()
    return res.data.data
  })
}
```

---

## 登录示例

```ts
await client.post("/auth/login", {
  username: "2023001",
  password: "123456"
})
```

## 登出示例

```ts
await client.post("/auth/logout")
```

---

## 错误统一处理建议

```ts
client.interceptors.response.use(
  res => res,
  err => {
    if (err.response?.status === 401) {
      // Session 已失效，跳转登录页
      window.location.href = "/login"
    }
    return Promise.reject(err)
  }
)
```
