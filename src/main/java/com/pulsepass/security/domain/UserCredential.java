package com.pulsepass.security.domain;

import com.pulsepass.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "user_credentials")
public class UserCredential {
    @Id
    @Column(name = "user_id")
    private Long userId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Column(name = "password_changed_at", nullable = false)
    private Instant passwordChangedAt = Instant.now();

    protected UserCredential() { }

    public UserCredential(User user, String passwordHash) {
        this.user = user;
        this.passwordHash = passwordHash;
        this.passwordChangedAt = Instant.now();
    }

    public Long getUserId() { return userId; }
    public User getUser() { return user; }
    public String getPasswordHash() { return passwordHash; }
    public Instant getPasswordChangedAt() { return passwordChangedAt; }
}
