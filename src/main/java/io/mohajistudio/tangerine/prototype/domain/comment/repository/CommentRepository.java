package io.mohajistudio.tangerine.prototype.domain.comment.repository;

import io.mohajistudio.tangerine.prototype.domain.comment.domain.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    @Query("SELECT MAX(c.groupNumber) FROM Comment c WHERE c.post.id = :postId")
    Integer findMaxGroupNumberByPostId(@Param("postId") Long postId);

    @Query("SELECT c.groupNumber FROM Comment c WHERE c.id = :id")
    Integer findGroupNumberById(@Param("id") Long id);

    @Query("SELECT c FROM Comment c " +
            "left join fetch c.member m " +
            "left join fetch m.memberProfile mp " +
            "WHERE c.post.id = :postId " +
            "ORDER BY c.groupNumber")
    Page<Comment> findByPostId(@Param("postId") Long postId, Pageable pageable);
}
