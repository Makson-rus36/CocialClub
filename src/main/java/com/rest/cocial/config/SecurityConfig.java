package com.rest.cocial.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rest.cocial.domain.User;
import com.rest.cocial.repo.UserDetailsRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private UserDetailsRepo userDetailsRepo;


    private final ObjectMapper mapper;
    private final TokenStore tokenStore;
    private final TokenFilter tokenFilter;

    public SecurityConfig( ObjectMapper mapper, TokenStore tokenStore,
                           TokenFilter tokenFilter ) {
        this.mapper = mapper;
        this.tokenStore = tokenStore;
        this.tokenFilter = tokenFilter;
    }

    @Override
    protected void configure( HttpSecurity http ) throws Exception {
        http.csrf().disable().cors().and().authorizeRequests()
                .antMatchers( "/oauth2/**", "/login**" ).permitAll()
                .anyRequest().authenticated()
                .and()
                    .oauth2Login()
                        .authorizationEndpoint()
                            .authorizationRequestRepository( new InMemoryRequestRepository() )
                    .and()
                        .successHandler( this::successHandler )
                .and()
                    .exceptionHandling()
                        .authenticationEntryPoint( this::authenticationEntryPoint )
                .and()
                    .logout(cust -> cust.addLogoutHandler( this::logout )
                            .logoutSuccessHandler( this::onLogoutSuccess ));
        http.addFilterBefore( tokenFilter, UsernamePasswordAuthenticationFilter.class );
    }

    private void logout(HttpServletRequest request, HttpServletResponse response,
                        Authentication authentication) {
        // You can process token here
        System.out.println("Auth token is - " + request.getHeader( "Authorization" ));
    }

    void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response,
                         Authentication authentication) throws IOException, ServletException {
        // this code is just sending the 200 ok response and preventing redirect
        response.setStatus( HttpServletResponse.SC_OK );
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedMethods( Collections.singletonList( "*" ) );
        config.setAllowedOrigins( Collections.singletonList( "*" ) );
        config.setAllowedHeaders( Collections.singletonList( "*" ) );

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration( "/**", config );
        return source;
    }


    private void successHandler( HttpServletRequest request,
                                 HttpServletResponse response, Authentication authentication ) throws IOException {
        String token = tokenStore.generateToken( authentication );
        if(authentication.isAuthenticated())
        {
            DefaultOidcUser authenticationPrincipal = (DefaultOidcUser) authentication.getPrincipal();
            Optional<User> userOptional = userDetailsRepo.findByEmail( authenticationPrincipal.getEmail());
            if (!userOptional.isPresent()) {
                User user = new User();
                user.setEmail(authenticationPrincipal.getEmail());
                user.setName(authenticationPrincipal.getFullName());
                user.setUserpic(authenticationPrincipal.getPicture());
                user.setGender(authenticationPrincipal.getGender());
                user.setLocale(authenticationPrincipal.getLocale());
                user.setId(UUID.randomUUID().toString()+authenticationPrincipal.getEmail());
                userDetailsRepo.save(user);
            }
        }
        response.getWriter().write(
                mapper.writeValueAsString( Collections.singletonMap( "accessToken", token ) )
        );
    }

    private void authenticationEntryPoint( HttpServletRequest request, HttpServletResponse response,
                                           AuthenticationException authException ) throws IOException {
        response.setStatus( HttpServletResponse.SC_FORBIDDEN );
        response.getWriter().write( mapper.writeValueAsString( Collections.singletonMap( "error", "Unauthenticated" ) ) );
    }
}
