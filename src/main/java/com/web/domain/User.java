package com.web.domain;

import com.web.domain.enums.SocialType;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@Entity
@Table
public class User implements Serializable {

    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    @Column
    private String name;

    @Column
    private String password;

    @Column
    private String email;
    
    @Column
    private String principal;   // OAuth2 인증으로 제공 받은 키 값
    
    @Column
    private SocialType socialType;      // 어떤 소셜 미디어로 인증 받았는지 여부
    
    @Column
    private LocalDateTime createdDate;

    @Column
    private LocalDateTime updateDate;

    @Builder
    public User(String name, String password, String email, String principal, SocialType socialType, LocalDateTime createdDate, LocalDateTime updateDate) {
        this.name = name;
        this.password = password;
        this.email = email;
        this.principal = principal;
        this.socialType = socialType;
        this.createdDate = createdDate;
        this.updateDate = updateDate;
    }


}
