package jp.co.cybermisisons.itspj.java.demo.services;

import java.util.Collections;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import jp.co.cybermisisons.itspj.java.demo.models.AppUser;
import jp.co.cybermisisons.itspj.java.demo.models.AppUserRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class MyUserService implements UserDetailsService {

  private final AppUserRepository rep;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    AppUser usr = rep.findByUsername(username);
    if (usr != null) {
      return new User(username, usr.getPassword(), Collections.emptySet());
    } else {
      // ユーザーが見つからない場合
      throw new UsernameNotFoundException("User is not found.");
    }
  }
}
