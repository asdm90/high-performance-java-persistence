package com.vladmihalcea.book.hpjp.spring.transaction.jta.dao;

import com.vladmihalcea.book.hpjp.hibernate.transaction.forum.Tag;

import java.util.List;

/**
 * @author Vlad Mihalcea
 */
public interface TagDAO extends GenericDAO<Tag, Long> {

    List<Tag> findByName(String... tags);
}
