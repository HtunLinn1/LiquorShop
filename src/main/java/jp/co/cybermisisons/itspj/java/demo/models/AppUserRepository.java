package jp.co.cybermisisons.itspj.java.demo.models;

import java.util.Collection;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AppUserRepository extends JpaRepository<AppUser, Integer> {
  AppUser findByUsername(String username);

  @Query(nativeQuery = true, value = "SELECT * FROM app_user where username = :username")
  Collection<AppUser> findUsername(@Param("username") String username);

}
