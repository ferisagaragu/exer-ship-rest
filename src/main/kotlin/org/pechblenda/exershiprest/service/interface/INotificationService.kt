package org.pechblenda.exershiprest.service.`interface`

import org.pechblenda.exershiprest.enum.NotificationType
import org.springframework.http.ResponseEntity

import java.util.UUID

interface INotificationService {
	@Throws fun findAllNotifications(): ResponseEntity<Any>
	@Throws fun setNotificationSee(notificationSeeUid: UUID): ResponseEntity<Any>
	@Throws fun notify(userUid: UUID, title: String, message: String, go: String?, notificationType: NotificationType)
}