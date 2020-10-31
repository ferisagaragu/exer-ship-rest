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

	@GetMapping("/test")
	fun data(): String {
		/*val data = mutableMapOf<String, String>()
		data["id"] = "1"
		data["saludo"] = "como estas 🍕"
		firebaseDatabase.post("/notifications", data)
		firebaseDatabase.delete("/notifications/b752c269-93b1-44f2-b0e3-26c032d6de43")*/
		notificationService.notify(
			UUID.fromString("804d95c0-1556-4982-a34f-c49875427af8"),
			"Demo",
			"Hola Amigo 🌭",
			"/hola-adios",
			NotificationType.INFO
		)

		notificationService.notify(
			UUID.fromString("8238a1f5-9287-42e7-b764-460b9a5d1285"),
			"Demo",
			"Hola Amigo 🌭",
			null,
			NotificationType.WARNING
		)

		notificationService.notify(
			UUID.fromString("8238a1f5-9287-42e7-b764-460b9a5d1285"),
			"Demo",
			"Hola Amigo 🌭",
			null,
			NotificationType.ERROR
		)

		return "hola ❤🧡💛💚💙💜🤎🖤";
	}

  /*@MessageMapping("/socket")
  @SendTo("/message")
  fun greeting(message: String): String {
    println(message)
    simpMessagingTemplate.convertAndSend("/message", "Hola wey")
    return "hola 12"
  }*/

}