package org.pechblenda.exershiprest.controller

import org.pechblenda.rest.Response
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
		RequestMethod.GET,
		RequestMethod.POST
])
@RestController
@RequestMapping("/security")
class SecurityController(
	val response: Response
) {

	@GetMapping
	fun canActivate(): ResponseEntity<Any> {
		return response.ok("hola")
	}

}