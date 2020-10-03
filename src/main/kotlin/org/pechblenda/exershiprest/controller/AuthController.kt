package org.pechblenda.exershiprest.controller

import org.pechblenda.exception.HttpExceptionResponse
import org.pechblenda.rest.Request
import org.pechblenda.exershiprest.service.`interface`.IAuthService

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

import java.util.UUID

@CrossOrigin(methods = [
	RequestMethod.GET,
	RequestMethod.POST
])
@RestController
@RequestMapping("/auth")
class AuthController(
	private val authService: IAuthService,
	private val httpExceptionResponse: HttpExceptionResponse
) {

	@GetMapping("/can-activate-account/{userUid}")
	fun canActivate(
		@PathVariable("userUid") userUid: UUID
	): ResponseEntity<Any> {
		return try {
			authService.canActivate(userUid)
		} catch (e: ResponseStatusException) {
			httpExceptionResponse.error(e)
		}
	}

	@GetMapping("/can-change-password/{activatePassword}")
	fun canChangePassword(
		@PathVariable("activatePassword") activatePassword: UUID
	): ResponseEntity<Any> {
		return try {
			authService.canChangePassword(activatePassword)
		} catch (e: ResponseStatusException) {
			httpExceptionResponse.error(e)
		}
	}

	@PostMapping("/activate-account")
	fun activateAccount(
		@RequestBody request: Request
	): ResponseEntity<Any> {
		return try {
			authService.activateAccount(request)
		} catch (e: ResponseStatusException) {
			httpExceptionResponse.error(e)
		}
	}

	@PostMapping("/change-password")
	fun changePassword(
		@RequestBody request: Request
	): ResponseEntity<Any> {
		return try {
			authService.changePassword(request)
		} catch (e: ResponseStatusException) {
			httpExceptionResponse.error(e)
		}
	}

	@PostMapping("/recover-password")
	fun recoverPasswordEmail(
		@RequestBody request: Request
	): ResponseEntity<Any> {
		return try {
			authService.recoverPassword(request)
		} catch (e: ResponseStatusException) {
			httpExceptionResponse.error(e)
		}
	}

	@PostMapping("/sign-up")
	fun signUp(
		@RequestBody request: Request
	): ResponseEntity<Any> {
		return try {
			authService.signUp(request)
		} catch (e: ResponseStatusException) {
			httpExceptionResponse.error(e)
		}
	}

	@PostMapping("/sign-in")
	fun signIn(
		@RequestBody request: Request
	): ResponseEntity<Any> {
		return try {
			authService.signIn(request)
		} catch (e: ResponseStatusException) {
			httpExceptionResponse.error(e)
		}
	}

}