package org.pechblenda.exershiprest.entity

import org.pechblenda.rest.annotation.Required

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

	@Required(message = "El nombre es requerido")
	var name: String,

	var lastName: String,

	@Column(unique = true)
	@Required(message = "El nombre de usuario es requerido")
	var userName: String,

	@Column(unique = true)
	@Required(message = "El correo electrónico es requerido")
	var email: String,

	var password: String,

	@Column(columnDefinition = "boolean default false")
	var enabled: Boolean,

	@Column(columnDefinition = "boolean default false")
	var active: Boolean,

	@Lob
	var photo: String,

	@ManyToMany
	var roles: MutableList<Role>
) {

	/*@Key(name = "roles", autoCall = true, defaultNullValue = DefaultValue.JSON_ARRAY)
	fun convertRoles(): Any {
		return roles
	}*/

}