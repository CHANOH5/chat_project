package com.whispeer.chat.user.controller;

import com.whispeer.chat.user.dto.UserRegisterDTO;
import com.whispeer.chat.user.dto.UserResponseDTO;
import com.whispeer.chat.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.whispeer.chat.common.ApiPrefix.API_V1;

@RestController
@RequestMapping(API_V1 + "/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserResponseDTO> createUser(@RequestBody UserRegisterDTO dto) {
        System.out.println("1");
        return ResponseEntity.ok(userService.createUser(dto));
    } // createUser

    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.findAll());
    } // getAllUsers

    @PutMapping
    public ResponseEntity<UserResponseDTO> updateUser(@RequestBody UserRegisterDTO dto) {
        return ResponseEntity.ok(userService.updateUser(dto));
    } // updateUser

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable String userId) {
        userService.deleteUser(userId);
        return ResponseEntity.ok().body("회원이 삭제되었습니다.");
    } // deleteUser

} // end class
