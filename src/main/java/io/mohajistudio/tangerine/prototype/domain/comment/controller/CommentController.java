package io.mohajistudio.tangerine.prototype.domain.comment.controller;

import io.mohajistudio.tangerine.prototype.domain.comment.domain.Comment;
import io.mohajistudio.tangerine.prototype.domain.comment.domain.FavoriteComment;
import io.mohajistudio.tangerine.prototype.domain.comment.dto.CommentDTO;
import io.mohajistudio.tangerine.prototype.domain.comment.dto.FavoriteCommentDTO;
import io.mohajistudio.tangerine.prototype.domain.comment.mapper.CommentMapper;
import io.mohajistudio.tangerine.prototype.domain.comment.service.CommentService;
import io.mohajistudio.tangerine.prototype.global.auth.domain.SecurityMemberDTO;
import io.mohajistudio.tangerine.prototype.global.common.PageableParam;
import io.mohajistudio.tangerine.prototype.global.enums.ErrorCode;
import io.mohajistudio.tangerine.prototype.global.error.exception.BusinessException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/posts/{postId}/comments")
@RequiredArgsConstructor
@Tag(name = "Comment", description = "Comment API")
public class CommentController {
    private final CommentService commentService;
    private final CommentMapper commentMapper;

    @GetMapping
    @Operation(summary = "페이징 된 댓글 목록 조회", description = "page와 size 값을 넘기면 페이징 된 댓글 목록을 조회합니다. 기본 값은 page는 1, size는 10 입니다.")
    public Page<CommentDTO.Details> commentListByPage(@PathVariable(name = "postId") Long postId, @ModelAttribute PageableParam pageableParam) {
        Pageable pageable = PageRequest.of(pageableParam.getPage(), pageableParam.getSize());

        Page<Comment> commentListByPage = commentService.findCommentListByPage(postId, pageable, pageableParam.getSort());
        return commentListByPage.map(commentMapper::toDTO);
    }


    @GetMapping("/{id}")
    public CommentDTO.Details commentDetails(@PathVariable(name = "postId") Long postId, @PathVariable(name = "id") Long id) {
        return commentMapper.toDTO(commentService.findComment(postId, id));
    }

    @GetMapping("/{id}/replies")
    public Page<CommentDTO.Details> replyCommentListByPage(@PathVariable(name = "postId") Long postId, @PathVariable(name = "id") Long id, @ModelAttribute PageableParam pageableParam) {
        Pageable pageable = PageRequest.of(pageableParam.getPage(), pageableParam.getSize());

        Page<Comment> commentListByPage = commentService.findReplyCommentListBypage(postId, id, pageable, pageableParam.getSort());
        return commentListByPage.map(commentMapper::toDTO);
    }

    @PostMapping
    @Operation(summary = "댓글 추가", description = "댓글 형식에 맞게 데이터를 전달해주세요.")
    public CommentDTO.Details commentAdd(@RequestBody @Valid CommentDTO.Add commentAddDTO, @PathVariable(name = "postId") Long postId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        SecurityMemberDTO securityMember = (SecurityMemberDTO) authentication.getPrincipal();

        Comment comment = commentService.AddComment(commentMapper.toEntity(commentAddDTO), postId, securityMember.getId());
        return commentMapper.toDTO(comment);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "댓글 수정", description = "댓글 형식에 맞게 데이터를 전달해주세요.")
    public void commentModify(@RequestBody @Valid CommentDTO.Patch commentPatchDTO, @PathVariable(name = "postId") Long postId, @PathVariable(name = "id") Long id, @AuthenticationPrincipal SecurityMemberDTO securityMemberDTO) {
        if (!Objects.equals(id, commentPatchDTO.getId())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        commentService.modifyComment(commentMapper.toEntity(commentPatchDTO), postId, securityMemberDTO.getId());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "댓글 삭제", description = "댓글을 삭제합니다.")
    public void commentDelete(@PathVariable(name = "postId") Long postId, @PathVariable(name = "id") Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        SecurityMemberDTO securityMember = (SecurityMemberDTO) authentication.getPrincipal();

        commentService.deleteComment(id, postId, securityMember.getId());
    }

    @PatchMapping("/{id}/favorites")
    @Operation(summary = "좋아하는 댓글 추가/삭제", description = "좋아하는 댓글을 추가 또는 삭제합니다.")
    public void favoriteCommentModify(@PathVariable(name = "postId") Long postId, @PathVariable(name = "id") Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        SecurityMemberDTO securityMember = (SecurityMemberDTO) authentication.getPrincipal();

        commentService.modifyFavoriteComment(id, postId, securityMember.getId());
    }

    @GetMapping("/favorites")
    @Operation(summary = "게시글에 등록된 좋아하는 댓글 목록 조회", description = "게시글에 등록된 좋아하는 댓글 목록을 조회합니다.")
    public Set<FavoriteCommentDTO> favoriteCommentList(@PathVariable(name = "postId") Long postId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication.getPrincipal() == "anonymousUser") {
            return new HashSet<>();
        }
        SecurityMemberDTO securityMember = (SecurityMemberDTO) authentication.getPrincipal();

        Set<FavoriteComment> favoriteCommentListAtPost = commentService.findFavoriteCommentListAtPost(postId, securityMember.getId());
        return favoriteCommentListAtPost.stream().map(commentMapper::toDTO).collect(Collectors.toSet());
    }
}
