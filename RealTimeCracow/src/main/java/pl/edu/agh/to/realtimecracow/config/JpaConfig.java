package pl.edu.agh.to.realtimecracow.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJpaRepositories(basePackages = "pl.edu.agh.to.realtimecracow.repository")
@EnableTransactionManagement
public class JpaConfig {
}
