package org.pechblenda.exershiprest.service.`interface`

import org.pechblenda.rest.Request

import org.springframework.http.ResponseEntity

interface IUserService {
	@Throws fun findUser(): ResponseEntity<Any>
	@Throws fun putUser(request: Request): ResponseEntity<Any>
}