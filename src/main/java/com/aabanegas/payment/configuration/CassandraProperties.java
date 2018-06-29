package com.aabanegas.payment.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "cassandra")
public class CassandraProperties {

    private String contactPoints = "localhost";

    private int port = 9042;

    private String keyspaceName;

    private boolean createKeyspace = false;

    private String keyspaceReplicationClass = "SimpleStrategy";

    private int keyspaceReplicationFactor = 1;

    private boolean enabled;
}
