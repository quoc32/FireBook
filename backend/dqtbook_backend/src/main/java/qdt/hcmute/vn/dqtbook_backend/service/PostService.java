package qdt.hcmute.vn.dqtbook_backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import qdt.hcmute.vn.dqtbook_backend.model.Post;
import qdt.hcmute.vn.dqtbook_backend.model.PostMedia;
import qdt.hcmute.vn.dqtbook_backend.model.User;
import qdt.hcmute.vn.dqtbook_backend.repository.PostRepository;
import qdt.hcmute.vn.dqtbook_backend.repository.PostMediaRepository;
import qdt.hcmute.vn.dqtbook_backend.repository.UserRepository;
import qdt.hcmute.vn.dqtbook_backend.repository.FriendRepository;
import qdt.hcmute.vn.dqtbook_backend.dto.PostContentResponseDTO;
import qdt.hcmute.vn.dqtbook_backend.dto.PostCreateRequestDTO;
import qdt.hcmute.vn.dqtbook_backend.dto.PostCreateResponseDTO;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpSession;
import qdt.hcmute.vn.dqtbook_backend.dto.PostUpdateRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostMediaRepository postMediaRepository;
    private final FriendRepository friendRepository;

    @Autowired
    private HttpSession session;

    public PostService(PostRepository postRepository, UserRepository userRepository, PostMediaRepository postMediaRepository, FriendRepository friendRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.postMediaRepository = postMediaRepository;
        this.friendRepository = friendRepository;
    }

    public ResponseEntity<List<PostContentResponseDTO>> getAllPosts() {
        List<Post> posts = postRepository.findAll();

        List<PostContentResponseDTO> dtos = posts.stream()
                .map(post -> {
                    PostContentResponseDTO dto = new PostContentResponseDTO();
                    dto.setId(post.getId());
                    dto.setContent(post.getContent());
                    dto.setVisibility(post.getVisibility());
                    dto.setPostType(post.getPostType());
                    dto.setStatus(post.getStatus());
                    dto.setCreatedAt(post.getCreatedAt());

                    // map author
                    PostContentResponseDTO.AuthorDTO authorDto = new PostContentResponseDTO.AuthorDTO();
                    if (post.getAuthor() != null) {
                        authorDto.setId(post.getAuthor().getId());
                        authorDto.setFullName(post.getAuthor().getFullName());
                        authorDto.setAvatarUrl(post.getAuthor().getAvatarUrl());
                        authorDto.setEmail(post.getAuthor().getEmail());
                    }
                    dto.setAuthor(authorDto);

                    // map medias
                    List<PostContentResponseDTO.MediaDTO> mediaDtos = post.getMedias().stream()
                            .map(m -> new PostContentResponseDTO.MediaDTO(m.getMediaType(), m.getMediaUrl()))
                            .toList();
                    dto.setMedia(mediaDtos);

                    return dto;
            })
            .toList();

        return ResponseEntity.ok(dtos);
    }

    public Optional<Post> findById(Integer id) {
        return postRepository.findById(id);
    }

    public Post savePost(Post post) {
        return postRepository.save(post);
    }



    public ResponseEntity<List<PostContentResponseDTO>> getPosts(Integer userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId is required");
        }
        if (userId != (Integer)session.getAttribute("userId")) {
            throw new IllegalArgumentException("author_id does not match the logged-in user");
        }

        List<Post> selfPosts = postRepository.findByAuthorId(userId);

        List<Post> friendPosts = new ArrayList<>();
        List<Integer> friendIds = friendRepository.findFriendIdsByUserId(userId);
        for (Integer fid : friendIds) {
            friendPosts.addAll(postRepository.findByAuthorId(fid));
        }

        List<Post> allPosts = postRepository.findAll();
        List<Post> publicPosts = allPosts.stream() 
                                .filter(p -> "public".equalsIgnoreCase(p.getVisibility())) 
                                .toList();

        List<Post> combinedPosts = new ArrayList<>();
        combinedPosts.addAll(selfPosts);
        combinedPosts.addAll(friendPosts);
        combinedPosts.addAll(publicPosts);

        combinedPosts = combinedPosts.stream()
                            .distinct() // loại bỏ trùng lặp
                            .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt())) // sắp xếp theo createdAt giảm dần
                            .toList();

        List<PostContentResponseDTO> dtos = combinedPosts.stream()
                .map(post -> {
                    PostContentResponseDTO dto = new PostContentResponseDTO();
                    dto.setId(post.getId());
                    dto.setContent(post.getContent());
                    dto.setVisibility(post.getVisibility());
                    dto.setPostType(post.getPostType());
                    dto.setStatus(post.getStatus());
                    dto.setCreatedAt(post.getCreatedAt());

                    PostContentResponseDTO.AuthorDTO authorDto = new PostContentResponseDTO.AuthorDTO();
                    if (post.getAuthor() != null) {
                        authorDto.setId(post.getAuthor().getId());
                        authorDto.setFullName(post.getAuthor().getFullName());
                        authorDto.setAvatarUrl(post.getAuthor().getAvatarUrl());
                        authorDto.setEmail(post.getAuthor().getEmail());
                    }
                    dto.setAuthor(authorDto);

                    List<PostContentResponseDTO.MediaDTO> mediaDtos = post.getMedias().stream()
                            .map(m -> new PostContentResponseDTO.MediaDTO(m.getMediaType(), m.getMediaUrl()))
                            .toList();
                    dto.setMedia(mediaDtos);

                    return dto;
                })
                .toList();

        return ResponseEntity.ok(dtos);
    }

    /**
     * Lấy danh sách bài viết của một người dùng theo userId và ánh xạ sang PostContentResponseDTO
     * (bao gồm thông tin tác giả và media). Trả về ResponseEntity với trạng thái 200 OK khi thành công.
     *
     * @param userId ID của người dùng cần lấy bài viết (không được null)
     * @return ResponseEntity chứa danh sách PostContentResponseDTO của người dùng
     * @throws IllegalArgumentException nếu userId là null
     */
    public ResponseEntity<List<PostContentResponseDTO>> getUserPosts(Integer userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId is required");
        }

        List<Post> posts = postRepository.findByAuthorId(userId);

        List<PostContentResponseDTO> dtos = posts.stream()
                .map(post -> {
                    PostContentResponseDTO dto = new PostContentResponseDTO();
                    dto.setId(post.getId());
                    dto.setContent(post.getContent());
                    dto.setVisibility(post.getVisibility());
                    dto.setPostType(post.getPostType());
                    dto.setStatus(post.getStatus());
                    dto.setCreatedAt(post.getCreatedAt());

                    PostContentResponseDTO.AuthorDTO authorDto = new PostContentResponseDTO.AuthorDTO();
                    if (post.getAuthor() != null) {
                        authorDto.setId(post.getAuthor().getId());
                        authorDto.setFullName(post.getAuthor().getFullName());
                        authorDto.setAvatarUrl(post.getAuthor().getAvatarUrl());
                        authorDto.setEmail(post.getAuthor().getEmail());
                    }
                    dto.setAuthor(authorDto);

                    List<PostContentResponseDTO.MediaDTO> mediaDtos = post.getMedias().stream()
                            .map(m -> new PostContentResponseDTO.MediaDTO(m.getMediaType(), m.getMediaUrl()))
                            .toList();
                    dto.setMedia(mediaDtos);

                    return dto;
                })
                .toList();

        return ResponseEntity.ok(dtos);
    }

    public ResponseEntity<List<PostContentResponseDTO>> getFriendPosts(Integer userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId is required");
        }
        if (userId != (Integer)session.getAttribute("userId")) {
            throw new IllegalArgumentException("author_id does not match the logged-in user");
        }
        List<Integer> friendIds = friendRepository.findFriendIdsByUserId(userId);
        List<Post> posts = new ArrayList<>();
        for (Integer fid : friendIds) {
            posts.addAll(postRepository.findByAuthorId(fid));
        }
        List<PostContentResponseDTO> dtos = posts.stream()
                .map(post -> {
                    PostContentResponseDTO dto = new PostContentResponseDTO();
                    dto.setId(post.getId());
                    dto.setContent(post.getContent());
                    dto.setVisibility(post.getVisibility());
                    dto.setPostType(post.getPostType());
                    dto.setStatus(post.getStatus());
                    dto.setCreatedAt(post.getCreatedAt());

                    PostContentResponseDTO.AuthorDTO authorDto = new PostContentResponseDTO.AuthorDTO();
                    if (post.getAuthor() != null) {
                        authorDto.setId(post.getAuthor().getId());
                        authorDto.setFullName(post.getAuthor().getFullName());
                        authorDto.setAvatarUrl(post.getAuthor().getAvatarUrl());
                        authorDto.setEmail(post.getAuthor().getEmail());
                    }
                    dto.setAuthor(authorDto);

                    List<PostContentResponseDTO.MediaDTO> mediaDtos = post.getMedias().stream()
                            .map(m -> new PostContentResponseDTO.MediaDTO(m.getMediaType(), m.getMediaUrl()))
                            .toList();
                    dto.setMedia(mediaDtos);

                    return dto;
                })
                .toList();

        return ResponseEntity.ok(dtos);
    }

    public ResponseEntity<List<PostContentResponseDTO>> getPublicPosts() {
        List<Post> posts = postRepository.findAll();
        List<Post> publicPosts = posts.stream() 
                                .filter(p -> "public".equalsIgnoreCase(p.getVisibility())) 
                                .toList();

        List<PostContentResponseDTO> dtos = publicPosts.stream()
                .map(post -> {
                    PostContentResponseDTO dto = new PostContentResponseDTO();
                    dto.setId(post.getId());
                    dto.setContent(post.getContent());
                    dto.setVisibility(post.getVisibility());
                    dto.setPostType(post.getPostType());
                    dto.setStatus(post.getStatus());
                    dto.setCreatedAt(post.getCreatedAt());

                    PostContentResponseDTO.AuthorDTO authorDto = new PostContentResponseDTO.AuthorDTO();
                    if (post.getAuthor() != null) {
                        authorDto.setId(post.getAuthor().getId());
                        authorDto.setFullName(post.getAuthor().getFullName());
                        authorDto.setAvatarUrl(post.getAuthor().getAvatarUrl());
                        authorDto.setEmail(post.getAuthor().getEmail());
                    }
                    dto.setAuthor(authorDto);

                    List<PostContentResponseDTO.MediaDTO> mediaDtos = post.getMedias().stream()
                            .map(m -> new PostContentResponseDTO.MediaDTO(m.getMediaType(), m.getMediaUrl()))
                            .toList();
                    dto.setMedia(mediaDtos);

                    return dto;
                })
                .toList();

        return ResponseEntity.ok(dtos);
    }

    /**
     * Chuyển đổi từ Entity Post và danh sách PostMedia đã lưu 
     * sang PostCreateResponseDTO để trả về cho client.
     *
     * @param savedPost   Bài viết đã lưu trong DB
     * @param savedMedias Danh sách media đã lưu gắn với bài viết
     * @return DTO chứa thông tin bài viết + tác giả + media
     */
    private PostCreateResponseDTO toPostCreateResponseDTO(Post savedPost, List<PostMedia> savedMedias) {
        PostCreateResponseDTO resDTO = new PostCreateResponseDTO();

        // Post cơ bản
        resDTO.setId(savedPost.getId());
        resDTO.setContent(savedPost.getContent());
        resDTO.setVisibility(savedPost.getVisibility());
        resDTO.setPostType(savedPost.getPostType());
        resDTO.setStatus(savedPost.getStatus());
        resDTO.setCreatedAt(savedPost.getCreatedAt());
        resDTO.setUpdatedAt(savedPost.getUpdatedAt());

        // Author
        if (savedPost.getAuthor() != null) {
            PostCreateResponseDTO.AuthorDTO authorDTO = new PostCreateResponseDTO.AuthorDTO();
            authorDTO.setId(savedPost.getAuthor().getId());
            authorDTO.setEmail(savedPost.getAuthor().getEmail());
            authorDTO.setFullName(savedPost.getAuthor().getFullName());
            authorDTO.setFirstName(savedPost.getAuthor().getFirstName());
            authorDTO.setLastName(savedPost.getAuthor().getLastName());
            authorDTO.setGender(savedPost.getAuthor().getGender());
            authorDTO.setBio(savedPost.getAuthor().getBio());
            resDTO.setAuthor(authorDTO);
        }

        // Media
        resDTO.setMedias(new ArrayList<>());
        for (PostMedia pm : savedMedias) {
            PostCreateResponseDTO.MediaDTO mediaDTO = new PostCreateResponseDTO.MediaDTO();
            mediaDTO.setMediaType(pm.getMediaType());
            mediaDTO.setMediaUrl(pm.getMediaUrl());
            resDTO.getMedias().add(mediaDTO);
        }

        return resDTO;
    }

    /**
     * Tạo Post mới từ PostCreateRequestDTO và trả về Optional<PostCreateResponseDTO>.
     * 
     * Quy trình:
     * 1. Kiểm tra authorId trong request có hợp lệ và khớp với user đang đăng nhập.
     * 2. Lấy thông tin User từ DB (theo authorId).
     * 3. Tạo entity Post và lưu vào DB.
     * 4. Nếu có media trong request thì duyệt danh sách, tạo PostMedia và lưu vào DB.
     * 5. Gộp Post và danh sách PostMedia đã lưu thành PostCreateResponseDTO để trả về.
     *
     * @param ReqDTO DTO chứa dữ liệu từ client gửi lên để tạo bài viết
     * @return Optional<PostCreateResponseDTO> chứa thông tin bài viết vừa tạo
     */
    @Transactional
    public Optional<PostCreateResponseDTO> createFromDto(PostCreateRequestDTO ReqDTO) {
        // Todo: 1. Kiểm tra authorId trong request có hợp lệ và khớp với user đang đăng nhập.
        if (ReqDTO.getAuthorId() == null) throw new IllegalArgumentException("author_id is required");
        if (ReqDTO.getAuthorId() != (Integer)session.getAttribute("userId")) {
            throw new IllegalArgumentException("author_id does not match the logged-in user");
        }
        // Todo: 2. Lấy thông tin User từ DB (theo authorId).
        Optional<User> userOpt = userRepository.findById(ReqDTO.getAuthorId());
        if (userOpt.isEmpty()) throw new IllegalArgumentException("author not found for id=" + ReqDTO.getAuthorId());
        // Todo: 3. Tạo entity Post và lưu vào DB.
        Post p = new Post();
        p.setAuthor(userOpt.get());
        p.setContent(ReqDTO.getContent());
        p.setVisibility(ReqDTO.getVisibility());
        p.setPostType(ReqDTO.getPostType());
        p.setStatus(ReqDTO.getStatus());
        // Todo: 4. Nếu có media trong request thì duyệt danh sách, tạo PostMedia và lưu vào DB.
        Post savedPost = postRepository.save(p);
        List<PostMedia> savedPostMedias = new ArrayList<>();
        // Todo: 5. Gộp Post và danh sách PostMedia đã lưu thành PostCreateResponseDTO để trả về.
        if (ReqDTO.getMedia() != null) {
            for (PostCreateRequestDTO.MediaItem m : ReqDTO.getMedia()) {
                if (m.getMediaType() == null || m.getMediaUrl() == null) continue;
                PostMedia pm = new PostMedia();
                pm.setPost(savedPost);
                pm.setMediaType(m.getMediaType());
                pm.setMediaUrl(m.getMediaUrl());
                PostMedia savedPostMedia =  postMediaRepository.save(pm);
                savedPostMedias.add(savedPostMedia);
            }
        }
        PostCreateResponseDTO ResDTO = toPostCreateResponseDTO(savedPost, savedPostMedias);
        
        return Optional.of(ResDTO);
    }

    public Post updatePost(Integer id, Post post) {
        Optional<Post> existing = postRepository.findById(id);
        if (existing.isEmpty()) return null;
        Post e = existing.get();
        // simple merge of fields (null-safe)
        if (post.getContent() != null) e.setContent(post.getContent());
        if (post.getVisibility() != null) e.setVisibility(post.getVisibility());
        if (post.getPostType() != null) e.setPostType(post.getPostType());
        if (post.getStatus() != null) e.setStatus(post.getStatus());
        return postRepository.save(e);
    }

    @Transactional
    public Optional<Post> updateFromDto(Integer id, PostUpdateRequest dto) {
        Optional<Post> existing = postRepository.findById(id);
        if (existing.isEmpty()) return Optional.empty();
        Post e = existing.get();

        if (dto.getContent() != null) e.setContent(dto.getContent());
        if (dto.getVisibility() != null) e.setVisibility(dto.getVisibility());
        if (dto.getPostType() != null) e.setPostType(dto.getPostType());
        if (dto.getStatus() != null) e.setStatus(dto.getStatus());

        Post saved = postRepository.save(e);

        // If media provided, replace existing media for the post with the provided list
        if (dto.getMedia() != null) {
            postMediaRepository.deleteByPostId(saved.getId());
            for (PostUpdateRequest.MediaItem m : dto.getMedia()) {
                if (m.getMediaType() == null || m.getMediaUrl() == null) continue;
                PostMedia pm = new PostMedia();
                pm.setPost(saved);
                pm.setMediaType(m.getMediaType());
                pm.setMediaUrl(m.getMediaUrl());
                postMediaRepository.save(pm);
            }
        }

        return Optional.of(saved);
    }

    public void deletePost(Integer id) {
        postRepository.deleteById(id);
    }
}
