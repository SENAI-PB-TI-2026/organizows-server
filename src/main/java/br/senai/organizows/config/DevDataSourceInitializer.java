package br.senai.organizows.config;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.StringUtils;

public class DevDataSourceInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext context) {
        var environment = context.getEnvironment();
        if (environment.matchesProfiles("dev")
                && !StringUtils.hasText(environment.getProperty("spring.datasource.url"))) {
            throw new IllegalStateException(
                    "A URL JDBC do perfil dev está vazia. Preencha DEV_DB_URL (spring.datasource.url) "
                            + "ou remova a definição vazia para usar o H2 padrão."
            );
        }
    }
}
