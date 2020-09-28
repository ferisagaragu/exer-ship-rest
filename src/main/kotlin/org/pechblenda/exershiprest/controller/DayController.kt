package org.pechblenda.exershiprest.controller

import org.pechblenda.exception.HttpExceptionResponse
import org.pechblenda.exershiprest.service.`interface`.IDayService
import org.pechblenda.rest.Request
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@CrossOrigin(methods = [
	RequestMethod.POST
])
@RestController
@RequestMapping("/days")
class DayController(
	private val dayService: IDayService,
	private val httpExceptionResponse: HttpExceptionResponse
) {

	@PostMapping
	fun saveDay(
		@RequestBody request: Request
	): ResponseEntity<Any> {
		return try {
			dayService.saveDay(request)
		} catch (e: ResponseStatusException) {
			httpExceptionResponse.error(e)
		}
	}

}