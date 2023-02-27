package openmam.mediamicroservice.security.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class AuthenticationProviderImpl implements AuthenticationManager {

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        var username = authentication.getPrincipal() + "";
        var password = authentication.getCredentials() + "";
        var user = userDetailsService.loadUserByUsername(username);
        var result = passwordEncoder.matches(password, user.getPassword());
        if (result) {
            var grantedAuths = new ArrayList<GrantedAuthority>();
            grantedAuths.addAll(user.getAuthorities());
            return new UsernamePasswordAuthenticationToken(username, null, grantedAuths);
        }
        return null;
    }
}
