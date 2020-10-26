package org.pechblenda.exershiprest.entity

import java.util.UUID
import java.util.Date

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table
import javax.persistence.Column
import javax.persistence.ManyToOne
import javax.persistence.Temporal
import javax.persistence.TemporalType

@Entity
@Table(name = "notifications")
class Notification(
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	var uid: UUID,
	var title: String,
	var message: String,
	var type: String,

	@Column(columnDefinition = "boolean default false")
	var see: Boolean,

	@Temporal(TemporalType.TIMESTAMP)
	var createDate: Date,

	var go: String?,

	@ManyToOne
	var user: User
) {

	constructor(): this(
		uid = UUID.randomUUID(),
		title = "",
		message = "",
		type = "",
		see = false,
		createDate = Date(),
		go = null,
		user = User()
	)

}