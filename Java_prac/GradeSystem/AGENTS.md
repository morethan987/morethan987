# AGENTS.md - GradeSystem

Java Spring Boot 4.0 backend + Bun/React 19 frontend grade management system.

## Quick Commands

### Backend (GradeSystemBackend/)
```bash
./mvnw spring-boot:run              # Run app
./mvnw test                         # All tests
./mvnw test -Dtest=StudentServiceTest                    # Single class
./mvnw test -Dtest=StudentServiceTest#testGetTotalStudents  # Single method
./mvnw test -Dtest="*Student*"      # Pattern match
./mvnw compile                      # Check compilation
```

### Frontend (frontend/)
```bash
bun install    # Install deps
bun dev        # Dev server (hot reload)
bun run build  # Production build
bun start      # Production start
```

### Infrastructure (nacos-docker/)
```bash
# Start Nacos (standalone mode with Derby)
docker compose -f nacos-docker/example/standalone-derby.yaml up -d

# Stop Nacos
docker compose -f nacos-docker/example/standalone-derby.yaml down

# View logs
docker logs nacos-standalone -f
```
Nacos console: http://localhost:8848/nacos (default: nacos/nacos)
Ports: 8848 (HTTP API), 9848 (gRPC), 8080 (management)

## Project Structure

### Backend
```
src/main/java/com/example/GradeSystemBackend/
├── controller/    # @RestController, @RequestMapping
├── service/       # @Service, business logic
├── repository/    # JpaRepository<Entity, UUID>
├── domain/        # JPA entities by feature (auth/, student/, course/)
├── dto/           # Data Transfer Objects
├── config/        # Spring configuration
└── security/      # Spring Security
```

### Frontend (Three-Layer Pattern)
```
src/
├── api/client.ts              # Axios instance
├── api/v1/modules/*.ts        # API endpoints
├── hooks/use-*.ts             # Data fetching hooks
├── types/*.ts                 # TypeScript interfaces
├── pages/*.tsx                # Page components
└── components/                # shadcn/ui components
```

## Code Style

### Java Naming
- Classes: `PascalCase` (StudentService, StudentDTO)
- Methods/Variables: `camelCase` (getStudentByUserId)
- Constants: `SCREAMING_SNAKE_CASE` (RoleConstants.ADMIN)
- DB columns: auto snake_case via naming strategy

### Java Patterns

**Entity:**
```java
@Entity @Table(name = "student")
public class Student {
    @Id @GeneratedValue private UUID id;
    @Column(nullable = false, unique = true) private String studentCode;
    @Enumerated(EnumType.STRING) private StudentStatus status;
    @PreUpdate public void preUpdate() { this.updatedAt = LocalDateTime.now(); }
}
```

**Repository:**
```java
@Repository
public interface StudentRepository extends JpaRepository<Student, UUID> {
    Optional<Student> findByStudentCode(String code);
    @Query("SELECT s FROM Student s WHERE s.user.id = :userId")
    Optional<Student> findByUserId(@Param("userId") UUID userId);
}
```

**Service:**
```java
@Service
public class StudentService {
    @Autowired private StudentRepository studentRepository;
    public StudentDTO getStudentByUserId(UUID userId) {
        return studentRepository.findByUserId(userId)
            .map(StudentDTO::new)
            .orElseThrow(() -> new RuntimeException("Student not found: " + userId));
    }
}
```

**Controller:**
```java
@RestController @RequestMapping("/student")
public class StudentController {
    @Autowired private StudentService studentService;
    @GetMapping("/by-user/{userId}")
    @PreAuthorize("hasAnyAuthority('student:view', 'admin:all')")
    public StudentDTO getStudent(@PathVariable UUID userId) { ... }
}
```

### TypeScript Patterns

**Types (types/*.ts):**
```typescript
export interface StudentDTO {
  id: string;
  studentCode: string;
  status: string;
  createdAt: string;
}
```

**API Module (api/v1/modules/*.ts):**
```typescript
import client from "@/api/client";
export const studentApi = {
  getByUserId: (userId: string): Promise<StudentDTO> =>
    client.get(`/student/by-user/${userId}`),
};
```

**Hook (hooks/use-*.ts):**
```typescript
export function useStudent() {
  const [student, setStudent] = useState<StudentDTO | null>(null);
  const getStudentByUserId = useCallback(async (userId: string) => {
    const data = await studentApi.getByUserId(userId);
    setStudent(data);
  }, []);
  return { student, getStudentByUserId };
}
```

**Imports:** Use `@/` alias, `type` keyword for type imports.

## Testing (JUnit 5 + Mockito)
```java
@ExtendWith(MockitoExtension.class)
public class StudentServiceTest {
    @Mock private StudentRepository studentRepository;
    @InjectMocks private StudentService studentService;

    @Test @DisplayName("Get student by user ID - success")
    void testGetStudentByUserIdSuccess() {
        when(studentRepository.findByUserId(testUserId)).thenReturn(Optional.of(mockStudent));
        StudentDTO result = studentService.getStudentByUserId(testUserId);
        assertNotNull(result);
        verify(studentRepository, times(1)).findByUserId(testUserId);
    }
}
```

Test config: `src/test/resources/application-test.yml` (H2 in-memory DB)

## Key Config
- Backend port: `8081`
- API base: `/api/v1`
- Auth: Session-based cookies (`withCredentials: true`)
- JSON date: `yyyy-MM-dd HH:mm:ss`, timezone `GMT+8`
- DB: PostgreSQL, schema `grade_system`, UUID PKs
- JPA ddl-auto: `create` (destructive in dev)

## Dependencies
**Backend:** Spring Boot 4.0, Java 21, JPA, PostgreSQL/H2, Spring Security, springdoc-openapi
**Frontend:** Bun, React 19, TypeScript strict, Tailwind 4, axios, wouter, zod, Radix/shadcn

## Adding New Entity
1. `domain/{feature}/Entity.java` - JPA entity
2. `dto/EntityDTO.java` - DTO with constructor from entity
3. `repository/EntityRepository.java` - extends JpaRepository
4. `service/EntityService.java` - business logic
5. `controller/EntityController.java` - REST endpoints
6. `frontend/src/types/entity.ts` - TypeScript interface
7. `frontend/src/api/v1/modules/entity.ts` - API calls
8. `frontend/src/hooks/use-entity.ts` - React hook
