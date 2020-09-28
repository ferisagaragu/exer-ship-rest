package org.pechblenda.exershiprest.service

import org.pechblenda.exershiprest.service.`interface`.IDayService
import org.pechblenda.rest.Request
import org.pechblenda.rest.Response
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class IDayServiceImpl(
	private val response: Response
): IDayService {

	@Transactional
	override fun saveDay(request: Request): ResponseEntity<Any> {


		return response.ok("hola dev")
	}

}