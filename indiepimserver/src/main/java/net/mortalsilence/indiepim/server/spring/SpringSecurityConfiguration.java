package net.mortalsilence.indiepim.server.spring;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Configuration
@EnableWebSecurity
@Controller
public class SpringSecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Override
    public void configure(WebSecurity web) {
        web.ignoring().antMatchers("*.bundle.*");
    }



    @GetMapping(value = "/frontend/{path:[^\\.]*}/**")
    public String redirect() {
        return "forward:/";
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {


        http
            .httpBasic()
                .and()
            .formLogin()
                .and()
            .authorizeRequests()
            .antMatchers("/frontend/**", "/assets/**", "/*").permitAll()
            .anyRequest().authenticated()
                .and().csrf().disable()
        ;
    }
}
