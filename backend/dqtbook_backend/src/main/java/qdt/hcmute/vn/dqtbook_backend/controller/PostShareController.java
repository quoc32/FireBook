package qdt.hcmute.vn.dqtbook_backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import qdt.hcmute.vn.dqtbook_backend.model.PostShare;
import qdt.hcmute.vn.dqtbook_backend.service.PostShareService;
import qdt.hcmute.vn.dqtbook_backend.dto.PostShareRequest;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/posts/{postId}/shares")
public class PostShareController {
    private final PostShareService postShareService;

    public PostShareController(PostShareService postShareService) {
        this.postShareService = postShareService;
    }

    @GetMapping
    public List<PostShare> list(@PathVariable Integer postId) {
        return postShareService.listSharesForPost(postId);
    }

    @PostMapping
    public ResponseEntity<?> create(@PathVariable Integer postId, @RequestBody PostShareRequest payload) {
        try {
            var createdOpt = postShareService.createShare(postId, payload);
            if (createdOpt.isPresent()) {
                var ps = createdOpt.get();
                return ResponseEntity.created(URI.create("/posts/" + postId + "/shares/" + ps.getId())).body(ps);
            }
            return ResponseEntity.status(400).body(new qdt.hcmute.vn.dqtbook_backend.dto.ErrorResponse(400, "Invalid payload or missing user/post"));
        } catch (org.springframework.web.server.ResponseStatusException rse) {
            return ResponseEntity.status(rse.getStatusCode()).body(new qdt.hcmute.vn.dqtbook_backend.dto.ErrorResponse(rse.getStatusCode().value(), rse.getReason()));
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(new qdt.hcmute.vn.dqtbook_backend.dto.ErrorResponse(500, "Internal server error"));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer postId, @PathVariable Integer id) {
        try {
            postShareService.deleteShare(postId, id);
            return ResponseEntity.noContent().build();
        } catch (org.springframework.web.server.ResponseStatusException rse) {
            return ResponseEntity.status(rse.getStatusCode()).body(new qdt.hcmute.vn.dqtbook_backend.dto.ErrorResponse(rse.getStatusCode().value(), rse.getReason()));
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(new qdt.hcmute.vn.dqtbook_backend.dto.ErrorResponse(500, "Internal server error"));
        }
    }
}
