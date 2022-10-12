package com.github;

import static org.junit.Assert.assertTrue;

import java.time.Duration;
import java.util.Arrays;

import org.junit.AfterClass;
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
import com.couchbase.client.java.query.QueryStatus;

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
				.withExposedPorts(8091, 8092, 8093, 8094, 8095, 8096, 9100, 9101, 9102, 9103, 9104, 9105, 52623, 52659, 9119,
						9999, 21200, 21100, 21150, 21250, 21300, 21350)
				.withBucket(bucketDefinition).withStartupAttempts(2).withStartupTimeout(Duration.ofMinutes(2));
		container.setPortBindings(
				Arrays.asList("8091:8091", "8092:8092", "8093:8093", "8094:8094", "8095:8095", "8096:8096", "9100:9100",
						"9101:9101", "9102:9102", "9103:9103", "9104:9104", "9105:9105", "52623:52623", "52659:52659", "11210:11210",
						"9119:9119", "9999:9999", "21200:21200", "21250:21250", "21300:21300", "21350:21350", "21100:21100", "21150:21150"));
		container.start();
		cluster = Cluster.connect(COUCHBASE_CONNECTION_STRING, COUCHBASE_USERNAME, COUCHBASE_PASSWORD);
	}

	@SuppressWarnings("unused")
	@Test
	public void dummyTest() throws InterruptedException {
		QueryResult result;
		// Create Scope.
		String query = "CREATE SCOPE `default`.world_management;";
		result = cluster.query(query, QueryOptions.queryOptions().metrics(true));
		assertTrue(result.metaData().status() == QueryStatus.SUCCESS);
		// Create Collection.
		query = "CREATE COLLECTION `default`.world_management.countries;";
		result = cluster.query(query, QueryOptions.queryOptions().metrics(true));
		assertTrue(result.metaData().status() == QueryStatus.SUCCESS);
        // Avoid issue -> Keyspace not found in CB datastore
        Thread.sleep(10000);
		// Create Primary Index
		query = "CREATE PRIMARY INDEX idx_default_primary ON `default`.`world_management`.countries USING GSI;";
		result = cluster.query(query, QueryOptions.queryOptions().metrics(true));
		assertTrue(result.metaData().status() == QueryStatus.SUCCESS);
		// Create Row.
		query = "INSERT INTO `default`.`world_management`.countries ( KEY, VALUE ) VALUES (\"1\", \"Brazil\");";
		result = cluster.query(query);
		assertTrue(result.metaData().status() == QueryStatus.SUCCESS);
		// Query it.
		query = "SELECT * FROM `default`.`world_management`.countries WHERE ID  = 1;";
		result = cluster.query(query);
		assertTrue(result.metaData().status() == QueryStatus.SUCCESS);
		// Delete it.
		query = "DELETE FROM `default`.`world_management`.countries WHERE ID = 1";
		result = cluster.query(query, QueryOptions.queryOptions().metrics(true));
		assertTrue(result.metaData().status() == QueryStatus.SUCCESS);
		// Drop Collection.
		query = "DROP COLLECTION `default`.world_management.countries;";
		result = cluster.query(query, QueryOptions.queryOptions().metrics(true));
		assertTrue(result.metaData().status() == QueryStatus.SUCCESS);
		// Delete Scope.
		query = "DROP SCOPE `default`.world_management;";
		result = cluster.query(query, QueryOptions.queryOptions().metrics(true));
		assertTrue(result.metaData().status() == QueryStatus.SUCCESS);
	}

	@AfterClass
	public void destroy() {
		cluster.disconnect();
		container.stop();
	}
}
