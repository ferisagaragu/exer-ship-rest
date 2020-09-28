package org.pechblenda.exershiprest.service.`interface`

import org.pechblenda.rest.Request
import org.springframework.http.ResponseEntity

import java.util.UUID

interface IAuthService {
	@Throws fun canActivate(userUid: UUID): ResponseEntity<Any>
	@Throws fun signUp(request: Request): ResponseEntity<Any>
	@Throws fun signIn(request: Request): ResponseEntity<Any>
	@Throws fun activateAccount(request: Request): ResponseEntity<Any>
	@Throws fun recoverPassword(request: Request): ResponseEntity<Any>
}