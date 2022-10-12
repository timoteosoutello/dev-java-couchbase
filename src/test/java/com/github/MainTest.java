package com.github;

import java.util.Arrays;

import org.junit.After;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.couchbase.BucketDefinition;
import org.testcontainers.couchbase.CouchbaseContainer;

import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.query.QueryOptions;
import com.couchbase.client.java.query.QueryResult;

@SuppressWarnings("resource")
@ExtendWith(SpringExtension.class)
@SpringBootTest
@MockitoSettings(strictness = Strictness.LENIENT)
public class MainTest {

	private final static String COUCHBASE_CONNECTION_STRING = "localhost";
	private final static String COUCHBASE_USERNAME = "admin";
	private final static String COUCHBASE_PASSWORD = "test123";
	private final static String COUCHBASE_BUCKET_NAME = "default";
	private final static String COUCHBASE_CONTAINER_BASE_IMAGE = "couchbase/server";
	private static CouchbaseContainer container;
	private static Cluster cluster;

	static {
		BucketDefinition bucketDefinition = new BucketDefinition(COUCHBASE_BUCKET_NAME).withPrimaryIndex(true);
		container = new CouchbaseContainer(COUCHBASE_CONTAINER_BASE_IMAGE)
				.withCredentials(COUCHBASE_USERNAME, COUCHBASE_PASSWORD)
				.withExposedPorts(8091, 8092, 8093, 8094, 8095, 52623, 52659).withBucket(bucketDefinition);
		container.setPortBindings(Arrays.asList("8091:8091", "8092:8092", "8093:8093", "8094:8094", "8095:8095",
				"52623:52623", "52659:52659", "11210:11210"));
		container.start();
		cluster = Cluster.connect(COUCHBASE_CONNECTION_STRING, COUCHBASE_USERNAME, COUCHBASE_PASSWORD);
	}

	@SuppressWarnings("unused")
	@Test
	public void dummyTest() {
		QueryResult result;
        // Create Scope.
        String query = "CREATE SCOPE `default`.world_management;";
        result = cluster.query(query,QueryOptions.queryOptions().metrics(true));
        // Create Collection.
        query = "CREATE SCOPE `default`.world_management;";
        result = cluster.query(query,QueryOptions.queryOptions().metrics(true));
        // Create Primary Index
        query = "CREATE PRIMARY INDEX idx_default_primary ON `default`.`world_management`.countries(`key`) USING GSI;";
        result = cluster.query(query,QueryOptions.queryOptions().metrics(true));
        // Create Row.
        query = "INSERT INTO `default`.`world_management`.countries ( KEY, VALUE ) VALUES (\"1\", \"Brazil\");";
        result = cluster.query(query,QueryOptions.queryOptions().metrics(true));
        // Query it.
        query = "SELECT * FROM `default`.`world_management`.countries WHERE ID  = 1;";
        result = cluster.query(query,QueryOptions.queryOptions().metrics(true));
        // Delete it.
        query = "DELETE FROM ``default`.`world_management`.countries WHERE ID = 1";
        result = cluster.query(query,QueryOptions.queryOptions().metrics(true));
        // Drop Collection.
        query = "DROP COLLECTION `default`.world_management.countries;";
        result = cluster.query(query,QueryOptions.queryOptions().metrics(true));
        // Delete Scope.
        query = "DELETE SCOPE `default`.world_management;";
        result = cluster.query(query,QueryOptions.queryOptions().metrics(true));;
		assert true;
	}

	@After
	public void destroy() {
		cluster.disconnect();
		container.stop();
	}
}
