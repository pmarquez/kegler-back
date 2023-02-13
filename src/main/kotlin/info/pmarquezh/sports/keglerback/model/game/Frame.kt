package info.pmarquezh.sports.keglerback.model.game

import info.pmarquezh.sports.keglerback.enums.FrameStatus

class Frame(var first: String = " ",
            var second: String = " ",
            var extra: String = " ") {
    var pinCount = 0
    var frameWeight = 0
    var runningScore = 0
    var status = FrameStatus.PENDING
}