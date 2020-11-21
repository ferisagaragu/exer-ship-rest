package org.pechblenda.exershiprest.service.message

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class NotificationMessage(
	@Value("\${message.auth.user-not-fount}")
	val userNotFount: String,
	@Value("\${message.notification.notification-not-Fount}")
	val notificationNotFount: String,
	@Value("\${message.notification.notification-is-see}")
	val notificationIsSee: String
)