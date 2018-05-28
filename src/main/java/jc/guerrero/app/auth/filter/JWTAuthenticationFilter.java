package jc.guerrero.app.auth.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jc.guerrero.app.models.entity.Usuario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.core.userdetails.User;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JWTAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private AuthenticationManager authenticationManager;

    public JWTAuthenticationFilter(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
        setRequiresAuthenticationRequestMatcher(new AntPathRequestMatcher("/api/login", "POST"));
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {

        String username = this.obtainUsername(request);
        String password = this.obtainPassword(request);

        if(username != null && password != null){
            logger.info("Username desde request parameter (form-data):" + username);
            logger.info("Passsword desde request parameter (form-data): " + password);
        } else {
            Usuario usuario = null;
            try {

                usuario = new ObjectMapper().readValue(request.getInputStream(), Usuario.class);

                username = usuario.getUsername();
                password = usuario.getPassword();

                logger.info("Username desde request InputStream (raw):" + username);
                logger.info("Passsword desde request InputStream (raw): " + password);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        username = username.trim();

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(username, password);

        return authenticationManager.authenticate(authToken);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {

        String username = ((User) authResult.getPrincipal()).getUsername();

        Collection<? extends GrantedAuthority> roles = authResult.getAuthorities();

        Claims claims = Jwts.claims();
        claims.put("authorities", new ObjectMapper().writeValueAsString(roles));

        String token = Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .signWith(SignatureAlgorithm.HS512, "Hola, es la -- clase secreta......?".getBytes())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 36000000L)) //es una 1 hora
                .compact();

        response.addHeader("Authorization", "Bearer " + token); //importante el Bearer con ese nombre exacto, puesto que, es un ESTÁNDAR!!!!

        Map<String, Object> body = new HashMap<String, Object>();
        body.put("token", token);
        body.put("user", (User) authResult.getPrincipal());
        body.put("mensaje", String.format("Hola %s, has iniciado sesión con éxito", username));

        response.getWriter().write(new ObjectMapper().writeValueAsString(body)); //el body lo convierte a Json
        response.setStatus(200);
        response.setContentType(String.valueOf(MediaType.APPLICATION_JSON_UTF8));
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        Map<String, Object> body = new HashMap<String, Object>();
        body.put("mensaje", "Error de autentificación: username o password incorrecto");
        body.put("Error", failed.getMessage());

        response.getWriter().write(new ObjectMapper().writeValueAsString(body)); //el body lo convierte a Json
        response.setStatus(401);
        response.setContentType(String.valueOf(MediaType.APPLICATION_JSON_UTF8));
    }
}
