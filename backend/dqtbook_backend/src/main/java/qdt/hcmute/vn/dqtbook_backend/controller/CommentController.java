package qdt.hcmute.vn.dqtbook_backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import qdt.hcmute.vn.dqtbook_backend.model.Comment;
import qdt.hcmute.vn.dqtbook_backend.service.CommentService;

import java.util.List;

@RestController
@RequestMapping("/api/posts/{postId}/comments")
public class CommentController {
    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping
    public List<Comment> list(@PathVariable Integer postId) {
        return commentService.getCommentsForPost(postId);
    }

    @PostMapping
    public ResponseEntity<Comment> create(@PathVariable Integer postId, @RequestBody Comment comment) {
        Comment created = commentService.createComment(postId, comment).get();
        return ResponseEntity.created(null).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer postId, @PathVariable Integer id, @RequestBody Comment comment) {
        try {
            var updatedOpt = commentService.updateComment(postId, id, comment);
            if (updatedOpt.isPresent()) return ResponseEntity.ok(updatedOpt.get());
            return ResponseEntity.status(404).body(new qdt.hcmute.vn.dqtbook_backend.dto.ErrorResponse(404, "Comment not found for this post"));
        } catch (org.springframework.web.server.ResponseStatusException rse) {
            return ResponseEntity.status(rse.getStatusCode()).body(new qdt.hcmute.vn.dqtbook_backend.dto.ErrorResponse(rse.getStatusCode().value(), rse.getReason()));
        } catch (IllegalArgumentException iae) {
            return ResponseEntity.status(400).body(new qdt.hcmute.vn.dqtbook_backend.dto.ErrorResponse(400, iae.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(new qdt.hcmute.vn.dqtbook_backend.dto.ErrorResponse(500, "Internal server error"));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer postId, @PathVariable Integer id) {
        try {
            commentService.deleteComment(postId, id);
            return ResponseEntity.noContent().build();
        } catch (org.springframework.web.server.ResponseStatusException rse) {
            return ResponseEntity.status(rse.getStatusCode()).body(new qdt.hcmute.vn.dqtbook_backend.dto.ErrorResponse(rse.getStatusCode().value(), rse.getReason()));
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(new qdt.hcmute.vn.dqtbook_backend.dto.ErrorResponse(500, "Internal server error"));
        }
    }
}
