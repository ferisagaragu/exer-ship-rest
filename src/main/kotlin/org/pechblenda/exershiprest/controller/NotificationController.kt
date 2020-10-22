package org.pechblenda.exershiprest.controller

import org.pechblenda.exception.HttpExceptionResponse
import org.pechblenda.exershiprest.service.`interface`.INotificationService

import org.springframework.http.ResponseEntity
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

import java.util.UUID

@CrossOrigin(methods = [
  RequestMethod.GET,
  RequestMethod.POST,
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

	@PatchMapping
	fun setNotificationSee(@RequestBody notificationsSee: List<UUID>): ResponseEntity<Any> {
		return try {
			notificationService.setNotificationSee(notificationsSee)
		} catch (e: ResponseStatusException) {
			httpExceptionResponse.error(e)
		}
	}

  /*@MessageMapping("/socket")
  @SendTo("/message")
  fun greeting(message: String): String {
    println(message)
    simpMessagingTemplate.convertAndSend("/message", "Hola wey")
    return "hola 12"
  }*/

}