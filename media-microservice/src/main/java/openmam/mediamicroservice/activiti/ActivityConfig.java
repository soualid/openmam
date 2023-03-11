package openmam.mediamicroservice.activiti;

import jakarta.persistence.EntityManagerFactory;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.spring.ProcessEngineFactoryBean;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.Map;

@Configuration
public class ActivityConfig {

    @Primary
    @Bean(name="mainProps")
    @ConfigurationProperties("spring.datasource")
    public DataSourceProperties dataSourceProperties() {
        return new DataSourceProperties();
    }

    @Primary
    @Bean(name="datasource")
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSource datasource(@Qualifier("mainProps") DataSourceProperties properties){
        return properties.initializeDataSourceBuilder().build();
    }

    @Primary
    @Bean(name="entityManagerFactory")
    public LocalContainerEntityManagerFactoryBean entityManagerFactoryBean
            (EntityManagerFactoryBuilder builder,
             @Qualifier("datasource") DataSource dataSource){
        return builder.dataSource(dataSource)
                .packages("openmam")
                .persistenceUnit("openmam")
                .properties(Map.of(
                        "hibernate.physical_naming_strategy",
                        "org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy",
                        "hibernate.hbm2ddl.auto",
                        "update"
                ))
                .build();
    }

    @Primary
    @Bean(name = "transactionManager")
    @ConfigurationProperties("spring.jpa")
    public PlatformTransactionManager transactionManager(
            @Qualifier("entityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }

    @ConditionalOnProperty(value="activiti.enabled", havingValue="true")
    @Bean(name="activitiDatasource")
    @ConfigurationProperties(prefix="spring.datasource.activiti")
    public DataSource activitiDatasource() {
        return DataSourceBuilder.create().build();
    }

    @ConditionalOnProperty(value="activiti.enabled", havingValue="true")
    @Bean(name="activitiTransactionManager")
    public DataSourceTransactionManager getActivitiTransactionManager() {
        return new DataSourceTransactionManager(activitiDatasource());
    }


    @ConditionalOnProperty(value="activiti.enabled", havingValue="true")
    @Bean
    public ProcessEngineConfigurationImpl getProcessEngineConfiguration() {
        SpringProcessEngineConfiguration res = new SpringProcessEngineConfiguration();
        res.setDatabaseSchemaUpdate("true");
        res.setDataSource(activitiDatasource());
        res.setTransactionManager(getActivitiTransactionManager());
        return res;
    }

    @ConditionalOnProperty(value="activiti.enabled", havingValue="true")
    @Bean
    public ProcessEngineFactoryBean getProcessEngine() {
        ProcessEngineFactoryBean res = new ProcessEngineFactoryBean();
        res.setProcessEngineConfiguration(getProcessEngineConfiguration());
        return res;
    }

}
