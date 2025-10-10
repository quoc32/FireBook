package qdt.hcmute.vn.dqtbook_backend.service;

import org.springframework.stereotype.Service;
import qdt.hcmute.vn.dqtbook_backend.model.Comment;
import qdt.hcmute.vn.dqtbook_backend.model.Post;
import qdt.hcmute.vn.dqtbook_backend.model.User;
import qdt.hcmute.vn.dqtbook_backend.repository.CommentRepository;
import qdt.hcmute.vn.dqtbook_backend.repository.PostRepository;
import qdt.hcmute.vn.dqtbook_backend.repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.time.Instant;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Service
public class CommentService {
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public CommentService(CommentRepository commentRepository, PostRepository postRepository, UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    public List<Comment> getCommentsForPost(Integer postId) {
        return commentRepository.findByPostId(postId);
    }

    public Optional<Comment> createComment(Integer postId, Comment comment) {
        Optional<Post> postOpt = postRepository.findById(postId);
        if (postOpt.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found");
        Post post = postOpt.get();
        Integer authorId = null;
        if (comment.getAuthor() != null && comment.getAuthor().getId() != null) {
            authorId = comment.getAuthor().getId();
        } else if (comment.getAuthorId() != null) {
            authorId = comment.getAuthorId();
        } else if (comment.getUserId() != null) {
            authorId = comment.getUserId();
        }
        if (authorId == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Author id is required (use key 'user' or 'author' or author_id/user_id)");
        Optional<User> userOpt = userRepository.findById(authorId);
        if (userOpt.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Author (user) not found");
        comment.setPost(post);
        comment.setAuthor(userOpt.get());
        if (comment.getCreatedAt() == null) {
            comment.setCreatedAt(Instant.now());
        }
        // handle parent comment if provided
        if (comment.getParentCommentId() != null) {
            Optional<Comment> parentOpt = commentRepository.findById(comment.getParentCommentId());
            if (parentOpt.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Parent comment not found");
            comment.setParentComment(parentOpt.get());
        }
        return Optional.of(commentRepository.save(comment));
    }

    public Optional<Comment> updateComment(Integer postId, Integer id, Comment updated) {
        // ensure post exists first
        if (!postRepository.existsById(postId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found");
        }
        Optional<Comment> opt = commentRepository.findById(id);
        if (opt.isEmpty()) return Optional.empty();
        Comment existing = opt.get();
        // ensure the comment belongs to the post in the path
        if (existing.getPost() == null || existing.getPost().getId() == null || !existing.getPost().getId().equals(postId)) {
            // treat as not found when mismatch
            return Optional.empty();
        }
        existing.setContent(updated.getContent());
        existing.setUpdatedAt(Instant.now());
        return Optional.of(commentRepository.save(existing));
    }

    public void deleteComment(Integer postId, Integer id) {
        // ensure post exists
        if (!postRepository.existsById(postId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found");
        }
        Optional<Comment> opt = commentRepository.findById(id);
        if (opt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found");
        }
        Comment existing = opt.get();
        if (existing.getPost() == null || existing.getPost().getId() == null || !existing.getPost().getId().equals(postId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found for this post");
        }
        commentRepository.deleteById(id);
    }
}
