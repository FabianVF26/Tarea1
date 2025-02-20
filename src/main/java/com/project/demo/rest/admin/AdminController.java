package com.project.demo.rest.admin;

import com.project.demo.logic.entity.rol.Role;
import com.project.demo.logic.entity.rol.RoleEnum;
import com.project.demo.logic.entity.rol.RoleRepository;
import com.project.demo.logic.entity.user.User;
import com.project.demo.logic.entity.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RequestMapping("/admin")
@RestController
public class AdminController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AdminController(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public User createSuperAdmin(@RequestBody User newSuperAdminUser) {
        if (newSuperAdminUser.getName() == null || newSuperAdminUser.getEmail() == null || newSuperAdminUser.getPassword() == null) {
            throw new IllegalArgumentException("Los campos nombre, email y contraseña son obligatorios.");
        }

        Optional<Role> optionalRole = roleRepository.findByName(RoleEnum.SUPER_ADMIN);
        if (optionalRole.isEmpty()) {
            throw new IllegalStateException("El rol SUPER_ADMIN no está configurado en la base de datos.");
        }

        User user = new User();
        user.setName(newSuperAdminUser.getName());
        user.setEmail(newSuperAdminUser.getEmail());
        user.setPassword(passwordEncoder.encode(newSuperAdminUser.getPassword()));
        user.setRole(optionalRole.get());

        return userRepository.save(user);
    }
}