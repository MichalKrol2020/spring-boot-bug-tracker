package com.company.repository;

import com.company.entity.EmailToken;
import com.company.entity.User;
import com.company.enumeration.TokenPurpose;
import org.springframework.data.jpa.repository.JpaRepository;


public interface EmailTokenRepository extends JpaRepository<EmailToken, Long>
{
    EmailToken findByToken(String token);
    EmailToken findByUserAndPurpose(User user, TokenPurpose purpose);
}
