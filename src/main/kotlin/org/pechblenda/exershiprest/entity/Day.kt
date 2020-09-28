package org.pechblenda.exershiprest.entity

import java.util.*
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.ManyToOne
import javax.persistence.Table

@Entity
@Table(name = "days")
class Day(
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	var uid: UUID?,
	var date: Date?,
	var exercise: Boolean,
	var diet: Boolean,

	@ManyToOne
	var user: User?
)