package org.pechblenda.exershiprest.controller

import org.pechblenda.exception.HttpExceptionResponse
import org.pechblenda.exershiprest.service.`interface`.ITeamService

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

import java.util.*

@CrossOrigin(methods = [
	RequestMethod.GET
])
@RestController
@RequestMapping("/teams")
class TeamController(
	private val teamService: ITeamService,
	private val httpExceptionResponse: HttpExceptionResponse
) {

	@GetMapping("/by-user-uid/{uid}")
	fun findByUserUid(
		@PathVariable("uid") uid: UUID
	): ResponseEntity<Any> {
		return try {
			teamService.findByUserUid(uid)
		} catch (e: ResponseStatusException) {
			httpExceptionResponse.error(e)
		}
	}

}