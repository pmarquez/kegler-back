package info.pmarquezh.sports.keglerback.service

import info.pmarquezh.sports.keglerback.enums.FrameStatus
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

    val frameArray  = Array(10) { Frame() }

    fun gameShot(pinCount: String): String {
        var status = "OK"
        if ( allowedValues.keys.any { it == pinCount } ) {   //   Check if symbol received is a valid (allowed) one
            reportGameState()
            println("PinCount: $pinCount")

            when (currentFrame) {
                10 -> handleClosingFrame(pinCount, frameArray[currentFrame])
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

    private fun handleNormalFrame (pinCount: String, frame: Frame) {
        when(pinCount) {
            "X" -> normalStrike (frame, pinCount)
            "F","G","-" -> handleFault (frame, pinCount)
            "/" -> normalSpare (frame, pinCount)
            else -> normalElse (frame, pinCount)
        }
        if (currentShot > 1) { currentShot = 0; currentFrame++ }
    }

    private fun handleClosingFrame (pinCount: String, frame: Frame) {
        when(pinCount) {
            "X" -> closingStrike (frame, pinCount)
            "F","G","-" -> handleFault(frame, pinCount)
            "/" -> closingSpare(frame, pinCount)
            else -> closingElse (frame, pinCount)
        }
    }

    private fun normalElse(frame: Frame, pinCount: String) {
        if (currentShot == 0) {
            frame.first = pinCount
            frame.status = FrameStatus.PARTIAL
            currentShot++
        } else {
            if ((allowedValues[frame.first]!! + allowedValues[pinCount]!!) < 10) {
                frame.second = pinCount
                frame.status = FrameStatus.LAME
                currentShot++
            } else if ((allowedValues[frame.first]!! + allowedValues[pinCount]!!) == 10) {
                normalSpare(frame, "/")
            } else {
                println("Cannot throw more then 10 pins in a frame.")
            }
        }
    }

    //TODO Review this method... Jack might have passed by...
    private fun closingElse (frame: Frame, pinCount: String) {
        if (currentShot == 0) {
            frame.first = pinCount
            currentShot++
            frame.status = FrameStatus.CLOSING_PARTIAL

        } else if (currentShot == 1 ) {
            if (frame.first == "X") {
                frame.first = pinCount
                currentShot++
            } else if (allowedValues[frame.first]!! + allowedValues[pinCount]!! < 10) {
                frame.second = pinCount
                currentShot++
            } else if ((allowedValues[frame.first]!! + allowedValues[pinCount]!!) == 10) {
                closingSpare(frame, "/")
            }  else {
                println("Cannot throw more then 10 pins in a frame.")
            }

        } else if (currentShot == 2 ) {
            //????
            if (frame.first == "X") {
                frame.first = pinCount
                currentShot++
            } else if (allowedValues[frame.first]!! + allowedValues[pinCount]!! < 10) {
                frame.second = pinCount
                currentShot++
            } else if ((allowedValues[frame.first]!! + allowedValues[pinCount]!!) == 10) {
                closingSpare(frame, "/")
            }  else {
                println("Cannot throw more then 10 pins in a frame.")
            }
        } else {
            println("Game is Over.")
        }
    }

    private fun normalSpare (frame: Frame, pinCount: String) {
        if (currentShot == 0) {
            println("You cannot throw a spare in the first shot.")
        } else {
            frame.second = pinCount
            frame.status = FrameStatus.SPARE
            currentShot++
        }
    }

    private fun handleFault(frame: Frame, pinCount: String) {
        if (currentShot == 0) {
            frame.first = pinCount
            frame.status = FrameStatus.PARTIAL
        } else {
            frame.second = pinCount
            frame.status = FrameStatus.LAME
        }
        currentShot++
        println("Fault.")
    }

    private fun normalStrike ( frame: Frame, pinCount: String) {
        if (currentShot==0) {
            frame.first = pinCount
            frame.status = FrameStatus.STRIKE
            currentShot=2
        } else {
            println("You can only throw a strike in the first shot.")
        }
    }

    //   CLOSING
    private fun closingSpare ( frame: Frame, pinCount: String ) {
        when (currentShot) {
            0 -> { println("You cannot throw a spare in the first shot.") }
            1 -> {
                if (frame.first == "X") {
                    println("You cannot throw a Spare right after a Strike.")
                } else {
                    frame.second = pinCount
                    currentShot++
                    hasExtraShot = true
                    frame.status = FrameStatus.CLOSING_PARTIAL
                }
            }
            else -> {
                if (frame.second == "X") {
                    println("You cannot throw a Spare right after a Strike.")
                } else {
                    frame.extra = pinCount
                    currentShot++
                    frame.status = FrameStatus.CLOSING_COMPLETE
                    gameOver = true
                    println("Spare.")
                }
            }
        }
    }

    private fun closingStrike (frame: Frame, pinCount: String) {
        when (currentShot) {
            0 -> { frame.first = pinCount; hasExtraShot = true; frame.status = FrameStatus.CLOSING_PARTIAL }
            1 -> { frame.second = pinCount; if( !hasExtraShot ) frame.status = FrameStatus.CLOSING_COMPLETE; gameOver = true }
            else -> { frame.extra = pinCount; frame.status = FrameStatus.CLOSING_COMPLETE; gameOver = true }
        }
        currentShot++
        println("Strike.")
    }

    private fun calcScore() {
        var runningTotal = 0

        for(idx in frameArray.indices) {
            var frameTotal = 0
            val frame = frameArray[idx]

            runningTotal += when (frame.status ) {
                FrameStatus.PARTIAL -> lameSubtotal(idx)
                FrameStatus.CLOSING_PARTIAL, FrameStatus.CLOSING_COMPLETE -> closingSubtotal(idx)
                FrameStatus.PENDING -> 0
                FrameStatus.SPARE -> spareSubtotal(idx)
                FrameStatus.STRIKE -> strikeSubtotal(idx)
                FrameStatus.LAME -> lameSubtotal(idx)
            }
        }

        println("RunningTotal: $runningTotal")
    }






    private fun closingSubtotal(idx: Int): Int {
        val frame = frameArray[idx]

        frame.pinCount = (allowedValues[frame.first] ?: 0)
        frame.pinCount += when (allowedValues[frame.second] ?: 0) {
            -1 -> 10 - (allowedValues[frame.first] ?: 0)
            else -> allowedValues[frame.second] ?: 0
        }
        frame.pinCount += when (allowedValues[frame.extra] ?: 0) {
            -1 -> 10 - (allowedValues[frame.second] ?: 0)
            else -> allowedValues[frame.extra] ?: 0
        }

        frame.frameWeight = frame.pinCount
        return frame.frameWeight
    }

    private fun strikeSubtotal(idx: Int): Int {
        val frame = frameArray[idx]
        frame.pinCount = 10
        frame.frameWeight = frame.pinCount

        val isIndex1Safe = (idx < frameArray.size - 1)
        val isIndex2Safe = (idx < frameArray.size - 2)

        if (isIndex1Safe) {
            val lookAheadFrame = frameArray[idx+1]
            frame.frameWeight += when (lookAheadFrame.status) {
                                     FrameStatus.STRIKE -> {
                                         var totWeight = 10
                                         if ( isIndex2Safe && frameArray[idx+2].status != FrameStatus.PENDING ) { totWeight += allowedValues[frameArray[idx+2].first]!! }
                                         totWeight
                                     }
                                     FrameStatus.SPARE -> 10
                                     FrameStatus.LAME -> allowedValues[lookAheadFrame.first]!! + allowedValues[lookAheadFrame.second]!!
                                     FrameStatus.PARTIAL -> allowedValues[lookAheadFrame.first]!!
                                     FrameStatus.CLOSING_PARTIAL, FrameStatus.CLOSING_COMPLETE -> {
                                         var totWeight = 0
                                         if (lookAheadFrame.first != " ") totWeight += allowedValues[lookAheadFrame.first]!!
                                         if (lookAheadFrame.second != " ") totWeight += allowedValues[lookAheadFrame.second]!!
                                         totWeight
                                     }
                                     else -> 0
                                 }
        }
        return frame.frameWeight
    }

    private fun spareSubtotal(idx: Int): Int {
        val frame = frameArray[idx]
        frame.pinCount = 10

        if ( idx < frameArray.size - 1 ) {
            val lookAheadFrame = frameArray[idx+1]
            if( lookAheadFrame.status != FrameStatus.PENDING ) {
                frame.frameWeight = frame.pinCount + allowedValues[lookAheadFrame.first]!!
            } else {
                frame.frameWeight = frame.pinCount
            }
        }
        return frame.frameWeight
    }

    private fun lameSubtotal(idx: Int): Int {
        val frame = frameArray[idx]
        frame.pinCount = (allowedValues[frame.first] ?: 0) + (allowedValues[frame.second] ?: 0) + (allowedValues[frame.extra] ?: 0)
        frame.frameWeight = frame.pinCount
        return frame.frameWeight
    }

    private fun reportGame() {
//   SCOREBOARD - BEGIN
        for (i in 0..8) {
            print("| ${i+1} ")
        }
        println("|  10 |")

        for (j in 0..8) {
            val frame = frameArray[j]
            print("|${frame.first}|${frame.second}")
        }
        val frame = frameArray[9]
        println("|${frame.first}|${frame.second}|${frame.extra}|")
//   SCOREBOARD - END

//   PINCOUNTS - BEGUN
        for (k in 0..8) {
            val frame = frameArray[k]
            print("|${frame.pinCount}")
        }

        println("|${frame.pinCount}|")
//   PINCOUNTS - END
//   FRAMEWEIGHTS - BEGUN
        for (k in 0..8) {
            val frame = frameArray[k]
            print("|${frame.frameWeight}")
        }

        println("|${frame.frameWeight}|")
//   FRAMEWEIGHTS - END

//   STATUSES - BEGIN
//    for(frame in frameArray)
//        println("${frame.status}")
//   STATUSES - END

    }

    private fun reportGameState() {
        println("currentFrame: $currentFrame - currentShot: $currentShot")
    }

    companion object GameState {
        var currentFrame = 0
        var currentShot = 0
        var hasExtraShot = false
        var gameOver = false
    }
}