package dev.hireben.url_shortener.auth.service;

public interface AuthService {

  void register(String email, String password);

  String login(String email, String password);

}
