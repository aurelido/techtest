package com.aabanegas.payment.configuration;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;
import org.cassandraunit.CQLDataLoader;
import org.cassandraunit.dataset.cql.ClassPathCQLDataSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.annotation.PostConstruct;

@ConditionalOnProperty(prefix = "cassandra", name = "enabled")
@Configuration
@EnableConfigurationProperties(CassandraProperties.class)
public class CassandraConfiguration {

    private final CassandraProperties cassandraProperties;

    @Autowired
    public CassandraConfiguration(CassandraProperties cassandraProperties) {
        this.cassandraProperties = cassandraProperties;
    }

    @Bean
    Cluster cluster() {
        return Cluster
                .builder()
                .addContactPoint(cassandraProperties.getContactPoints())
                .withPort(cassandraProperties.getPort())
                .build();
    }

    @Bean
    Session session() {
        Session session;

        if (cassandraProperties.isCreateKeyspace()) {
            session = cluster().connect();
            ResultSet resultSet = session.execute("CREATE KEYSPACE IF NOT EXISTS " + cassandraProperties.getKeyspaceName()
                    + " WITH replication = {'class': '" + cassandraProperties.getKeyspaceReplicationClass()
                    + "', 'replication_factor': '" + cassandraProperties.getKeyspaceReplicationFactor() + "'}");

            if (resultSet.wasApplied()) {
                session.execute("USE " + cassandraProperties.getKeyspaceName());
            }
        } else {
            session = cluster().connect(cassandraProperties.getKeyspaceName());
        }

        return session;
    }

    //TODO: Not ideal, shouldn't have Cassandra Unit as a Runtime dependency, but current no other way to initialize Cassandra on Docker
    @Profile({"docker"})
    @Configuration
    @ConditionalOnProperty(prefix = "cassandra", name = "enabled")
    static class CassandraInitializationConfiguration {

        private final Session session;

        @Autowired
        public CassandraInitializationConfiguration(Session session) {
            this.session = session;
        }

        @PostConstruct
        void initializeCassandra() {
            CQLDataLoader cqlDataLoader = new CQLDataLoader(session);
            cqlDataLoader.load(new ClassPathCQLDataSet("create-schema.cql"));
        }
    }

}
