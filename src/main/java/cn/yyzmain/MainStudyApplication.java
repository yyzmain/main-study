package cn.yyzmain;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan(basePackages = {"cn.yyzmain"})
public class MainStudyApplication {

    public static void main(String[] args) {
        SpringApplication.run(MainStudyApplication.class, args);
    }

}
