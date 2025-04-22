package xyz.mxue.lazycatapp.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.TransactionManagementConfigurer;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionDefinition;

@Configuration
@EnableJpaRepositories(basePackages = "xyz.mxue.lazycatapp.repository")
@EnableTransactionManagement
public class JpaConfig implements TransactionManagementConfigurer {

    private final PlatformTransactionManager transactionManager;

    public JpaConfig(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    @Override
    public PlatformTransactionManager annotationDrivenTransactionManager() {
        return transactionManager;
    }

    public DefaultTransactionDefinition getTransactionDefinition() {
        DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
        // 禁用只读事务
        definition.setReadOnly(false);
        return definition;
    }
} 