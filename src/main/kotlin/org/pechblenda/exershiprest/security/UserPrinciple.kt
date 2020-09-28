package org.pechblenda.exershiprest.security

import com.fasterxml.jackson.annotation.JsonIgnore

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

import java.util.UUID

import org.pechblenda.exershiprest.entity.User
import org.springframework.security.core.authority.SimpleGrantedAuthority


class UserPrinciple(
	val id: UUID,
	private val userName: String,

	@JsonIgnore
	private val password: String,

	private val authority: MutableCollection<out GrantedAuthority>
) : UserDetails {

	companion object {
		@JvmStatic
		fun build(user: User): UserPrinciple {
			val authorities: List<SimpleGrantedAuthority> = user.roles.map {
				role -> SimpleGrantedAuthority(role.name)
			}

			return UserPrinciple(
				user.uid,
				user.userName,
				user.password,
				authorities as MutableCollection<out GrantedAuthority>
			)
		}
	}


	override fun getAuthorities(): MutableCollection<out GrantedAuthority> {
		return authority
	}

	override fun getUsername(): String {
		return userName
	}

	override fun getPassword(): String {
		return password
	}

	override fun isAccountNonExpired(): Boolean {
		return true
	}

	override fun isAccountNonLocked(): Boolean {
		return true
	}

	override fun isCredentialsNonExpired(): Boolean {
		return true
	}

	override fun isEnabled(): Boolean {
		return true
	}

}
