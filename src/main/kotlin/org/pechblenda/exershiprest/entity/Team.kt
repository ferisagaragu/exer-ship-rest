package org.pechblenda.exershiprest.entity

import org.pechblenda.rest.Response
import org.pechblenda.rest.annotation.Key
import org.pechblenda.rest.enum.DefaultValue
import org.pechblenda.rest.helper.ResponseList

import java.util.*

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.ManyToMany
import javax.persistence.Table

@Entity
@Table(name = "teams")
class Team(
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	var uid: UUID,
	var name: String,

	@ManyToMany
	var users: MutableList<User>
) {

	@Key(name = "users", autoCall = true, defaultNullValue = DefaultValue.JSON_ARRAY)
	fun convertRoles(): ResponseList {
		return Response().toListMap(users).exclude("password")
	}

}