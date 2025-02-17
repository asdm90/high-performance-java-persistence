package com.vladmihalcea.book.hpjp.spring.data.lock.repository;

import com.vladmihalcea.book.hpjp.spring.data.lock.domain.Post;

import jakarta.persistence.LockModeType;

/**
 * @author Vlad Mihalcea
 */
public interface CustomPostRepository {

    Post lockById(Long id, LockModeType lockMode);
}
