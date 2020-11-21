package org.pechblenda.exershiprest.entity

import java.util.UUID

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Lob
import javax.persistence.Table

@Entity
@Table(name = "storage")
class Storage(
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	var uid: UUID,
	var name: String,
	var directory: String,
	var contentType: String,
	var extension: String,

	@Lob
	var url: String
) {

	constructor(): this(
		uid = UUID.randomUUID(),
		directory = "",
		contentType = "",
		name = "",
		extension = "",
		url = ""
	)

}