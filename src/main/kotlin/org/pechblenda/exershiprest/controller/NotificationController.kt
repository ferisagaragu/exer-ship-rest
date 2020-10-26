package org.pechblenda.exershiprest.controller

import org.pechblenda.exception.HttpExceptionResponse
import org.pechblenda.exershiprest.enum.NotificationType
import org.pechblenda.exershiprest.service.`interface`.INotificationService

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

import java.util.UUID

@CrossOrigin(methods = [
	RequestMethod.GET,
  RequestMethod.PATCH
])
@RestController
@RequestMapping("/notifications")
class NotificationController(
  private val notificationService: INotificationService,
  private val httpExceptionResponse: HttpExceptionResponse
) {

	@GetMapping
	fun getNotifications(): ResponseEntity<Any> {
		return try {
			notificationService.getNotifications()
		} catch (e: ResponseStatusException) {
			httpExceptionResponse.error(e)
		}
	}

	@PatchMapping("/set-see/{notificationSeeUid}")
	fun setNotificationSee(
		@PathVariable("notificationSeeUid") notificationSeeUid: UUID
	): ResponseEntity<Any> {
		return try {
			notificationService.setNotificationSee(notificationSeeUid)
		} catch (e: ResponseStatusException) {
			httpExceptionResponse.error(e)
		}
	}

}