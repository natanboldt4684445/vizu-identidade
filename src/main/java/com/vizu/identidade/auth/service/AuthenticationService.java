package com.vizu.identidade.auth.service;
import com.vizu.identidade.auth.JwtService; import com.vizu.identidade.auth.dto.*; import com.vizu.identidade.auth.repository.AuthRepository; import java.nio.charset.StandardCharsets; import java.security.*; import java.util.*; import org.springframework.beans.factory.annotation.Value; import org.springframework.http.HttpStatus; import org.springframework.security.crypto.password.PasswordEncoder; import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional; import org.springframework.web.server.ResponseStatusException;
@Service public class AuthenticationService {
 private final AuthRepository repo;private final PasswordEncoder passwords;private final JwtService jwt;private final long refreshDays;
 public AuthenticationService(AuthRepository r,PasswordEncoder p,JwtService j,@Value("$"+"{app.jwt.refresh-ttl-days}")long d){repo=r;passwords=p;jwt=j;refreshDays=d;}
 @Transactional public TokenResponse login(LoginRequest request){var user=repo.findActiveByEmail(request.email()).filter(u->passwords.matches(request.senha(),u.passwordHash())).orElseThrow(()->new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Credenciais inválidas"));return issue(user);}
 @Transactional public TokenResponse refresh(RefreshTokenRequest request){String hash=hash(request.refreshToken());var user=repo.findActiveByRefreshHash(hash).orElseThrow(()->new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Refresh token inválido"));repo.revoke(hash);return issue(user);}
 @Transactional public void logout(RefreshTokenRequest request){repo.revoke(hash(request.refreshToken()));}
 private TokenResponse issue(AuthRepository.AuthenticatedUser user){String refresh=randomToken();repo.saveRefresh(user.id(),UUID.randomUUID(),hash(refresh),refreshDays);return new TokenResponse(jwt.accessToken(user.id(),user.tenantId(),user.nome(),repo.permissions(user.id())),refresh,900);}
 private static String randomToken(){byte[] b=new byte[48];new SecureRandom().nextBytes(b);return Base64.getUrlEncoder().withoutPadding().encodeToString(b);}
 private static String hash(String value){try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));}catch(NoSuchAlgorithmException e){throw new IllegalStateException(e);}}
}
