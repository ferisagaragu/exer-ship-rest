package org.pechblenda.exershiprest.dao

import org.pechblenda.exershiprest.entity.Storage
import org.springframework.data.repository.CrudRepository
import java.util.UUID

interface IStorageDAO : CrudRepository<Storage, UUID>