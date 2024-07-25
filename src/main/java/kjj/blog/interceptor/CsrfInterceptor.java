package kjj.blog.interceptor;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.HandlerInterceptor;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * CSRF 토큰을 생성하고 검증하는 인터셉터<br/>
 * req.method == "GET" 일 때 CSRF 토큰을 생성해서 세션에 저장한다.
 * req.method == "POST" 일 때 세션의 토큰과 폼에서 올라온 토큰을 비교한다.
 */
@Component
public class CsrfInterceptor implements HandlerInterceptor {

    private static final String CSRF = "csrf";

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse resp, Object handler) throws Exception {
        HttpSession session = req.getSession();

        if ("GET".equals(req.getMethod())) { // GET일 때 CSRF 토큰 생성해서 세션에 저장
            String csrfToken = generateRandomString(32);
            session.setAttribute(CSRF, csrfToken);
            return true;
        } else if ("POST".equals(req.getMethod())) { // POST일 때 폼의 CSRF 토큰 검증
            String csrfToken = (String) session.getAttribute(CSRF);
            if (csrfToken != null) {
                session.removeAttribute(CSRF); // 한번 사용한 토큰 삭제
                String _csrf = req.getParameter("_csrf");
                if (csrfToken.equals(_csrf)) {
                    return true;
                }
            }
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CSRF 에러");
    }

    private String generateRandomString(int length) {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[length];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}

