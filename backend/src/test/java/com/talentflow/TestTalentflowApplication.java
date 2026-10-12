package com.talentflow;

import org.springframework.boot.SpringApplication;

/**
 * Runs the application locally against a throwaway PostgreSQL container.
 */
public class TestTalentflowApplication {

    public static void main(String[] args) {
        SpringApplication.from(TalentflowApplication::main).with(TestcontainersConfiguration.class).run(args);
    }
}
