package it.pagopa.selfcare.external_api.core;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import it.pagopa.selfcare.commons.base.logging.LogUtils;
import it.pagopa.selfcare.external_api.model.user.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;


@Slf4j
@Component("jwtExternalService")
public class JwtService {

    private final PublicKey jwtSigningKey;

    public JwtService(@Value("${jwt.signingKey}") String jwtSigningKey) throws Exception {
        this.jwtSigningKey = getPublicKey(jwtSigningKey);
    }

    public Claims getClaims(String token) {
        log.trace("getClaims start");
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "getClaims token = {}" , token);
        return Jwts.parser()
                .setSigningKey(jwtSigningKey)
                .parseClaimsJws(token)
                .getBody();
    }

    private PublicKey getPublicKey(String signingKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
        log.trace("getPublicKey");
        String publicKeyPEM = signingKey
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replaceAll(System.lineSeparator(), "")
                .replace("-----END PUBLIC KEY-----", "");

        byte[] encoded = Base64.getMimeDecoder().decode(publicKeyPEM);

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encoded);
        return keyFactory.generatePublic(keySpec);
    }

    public void putUserIntoSecurityContext(User user) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String incomingToken = (String) authentication.getCredentials();
        Claims claims = this.getClaims(incomingToken);
        claims.put("uid", user.getId());
        authentication = new UsernamePasswordAuthenticationToken(authentication.getPrincipal(), claims);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
