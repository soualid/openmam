package openmam.mediamicroservice.security.configuration;

import openmam.mediamicroservice.security.filters.JwtRequestFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.access.vote.RoleHierarchyVoter;
import org.springframework.security.authorization.AuthorityAuthorizationManager;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true)
public class JwtSecurityConfig {

    private static final Logger log = LoggerFactory.getLogger(JwtSecurityConfig.class);

    @Autowired
    private JwtRequestFilter jwtRequestFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public RoleHierarchy roleHierarchy() {
        RoleHierarchyImpl r = new RoleHierarchyImpl();
        r.setHierarchy("ROLE_ADMIN > ROLE_USER > ROLE_PARTNER");
        return r;
    }

    @Bean
    AccessDecisionVoter hierarchyVoter() {
        return new RoleHierarchyVoter(roleHierarchy());
    }

    @Bean
    public SecurityFilterChain configure(final HttpSecurity http) throws Exception {

        var auth = AuthorityAuthorizationManager.<RequestAuthorizationContext>hasAnyRole("PARTNER", "USER", "ADMIN");
        auth.setRoleHierarchy(roleHierarchy());

        return http.cors().and()
                .csrf().disable()
                .authorizeHttpRequests()
                        .requestMatchers("/", "/authenticate").permitAll()
                        .requestMatchers("/static/**").permitAll()
                        .requestMatchers("/my/uploadRequests").hasRole("PARTNER")
                        .anyRequest().access(auth).and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

}