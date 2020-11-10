package org.pechblenda.exershiprest.controller

import org.pechblenda.exception.HttpExceptionResponse
import org.pechblenda.exershiprest.service.`interface`.IUserService
import org.pechblenda.rest.Request

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.server.ResponseStatusException

@CrossOrigin(methods = [
	RequestMethod.GET,
	RequestMethod.PUT
])
@RestController
@RequestMapping("/users")
class UserController(
	private val userService: IUserService,
	private val httpExceptionResponse: HttpExceptionResponse
) {

	@GetMapping
	fun findUser(): ResponseEntity<Any> {
		return try {
			userService.findUser()
		} catch (e: ResponseStatusException) {
			httpExceptionResponse.error(e)
		}
	}

	@PutMapping
	fun findUser(
		@RequestBody request: Request
	): ResponseEntity<Any> {
		return try {
			userService.putUser(request)
		} catch (e: ResponseStatusException) {
			httpExceptionResponse.error(e)
		}
	}

}