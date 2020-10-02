package org.pechblenda.exershiprest.dao

import org.pechblenda.exershiprest.entity.User
import org.springframework.data.jpa.repository.Query

import org.springframework.data.repository.CrudRepository

import java.util.UUID
import java.util.Optional


interface IUserDAO: CrudRepository<User, UUID> {

	fun findByUserName(userName: String): Optional<User>

	fun findByActivatePassword(activatePassword: UUID?): Optional<User>

	fun existsByUserName(userName: String): Boolean

	fun existsByEmail(email: String): Boolean

	fun existsByActivatePassword(activatePassword: UUID): Boolean

	@Query(
		"select user from User user where " +
		"user.email = :userName or user.userName = :userName"
	)
	fun findByUserNameOrEmail(userName: String): Optional<User>

}