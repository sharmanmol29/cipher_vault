package com.ciphervault.security.user;

import com.ciphervault.entity.User;
import com.ciphervault.enums.Role;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
public class CustomUserDetails implements UserDetails {

	private final Long id;
	private final String email;
	private final String password;
	private final Role role;
	private final boolean emailVerified;

	public CustomUserDetails(Long id, String email, String password, Role role, boolean emailVerified) {
		this.id = id;
		this.email = email;
		this.password = password;
		this.role = role;
		this.emailVerified = emailVerified;
	}

	public static CustomUserDetails fromUser(User user) {
		return new CustomUserDetails(
				user.getId(),
				user.getEmail(),
				user.getPasswordHash(),
				user.getRole(),
				user.isEmailVerified()
		);
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public String getUsername() {
		return email;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}
}
