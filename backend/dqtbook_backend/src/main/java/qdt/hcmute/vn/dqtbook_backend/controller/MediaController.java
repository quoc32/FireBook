package qdt.hcmute.vn.dqtbook_backend.controller;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;
import qdt.hcmute.vn.dqtbook_backend.service.FileStorageService;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/media")
public class MediaController {
    private final FileStorageService fileStorageService;

    public MediaController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    /**
     * Upload nhiều tệp (multipart/form-data, field "files").
     * Lưu từng tệp, tạo URL /media/files/{fileName} và trả về 200 với {"urls":[...]} (rỗng nếu không có tệp).
     * Lỗi I/O: 500 với thông báo "Upload failed: ...".
     *
     * @param files Mảng tệp tải lên.
     * @return ResponseEntity như mô tả.
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadMultiple(@RequestParam("files") MultipartFile[] files) {
      List<String> urls = new ArrayList<>();
      for (MultipartFile file : files) {
        try {
          String fileName = fileStorageService.storeFile(file);
          String fileUrl = "/api/media/files/" + fileName;
          urls.add(fileUrl);
        } catch (IOException e) {
          return ResponseEntity.status(500).body("Upload failed: " + e.getMessage());
        }
      }
      return ResponseEntity.ok(Map.of("urls", urls));
    }

    /**
     * Xử lý yêu cầu GET để trả về tệp từ thư mục cục bộ "uploads".
     *
     * Endpoint: GET /media/files/{filename}
     * Nếu tệp tồn tại, trả về HTTP 200 cùng tài nguyên tệp (Resource)
     * và đặt header Content-Disposition là "inline" với tên gốc để trình duyệt có thể hiển thị.
     * Nếu không tìm thấy tệp, trả về HTTP 404.
     *
     * Lưu ý: Tên tệp được chuẩn hóa (normalize) trong phạm vi thư mục "uploads".
     *
     * @param filename tên tệp cần lấy (biến đường dẫn)
     * @return ResponseEntity chứa tài nguyên tệp nếu thành công,
     *         hoặc 404 Not Found nếu tệp không tồn tại
     * @throws java.io.IOException nếu xảy ra lỗi I/O khi xử lý hoặc truy cập tệp
     */
    @GetMapping("/files/{filename}")
    public ResponseEntity<Resource> getFile(@PathVariable String filename) throws IOException {
        Path filePath = Paths.get("uploads").resolve(filename).normalize();
        Resource resource = new UrlResource(filePath.toUri());

        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        // Đoán content type dựa theo tên file
        String contentType = Files.probeContentType(filePath);
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

}

