package cn.yyzmain.h2.entity;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.persistence.Table;

@Data
@Table(name = "Student")
@Accessors(chain = true)
public class Student {

    private Integer studentId;

    private String studentName;

    private Integer gender;

    private Integer age;

}