package org.pechblenda.exershiprest.service.`interface`

import org.springframework.http.ResponseEntity
import java.util.UUID

interface INotificationService {
	@Throws fun getNotifications(): ResponseEntity<Any>
	@Throws fun setNotificationSee(notificationsSee: List<UUID>): ResponseEntity<Any>
	@Throws fun notify(userUid: UUID, title: String, message: String, type: String)
}