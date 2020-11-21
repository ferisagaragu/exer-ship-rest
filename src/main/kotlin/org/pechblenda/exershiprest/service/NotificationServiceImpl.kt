package org.pechblenda.exershiprest.service

import org.pechblenda.database.FirebaseDatabase
import org.pechblenda.exception.BadRequestException
import org.pechblenda.exershiprest.dao.INotificationDAO
import org.pechblenda.exershiprest.dao.IUserDAO
import org.pechblenda.exershiprest.entity.Notification
import org.pechblenda.exershiprest.enum.NotificationType
import org.pechblenda.exershiprest.service.`interface`.INotificationService
import org.pechblenda.exershiprest.service.message.NotificationMessage
import org.pechblenda.rest.Response
import org.pechblenda.rest.helper.ResponseMap

import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

import java.util.UUID

@Service
class NotificationServiceImpl(
	private val notificationDAO: INotificationDAO,
	private val notificationMessage: NotificationMessage,
	private val userDao: IUserDAO,
	private val response: Response,
	private val firebaseDatabase: FirebaseDatabase
): INotificationService {

	@Transactional(readOnly = true)
	override fun findAllNotifications(): ResponseEntity<Any> {
		val userName = SecurityContextHolder.getContext().authentication.name

		return response.toListMap(
			notificationDAO.findByUserNameAndSeeNot(userName)
		).ok()
	}

	@Transactional
	override fun setNotificationSee(notificationSeeUid: UUID): ResponseEntity<Any> {
		val user = userDao.findByUserName(
			SecurityContextHolder.getContext().authentication.name
		).orElseThrow { BadRequestException(notificationMessage.userNotFount) }

		val notification = notificationDAO.findById(notificationSeeUid).orElseThrow {
			BadRequestException(notificationMessage.notificationNotFount)
		}

		if (notification.see) {
			throw BadRequestException(notificationMessage.notificationIsSee)
		}

		notification.see = true
		firebaseDatabase.delete("/notifications/${user.uid}/${notification.uid}")

		return response.toMap(notification).ok()
	}

	@Transactional
	override fun notify(
		userUid: UUID,
		title: String,
		message: String,
		go: String?,
		notificationType: NotificationType
	) {
		val user = userDao.findById(userUid)

		if (!user.isEmpty) {
			val userSearched = user.get()
			val out: ResponseMap
			var notification = Notification()

			notification.title = title
			notification.message = message
			notification.type = notificationType.type
			notification.go = go
			notification.user = userSearched
			notification = notificationDAO.save(notification)

			out = response.toMap(notification)

			firebaseDatabase.put(
				"/notifications/${userSearched.uid}/${notification.uid}",
				out
			)
		}
	}

}