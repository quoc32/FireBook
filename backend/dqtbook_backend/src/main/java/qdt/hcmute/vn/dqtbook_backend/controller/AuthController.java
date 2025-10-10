package qdt.hcmute.vn.dqtbook_backend.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Cookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import qdt.hcmute.vn.dqtbook_backend.dto.AuthResponseDTO;
import qdt.hcmute.vn.dqtbook_backend.service.UserService;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    // Đăng nhập
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request, HttpServletRequest httpRequest) {
        String email = request.get("email");
        String password = request.get("password");

        return userService.login(email, password)
                .map(user -> {
                    HttpSession session = httpRequest.getSession(true); // tạo session nếu chưa có
                    session.setAttribute("userId", user.getId()); // Session lưu userId

                    AuthResponseDTO authResponse = new AuthResponseDTO(
                            "Đăng nhập thành công",
                            session.getId(),
                            user.getId(),
                            user.getEmail(),
                            user.getFullName(),
                            user.getFirstName(),
                            user.getLastName(),
                            user.getGender(),
                            user.getBio(),
                            user.getAvatarUrl(),
                            user.getCoverPhotoUrl()
                    );

                    return ResponseEntity.ok(authResponse);
                })
                .orElse(ResponseEntity.status(401).body(
                        new AuthResponseDTO(
                            "Sai email hoặc mật khẩu",
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null
                        )
                ));
    }

    // Lấy thông tin user hiện tại
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(HttpSession session) {
        Object userId = session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Chưa đăng nhập"));
        }
        return ResponseEntity.ok(Map.of(
                "message", "Đang đăng nhập",
                "userId", userId
        ));
    }

    // Đăng xuất
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false); // không tạo mới

        if (session == null) {
            return ResponseEntity.ok(Map.of(
                "message", "Đã đăng xuất hoặc session không tồn tại"
            ));
        }

        // Lấy thông tin trước khi invalidate (nếu cần log)
        String oldSessionId = session.getId();
        
        // Invalidate session
        session.invalidate();

        // Xóa tất cả các cookie session có thể
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("JSESSIONID".equals(cookie.getName())) {
                    cookie.setValue("");
                    cookie.setPath("/");
                    cookie.setHttpOnly(true);
                    cookie.setMaxAge(0);
                    cookie.setSecure(false); // có thể set true nếu dùng HTTPS
                    response.addCookie(cookie);
                }
            }
        }

        return ResponseEntity.ok(Map.of(
            "message", "Đăng xuất thành công",
            "sessionId", oldSessionId != null ? oldSessionId : "unknown"
        ));
    }

}
