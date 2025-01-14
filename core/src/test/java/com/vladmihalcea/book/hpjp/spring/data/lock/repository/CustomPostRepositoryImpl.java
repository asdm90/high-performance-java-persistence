package com.vladmihalcea.book.hpjp.spring.data.lock.repository;

import com.vladmihalcea.book.hpjp.spring.data.lock.domain.Post;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;

/**
 * @author Vlad Mihalcea
 */
public class CustomPostRepositoryImpl implements CustomPostRepository {
    
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Post lockById(Long id, LockModeType lockMode) {
        return entityManager.find(Post.class, id, lockMode);
    }
}
