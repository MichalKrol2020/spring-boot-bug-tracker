package com.company.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.company.domain.UserPrincipal;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.company.constant.SecurityConstant.*;
import static java.util.Arrays.stream;

@Component
public class JwtTokenProvider
{
    @Value("${jwt.secret}")
    private String secret;


    public String generateJwtToken(UserPrincipal userPrincipal)
    {
        String[] claims = getClaimsFromUser(userPrincipal);

        return JWT.create().withIssuer(ISSUER).withAudience(AUDIENCE)
                .withIssuedAt(new Date()).withSubject(userPrincipal.getUsername())
                .withArrayClaim(AUTHORITIES, claims).withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .sign(Algorithm.HMAC512(secret.getBytes()));
    }

    private String[] getClaimsFromUser(UserPrincipal userPrincipal)
    {
        List<String> authorities = new ArrayList<>();

        for(GrantedAuthority grantedAuthority: userPrincipal.getAuthorities())
        {
            authorities.add(grantedAuthority.getAuthority());
        }

        return authorities.toArray(new String[0]);
    }



    public List<GrantedAuthority> getAuthorities(String token)
    {
        String[] claims = getClaimsFromToken(token);

        return stream(claims).map(SimpleGrantedAuthority::new).collect(Collectors.toList());
    }

    private String[] getClaimsFromToken(String token)
    {
        JWTVerifier jwtVerifier = getJWTVerifier();

        return jwtVerifier.verify(token).getClaim(AUTHORITIES).asArray(String.class);
    }



    public Authentication getAuthentication(String username,
                                            List<GrantedAuthority> authorities,
                                            HttpServletRequest request)
    {
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken
                = new UsernamePasswordAuthenticationToken(username, null, authorities);

        usernamePasswordAuthenticationToken
                .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        return usernamePasswordAuthenticationToken;
    }



    public boolean isTokenValid(String username, String token)
    {
        JWTVerifier verifier = getJWTVerifier();

        return StringUtils.isNotEmpty(username) && !isTokenExpired(verifier, token);
    }

    private boolean isTokenExpired(JWTVerifier verifier, String token)
    {
        Date expirationDate = verifier.verify(token).getExpiresAt();

        return expirationDate.before(new Date());
    }



    public String getSubject(String token)
    {
        JWTVerifier verifier = getJWTVerifier();

        return verifier.verify(token).getSubject();
    }
    private JWTVerifier getJWTVerifier()
    {
        JWTVerifier verifier;

        try
        {
            Algorithm algorithm = Algorithm.HMAC512(secret);

            verifier = JWT.require(algorithm).withIssuer(ISSUER).build();
        } catch (JWTVerificationException e)
        {
            throw new JWTVerificationException(TOKEN_CANNOT_BE_VERIFIED);
        }

        return verifier;
    }
}
