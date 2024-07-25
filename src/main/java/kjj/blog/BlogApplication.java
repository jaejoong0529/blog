package kjj.blog;

import kjj.blog.interceptor.AuthInterceptor;
import kjj.blog.interceptor.CsrfInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication(exclude = SecurityAutoConfiguration.class)
public class BlogApplication implements WebMvcConfigurer {

	public static void main(String[] args) {
		SpringApplication.run(BlogApplication.class, args);
	}
	@Autowired
	private AuthInterceptor authInterceptor;

	@Autowired
	private CsrfInterceptor csrfInterceptor;

	/** 인터셉터 등록 */
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(authInterceptor)
				.addPathPatterns("/post/create", "/post/update", "/post/delete",
						"/user/profile", "/user/logout", "/user/delete");

		registry.addInterceptor(csrfInterceptor)
				.addPathPatterns("/user/login", "/user/logout", "/user/signup",
						"/user/profile", "/post/create", "/post/update",
						"/post/detail", "/post/delete");
	}
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder(); // 비밀번호 암호화에 BCrypt 사용
	}

}
