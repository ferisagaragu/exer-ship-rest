package org.pechblenda.exershiprest.dao

import org.pechblenda.exershiprest.entity.Notification
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository

import java.util.UUID

interface INotificationDAO: CrudRepository<Notification, UUID> {

	@Query(
		"select notification from Notification notification " +
		"inner join notification.user user where user.userName = :userName " +
		"and notification.see = false order by notification.createDate asc"
	)
	fun findByUserNameAndSeeNot(userName: String): List<Notification>

}
