package com.company.entity;

import com.company.enumeration.TokenPurpose;
import com.company.exception.InvalidTokenException;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

import static com.company.constant.EmailTokenConstant.*;

@Entity
@Getter
@Setter
@Table(name = "email_token")
public class EmailToken implements Serializable
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private String token;

    private LocalDateTime creationDate;

    private LocalDateTime expiryDate;

    @Setter
    private LocalDateTime usedDate;

    @Enumerated(EnumType.STRING)
    private TokenPurpose purpose;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public EmailToken()
    {}

    public EmailToken(User user, TokenPurpose purpose)
    {
        LocalDateTime now = LocalDateTime.now();

        this.token = UUID.randomUUID().toString();
        this.creationDate = now;
        this.expiryDate = this.getExpiryDate(now, purpose);
        this.user = user;
        this.purpose = purpose;
    }

    private LocalDateTime getExpiryDate(LocalDateTime now, TokenPurpose purpose)
    {
        if(purpose.equals(TokenPurpose.RESET_PASSWORD))
        {
            return now.plusMinutes(RESET_PASSWORD_TOKEN_LIFE_DURATION_MINUTES);
        } else if(purpose.equals(TokenPurpose.CONFIRM_ACCOUNT))
        {
            return now.plusMinutes(ACTIVATE_ACCOUNT_TOKEN_LIFE_DURATION_MINUTES);
        } else
        {
            throw new InvalidTokenException(INVALID_TOKEN);
        }
    }

    public boolean matchTokenPurpose(TokenPurpose purpose)
    {
        return this.purpose.equals(purpose);
    }
}
