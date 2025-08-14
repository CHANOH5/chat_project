package com.whispeer.chat.auth.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
@Builder
public class LoginRequestDTO {

    private String id;
    private String password;

    public LoginRequestDTO(String id, String password) {
        this.id = id;
        this.password = password;
    } // constructor

    @Override
    public String toString() {
        return "LoginRequestDTO{" +
                "id='" + id + '\'' +
                ", password='" + password + '\'' +
                '}';
    } // toString

} // end class
