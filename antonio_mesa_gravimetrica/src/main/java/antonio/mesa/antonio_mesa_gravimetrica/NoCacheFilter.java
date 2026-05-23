package antonio.mesa.antonio_mesa_gravimetrica;

import java.io.IOException;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * NoCacheFilter: A filter that prevents browser caching of sensitive pages.
 * 
 * This filter adds HTTP headers to all responses to instruct the browser
 * not to cache content. This is relevant for security in this application due to
 * the information can be accessible after logout by pressing the browser's back button.
 * 
 * The filter adds the following headers to prevent caching:
 * - Cache-Control: no-cache, no-store, must-revalidate
 * - Pragma: no-cache
 * - Expires: 0
 * 
 * This ensures that after logout, when a user presses the back button,
 * the browser will request a fresh copy from the server (which will
 * redirect them to login since their session is invalidated).
 */
@Component
public class NoCacheFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        // Add no-cache headers to prevent browser caching
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate, private, max-age=0");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");
        
        // Continue with the filter chain
        filterChain.doFilter(request, response);
    }
}
