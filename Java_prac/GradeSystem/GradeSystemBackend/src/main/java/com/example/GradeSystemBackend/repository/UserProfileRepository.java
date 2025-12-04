package com.example.GradeSystemBackend.repository;

import com.example.GradeSystemBackend.domain.auth.User;
import com.example.GradeSystemBackend.domain.info.Gender;
import com.example.GradeSystemBackend.domain.info.UserProfile;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserProfileRepository
    extends JpaRepository<UserProfile, UUID> {
    // 根据User查找UserProfile
    Optional<UserProfile> findByUser(User user);

    // 根据User ID查找UserProfile
    @Query("SELECT up FROM UserProfile up WHERE up.user.id = :userId")
    Optional<UserProfile> findByUserId(@Param("userId") UUID userId);

    // 根据用户名查找UserProfile
    @Query("SELECT up FROM UserProfile up WHERE up.user.username = :username")
    Optional<UserProfile> findByUsername(@Param("username") String username);

    // 根据真实姓名查找
    List<UserProfile> findByRealName(String realName);

    // 根据真实姓名模糊查询（不区分大小写）
    List<UserProfile> findByRealNameContainingIgnoreCase(String realName);

    // 根据性别查找用户
    List<UserProfile> findByGender(Gender gender);

    // 根据邮箱查找（邮箱应该是唯一的）
    Optional<UserProfile> findByEmail(String email);

    // 根据手机号查找（手机号应该是唯一的）
    Optional<UserProfile> findByPhone(String phone);

    // 根据出生日期范围查找用户
    List<UserProfile> findByBirthDateBetween(
        LocalDate startDate,
        LocalDate endDate
    );

    // 根据年龄范围查找用户（通过出生日期计算）
    @Query(
        "SELECT up FROM UserProfile up WHERE " +
            "YEAR(CURRENT_DATE) - YEAR(up.birthDate) BETWEEN :minAge AND :maxAge"
    )
    List<UserProfile> findByAgeBetween(
        @Param("minAge") Integer minAge,
        @Param("maxAge") Integer maxAge
    );

    // 根据性别和年龄范围查找用户
    @Query(
        "SELECT up FROM UserProfile up WHERE up.gender = :gender AND " +
            "YEAR(CURRENT_DATE) - YEAR(up.birthDate) BETWEEN :minAge AND :maxAge"
    )
    List<UserProfile> findByGenderAndAgeBetween(
        @Param("gender") Gender gender,
        @Param("minAge") Integer minAge,
        @Param("maxAge") Integer maxAge
    );

    // 统计用户总数
    @Query("SELECT COUNT(up) FROM UserProfile up")
    long countAllUserProfiles();

    // 根据性别统计用户数量
    long countByGender(Gender gender);

    // 计算用户的平均年龄
    @Query(
        "SELECT AVG(YEAR(CURRENT_DATE) - YEAR(up.birthDate)) " +
            "FROM UserProfile up WHERE up.birthDate IS NOT NULL"
    )
    Double calculateAverageAge();

    // 按真实姓名排序查找所有用户
    @Query("SELECT up FROM UserProfile up ORDER BY up.realName ASC")
    List<UserProfile> findAllOrderByRealNameAsc();

    // 按创建时间排序查找所有用户
    @Query("SELECT up FROM UserProfile up ORDER BY up.createdAt DESC")
    List<UserProfile> findAllOrderByCreatedAtDesc();

    // 查找有邮箱的用户
    @Query(
        "SELECT up FROM UserProfile up WHERE up.email IS NOT NULL AND up.email != ''"
    )
    List<UserProfile> findUsersWithEmail();

    // 查找有手机号的用户
    @Query(
        "SELECT up FROM UserProfile up WHERE up.phone IS NOT NULL AND up.phone != ''"
    )
    List<UserProfile> findUsersWithPhone();

    // 查找有头像的用户
    @Query(
        "SELECT up FROM UserProfile up WHERE up.avatarUrl IS NOT NULL AND up.avatarUrl != ''"
    )
    List<UserProfile> findUsersWithAvatar();

    // 查找最近注册的用户（按创建时间）
    @Query("SELECT up FROM UserProfile up ORDER BY up.createdAt DESC")
    List<UserProfile> findRecentlyCreatedUsers();

    // 查找指定日期之后创建的用户
    @Query(
        "SELECT up FROM UserProfile up WHERE up.createdAt >= :date ORDER BY up.createdAt DESC"
    )
    List<UserProfile> findUsersCreatedAfter(
        @Param("date") java.time.LocalDateTime date
    );

    // 检查邮箱是否已存在（排除指定用户）
    @Query(
        "SELECT COUNT(up) > 0 FROM UserProfile up WHERE up.email = :email AND up.id != :excludeId"
    )
    boolean existsByEmailAndIdNot(
        @Param("email") String email,
        @Param("excludeId") UUID excludeId
    );

    // 检查手机号是否已存在（排除指定用户）
    @Query(
        "SELECT COUNT(up) > 0 FROM UserProfile up WHERE up.phone = :phone AND up.id != :excludeId"
    )
    boolean existsByPhoneAndIdNot(
        @Param("phone") String phone,
        @Param("excludeId") UUID excludeId
    );
}
