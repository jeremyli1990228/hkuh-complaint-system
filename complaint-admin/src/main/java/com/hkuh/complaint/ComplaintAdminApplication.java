package com.hkuh.complaint;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.hkuh.complaint")
public class ComplaintAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(ComplaintAdminApplication.class, args);
    }

}
