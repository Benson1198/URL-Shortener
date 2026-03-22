package com.urlshortener.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name="urls")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Url {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="original_url", nullable= false, length=2048)
    private String originalUrl;

    @Column(name="short_code",unique=true,nullable=false,length=10)
    private String shortCode;

    @Column(name="custom_alias", length=50)
    private String customAlias;

    @Column(name="click_count")
    private Long clickCount=0L;

    @Column(name="expires_at")
    private LocalDateTime expiresAt;

    @Column(name="created_at",updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate(){
        createdAt = LocalDateTime.now();
        if(clickCount == null) clickCount = 0L;
    }
}
