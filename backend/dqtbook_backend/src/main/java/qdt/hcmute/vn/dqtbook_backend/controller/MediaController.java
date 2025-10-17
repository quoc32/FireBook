package qdt.hcmute.vn.dqtbook_backend.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.http.HttpHeaders;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/media")
public class MediaController {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    // POST /api/media/upload
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> upload(@RequestParam("files") List<MultipartFile> files) {
        try {
            if (files == null || files.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "No files uploaded"));
            }
            Path base = Paths.get(uploadDir).toAbsolutePath().normalize();
            if (!Files.exists(base)) Files.createDirectories(base);

            List<String> urls = new ArrayList<>();
            int idx = 1;
            for (MultipartFile mf : files) {
                if (mf.isEmpty()) continue;
                String original = Optional.ofNullable(mf.getOriginalFilename()).orElse("file");
                String ext = "";
                int dot = original.lastIndexOf('.');
                if (dot >= 0) ext = original.substring(dot);
                String stored = idx + "_" + UUID.randomUUID() + ext;
                Path target = base.resolve(stored).normalize();
                Files.copy(mf.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
                urls.add("/api/media/files/" + stored);
                idx++;
            }
            return ResponseEntity.ok(Map.of("urls", urls));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("message", "Upload failed"));
        }
    }

    // GET /api/media/files/{filename}
    @GetMapping("/files/{filename:.+}")
    public ResponseEntity<Resource> serve(@PathVariable String filename) {
        try {
            Path base = Paths.get(uploadDir).toAbsolutePath().normalize();
            Path file = base.resolve(filename).normalize();
            if (!file.startsWith(base) || !Files.exists(file)) {
                return ResponseEntity.notFound().build();
            }
            Resource res = new UrlResource(file.toUri());
            String contentType = Files.probeContentType(file);
            if (contentType == null) contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + res.getFilename() + "\"")
                    .body(res);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}