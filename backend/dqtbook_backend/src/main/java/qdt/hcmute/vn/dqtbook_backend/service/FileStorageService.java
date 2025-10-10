package qdt.hcmute.vn.dqtbook_backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {
    @Value("${file.upload-dir}")
    private String uploadDir;
    
    @Autowired
    private HttpSession session;

    /**
     * Lưu trữ file multipart vào thư mục upload đã cấu hình.
     * <p>
     * File sẽ được lưu với tiền tố UUID ngẫu nhiên để tránh trùng tên.
     * Nếu thư mục upload chưa tồn tại, sẽ tự động tạo mới.
     * </p>
     *
     * @param file file multipart cần lưu trữ
     * @return tên file đã được tạo dùng để lưu trữ
     * @throws IOException nếu xảy ra lỗi I/O trong quá trình lưu file
     */
    public String storeFile(MultipartFile file) throws IOException {
        Path uploadPath = Paths.get(uploadDir);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String fileName = session.getAttribute("userId") + "_" + UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return fileName; // Lưu tên file để ghi vào DB
    }
}

