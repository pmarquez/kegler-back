package info.pmarquezh.sports.keglerback.service

import info.pmarquezh.sports.keglerback.model.game.Frame
import lombok.extern.slf4j.Slf4j
import org.springframework.stereotype.Service

@Slf4j
@Service
class GameService () {
    val allowedValues: Map<String, Int> = mapOf( "X" to 10, "/" to -1, "F" to 0, "G" to 0, "-" to 0,
                                                 "0" to 0, "1" to 1, "2" to 2, "3" to 3, "4" to 4,
                                                 "5" to 5, "6" to 6, "7" to 7, "8" to 8, "9" to 9,
                                                 "10" to 10 )

    val frameArray  = Array<Frame>(10) { Frame()  }

    fun gameShot(pinCount: String): String {
        var status = "OK"
        if ( allowedValues.keys.any { it == pinCount } ) {
            reportGameState()
            println("PinCount: $pinCount")
            status = when (currentFrame) {
                         9 -> handleClosingFrame(pinCount, frameArray[currentFrame])
                         else -> handleNormalFrame(pinCount, frameArray[currentFrame])
                     }
            calcScore()
            reportGame()
            println()
        } else {
            status = "OutOfMapError"
        }
        return status
    }

    private fun handleNormalFrame (pinCount: String, frame: Frame) : String {

        var status = when(pinCount) {

            "X" -> normalStrike (frame, pinCount)

            "F","G","-" -> handleFault (frame, pinCount)

            "/" -> normalSpare (frame, pinCount)

            else -> normalElse (frame, pinCount)
        }

        if (currentShot > 1) { currentShot = 0; currentFrame++ }

        return status

    }

    private fun handleClosingFrame (pinCount: String, frame: Frame) : String {

        var status = when(pinCount) {
            "X" -> closingStrike (frame, pinCount)

            "F","G","-" -> handleFault(frame, pinCount)

            "/" -> closingSpare(frame, pinCount)

            else -> closingElse (frame, pinCount)
        }

        return status

    }

    private fun normalElse(frame: Frame, pinCount: String): String {
        var status = ""

        if (currentShot == 0) {
            frame.first = pinCount;
            currentShot++

        } else {
            if ((allowedValues[frame.first]!! + allowedValues[pinCount]!!) < 10) {
                frame.second = pinCount;
                currentShot++

            } else if ((allowedValues[frame.first]!! + allowedValues[pinCount]!!) == 10) {
                status = normalSpare(frame, "/")

            } else {
                status = "Cannot throw more then 10 pins in a frame."
            }
        }

        return status
    }

    private fun closingElse (frame: Frame, pinCount: String): String {
        var status = ""
        if (currentShot == 0) {
            frame.first = pinCount;
            currentShot++

        } else if (currentShot == 1 || currentShot == 2 ) {
            if (frame.first == "X") {
                frame.first = pinCount;
                currentShot++
            } else if (allowedValues[frame.first]!! + allowedValues[pinCount]!! < 10) {
                frame.second = pinCount;
                currentShot++
            } else if ((allowedValues[frame.first]!! + allowedValues[pinCount]!!) == 10) {
                status = closingSpare(frame, "/")

            }  else {
                status = "Cannot throw more then 10 pins in a frame."
            }
        } else {
            status = "Game is Over."
        }
        return status
    }

    private fun normalSpare (frame: Frame, pinCount: String): String {
        var status = "status"
        if (currentShot == 0) {
            status = "You cannot throw a spare in the first shot."
        } else {
            frame.second = pinCount
            status = "Spare."
            currentShot++
        }

        return status
    }

    private fun closingSpare ( frame: Frame, pinCount: String ): String {
        var status = ""
        when (currentShot) {
            0 -> { status = "You cannot throw a spare in the first shot." }

            1 -> {
                if (frame.first == "X") {
                    status = "You cannot throw a Spare right after a Strike."
                } else {
                    frame.second = pinCount
                    currentShot++
                    hasExtraShot = true
                }
            }

            else -> {
                if (frame.second == "X") {
                    status = "You cannot throw a Spare right after a Strike."
                } else {
                    frame.extra = pinCount
                    currentShot++
                    status = "Spare."
                }
            }
        }
        return status
    }

    private fun handleFault(frame: Frame, pinCount: String): String {
        var status = ""
        if (currentShot == 0) {
            frame.first = pinCount
        } else {
            frame.second = pinCount
        };
        currentShot++;
        status = "Fault."
        return status
    }

    private fun normalStrike ( frame: Frame, pinCount: String): String {
        var status: String = ""

        if (currentShot==0) {
            frame.first = pinCount;
            currentShot=2;
        } else {
            status = "You can only throw a strike in the first shot."
        }

        return status
    }

    private fun closingStrike (frame: Frame, pinCount: String): String {
        when (currentShot) {
            0 -> { frame.first = pinCount; hasExtraShot = true }
            1 -> frame.second = pinCount
            else -> frame.extra = pinCount
        }
        currentShot++;

        return "Strike."
    }

    private fun calcScore() {
        var total: Int = 0

        for (i in 0..8) {
            var frameTotal = 0
            val frame = frameArray[i]

            if (frame.first == "X") {
                frameTotal = 10
            } else if (frame.second == "/") {
                frameTotal = 10
            } else {
                frameTotal = (allowedValues[frame.first] ?: 0) + (allowedValues[frame.second] ?: 0)
            }
            frame.runningScore = frameTotal
        }

        val frame = frameArray[9]
        frame.runningScore = (allowedValues[frame.first] ?: 0) + (allowedValues[frame.second] ?: 0) + (allowedValues[frame.extra] ?: 0)

    }

    private fun reportGame() {
        for (i in 0..8) {
            print("| ${i+1} ")
        }
        println("|  10 |")

        for (j in 0..8) {
            val frame = frameArray[j]
            print("|${frame!!?.first}|${frame!!?.second}")
        }
        val frame = frameArray[9]
        println("|${frame!!?.first}|${frame!!?.second}|${frame!!?.extra}|")

        for (k in 0..8) {
            val frame = frameArray[k]
            print("|${frame.runningScore}")
        }

        println("|${frame.runningScore}|")

    }

    private fun reportGameState() {
        println("currentFrame: $currentFrame - currentShot: $currentShot")
    }

    companion object GameState {
        var currentFrame = 0
        var currentShot = 0
        var hasExtraShot = false
    }
}