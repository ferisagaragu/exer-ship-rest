package org.pechblenda.exershiprest.dao

import org.pechblenda.exershiprest.entity.Day
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import java.util.*

interface IDayDAO: CrudRepository<Day, UUID> {

	@Query(
		nativeQuery = true,
		value = "select days.* from days where " +
			"days.user_uid = :userUid " +
			"and to_char(days.date, 'DD-MM-YYYY') = to_char(now(), 'DD-MM-YYYY');"
	)
	fun findByUserUid(userUid: UUID): Optional<Day>

}