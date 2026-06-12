package com.luketran.identity.infrastructure.persistence.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@MappedSuperclass
public abstract class BaseJpaEntity {

      @Id
      @GeneratedValue(strategy = GenerationType.UUID)
      @Column(name = "id")
      private UUID id;

      @Column(name = "created_at", nullable = false, updatable = false)
      private LocalDateTime createdAt;

      @Column(name = "deleted_at")
      private LocalDateTime deletedAt;

      @PrePersist
      protected void onCreate(){
          if(createdAt == null){
              createdAt = LocalDateTime.now();
          }
      }
}
