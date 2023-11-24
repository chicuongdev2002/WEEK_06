package vn.edu.iuh.fit.frontend.controller;import org.springframework.beans.factory.annotation.Autowired;import org.springframework.stereotype.Controller;import org.springframework.ui.Model;import org.springframework.web.bind.annotation.GetMapping;import org.springframework.web.bind.annotation.PostMapping;import org.springframework.web.bind.annotation.RequestMapping;import org.springframework.web.bind.annotation.RequestParam;import vn.edu.iuh.fit.backend.entities.Post;import vn.edu.iuh.fit.backend.entities.PostComment;import vn.edu.iuh.fit.backend.entities.User;import vn.edu.iuh.fit.backend.repositories.PostCommentRepository;import vn.edu.iuh.fit.backend.repositories.PostRepository;import vn.edu.iuh.fit.backend.repositories.UserRepository;import java.time.Instant;import java.util.List;@Controller@RequestMapping("/users")public class UserController {    @Autowired    private UserRepository userRepository;    @Autowired    private  PostRepository postRepository;    @Autowired    private  PostCommentRepository postCommentRepository;    @Autowired    public UserController(UserRepository userRepository, PostRepository postRepository, PostCommentRepository postCommentRepository) {        this.userRepository = userRepository;        this.postRepository = postRepository;        this.postCommentRepository = postCommentRepository;    }    @GetMapping("/login")    public String loginCandidate() {        return "user/login";    }    @PostMapping("/check-login")    public String checkLogin(@RequestParam String email, @RequestParam String passwordHash, Model model) {        boolean check = userRepository.existsUserByEmailAndPassword(email, passwordHash);        if (check) {            String lastName = userRepository.findNameByEmail(email);            model.addAttribute("lastName", lastName);            model.addAttribute("email", email);            return "redirect:/users/home?email=" + email;  // Thêm email vào URL khi chuyển hướng        }        return "user/login";    }    @GetMapping("/home")    public String loadHome(@RequestParam String email, Model model) {        // Lấy thông tin người dùng và các bài viết của họ        User user = userRepository.findByEmail(email);        List<Post> posts = postRepository.findByAuthor(user);        // Lấy tất cả bình luận cho các bài viết        List<PostComment> comments = postCommentRepository.findAllByPostIn(posts);        model.addAttribute("user", user);        model.addAttribute("posts", posts);        model.addAttribute("comments", comments);        return "user/home";    }    @PostMapping("/add-comment")    public String addComment(@RequestParam Long postId, @RequestParam String content, @RequestParam String email, @RequestParam(required = false) Long parentCommentId) {        // Lấy thông tin người dùng từ email        User user = userRepository.findByEmail(email);        // Lấy thông tin bài viết        Post post = postRepository.findById(postId).orElse(null);        // Tạo bình luận và lưu vào cơ sở dữ liệu        PostComment comment = new PostComment();        comment.setContent(content);        comment.setUser(user);        comment.setPost(post);        comment.setCreatedAt(Instant.now());        comment.setPublishedAt(Instant.now());        comment.setUpdateAt(Instant.now());        comment.setMetaTitle("Comment");        comment.setSummary("Sumary");        // Nếu là một trả lời, cập nhật mối quan hệ cha-con        if (parentCommentId != null) {            PostComment parentComment = postCommentRepository.findById(parentCommentId).orElse(null);            comment.setParent(parentComment);            parentComment.addChildComment(comment);            postCommentRepository.save(parentComment); // Cập nhật bình luận cha để cập nhật mối quan hệ        }        postCommentRepository.save(comment);        return "redirect:/users/home?email=" + email;    }}