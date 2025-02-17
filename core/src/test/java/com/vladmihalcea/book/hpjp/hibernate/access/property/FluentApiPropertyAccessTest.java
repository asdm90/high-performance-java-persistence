package com.vladmihalcea.book.hpjp.hibernate.access.property;

import com.vladmihalcea.book.hpjp.util.AbstractTest;
import org.junit.Test;

import jakarta.persistence.*;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Vlad Mihalcea
 */
public class FluentApiPropertyAccessTest extends AbstractTest {

    @Override
    protected Class<?>[] entities() {
        return new Class<?>[] {
            Post.class,
            PostComment.class
        };
    }

    @Test
    public void testLifecycle() {
        doInJPA(entityManager -> {
            Post post = new Post()
            .id(1L)
            .title("High-Performance Java Persistence")
            .addComment(new PostComment()
                .review("Awesome book")
                .createdOn(Timestamp.from(
                    LocalDateTime.now().minusDays(1).toInstant(ZoneOffset.UTC))
                )
            )
            .addComment(new PostComment()
                .review("High-Performance Rocks!")
                .createdOn(Timestamp.from(
                    LocalDateTime.now().minusDays(2).toInstant(ZoneOffset.UTC))
                )
            )
            .addComment(new PostComment()
                .review("Database essentials to the rescue!")
                .createdOn(Timestamp.from(
                    LocalDateTime.now().minusDays(3).toInstant(ZoneOffset.UTC))
                )
            );
            entityManager.persist(post);
        });
        doInJPA(entityManager -> {
            Post post = entityManager.find(Post.class, 1L);
            assertEquals(3, post.getComments().size());
        });
    }

    @Entity(name = "Post")
    @Table(name = "post")
    public static class Post {

        private Long id;

        private String title;

        private List<PostComment> comments = new ArrayList<>();

        public Post() {}

        public Post(String title) {
            this.title = title;
        }

        public Post id(Long id) {
            this.id = id;
            return this;
        }

        @Id
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

        public Post title(String title) {
            this.title = title;
            return this;
        }

        @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "post")
        public List<PostComment> getComments() {
            return comments;
        }

        public void setComments(List<PostComment> comments) {
            this.comments = comments;
        }

        public Post addComment(PostComment comment) {
            comments.add(comment.post(this));
            return this;
        }
    }

    @Entity(name = "PostComment")
    @Table(name = "post_comment")
    public static class PostComment {

        private Long id;

        private String review;

        private Date createdOn;

        private Post post;

        @Id
        @GeneratedValue
        public Long getId() {
            return id;
        }

        public PostComment setId(Long id) {
            this.id = id;
            return this;
        }

        public String getReview() {
            return review;
        }

        public void setReview(String review) {
            this.review = review;
        }

        public PostComment review(String review) {
            this.review = review;
            return this;
        }

        public Date getCreatedOn() {
            return createdOn;
        }

        public void setCreatedOn(Date createdOn) {
            this.createdOn = createdOn;
        }

        public PostComment createdOn(Date createdOn) {
            this.createdOn = createdOn;
            return this;
        }

        @ManyToOne(fetch = FetchType.LAZY)
        public Post getPost() {
            return post;
        }

        public void setPost(Post post) {
            this.post = post;
        }

        public PostComment post(Post post) {
            this.post = post;
            return this;
        }
    }
}
