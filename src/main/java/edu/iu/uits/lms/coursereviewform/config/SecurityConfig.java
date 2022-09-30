package edu.iu.uits.lms.coursereviewform.config;

import edu.iu.uits.lms.common.oauth.CustomJwtAuthenticationConverter;
import edu.iu.uits.lms.lti.service.LmsDefaultGrantedAuthoritiesMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import uk.ac.ox.ctl.lti13.Lti13Configurer;

import static edu.iu.uits.lms.lti.LTIConstants.BASE_USER_ROLE;
import static edu.iu.uits.lms.lti.LTIConstants.WELL_KNOWN_ALL;

@Configuration
public class SecurityConfig {
    @Configuration
    @Order(SecurityProperties.BASIC_AUTH_ORDER - 5)
    public static class RestSecurityConfigurationAdapter extends WebSecurityConfigurerAdapter {

        @Override
        public void configure(HttpSecurity http) throws Exception {
            http.requestMatchers().antMatchers("/rest/db/**")
                    .and()
                    .authorizeRequests()
                    .antMatchers("/rest/db/**")
                    .access("hasAuthority('SCOPE_lms:rest') and hasAuthority('ROLE_LMS_REST_ADMINS')")
                    .and()
                    .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                    .and()
                    .oauth2ResourceServer()
                    .jwt().jwtAuthenticationConverter(new CustomJwtAuthenticationConverter());
        }
    }

    @Configuration
    @Order(SecurityProperties.BASIC_AUTH_ORDER - 4)
    public static class OpenEndpointConfig extends WebSecurityConfigurerAdapter {

        @Override
        public void configure(HttpSecurity http) throws Exception {
            http.requestMatchers().antMatchers("/rest/submit/**", "/api/**")
                    .and()
                    .authorizeRequests()
                    .antMatchers("/api/**").permitAll()
                    .antMatchers("/rest/submit/**").permitAll();
        }
    }

    @Configuration
    @Order(SecurityProperties.BASIC_AUTH_ORDER - 3)
    public static class AppWebSecurityConfigurerAdapter extends WebSecurityConfigurerAdapter {
        @Autowired
        private LmsDefaultGrantedAuthoritiesMapper lmsDefaultGrantedAuthoritiesMapper;

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http
                    .requestMatchers()
                    .antMatchers(WELL_KNOWN_ALL, "/app/**")
                    .and()
                    .authorizeRequests()
                    .antMatchers("/**").hasRole(BASE_USER_ROLE);

            // Set up the LTI handshake
            Lti13Configurer lti13Configurer = new Lti13Configurer()
                    .grantedAuthoritiesMapper(lmsDefaultGrantedAuthoritiesMapper);
            http.apply(lti13Configurer);

            //Fallback for everything else
            http.requestMatchers().antMatchers("/**")
                    .and()
                    .authorizeRequests()
                    .anyRequest().authenticated();
        }

        @Override
        public void configure(WebSecurity web) throws Exception {
            // ignore everything except paths specified
            web.ignoring().antMatchers("/app/jsrivet/**", "/app/webjars/**", "/actuator/**", "/app/css/**", "/app/js/**");
        }

    }

    @Configuration
    @Order(SecurityProperties.BASIC_AUTH_ORDER - 2)
    public static class CatchAllSecurityConfig extends WebSecurityConfigurerAdapter {

        @Override
        public void configure(HttpSecurity http) throws Exception {
            http.requestMatchers().antMatchers("/**")
                  .and()
                  .authorizeRequests()
                  .anyRequest().authenticated();
        }
    }
}
