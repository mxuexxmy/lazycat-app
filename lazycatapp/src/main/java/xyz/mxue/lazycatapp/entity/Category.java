package xyz.mxue.lazycatapp.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "t_category")
public class Category {
    @Id
    private Long id;
    private String name;
    private String icon;
    private String englishName;
} 