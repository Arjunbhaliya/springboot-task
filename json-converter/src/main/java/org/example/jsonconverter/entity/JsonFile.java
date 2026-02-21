package org.example.jsonconverter.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class JsonFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50 ,unique = true )
    private String fileName;

    @CreationTimestamp
    private LocalDateTime uploadedTime;

    @Column(nullable = false)
    private String status;
}
