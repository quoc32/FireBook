package qdt.hcmute.vn.dqtbook_backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.web.bind.annotation.*;
import qdt.hcmute.vn.dqtbook_backend.service.PostService;
import qdt.hcmute.vn.dqtbook_backend.dto.PostCreateRequestDTO;
import qdt.hcmute.vn.dqtbook_backend.dto.PostUpdateRequest;
import qdt.hcmute.vn.dqtbook_backend.dto.ErrorResponse;
import qdt.hcmute.vn.dqtbook_backend.dto.PostContentResponseDTO;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;


@RestController
@RequestMapping("/api/posts")
public class PostController {
    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping()
    public ResponseEntity<List<PostContentResponseDTO>> getAllPosts() {
        return postService.getAllPosts();
    }

    @GetMapping("/suitable/{userId}")
    public ResponseEntity<List<PostContentResponseDTO>> getPosts(@PathVariable Integer userId) {
        return postService.getPosts(userId);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PostContentResponseDTO>> getSelfPosts(@PathVariable Integer userId) {
        return postService.getUserPosts(userId);
    }

    @GetMapping("/friends/{userId}")
    public ResponseEntity<List<PostContentResponseDTO>> getFriendsPosts(@PathVariable Integer userId) {
        return postService.getFriendPosts(userId);
    }

    @GetMapping("/public")
    public ResponseEntity<List<PostContentResponseDTO>> getPublicPosts() {
        return postService.getPublicPosts();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getPostById(@PathVariable Integer id) {
        try {
            return postService.findById(id)
                    .map(p -> ResponseEntity.ok((Object)p))
                    .orElseGet(() -> {
                        ErrorResponse err = new ErrorResponse(404, "post not found for id=" + id);
                        return ResponseEntity.status(404).body(err);
                    });
        } catch (IllegalArgumentException ex) {
            ErrorResponse err = new ErrorResponse(400, ex.getMessage());
            return ResponseEntity.badRequest().body(err);
        } catch (Exception ex) {
            ErrorResponse err = new ErrorResponse(500, "internal server error: " + ex.getMessage());
            return ResponseEntity.status(500).body(err);
        }
    }

    @PostMapping
    public ResponseEntity<Object> createPost(@RequestBody PostCreateRequestDTO dto) {
        try {
            return postService.createFromDto(dto)
                .map(p -> ResponseEntity.status(201).body((Object)p))
                .orElseGet(() -> ResponseEntity.status(500).build());
        } catch (IllegalArgumentException ex) {
            ErrorResponse err = new ErrorResponse(400, ex.getMessage());
            return ResponseEntity.badRequest().body(err);
        } catch (Exception ex) {
            ErrorResponse err = new ErrorResponse(500, "internal server error: " + ex.getMessage());
            return ResponseEntity.status(500).body(err);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> updatePost(@PathVariable Integer id, @RequestBody PostUpdateRequest dto) {
        try {
            return postService.updateFromDto(id, dto)
                    .map(p -> ResponseEntity.ok((Object)p))
                    .orElseGet(() -> {
                        ErrorResponse err = new ErrorResponse(404, "post not found for id=" + id);
                        return ResponseEntity.status(404).body(err);
                    });
        } catch (IllegalArgumentException ex) {
            ErrorResponse err = new ErrorResponse(400, ex.getMessage());
            return ResponseEntity.badRequest().body(err);
        } catch (Exception ex) {
            ErrorResponse err = new ErrorResponse(500, "internal server error: " + ex.getMessage());
            return ResponseEntity.status(500).body(err);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deletePost(@PathVariable Integer id) {
        try {
            // Check existence first so we can return a JSON error when the post doesn't exist
            if (postService.findById(id).isEmpty()) {
                ErrorResponse err = new ErrorResponse(404, "post not found for id=" + id);
                return ResponseEntity.status(404).body(err);
            }

            postService.deletePost(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException ex) {
            ErrorResponse err = new ErrorResponse(400, ex.getMessage());
            return ResponseEntity.badRequest().body(err);
        } catch (EmptyResultDataAccessException ex) {
            ErrorResponse err = new ErrorResponse(404, "post not found for id=" + id);
            return ResponseEntity.status(404).body(err);
        } catch (Exception ex) {
            ErrorResponse err = new ErrorResponse(500, "internal server error: " + ex.getMessage());
            return ResponseEntity.status(500).body(err);
        }
    }

}
