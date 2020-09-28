package org.pechblenda.exershiprest.service.`interface`

import org.springframework.http.ResponseEntity
import java.util.*

interface ITeamService {
	@Throws fun findByUserUid(uid: UUID): ResponseEntity<Any>
}