package com.pharma.drugverification;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = { DrugVerificationApplication.class, com.pharma.drugverification.config.TestConfig.class })
class DrugVerificationApplicationTests {

    @Test
    void contextLoads() {
    }
}
