package info.pmarquezh.sports.keglerback.model.person

import java.time.LocalDateTime

class Person (personId: String,
              firstName: String,
              lastName: String,
              phone: String = "",
              email: String = "",
              creationTimestamp: LocalDateTime,
              active: Boolean)