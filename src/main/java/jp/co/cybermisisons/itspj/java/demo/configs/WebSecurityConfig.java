package jp.co.cybermisisons.itspj.java.demo.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import jp.co.cybermisisons.itspj.java.demo.services.MyUserService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

  private final MyUserService userDetailsService;

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.authorizeRequests()
        // [login]だけを許可
        .antMatchers("/liquorshop").permitAll() //
        .antMatchers("/h2-console/**").permitAll()
        // その他のパスにはすべて認証が必要
        .anyRequest().authenticated().and()
        // フォーム認証
        .formLogin()
        // ログインページの設定
        .loginPage("/liquorshop").usernameParameter("usr").passwordParameter("passwd")
        .defaultSuccessUrl("/liquorshop/index", true).and()
        // ログアウン時の
        .logout().logoutRequestMatcher(new AntPathRequestMatcher("/logout")).logoutSuccessUrl("/admin/index")
        .invalidateHttpSession(true);
    // ajax data
    http.cors().and().csrf().disable();
    http.csrf().disable();
    http.headers().frameOptions().disable();
    // http.cors().and().csrf().disable();

  }

  // UserDetailServiceを登録
  @Override
  protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    auth.userDetailsService(userDetailsService)
        // password encoder
        .passwordEncoder(passwordEncoder());
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

}

// import org.springframework.context.annotation.Configuration;
// import
// org.springframework.security.config.annotation.web.builders.HttpSecurity;
// import
// org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
// import
// org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

// @Configuration
// @EnableWebSecurity
// public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

// @Override
// protected void configure(HttpSecurity http) throws Exception {
// http.authorizeRequests().anyRequest().permitAll();

// http.csrf().disable();
// http.headers().frameOptions().disable();
// }
// }
