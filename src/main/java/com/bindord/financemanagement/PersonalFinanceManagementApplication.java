package com.bindord.financemanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class PersonalFinanceManagementApplication {

  public static void main(String[] args) {
    SpringApplication.run(PersonalFinanceManagementApplication.class, args);
  }

}
