package org.pechblenda.exershiprest.entity

import org.pechblenda.rest.annotation.Key
import org.pechblenda.rest.enum.DefaultValue

import java.util.UUID

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Lob
import javax.persistence.ManyToMany
import javax.persistence.Table

@Entity
@Table(name = "users")
class User(
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	var uid: UUID,

	var name: String,

	var lastName: String,

	@Column(unique = true)
	var userName: String,

	@Column(unique = true)
	var email: String,

	var password: String,

	@Column(columnDefinition = "boolean default false")
	var enabled: Boolean,

	@Column(columnDefinition = "boolean default false")
	var active: Boolean,

	var activatePassword: UUID?,

	@Lob
	var photo: String,

	var refreshToken: String?,

	@ManyToMany
	var roles: MutableList<Role>
) {

	@Key(name = "roles", autoCall = true, defaultNullValue = DefaultValue.JSON_ARRAY)
	fun convertRoles(): Any {
		return roles
	}

}