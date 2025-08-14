package com.whispeer.chat.user.repository;

import com.whispeer.chat.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, String> {

    boolean existsByEmail(String email);

} // end class