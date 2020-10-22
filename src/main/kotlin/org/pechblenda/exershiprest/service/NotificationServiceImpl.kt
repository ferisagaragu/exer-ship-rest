package org.pechblenda.exershiprest.service

import org.pechblenda.exception.BadRequestException
import org.pechblenda.exershiprest.dao.INotificationDAO
import org.pechblenda.exershiprest.dao.IUserDAO
import org.pechblenda.exershiprest.entity.Notification
import org.pechblenda.exershiprest.service.`interface`.INotificationService
import org.pechblenda.rest.Response

import org.springframework.http.ResponseEntity
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

import java.util.UUID

@Service
class NotificationServiceImpl(
	private val notificationDAO: INotificationDAO,
	private val userDao: IUserDAO,
	private val response: Response,
	private val simpMessagingTemplate: SimpMessagingTemplate
): INotificationService {

	@Transactional(readOnly = true)
	override fun getNotifications(): ResponseEntity<Any> {
		val notifications = notificationDAO.findByUserNameAndSeeNot(
			SecurityContextHolder.getContext().authentication.name
		)

		return response.toListMap(notifications).ok()
	}

	@Transactional
	override fun setNotificationSee(notificationsSee: List<UUID>): ResponseEntity<Any> {
		notificationsSee.forEach { notificationUid ->
			val notification = notificationDAO.findById(notificationUid).orElseThrow {
				BadRequestException("Upps no se encuentra la notificación")
			}

			notification.see = true
		}

		return response.ok()
	}

	@Transactional
	override fun notify(userUid: UUID, title: String, message: String, type: String) {
		val user = userDao.findById(userUid)

		if (!user.isEmpty) {
			val userSearched = user.get()
			val notification = Notification()

			notification.title = title
			notification.message = message
			notification.type = type
			notification.user = userSearched

			simpMessagingTemplate.convertAndSend(
				"/notify/${userSearched.uid}",
				response.toMap(notificationDAO.save(notification))
			)
		}
	}

}