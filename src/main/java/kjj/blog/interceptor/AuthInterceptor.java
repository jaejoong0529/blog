package kjj.blog.interceptor;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/** 로그인 여부를 체크하는 인터셉터 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse resp, Object handler) throws Exception {
        if (req.getSession().getAttribute("user") == null) { // 로그인 안했을 경우 로그인 화면으로
            resp.sendRedirect(req.getContextPath() + "/user/login");
            return false;
        }
        return true; // 로그인 했을 경우 그대로 진행
    }
}
