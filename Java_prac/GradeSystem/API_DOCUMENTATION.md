# GradeSystem API Documentation

> **Version**: 1.0.0  
> **Base URL**: `http://localhost:8081/api/v1`  
> **Authentication**: Session-based (Cookies)  
> **Last Updated**: 2026-01-12

---

## Table of Contents

1. [Overview](#overview)
2. [Authentication](#authentication)
3. [API Endpoints](#api-endpoints)
   - [Auth API](#1-auth-api)
   - [User Profile API](#2-user-profile-api)
   - [User Account API](#3-user-account-api)
   - [Student API](#4-student-api)
   - [Course API](#5-course-api)
   - [Grade API](#6-grade-api)
   - [Teacher API](#7-teacher-api) *(TO BE IMPLEMENTED)*
   - [Dashboard API](#8-dashboard-api) *(TO BE IMPLEMENTED)*
4. [Data Types](#data-types)
5. [Error Handling](#error-handling)
6. [Frontend Integration](#frontend-integration)

---

## Overview

GradeSystem is a comprehensive academic management system with the following capabilities:

- **Student Features**: View grades, enroll/drop courses, track academic progress
- **Teacher Features**: Input grades, view class statistics, manage teaching assignments
- **Admin Features**: System-wide statistics, user management

### Architecture

```
Frontend (React 19 + TypeScript)
    ↓
API Layer (axios client with interceptors)
    ↓
Backend (Spring Boot 4.0 + Java 21)
    ↓
Database (PostgreSQL)
```

### Request/Response Format

- **Content-Type**: `application/json` (default), `multipart/form-data` (file uploads)
- **Date Format**: `yyyy-MM-dd HH:mm:ss`
- **Timezone**: `GMT+8`
- **ID Format**: UUID strings

---

## Authentication

### Session-Based Authentication

All API requests use **session-based authentication via cookies**:

```typescript
// Frontend axios configuration
const client = axios.create({
  baseURL: 'http://localhost:8081/api/v1',
  timeout: 10000,
  withCredentials: true  // Required for session cookies
});
```

### Authentication Flow

```
1. User logs in → POST /auth/login
2. Server creates session → Returns user info + sets session cookie
3. Subsequent requests include session cookie automatically
4. Logout → POST /auth/logout → Server invalidates session
```

### Role-Based Access

| Role | Description | Permissions |
|------|-------------|-------------|
| `student` | Student user | View own grades, enroll/drop courses |
| `teacher` | Teacher user | View/edit grades for assigned classes |
| `admin` | Administrator | Full system access |

---

## API Endpoints

### 1. Auth API

Base path: `/auth`

#### 1.1 Login

**`POST /auth/login`**

Authenticates a user and creates a session.

**Request Body:**
```typescript
interface LoginRequest {
  username: string;  // Required
  password: string;  // Required
}
```

**Response:**
```typescript
interface AuthResponse {
  success: boolean;
  message: string;
  user?: UserBasicInfo;
}

interface UserBasicInfo {
  id: string;           // UUID
  username: string;
  enabled: boolean;
  roles: string[];      // e.g., ["student"], ["teacher"], ["admin"]
  uiType: string;       // UI type for role-based routing
  realName?: string;
  email?: string;
  avatarUrl?: string;
}
```

**Example:**
```bash
curl -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "student001", "password": "password123"}'
```

**Response Codes:**
| Code | Description |
|------|-------------|
| 200 | Login successful |
| 400 | Invalid credentials |
| 401 | Authentication failed |

---

#### 1.2 Logout

**`POST /auth/logout`**

Invalidates the current session.

**Request Body:** None

**Response:**
```typescript
void  // Empty response on success
```

**Response Codes:**
| Code | Description |
|------|-------------|
| 200 | Logout successful |
| 401 | Not authenticated |

---

#### 1.3 Get Current User

**`GET /auth/me`**

Returns the currently authenticated user's information.

**Response:**
```typescript
interface AuthResponse {
  success: boolean;
  message: string;
  user?: UserBasicInfo;
}
```

**Response Codes:**
| Code | Description |
|------|-------------|
| 200 | User info returned |
| 401 | Not authenticated |

---

#### 1.4 Check Auth Status

**`GET /auth/status`**

Checks if the current session is valid.

**Response:**
```typescript
interface AuthResponse {
  success: boolean;
  message: string;
  user?: UserBasicInfo;
}
```

**Response Codes:**
| Code | Description |
|------|-------------|
| 200 | Session valid |
| 401 | Session invalid/expired |

---

### 2. User Profile API

Base path: `/user/profile`

#### 2.1 Get Profile by User ID

**`GET /user/profile/{userId}`**

**Path Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| userId | UUID | User's unique identifier |

**Response:**
```typescript
interface UserProfile {
  realName: string;
  gender: Gender;           // 0=UNKNOWN, 1=MALE, 2=FEMALE, 3=OTHER
  birthDate?: string;       // Format: YYYY-MM-DD
  email?: string;
  phone?: string;
  address?: string;
  bio?: string;             // Max 1000 characters
  avatarUrl?: string;
  createdAt: string;        // ISO datetime
  updatedAt: string;        // ISO datetime
}
```

---

#### 2.2 Get Profile by Username

**`GET /user/profile/by-username/{userName}`**

**Path Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| userName | string | Username |

**Response:** `UserProfile`

---

#### 2.3 Get Current User Profile

**`GET /user/profile/me`**

Returns the profile of the currently authenticated user.

**Response:** `UserProfile`

---

#### 2.4 Create User Profile

**`POST /user/profile`**

**Request Body:**
```typescript
interface CreateUserProfileRequest {
  userId: string;       // Required - UUID
  realName: string;     // Required
  gender: Gender;       // Required
  birthDate?: string;
  email?: string;
  phone?: string;
  address?: string;
  bio?: string;
}
```

**Response:** `UserProfile`

---

#### 2.5 Update User Profile

**`PUT /user/profile/{userId}`**

**Path Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| userId | UUID | User's unique identifier |

**Request Body:**
```typescript
interface UpdateUserProfileRequest {
  realName?: string;
  gender?: Gender;
  birthDate?: string;
  email?: string;
  phone?: string;
  address?: string;
  bio?: string;
}
```

**Response:** `UserProfile`

---

#### 2.6 Update Current User Profile

**`PUT /user/profile/me`**

Same as 2.5, but updates the current authenticated user.

**Request Body:** `UpdateUserProfileRequest`

**Response:** `UserProfile`

---

#### 2.7 Get User Profile List

**`GET /user/profile`**

Returns a paginated list of user profiles.

**Query Parameters:**
| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| page | number | 0 | Page number (0-indexed) |
| size | number | 20 | Page size |
| search | string | - | Search by name or email |
| gender | Gender | - | Filter by gender |
| sortBy | string | "createdAt" | Sort field |
| sortOrder | string | "desc" | Sort order: "asc" or "desc" |

**Response:**
```typescript
interface UserProfileListResponse {
  profiles: UserProfile[];
  total: number;
  page: number;
  size: number;
}
```

---

#### 2.8 Upload Avatar

**`POST /user/profile/{userId}/avatar`**

**Content-Type:** `multipart/form-data`

**Path Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| userId | UUID | User's unique identifier |

**Request Body:**
| Field | Type | Description |
|-------|------|-------------|
| avatar | File | Image file (JPEG, PNG, etc.) |

**Response:**
```typescript
{
  avatarUrl: string;
}
```

---

#### 2.9 Delete Avatar

**`DELETE /user/profile/{userId}/avatar`**

**Path Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| userId | UUID | User's unique identifier |

**Response:** `void`

---

### 3. User Account API

Base path: `/user`

#### 3.1 Change Password

**`POST /user/{userId}/password`**

**Path Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| userId | UUID | User's unique identifier |

**Request Body:**
```typescript
interface ChangePasswordRequest {
  oldPassword: string;
  newPassword: string;
  newPasswordConfirm: string;
}
```

**Response:** `void`

**Response Codes:**
| Code | Description |
|------|-------------|
| 200 | Password changed |
| 400 | Passwords don't match or invalid |
| 401 | Old password incorrect |

---

#### 3.2 Change Username

**`POST /user/{userId}/username`**

**Path Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| userId | UUID | User's unique identifier |

**Request Body:**
```typescript
interface ChangeUsernameRequest {
  password: string;      // Current password for verification
  newUsername: string;
}
```

**Response:** `void`

**Response Codes:**
| Code | Description |
|------|-------------|
| 200 | Username changed |
| 400 | Username already exists |
| 401 | Password incorrect |

---

#### 3.3 Toggle User Status

**`PATCH /user/{userId}/status`**

**Path Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| userId | UUID | User's unique identifier |

**Request Body:**
```typescript
{
  enabled: boolean;
}
```

**Response:** `void`

**Permissions:** Admin only

---

### 4. Student API

Base path: `/student`

#### 4.1 Get Student by User ID

**`GET /student/by-user/{userId}`**

**Path Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| userId | UUID | User's unique identifier |

**Response:**
```typescript
interface StudentDTO {
  id: string;               // UUID
  studentCode: string;      // e.g., "2024001"
  major?: string;
  className?: string;
  enrollmentYear?: number;
  currentSemester: number;
  status: StudentStatus;
  totalCredits?: number;
  advisor?: string;
  expectedGraduationDate?: string;
  createdAt: string;
  updatedAt: string;
}

enum StudentStatus {
  ENROLLED = "ENROLLED",
  SUSPENDED = "SUSPENDED",
  WITHDRAWN = "WITHDRAWN",
  GRADUATED = "GRADUATED",
  TRANSFERRED = "TRANSFERRED",
  EXPELLED = "EXPELLED",
  DEFERRED = "DEFERRED",
  EXCHANGE = "EXCHANGE"
}
```

---

### 5. Course API

Base path: `/courses`

#### 5.1 Get Student Courses

**`GET /courses/student/{studentId}`**

Returns courses that the student is currently enrolled in.

**Path Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| studentId | UUID | Student's unique identifier |

**Query Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| courseType | CourseType | Filter by course type |

**Response:**
```typescript
TeachingClass[]
```

```typescript
interface TeachingClass {
  id: string;
  name?: string;
  course: Course;
  teacherName: string;
  classroom: string;
  timeSchedule: string;     // e.g., "周一7-8节，周三7-8节"
  capacity: number;
  enrolled: number;
  status: TeachingClassStatus;
  semesterName: string;
}

interface Course {
  id: string;
  name: string;
  description?: string;
  credit: number;
  semester: number;
  courseType: CourseType;
}

enum CourseType {
  REQUIRED = "REQUIRED",
  ELECTIVE = "ELECTIVE",
  LIMITED_ELECTIVE = "LIMITED_ELECTIVE",
  GENERAL = "GENERAL",
  PROFESSIONAL = "PROFESSIONAL"
}

enum TeachingClassStatus {
  PLANNED = "PLANNED",
  OPEN_FOR_ENROLLMENT = "OPEN_FOR_ENROLLMENT",
  ENROLLMENT_CLOSED = "ENROLLMENT_CLOSED",
  ACTIVE = "ACTIVE",
  COMPLETED = "COMPLETED",
  CANCELLED = "CANCELLED",
  SUSPENDED = "SUSPENDED",
  MERGED = "MERGED"
}
```

---

#### 5.2 Get Available Courses

**`GET /courses/available`**

Returns courses available for the student to select.

**Query Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| studentId | UUID | Yes | Student's unique identifier |
| courseType | CourseType | No | Filter by course type |
| search | string | No | Search by course name |

**Response:** `TeachingClass[]`

---

#### 5.3 Select Course

**`POST /courses/select`**

Enrolls a student in a course.

**Query Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| studentId | UUID | Yes | Student's unique identifier |
| teachingClassId | UUID | Yes | Teaching class identifier |

**Response:** `string` (success message)

**Response Codes:**
| Code | Description |
|------|-------------|
| 200 | Enrollment successful |
| 400 | Course full or enrollment closed |
| 409 | Already enrolled |

---

#### 5.4 Drop Course

**`DELETE /courses/drop`**

Removes a student from a course.

**Query Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| studentId | UUID | Yes | Student's unique identifier |
| teachingClassId | UUID | Yes | Teaching class identifier |

**Response:** `string` (success message)

**Response Codes:**
| Code | Description |
|------|-------------|
| 200 | Drop successful |
| 400 | Cannot drop (past deadline) |
| 404 | Enrollment not found |

---

#### 5.5 Get Course by ID

**`GET /courses/{courseId}`**

**Path Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| courseId | UUID | Course identifier |

**Response:** `Course`

---

### 6. Grade API

Base path: `/grades`

#### 6.1 Get Student Grades

**`GET /grades/student/{studentId}`**

**Path Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| studentId | UUID | Student's unique identifier |

**Query Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| semester | string | Filter by semester (e.g., "2024-秋") |
| courseType | CourseType | Filter by course type |

**Response:**
```typescript
Grade[]

interface Grade {
  id: string;
  student: Student;
  course: Course;
  usualScore?: number;       // 平时成绩 (0-100)
  midtermScore?: number;     // 期中成绩 (0-100)
  finalExamScore?: number;   // 期末成绩 (0-100)
  experimentScore?: number;  // 实验成绩 (0-100)
  finalScore?: number;       // 总评成绩 (calculated)
  gpa?: number;              // 绩点 (0-4.0)
}
```

---

#### 6.2 Get Student Grade Statistics

**`GET /grades/student/{studentId}/stats`**

**Path Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| studentId | UUID | Student's unique identifier |

**Response:**
```typescript
interface GradeStats {
  totalCredits: number;      // Total earned credits
  averageGPA: number;        // Overall GPA
  averageScore: number;      // Weighted average score
  passedCourses: number;     // Number of passed courses
}
```

---

#### 6.3 Get Semester Statistics

**`GET /grades/student/{studentId}/semester/{semester}/stats`**

**Path Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| studentId | UUID | Student's unique identifier |
| semester | string | Semester name (e.g., "2024-秋") |

**Response:** `GradeStats`

---

#### 6.4 Get Student Semesters

**`GET /grades/student/{studentId}/semesters`**

Returns all semesters where the student has grades.

**Path Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| studentId | UUID | Student's unique identifier |

**Response:** `string[]` (array of semester names)

---

#### 6.5 Get Grade by ID

**`GET /grades/{gradeId}`**

**Path Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| gradeId | UUID | Grade record identifier |

**Response:** `Grade`

---

#### 6.6 Update Grade

**`PUT /grades/{gradeId}`**

**Permissions:** Teacher only (must be assigned to the course)

**Path Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| gradeId | UUID | Grade record identifier |

**Request Body:**
```typescript
interface UpdateGradeRequest {
  usualScore?: number;
  midtermScore?: number;
  finalExamScore?: number;
  experimentScore?: number;
}
```

**Response:** `Grade` (updated record with recalculated finalScore and gpa)

---

### 7. Teacher API

> **Status: TO BE IMPLEMENTED**

Base path: `/teacher`

These endpoints are required to support the teacher pages in the frontend.

#### 7.1 Get Teaching Classes

**`GET /teacher/{teacherId}/teaching-classes`**

Returns all teaching classes assigned to a teacher.

**Path Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| teacherId | UUID | Teacher's unique identifier |

**Response:**
```typescript
interface TeachingClassWithStats {
  id: string;
  className: string;
  courseName: string;
  courseType: CourseType;
  credit: number;
  studentCount: number;
  semester: string;
  schedule: string;
  location: string;
}
```

**Frontend Usage:** `pages/teacher/courses.tsx`

---

#### 7.2 Get Students in Teaching Class

**`GET /teacher/teaching-classes/{teachingClassId}/students`**

Returns all students in a specific teaching class.

**Path Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| teachingClassId | UUID | Teaching class identifier |

**Response:**
```typescript
interface StudentGradeInput {
  id: string;
  studentCode: string;
  name: string;
  className: string;
  usualScore?: number;
  midtermScore?: number;
  finalExamScore?: number;
  experimentScore?: number;
  finalScore?: number;
  gpa?: number;
  isModified?: boolean;
}
```

**Frontend Usage:** `pages/teacher/grade-input.tsx`

---

#### 7.3 Get Grade Distribution

**`GET /teacher/teaching-classes/{teachingClassId}/grades/distribution`**

Returns grade distribution for a teaching class.

**Path Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| teachingClassId | UUID | Teaching class identifier |

**Response:**
```typescript
interface DistributionData {
  range: string;    // e.g., "90-100", "80-89", "70-79", etc.
  count: number;
}
```

**Frontend Usage:** `pages/teacher/grade-view.tsx`

---

#### 7.4 Batch Update Grades

**`POST /teacher/teaching-classes/{teachingClassId}/grades/batch`**

Batch update grades for multiple students.

**Path Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| teachingClassId | UUID | Teaching class identifier |

**Request Body:**
```typescript
interface BatchGradeUpdate {
  grades: StudentGradeInput[];
}
```

**Response:**
```typescript
{
  success: boolean;
  updatedCount: number;
  errors?: string[];
}
```

**Frontend Usage:** `pages/teacher/grade-input.tsx` (Save All button)

---

#### 7.5 Export Grades

**`GET /teacher/teaching-classes/{teachingClassId}/grades/export`**

Exports grades as Excel/CSV file.

**Path Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| teachingClassId | UUID | Teaching class identifier |

**Response:** File download (application/vnd.openxmlformats-officedocument.spreadsheetml.sheet)

**Frontend Usage:** `pages/teacher/grade-input.tsx` (Export button)

---

#### 7.6 Import Grades Template

**`POST /teacher/teaching-classes/{teachingClassId}/grades/import`**

Imports grades from an uploaded file.

**Content-Type:** `multipart/form-data`

**Path Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| teachingClassId | UUID | Teaching class identifier |

**Request Body:**
| Field | Type | Description |
|-------|------|-------------|
| file | File | Excel/CSV file with grades |

**Response:**
```typescript
{
  success: boolean;
  importedCount: number;
  errors?: string[];
}
```

**Frontend Usage:** `pages/teacher/grade-input.tsx` (Import button)

---

### 8. Dashboard API

> **Status: TO BE IMPLEMENTED**

Base path: `/dashboard`

These endpoints provide dashboard statistics for different user roles.

#### Common Response Structure

All dashboard endpoints return the same card data structure:

```typescript
interface CardData {
  id: string;
  title: string;
  value: string;
  trend: {
    direction: 'up' | 'down' | 'neutral';
    value: string;
    isVisible: boolean;
  };
  footer: {
    status: string;
    description: string;
  };
}

type DashboardResponse = CardData[];
```

---

#### 8.1 Admin Dashboard

**`GET /dashboard/admin`**

**Permissions:** Admin only

**Response:** `CardData[]`

**Expected Cards:**
| ID | Title | Data Source |
|----|-------|-------------|
| total-users | 在校师生总数 | Count students + teachers |
| active-classes | 本学期开设课程 | Count active teaching classes |
| avg-enrollment | 平均报到率 | Calculate from enrollment records |
| system-health | 成绩录入进度 | Count completed vs total grade entries |

**Frontend Usage:** `pages/general.tsx` (when user is admin)

---

#### 8.2 Student Dashboard

**`GET /dashboard/student/{studentId}`**

**Path Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| studentId | UUID | Student's unique identifier |

**Response:** `CardData[]`

**Expected Cards:**
| ID | Title | Data Source |
|----|-------|-------------|
| gpa-stats | 当前绩点 (GPA) | Calculate from grades |
| credits-progress | 修读学分 | Sum completed credits |
| current-courses | 本学期课程 | Count current enrollments |
| weighted-score | 加权平均分 | Calculate weighted average |

**Frontend Usage:** `pages/general.tsx` (when user is student)

---

#### 8.3 Teacher Dashboard

**`GET /dashboard/teacher/{teacherId}`**

**Path Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| teacherId | UUID | Teacher's unique identifier |

**Response:** `CardData[]`

**Expected Cards:**
| ID | Title | Data Source |
|----|-------|-------------|
| teaching-classes | 执教班级 | Count teaching assignments |
| total-students | 学生总数 | Sum students in all classes |
| workload-hours | 本周课时 | Calculate from schedule |
| avg-pass-rate | 所教课程及格率 | Calculate from grades |

**Frontend Usage:** `pages/general.tsx` (when user is teacher)

---

## Data Types

### Enums

#### Gender
```typescript
enum Gender {
  UNKNOWN = 0,
  MALE = 1,
  FEMALE = 2,
  OTHER = 3
}
```

#### StudentStatus
```typescript
enum StudentStatus {
  ENROLLED = "ENROLLED",           // 在读
  SUSPENDED = "SUSPENDED",         // 休学
  WITHDRAWN = "WITHDRAWN",         // 退学
  GRADUATED = "GRADUATED",         // 毕业
  TRANSFERRED = "TRANSFERRED",     // 转学
  EXPELLED = "EXPELLED",           // 开除
  DEFERRED = "DEFERRED",           // 延期
  EXCHANGE = "EXCHANGE"            // 交换
}
```

#### CourseType
```typescript
enum CourseType {
  REQUIRED = "REQUIRED",                     // 必修
  ELECTIVE = "ELECTIVE",                     // 选修
  LIMITED_ELECTIVE = "LIMITED_ELECTIVE",     // 限选
  GENERAL = "GENERAL",                       // 通识
  PROFESSIONAL = "PROFESSIONAL"              // 专业
}
```

#### TeachingClassStatus
```typescript
enum TeachingClassStatus {
  PLANNED = "PLANNED",                       // 计划中
  OPEN_FOR_ENROLLMENT = "OPEN_FOR_ENROLLMENT", // 开放选课
  ENROLLMENT_CLOSED = "ENROLLMENT_CLOSED",   // 选课结束
  ACTIVE = "ACTIVE",                         // 进行中
  COMPLETED = "COMPLETED",                   // 已完成
  CANCELLED = "CANCELLED",                   // 已取消
  SUSPENDED = "SUSPENDED",                   // 已暂停
  MERGED = "MERGED"                          // 已合并
}
```

#### TeacherTitle
```typescript
enum TeacherTitle {
  PROFESSOR = "PROFESSOR",                       // 教授
  ASSOCIATE_PROFESSOR = "ASSOCIATE_PROFESSOR",   // 副教授
  ASSISTANT_PROFESSOR = "ASSISTANT_PROFESSOR",   // 助理教授
  LECTURER = "LECTURER",                         // 讲师
  TEACHING_ASSISTANT = "TEACHING_ASSISTANT",     // 助教
  RESEARCH_PROFESSOR = "RESEARCH_PROFESSOR",     // 研究员
  CLINICAL_PROFESSOR = "CLINICAL_PROFESSOR",     // 临床教授
  ADJUNCT_PROFESSOR = "ADJUNCT_PROFESSOR",       // 兼职教授
  EMERITUS_PROFESSOR = "EMERITUS_PROFESSOR",     // 荣誉退休教授
  VISITING_PROFESSOR = "VISITING_PROFESSOR",     // 访问教授
  DISTINGUISHED_PROFESSOR = "DISTINGUISHED_PROFESSOR" // 杰出教授
}
```

#### TeacherStatus
```typescript
enum TeacherStatus {
  ACTIVE = "ACTIVE",           // 在职
  ON_LEAVE = "ON_LEAVE",       // 休假
  UNPAID_LEAVE = "UNPAID_LEAVE", // 无薪休假
  RETIRED = "RETIRED",         // 退休
  RESIGNED = "RESIGNED",       // 辞职
  TERMINATED = "TERMINATED",   // 解雇
  TRANSFERRED = "TRANSFERRED", // 调动
  TEMPORARY = "TEMPORARY",     // 临时
  PART_TIME = "PART_TIME",     // 兼职
  VISITING = "VISITING",       // 访问
  SUSPENDED = "SUSPENDED"      // 停职
}
```

---

### Core Interfaces

#### User & Authentication

```typescript
interface User {
  id: string;
  username: string;
  enabled: boolean;
  roles: string[];
  uiType: string;
}

interface UserBasicInfo extends User {
  realName?: string;
  email?: string;
  avatarUrl?: string;
}

interface UserProfile {
  realName: string;
  gender: Gender;
  birthDate?: string;
  email?: string;
  phone?: string;
  address?: string;
  bio?: string;
  avatarUrl?: string;
  createdAt: string;
  updatedAt: string;
}

interface AuthResponse {
  success: boolean;
  message: string;
  user?: UserBasicInfo;
}
```

#### Student

```typescript
interface Student {
  id: string;
  user: User;
  studentCode: string;
  major?: string;
  className?: string;
  enrollmentYear?: number;
  currentSemester: number;
  status: StudentStatus;
  totalCredits?: number;
  advisor?: string;
  expectedGraduationDate?: string;
  createdAt: string;
  updatedAt: string;
}

// DTO without nested user object
interface StudentDTO {
  id: string;
  studentCode: string;
  major?: string;
  className?: string;
  enrollmentYear?: number;
  currentSemester: number;
  status: StudentStatus;
  totalCredits?: number;
  advisor?: string;
  expectedGraduationDate?: string;
  createdAt: string;
  updatedAt: string;
}
```

#### Teacher

```typescript
interface Teacher {
  id: string;
  user: User;
  employeeCode: string;
  department?: string;
  title: TeacherTitle;
  specialization?: string;
  hireDate?: string;
  status: TeacherStatus;
  salary?: number;
  workload?: number;
  maxCourses?: number;
  office?: string;
  officePhone?: string;
  officeHours?: string;
  qualifications?: string;
  researchInterests?: string;
  createdAt: string;
  updatedAt: string;
}
```

#### Course & Teaching Class

```typescript
interface Course {
  id: string;
  name: string;
  description?: string;
  credit: number;
  semester: number;
  courseType: CourseType;
}

interface TeachingClass {
  id: string;
  name?: string;
  course: Course;
  teacherName: string;
  classroom: string;
  timeSchedule: string;
  capacity: number;
  enrolled: number;
  status: TeachingClassStatus;
  semesterName: string;
}
```

#### Grade

```typescript
interface Grade {
  id: string;
  student: Student;
  course: Course;
  usualScore?: number;
  midtermScore?: number;
  finalExamScore?: number;
  experimentScore?: number;
  finalScore?: number;
  gpa?: number;
}

interface GradeStats {
  totalCredits: number;
  averageGPA: number;
  averageScore: number;
  passedCourses: number;
}
```

#### Dashboard

```typescript
interface CardData {
  id: string;
  title: string;
  value: string;
  trend: {
    direction: 'up' | 'down' | 'neutral';
    value: string;
    isVisible: boolean;
  };
  footer: {
    status: string;
    description: string;
  };
}
```

---

## Error Handling

### HTTP Status Codes

| Code | Meaning | When Used |
|------|---------|-----------|
| 200 | OK | Successful request |
| 201 | Created | Resource created successfully |
| 400 | Bad Request | Invalid parameters or validation error |
| 401 | Unauthorized | Not authenticated or session expired |
| 403 | Forbidden | Authenticated but not authorized |
| 404 | Not Found | Resource doesn't exist |
| 409 | Conflict | Resource conflict (e.g., duplicate) |
| 500 | Internal Server Error | Server-side error |

### Error Response Format

```typescript
interface ErrorResponse {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
}
```

### Frontend Error Handling

The axios client automatically handles errors:

```typescript
// Interceptor in client.ts
response.interceptors.response.use(
  (response) => response.data,
  (error) => {
    const status = error.response?.status;
    switch (status) {
      case 400:
        console.error('请求参数错误');
        break;
      case 401:
        window.location.href = '/login';  // Auto-redirect
        break;
      case 403:
        console.error('无权限访问该资源');
        break;
      case 500:
        console.error('服务器内部错误');
        break;
    }
    return Promise.reject(error);
  }
);
```

---

## Frontend Integration

### Three-Layer Architecture

```
┌─────────────────────────────────────────┐
│ COMPONENTS (pages/*.tsx)                │
│   - UI rendering                        │
│   - User interactions                   │
└─────────────────┬───────────────────────┘
                  │
┌─────────────────▼───────────────────────┐
│ HOOKS (hooks/use-*.ts)                  │
│   - State management                    │
│   - Loading/error handling              │
│   - Auto-refresh logic                  │
└─────────────────┬───────────────────────┘
                  │
┌─────────────────▼───────────────────────┐
│ API MODULES (api/v1/modules/*.ts)       │
│   - Endpoint definitions                │
│   - No business logic                   │
└─────────────────┬───────────────────────┘
                  │
┌─────────────────▼───────────────────────┐
│ CLIENT (api/client.ts)                  │
│   - Axios instance                      │
│   - Interceptors                        │
│   - Auth handling                       │
└─────────────────────────────────────────┘
```

### Existing API Modules

| Module | Path | Endpoints |
|--------|------|-----------|
| `authApi` | `api/v1/modules/auth.ts` | login, logout, getCurrentUser, checkAuth |
| `userProfileApi` | `api/v1/modules/user.ts` | CRUD profile, avatar operations |
| `userApi` | `api/v1/modules/user.ts` | password, username, status |
| `studentApi` | `api/v1/modules/student.ts` | getStudentByUserId |
| `courseApi` | `api/v1/modules/course.ts` | courses, enrollment |
| `gradeApi` | `api/v1/modules/grade.ts` | grades, stats, update |

### Existing Hooks

| Hook | Path | Purpose |
|------|------|---------|
| `useAuth` | `hooks/use-auth.ts` | Authentication state |
| `useUserProfile` | `hooks/use-user.ts` | Profile CRUD |
| `useStudent` | `hooks/use-student.ts` | Student data |
| `useCourses` | `hooks/use-courses.ts` | Course management |
| `useGrades` | `hooks/use-grades.ts` | Grade data |

### Adding New API Integration

Example: Adding Teacher API

1. **Create Types** (`types/teacher.ts` - already exists)

2. **Create API Module** (`api/v1/modules/teacher.ts`):
```typescript
import client from '@/api/client';
import type { TeachingClassWithStats, StudentGradeInput } from '@/types/teacher';

export const teacherApi = {
  getTeachingClasses: (teacherId: string): Promise<TeachingClassWithStats[]> =>
    client.get(`/teacher/${teacherId}/teaching-classes`),
    
  getStudentsInClass: (teachingClassId: string): Promise<StudentGradeInput[]> =>
    client.get(`/teacher/teaching-classes/${teachingClassId}/students`),
    
  batchUpdateGrades: (teachingClassId: string, grades: StudentGradeInput[]) =>
    client.post(`/teacher/teaching-classes/${teachingClassId}/grades/batch`, { grades }),
};
```

3. **Create Hook** (`hooks/use-teacher.ts`):
```typescript
import { useState, useCallback } from 'react';
import { teacherApi } from '@/api/v1/modules/teacher';

export function useTeacher() {
  const [teachingClasses, setTeachingClasses] = useState([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);

  const fetchTeachingClasses = useCallback(async (teacherId: string) => {
    setIsLoading(true);
    try {
      const data = await teacherApi.getTeachingClasses(teacherId);
      setTeachingClasses(data);
    } catch (e) {
      setError(e.message);
    } finally {
      setIsLoading(false);
    }
  }, []);

  return { teachingClasses, isLoading, error, fetchTeachingClasses };
}
```

4. **Use in Component**:
```typescript
function TeacherCoursesPage() {
  const { user } = useAuthContext();
  const { teachingClasses, fetchTeachingClasses } = useTeacher();
  
  useEffect(() => {
    if (user?.id) {
      fetchTeachingClasses(user.id);
    }
  }, [user?.id]);
  
  return <div>{/* render teachingClasses */}</div>;
}
```

---

## Implementation Status

### Implemented (Backend Ready)

| API | Endpoints | Frontend Integration |
|-----|-----------|---------------------|
| Auth | 4/4 | ✅ Complete |
| User Profile | 9/9 | ✅ Complete |
| User Account | 3/3 | ✅ Complete |
| Student | 1/1 | ✅ Complete |
| Course | 5/5 | ✅ Complete |
| Grade | 7/7 | ✅ Complete |

### To Be Implemented

| API | Endpoints | Priority | Frontend Pages |
|-----|-----------|----------|----------------|
| Teacher | 6 | High | courses.tsx, grade-input.tsx, grade-view.tsx |
| Dashboard | 3 | Medium | general.tsx |
| Signup | 1 | Low | signup.tsx |
| Export/Import | 2 | Medium | grade-input.tsx |

---

## Changelog

### v1.0.0 (2026-01-12)
- Initial documentation
- Documented all existing APIs (29 endpoints)
- Defined Teacher API requirements (6 endpoints)
- Defined Dashboard API requirements (3 endpoints)
- Added type definitions and error handling

---

*Document generated from frontend source code analysis.*
