package com.vladmihalcea.book.hpjp.hibernate.fetching.maxrows;

import com.vladmihalcea.book.hpjp.util.AbstractMySQLIntegrationTest;
import com.vladmihalcea.book.hpjp.util.AbstractPostgreSQLIntegrationTest;
import org.hibernate.Session;
import org.hibernate.annotations.CreationTimestamp;
import org.junit.Test;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Date;
import java.util.Properties;
import java.util.stream.LongStream;

import static org.junit.Assert.assertEquals;

/**
 * @author Vlad Mihalcea
 */
public class MySQLSetMaxRowsTest extends AbstractMySQLIntegrationTest {

    @Override
    protected Class<?>[] entities() {
        return new Class[]{
            Post.class
        };
    }

    @Override
    public void afterInit() {
        doInJPA(entityManager -> {
            entityManager.unwrap(Session.class).doWork(connection -> {
                try (Statement statement = connection.createStatement()) {
                    statement.execute("CREATE INDEX idx_post_created_on ON post (created_on DESC)");
                }
            });
            LongStream.range(0, 50 * 100).forEach(i -> {
                Post post = new Post(i);
                post.setTitle(String.format("Post nr. %d", i));
                entityManager.persist(post);
                if (i % 50 == 0 && i > 0) {
                    entityManager.flush();
                    entityManager.clear();
                }
            });
        });
    }

    @Override
    protected void additionalProperties(Properties properties) {
        properties.put("hibernate.jdbc.batch_size", "50");
        properties.put("hibernate.order_inserts", "true");
        properties.put("hibernate.order_updates", "true");
    }

    @Test
    public void testSetMaxSize() {
        doInJPA(entityManager -> {

            entityManager.unwrap(Session.class).doWork(connection -> {
                try(PreparedStatement statement = connection.prepareStatement("""
                    EXPLAIN ANALYZE
                    SELECT p.title
                    FROM post p
                    ORDER BY p.created_on DESC
                    """
                )) {
                    statement.setMaxRows(50);
                    ResultSet resultSet = statement.executeQuery();

                    while (resultSet.next()) {
                        String executionPlanLines = resultSet.getString(1);
                        LOGGER.info("Execution plan: {}{}",
                            System.lineSeparator(),
                            executionPlanLines
                        );
                    }
                }
            });
        });
    }

    @Test
    public void testLimit() {
        doInJPA(entityManager -> {

            entityManager.unwrap(Session.class).doWork(connection -> {
                try(PreparedStatement statement = connection.prepareStatement("""
                    EXPLAIN ANALYZE
                    SELECT p.title
                    FROM post p
                    ORDER BY p.created_on DESC
                    LIMIT 50
                    """
                )) {
                    ResultSet resultSet = statement.executeQuery();

                    while (resultSet.next()) {
                        String executionPlanLines = resultSet.getString(1);
                        LOGGER.info("Execution plan: {}{}",
                            System.lineSeparator(),
                            executionPlanLines
                        );
                    }
                }
            });
        });
    }

    @Entity(name = "Post")
    @Table(name = "post")
    public static class Post {

        @Id
        private Long id;

        private String title;

        @Column(name = "created_on")
        @CreationTimestamp
        private Date createdOn;

        public Post() {
        }

        public Post(Long id) {
            this.id = id;
        }

        public Post(String title) {
            this.title = title;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }
    }
}
