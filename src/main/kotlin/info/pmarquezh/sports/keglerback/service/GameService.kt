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

    val frameArray  = Array(11) { Frame() }

    fun gameShot(pinCount: String): String {
        var status = "OK"
        if ( allowedValues.keys.any { it == pinCount } ) {   //   Check if symbol received is a valid (allowed) one
            reportGameState()
            println("PinCount: $pinCount")

            when (currentFrame) {
                0,1,2,3,4,5,6,7,8 -> handleNormalFrame(pinCount, frameArray[currentFrame])
                9 -> handleClosingFrame(pinCount, frameArray[currentFrame])
                else -> println("Game is Over.")
            }

            calcScore()
            reportGame()
            println()
        } else {
            status = "OutOfMapError"
        }
        return status
    }

// NORMAL FRAMES 0..8 (1 through 9) - BEGIN
    private fun handleNormalFrame (pinCount: String, frame: Frame) {
        when(pinCount) {
            "X" -> normalStrike (frame, pinCount)
            "/" -> normalSpare (frame, pinCount)
            "F","G","-" -> normalBlank (frame, pinCount)
            else -> normalElse (frame, pinCount)
        }
        if (currentShot > 1) { currentShot = 0; currentFrame++ }
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

    private fun normalSpare (frame: Frame, pinCount: String) {
        if (currentShot == 0) {
            println("You cannot throw a spare in the first shot.")
        } else {
            frame.second = pinCount
            frame.status = FrameStatus.SPARE
            currentShot++
        }
    }

    private fun normalBlank(frame: Frame, pinCount: String) {
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
// NORMAL FRAMES 0..8 (1 through 9) - END

// CLOSING FRAME 9 (10) - BEGIN
    private fun handleClosingFrame (pinCount: String, frame: Frame) {
        when(pinCount) {
            "X" -> closingStrike (frame, pinCount)
            "/" -> closingSpare(frame, pinCount)
            "F","G","-" -> closingBlank (frame, pinCount)
            else -> closingElse (frame, pinCount)
        }
    }

    private fun closingStrike (frame: Frame, pinCount: String) {
        when (currentShot) {
            0 -> { frame.first = pinCount;
                   hasExtraShot = true;
                   frame.status = FrameStatus.CLOSING_PARTIAL
                 }

            1 -> { if (frame.first == "X") {
                       frame.second = pinCount
                   } else {
                       println("SHOT NOT POSSIBLE")
                   }
                 }

            else -> { if (!gameOver) {
                          if ((frame.second == "X") || (frame.second == "/")) {
                              frame.extra = pinCount
                              frame.status = FrameStatus.CLOSING_COMPLETE
                          } else {
                              println("SHOT NOT POSSIBLE")
                          }
                      } else {
                          println("Game is Over.")
                      }
                    }
        }
        currentShot++
        println("Strike.")
    }

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
                if(!gameOver) {
                    if (frame.second == "X") {
                        println("You cannot throw a Spare right after a Strike.")
                    } else {
                        frame.extra = pinCount
                        currentShot++
                        frame.status = FrameStatus.CLOSING_COMPLETE
                        gameOver = true
                        println("Spare.")
                    }
                } else {
                    println("Game is Over.")
                }
            }
        }
    }

    private fun closingBlank(frame: Frame, pinCount: String) {
        if (currentShot == 0) {
            frame.first = pinCount
            frame.status = FrameStatus.PARTIAL
        } else if (currentShot == 1) {
            frame.second = pinCount
            if ( frame.first != "X") {
                frame.status = FrameStatus.CLOSING_LAME
                gameOver = true
            }
        } else {
            if (!gameOver) {
                frame.extra = pinCount
                frame.status = FrameStatus.CLOSING_LAME
                gameOver = true
            }
        }
        currentShot++
        println("Fault.")
    }

    private fun closingElse (frame: Frame, pinCount: String) {
        if (currentShot == 0) {
            frame.first = pinCount
            currentShot++
            frame.status = FrameStatus.CLOSING_PARTIAL

        } else if (currentShot == 1 ) {
            if (frame.first == "X") {
                frame.second = pinCount
                currentShot++
            } else if (allowedValues[frame.first]!! + allowedValues[pinCount]!! < 10) {
                frame.second = pinCount
                currentShot++
                frame.status = FrameStatus.CLOSING_COMPLETE
                gameOver = true
            } else if ((allowedValues[frame.first]!! + allowedValues[pinCount]!!) == 10) {
                closingSpare(frame, "/")
            }  else {
                println("Cannot throw more then 10 pins in a frame.")
            }

        } else if (currentShot == 2 ) {
            if(!gameOver) {
                if (frame.second == "X") {
                    frame.extra = pinCount
                    currentShot++
                    frame.status = FrameStatus.CLOSING_COMPLETE
                } else if (allowedValues[frame.second]!! + allowedValues[pinCount]!! < 10) {
                    frame.extra = pinCount
                    currentShot++
                    frame.status = FrameStatus.CLOSING_COMPLETE
                } else if ((allowedValues[frame.second]!! + allowedValues[pinCount]!!) == 10) {
                    closingSpare(frame, "/")
                    frame.status = FrameStatus.CLOSING_COMPLETE
                }  else {
                    println("Cannot throw more then 10 pins in a frame.")
                }
            } else {
                println("Game is Over.")
            }
        } else {
            println("Game is Over.")
        }
    }

// CLOSING FRAME 9 (10) - END
















    private fun calcScore() {
        var runningTotal = 0

//        for(idx in 0..8) {
        for(idx in frameArray.indices) {
            val frame = frameArray[idx]

            runningTotal += when (frame.status ) {
                FrameStatus.STRIKE -> strikeSubtotal(idx)   // OK
                FrameStatus.PARTIAL -> lameSubtotal(idx)    // OK
                FrameStatus.SPARE -> spareSubtotal(idx)
                FrameStatus.LAME -> lameSubtotal(idx)

                FrameStatus.CLOSING -> TODO()
                FrameStatus.CLOSING_EXTRA_SHOT -> TODO()
                FrameStatus.CLOSING_PARTIAL, FrameStatus.CLOSING_LAME, FrameStatus.CLOSING_COMPLETE -> closingSubtotal(idx)

                FrameStatus.PENDING -> 0
                FrameStatus.DUMMY_FRAME -> 0
            }
        }

        println("RunningTotal: $runningTotal")
    }

    private fun strikeSubtotal(idx: Int): Int {
        val frame = frameArray[idx]
        frame.pinCount = 10
        frame.frameWeight = frame.pinCount

        // Can we lookahead?
        val isIndex1Safe = (idx < frameArray.size - 1)
        val isIndex2Safe = (idx < frameArray.size - 2)

        if (isIndex1Safe) {
            val lookAheadFrame = frameArray[idx+1]
            frame.frameWeight += when (lookAheadFrame.status) {
                FrameStatus.STRIKE -> 10
                FrameStatus.SPARE -> 10
                FrameStatus.LAME -> allowedValues[lookAheadFrame.first]!! + allowedValues[lookAheadFrame.second]!!
                FrameStatus.PARTIAL -> allowedValues[lookAheadFrame.first]!!
                FrameStatus.CLOSING_PARTIAL,
                FrameStatus.CLOSING_COMPLETE,
                FrameStatus.CLOSING,
                FrameStatus.CLOSING_LAME,
                FrameStatus.CLOSING_EXTRA_SHOT  -> closingStrikeSubtotal(lookAheadFrame)
                FrameStatus.PENDING -> 0
                FrameStatus.DUMMY_FRAME -> 0
            }
        }
        return frame.frameWeight
    }

    private fun closingStrikeSubtotal ( lookAheadFrame: Frame ): Int {
            var totWeight = 0
            if (lookAheadFrame.first != " ") totWeight += allowedValues[lookAheadFrame.first]!!
            if (lookAheadFrame.second != " ") totWeight += allowedValues[lookAheadFrame.second]!!
            return totWeight
    }

    private fun spareSubtotal(idx: Int): Int {
        val frame = frameArray[idx]
        frame.pinCount = 10

        if ( idx < frameArray.size - 1 ) {
            val lookAheadFrame = frameArray[idx+1]
            if( lookAheadFrame.status != FrameStatus.PENDING ) {
                println("'/' Frame: ${idx+1} - LookAhead-1: ${lookAheadFrame.first}")
                frame.frameWeight = frame.pinCount + allowedValues[lookAheadFrame.first]!!
            } else {
                println("Frame: $idx - LookAhead-1: 0")
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