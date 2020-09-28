package org.pechblenda.exershiprest.service.`interface`

import org.pechblenda.rest.Request
import org.springframework.http.ResponseEntity

interface IDayService {
	@Throws fun saveDay(request: Request): ResponseEntity<Any>
}