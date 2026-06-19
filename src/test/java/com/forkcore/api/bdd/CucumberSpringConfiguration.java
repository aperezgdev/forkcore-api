package com.forkcore.api.bdd;

import com.forkcore.api.PostgresContainerTestConfiguration;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(OrderBddTestConfiguration.class)
public class CucumberSpringConfiguration extends PostgresContainerTestConfiguration {
}
