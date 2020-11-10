package org.pechblenda.exershiprest.entity

import org.pechblenda.rest.annotation.Key
import org.pechblenda.rest.enum.DefaultValue

import java.util.UUID
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table
import javax.persistence.Id
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.ManyToMany
import javax.persistence.OneToOne

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

	@OneToOne
	var photo: Storage?,

	var refreshToken: String?,

	@ManyToMany
	var roles: MutableList<Role>
) {

	constructor(): this(
		uid = UUID.randomUUID(),
		name = "",
		lastName = "",
		userName = "",
		email = "",
		password = "",
		enabled = false,
		active = false,
		activatePassword = UUID.randomUUID(),
		photo = null,
		refreshToken = "",
		roles = mutableListOf<Role>()
	)

	@Key(name = "roles", autoCall = true, defaultNullValue = DefaultValue.JSON_ARRAY)
	fun convertRoles(): Any {
		return roles
	}

	@Key(name = "photo", autoCall = true, defaultNullValue = DefaultValue.TEXT)
	fun convertPhoto(): String {
		return photo!!.url
	}

	@Key(name = "photoFile", defaultNullValue = DefaultValue.TEXT)
	fun convertPhotoName(): String {
		return "${photo!!.name}${photo!!.extension}"
	}

}