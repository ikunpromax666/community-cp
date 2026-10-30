package com.community.communitycp;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@MapperScan("com.community.communitycp.**.mapper")
public class CommunityCpApplication {

    public static void main(String[] args) {
        SpringApplication.run(CommunityCpApplication.class, args);
    }

}
