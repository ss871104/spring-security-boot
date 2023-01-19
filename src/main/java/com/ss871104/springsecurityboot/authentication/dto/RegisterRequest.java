package com.ss871104.springsecurityboot.authentication.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
    @NotBlank(message = "姓名不得為空")
    private String name;
    @Size(min = 8,  max = 20, message = "帳號長度不得低於 8 或大於 20")
    private String username;
    @Size(min = 8,  max = 20, message = "密碼長度不得低於 8 或大於 20")
    private String password;
    @Size(max = 255, message = "Email 長度不得大於 255")
    @Email(message = "Email 格式不正確")
    @NotBlank(message = "信箱不得為空")
    private String email;
}
