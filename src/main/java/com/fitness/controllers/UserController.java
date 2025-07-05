package com.fitness.controllers;

import com.fitness.dto.ChangePasswordRequest;
import com.fitness.dto.UpdateUserRequest;
import com.fitness.dto.UserDTO;
import com.fitness.services.interfaces.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "User", description = "Управление пользователями")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;



    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'USER_PRO', 'ADMIN', 'DEV')")
    public ResponseEntity<UserDTO> getUser(@PathVariable Long id) {
        UserDTO userDTO = userService.getUser(id);
        return ResponseEntity.ok(userDTO);
    }
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DEV')")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'USER_PRO', 'ADMIN', 'DEV')")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest dto) {
        return ResponseEntity.ok(userService.updateUser(id, dto));
    }
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER','USER_PRO','ADMIN','DEV')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
    @PutMapping("/{id}/password")
    @PreAuthorize("hasAnyRole('USER','USER_PRO','ADMIN','DEV')")
    public ResponseEntity<Void> changePassword(
            @PathVariable Long id,
            @Valid @RequestBody ChangePasswordRequest req
    ) {
        userService.changePassword(id, req);
        return ResponseEntity.noContent().build();
    }


}
