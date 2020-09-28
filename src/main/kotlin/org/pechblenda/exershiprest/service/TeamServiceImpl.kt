package org.pechblenda.exershiprest.service

import org.pechblenda.exershiprest.dao.IDayDAO
import org.pechblenda.exershiprest.dao.ITeamDAO
import org.pechblenda.exershiprest.entity.Team
import org.pechblenda.exershiprest.service.`interface`.ITeamService
import org.pechblenda.rest.Response

import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

import java.util.*

@Service
class TeamServiceImpl(
	private val teamDAO: ITeamDAO,
	private val dayDAO: IDayDAO,
	private val response: Response
): ITeamService {

	@Transactional(readOnly = true)
	override fun findByUserUid(uid: UUID): ResponseEntity<Any> {
		val team: Team = teamDAO.findByUserUID(uid)
		val responseMap = response.toMap(team)

		(responseMap["users"] as MutableList<MutableMap<String, Any>>).map {
			user ->
				val day = dayDAO.findByUserUid(user["uid"] as UUID).orElse(null)
				user["day"] = if (day != null) response.toMap(day) else emptyMap<String, Any>()
		}

		return responseMap.ok()
	}

}