package org.pechblenda.exershiprest.entity

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.ManyToOne
import javax.persistence.Table

import java.util.UUID
import javax.persistence.Column

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

	@ManyToOne
	var user: User
) {

	constructor(): this(
		uid = UUID.randomUUID(),
		title = "",
		message = "",
		type = "",
		see = false,
		user = User()
	)

}