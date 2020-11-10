package org.pechblenda.exershiprest.entity

import java.util.UUID
import javax.persistence.*

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
		directory = "users",
		contentType = "image/png",
		name = UUID.randomUUID().toString(),
		extension = ".png",
		url = ""
	)

}