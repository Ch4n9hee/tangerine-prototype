package io.mohajistudio.tangerine.prototype.domain.post.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.mohajistudio.tangerine.prototype.domain.comment.domain.Comment;
import io.mohajistudio.tangerine.prototype.domain.member.domain.Member;
import io.mohajistudio.tangerine.prototype.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.Set;

@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Table(name = "post")
public class Post extends BaseEntity {
    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private LocalDate visitedAt;

    @Column(nullable = false)
    private int commentCnt = 0;

    @Column(nullable = false)
    private int favoriteCnt = 0;

    private short blockCnt = 0;

    //대표지역
    private  String province;//광역시/도
    private  String city;//시/군/구
    private  String district;//읍/면/동

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

//    @OneToOne(mappedBy = "post", fetch = FetchType.LAZY)
//    private TrendingPost trendingPost;

    @JsonIgnore
    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY)
    private Set<ScrapPost> scrapPosts;

    @JsonIgnore
    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY)
    private Set<FavoritePost> favoritePosts;

    @JsonIgnore
    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY)
    private Set<Comment> comments;

    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY)
    private Set<TextBlock> textBlocks;

    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY)
    private Set<PlaceBlock> placeBlocks;

    public void setPlaceBlocks(Set<PlaceBlock> placeBlocks) {
        this.placeBlocks = placeBlocks;
        blockCnt += (short) placeBlocks.size();
        placeBlocks.forEach(placeBlock -> placeBlock.setPost(this));
    }

    public void setTextBlocks(Set<TextBlock> textBlocks) {
        this.textBlocks = textBlocks;
        blockCnt += (short) textBlocks.size();
        textBlocks.forEach(textBlock -> textBlock.setPost(this));
    }
}
