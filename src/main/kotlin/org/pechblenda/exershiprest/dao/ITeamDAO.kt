package org.pechblenda.exershiprest.dao

import org.pechblenda.exershiprest.entity.Team
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import java.util.*

interface ITeamDAO: CrudRepository<Team, UUID> {

	@Query(
		"select teams from Team teams " +
		"inner join teams.users user where user.uid = :userUID "
	)
	fun findByUserUID(userUID: UUID): Team

}