package com.example.user.repository;

import com.example.user.domain.Gender;
import com.example.user.domain.UserProfile;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, UUID> {

    Optional<UserProfile> findByUserId(UUID userId);

    List<UserProfile> findByRealName(String realName);

    List<UserProfile> findByRealNameContainingIgnoreCase(String realName);

    List<UserProfile> findByGender(Gender gender);

    Optional<UserProfile> findByEmail(String email);

    Optional<UserProfile> findByPhone(String phone);

    List<UserProfile> findByBirthDateBetween(LocalDate startDate, LocalDate endDate);

    @Query("SELECT up FROM UserProfile up WHERE YEAR(CURRENT_DATE) - YEAR(up.birthDate) BETWEEN :minAge AND :maxAge")
    List<UserProfile> findByAgeBetween(@Param("minAge") Integer minAge, @Param("maxAge") Integer maxAge);

    @Query("SELECT up FROM UserProfile up WHERE up.gender = :gender AND YEAR(CURRENT_DATE) - YEAR(up.birthDate) BETWEEN :minAge AND :maxAge")
    List<UserProfile> findByGenderAndAgeBetween(@Param("gender") Gender gender, @Param("minAge") Integer minAge, @Param("maxAge") Integer maxAge);

    @Query("SELECT COUNT(up) FROM UserProfile up")
    long countAllUserProfiles();

    long countByGender(Gender gender);

    @Query("SELECT AVG(YEAR(CURRENT_DATE) - YEAR(up.birthDate)) FROM UserProfile up WHERE up.birthDate IS NOT NULL")
    Double calculateAverageAge();

    @Query("SELECT up FROM UserProfile up ORDER BY up.realName ASC")
    List<UserProfile> findAllOrderByRealNameAsc();

    @Query("SELECT up FROM UserProfile up ORDER BY up.createdAt DESC")
    List<UserProfile> findAllOrderByCreatedAtDesc();

    @Query("SELECT up FROM UserProfile up WHERE up.email IS NOT NULL AND up.email != ''")
    List<UserProfile> findUsersWithEmail();

    @Query("SELECT up FROM UserProfile up WHERE up.phone IS NOT NULL AND up.phone != ''")
    List<UserProfile> findUsersWithPhone();

    @Query("SELECT up FROM UserProfile up WHERE up.avatarUrl IS NOT NULL AND up.avatarUrl != ''")
    List<UserProfile> findUsersWithAvatar();

    @Query("SELECT up FROM UserProfile up WHERE up.createdAt >= :date ORDER BY up.createdAt DESC")
    List<UserProfile> findUsersCreatedAfter(@Param("date") LocalDateTime date);

    @Query("SELECT COUNT(up) > 0 FROM UserProfile up WHERE up.email = :email AND up.id != :excludeId")
    boolean existsByEmailAndIdNot(@Param("email") String email, @Param("excludeId") UUID excludeId);

    @Query("SELECT COUNT(up) > 0 FROM UserProfile up WHERE up.phone = :phone AND up.id != :excludeId")
    boolean existsByPhoneAndIdNot(@Param("phone") String phone, @Param("excludeId") UUID excludeId);

    boolean existsByUserId(UUID userId);
}
