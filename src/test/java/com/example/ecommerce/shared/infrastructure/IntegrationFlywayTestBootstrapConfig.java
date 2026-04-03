package com.example.ecommerce.shared.infrastructure;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

@TestConfiguration(proxyBeanMethods = false)
@Profile("test")
public class IntegrationFlywayTestBootstrapConfig {

    private static final Logger log = LoggerFactory.getLogger(IntegrationFlywayTestBootstrapConfig.class);

    @Bean
    InitializingBean integrationFlywayAssurance(Flyway flyway, DataSource dataSource) {
        return () -> {
            flyway.migrate();

            MigrationInfo[] applied = flyway.info().applied();
            String latestVersion = applied.length == 0
                    ? "none"
                    : String.valueOf(applied[applied.length - 1].getVersion());

            String activeSchema = resolveCurrentSchema(dataSource);
            boolean cartItemsExists = tableExistsInSchema(dataSource, activeSchema, "cart_items");

            log.info(
                    "IT bootstrap diagnostics: activeSchema={}, flywayAppliedCount={}, latestVersion={}, cartItemsExists={}",
                    activeSchema,
                    applied.length,
                    latestVersion,
                    cartItemsExists
            );
        };
    }

    @Bean
    static BeanFactoryPostProcessor integrationFlywayOrderingPostProcessor() {
        return IntegrationFlywayTestBootstrapConfig::ensureEntityManagerDependsOnAssurance;
    }

    private static void ensureEntityManagerDependsOnAssurance(ConfigurableListableBeanFactory beanFactory) {
        if (!beanFactory.containsBeanDefinition("entityManagerFactory")) {
            return;
        }

        String[] existingDependsOn = beanFactory.getBeanDefinition("entityManagerFactory").getDependsOn();
        String[] mergedDependsOn;
        if (existingDependsOn == null || existingDependsOn.length == 0) {
            mergedDependsOn = new String[]{"integrationFlywayAssurance"};
        } else if (Arrays.asList(existingDependsOn).contains("integrationFlywayAssurance")) {
            return;
        } else {
            mergedDependsOn = Arrays.copyOf(existingDependsOn, existingDependsOn.length + 1);
            mergedDependsOn[existingDependsOn.length] = "integrationFlywayAssurance";
        }

        beanFactory.getBeanDefinition("entityManagerFactory").setDependsOn(mergedDependsOn);
    }

    private String resolveCurrentSchema(DataSource dataSource) {
        String sql = "select current_schema()";
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()
        ) {
            if (resultSet.next()) {
                return resultSet.getString(1);
            }
            return "unknown";
        } catch (SQLException ex) {
            return "unresolved(" + ex.getClass().getSimpleName() + ")";
        }
    }

    private boolean tableExistsInSchema(DataSource dataSource, String schema, String tableName) {
        String sql = """
                select exists (
                    select 1
                    from information_schema.tables
                    where table_schema = ?
                      and table_name = ?
                )
                """;
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, schema);
            statement.setString(2, tableName);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getBoolean(1);
                }
                return false;
            }
        } catch (SQLException ex) {
            return false;
        }
    }
}
