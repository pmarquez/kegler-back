package info.pmarquezh.sports.keglerback.model.game

import java.time.LocalDateTime

class Game ( playerId: String,
             playerName: String = "",
             playerLastName: String = "",
             gameDate: LocalDateTime = LocalDateTime.now ( ),
             gameId: String,
             gameName: String = "",
             shots: String = "",
             targetCheck: Int = 0 )
