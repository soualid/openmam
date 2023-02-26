package openmam.mediamicroservice.security.entities;

import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class SpringSecurityOpenMamUser extends org.springframework.security.core.userdetails.User {


    private final User user;

    public SpringSecurityOpenMamUser(User user, String username, String password, Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
        this.user = user;
    }

    public SpringSecurityOpenMamUser(User user, String username, String password, boolean enabled, boolean accountNonExpired, boolean credentialsNonExpired, boolean accountNonLocked, Collection<? extends GrantedAuthority> authorities) {
        super(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities);
        this.user = user;
    }

    public User getUser() {
        return user;
    }
}
