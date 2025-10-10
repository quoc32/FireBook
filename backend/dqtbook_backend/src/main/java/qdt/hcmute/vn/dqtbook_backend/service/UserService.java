package qdt.hcmute.vn.dqtbook_backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpSession;
import qdt.hcmute.vn.dqtbook_backend.model.User;
import qdt.hcmute.vn.dqtbook_backend.model.Department;
import qdt.hcmute.vn.dqtbook_backend.repository.UserRepository;
import qdt.hcmute.vn.dqtbook_backend.repository.DepartmentRepository;
import qdt.hcmute.vn.dqtbook_backend.dto.UserCreateRequestDTO;
import qdt.hcmute.vn.dqtbook_backend.dto.UserUpdateRequestDTO;
import qdt.hcmute.vn.dqtbook_backend.dto.UserResponseDTO;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Autowired
    private HttpSession session;

    public UserService(UserRepository userRepository,
                        DepartmentRepository departmentRepository,
                        PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    public Optional<UserResponseDTO> getUserById(Integer id) {
        return userRepository.findById(id)
                .map(this::convertToResponseDTO);
    }

    public Optional<UserResponseDTO> getUserByEmail(String email) {
        User user = userRepository.findByEmail(email);
        return Optional.ofNullable(user).map(this::convertToResponseDTO);
    }

    @Transactional
    public Optional<UserResponseDTO> createUser(UserCreateRequestDTO dto) {
        // Check if email already exists
        if (userRepository.findByEmail(dto.getEmail()) != null) {
            return Optional.empty();
        }

        User user = new User();

        user.setFullName(dto.getFullName());
        user.setEmail(dto.getEmail());
        // user.setPasswordHash(dto.getPasswordHash());
        // Băm mật khẩu trước khi lưu
        user.setPasswordHash(passwordEncoder.encode(dto.getPasswordHash()));
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setGender(dto.getGender());
        user.setDateOfBirth(dto.getDateOfBirth());
        user.setAvatarUrl(dto.getAvatarUrl() != null ? dto.getAvatarUrl() : "http://localhost:8080/default-avatar.png");
        user.setCoverPhotoUrl(dto.getCoverPhotoUrl());
        user.setBio(dto.getBio());
        user.setSchoolId(dto.getSchoolId());
        user.setAcademicYear(dto.getAcademicYear());
        user.setRole(dto.getRole() != null ? dto.getRole() : "student");
        user.setPhone(dto.getPhone());
        user.setWebsite(dto.getWebsite());
        user.setCountry(dto.getCountry());
        user.setCity(dto.getCity());
        user.setEducation(dto.getEducation());
        user.setWorkplace(dto.getWorkplace());
        user.setFacebookUrl(dto.getFacebookUrl());
        user.setInstagramUrl(dto.getInstagramUrl());
        user.setLinkedinUrl(dto.getLinkedinUrl());
        user.setTwitterUrl(dto.getTwitterUrl());
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());

        // Set department if provided
        if (dto.getDepartmentId() != null) {
            Optional<Department> department = departmentRepository.findById(dto.getDepartmentId());
            department.ifPresent(user::setDepartment);
        }

        User savedUser = userRepository.save(user);
        return Optional.of(convertToResponseDTO(savedUser));
    }

    @Transactional
    public Optional<UserResponseDTO> updateUser(Integer id, UserUpdateRequestDTO dto) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("User with id " + id + " does not exist");
        }

        // Session check
        if (id != (Integer)session.getAttribute("userId")) {
            throw new IllegalArgumentException("id does not match the logged-in user");
        }

        User user = userOpt.get();

        // Check if email is being changed and if new email already exists
        if (dto.getEmail() != null && !dto.getEmail().equals(user.getEmail())) {
            if (userRepository.findByEmail(dto.getEmail()) != null) {
                return Optional.empty();
            }
        }

        // Update fields if provided
        if (dto.getFullName() != null) user.setFullName(dto.getFullName());
        if (dto.getEmail() != null) user.setEmail(dto.getEmail());
        if (dto.getPasswordHash() != null) user.setPasswordHash(dto.getPasswordHash());
        if (dto.getFirstName() != null) user.setFirstName(dto.getFirstName());
        if (dto.getLastName() != null) user.setLastName(dto.getLastName());
        if (dto.getGender() != null) user.setGender(dto.getGender());
        if (dto.getDateOfBirth() != null) user.setDateOfBirth(dto.getDateOfBirth());
        if (dto.getAvatarUrl() != null) user.setAvatarUrl(dto.getAvatarUrl());
        if (dto.getCoverPhotoUrl() != null) user.setCoverPhotoUrl(dto.getCoverPhotoUrl());
        if (dto.getBio() != null) user.setBio(dto.getBio());
        if (dto.getSchoolId() != null) user.setSchoolId(dto.getSchoolId());
        if (dto.getAcademicYear() != null) user.setAcademicYear(dto.getAcademicYear());
        if (dto.getRole() != null) user.setRole(dto.getRole());
        if (dto.getPhone() != null) user.setPhone(dto.getPhone());
        if (dto.getWebsite() != null) user.setWebsite(dto.getWebsite());
        if (dto.getCountry() != null) user.setCountry(dto.getCountry());
        if (dto.getCity() != null) user.setCity(dto.getCity());
        if (dto.getEducation() != null) user.setEducation(dto.getEducation());
        if (dto.getWorkplace() != null) user.setWorkplace(dto.getWorkplace());
        if (dto.getFacebookUrl() != null) user.setFacebookUrl(dto.getFacebookUrl());
        if (dto.getInstagramUrl() != null) user.setInstagramUrl(dto.getInstagramUrl());
        if (dto.getLinkedinUrl() != null) user.setLinkedinUrl(dto.getLinkedinUrl());
        if (dto.getTwitterUrl() != null) user.setTwitterUrl(dto.getTwitterUrl());

        // Update department if provided
        if (dto.getDepartmentId() != null) {
            Optional<Department> department = departmentRepository.findById(dto.getDepartmentId());
            department.ifPresent(user::setDepartment);
        }

        user.setUpdatedAt(Instant.now());
        User savedUser = userRepository.save(user);
        return Optional.of(convertToResponseDTO(savedUser));
    }

    @Transactional
    public boolean deleteUser(Integer id) {
        if (!userRepository.existsById(id)) {
            return false;
        }
        userRepository.deleteById(id);
        return true;
    }

    private UserResponseDTO convertToResponseDTO(User user) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setFullName(user.getFullName());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setGender(user.getGender());
        dto.setDateOfBirth(user.getDateOfBirth());
        dto.setAvatarUrl(user.getAvatarUrl());
        dto.setCoverPhotoUrl(user.getCoverPhotoUrl());
        dto.setBio(user.getBio());
        dto.setSchoolId(user.getSchoolId());
        dto.setDepartmentId(user.getDepartment() != null ? user.getDepartment().getId() : null);
        dto.setAcademicYear(user.getAcademicYear());
        dto.setRole(user.getRole());
        dto.setPhone(user.getPhone());
        dto.setWebsite(user.getWebsite());
        dto.setCountry(user.getCountry());
        dto.setCity(user.getCity());
        dto.setEducation(user.getEducation());
        dto.setWorkplace(user.getWorkplace());
        dto.setFacebookUrl(user.getFacebookUrl());
        dto.setInstagramUrl(user.getInstagramUrl());
        dto.setLinkedinUrl(user.getLinkedinUrl());
        dto.setTwitterUrl(user.getTwitterUrl());
        dto.setLastSeenAt(user.getLastSeenAt());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        return dto;
    }

    public Optional<User> login(String email, String rawPassword) {
        User user = userRepository.findByEmail(email);
        if (user != null && passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            return Optional.of(user);
        }
        return Optional.empty();
    }

}