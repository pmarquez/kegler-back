package info.pmarquezh.sports.keglerback.model.game

import java.time.LocalDateTime

class Game ( playerId: String,
             gameDate: LocalDateTime = LocalDateTime.now ( ),
             gameId: String,
             gameName: String = "",
             shots: String = "",
             targetCheck: Int = 0 )
