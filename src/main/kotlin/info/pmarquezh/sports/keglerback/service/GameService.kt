package info.pmarquezh.sports.keglerback.service

import info.pmarquezh.sports.keglerback.enums.FrameStatus
import info.pmarquezh.sports.keglerback.model.game.Frame
import org.springframework.stereotype.Service

@Service
class GameService() {

    fun gameShot(pinCount: String) {

        if ( allowedValues.keys.any { it == pinCount } ) {   //   Check if symbol received is a valid (allowed) one
           if (!gameOver) {
                handleFrame ( pinCount )
            } else {
               println("Game is Over.")
            }

            calcScore()
            reportGame()
            println()
        }
    }

    private fun handleFrame (pinCount: String) {
        if (currentFrame == lastFrameIndex) {
            closingShot (pinCount)
        } else {
            when(pinCount) {
                strikeGlyph -> strikeShot (pinCount)
                spareGlyph -> spareShot (pinCount)
                else -> lameShot (pinCount)
            }
            if (currentShot > 1) { currentShot = 0; currentFrame++ } //  Reset the current shot to 0, advance frame
        }
    }

    private fun strikeShot (pinCount: String) {
        val frame = frameArray[currentFrame]
        if (currentShot ==0) {
            frame.first = pinCount
            frame.status = FrameStatus.STRIKE
            currentShot =2
        } else {
            println("ERROR. You can only throw a strike in the first shot.")
        }
    }

    private fun spareShot (pinCount: String) {
        val frame = frameArray[currentFrame]
        if (currentShot == 0) {
            println("You cannot throw a spare in the first shot.")
        } else {
            frame.second = pinCount
            frame.status = FrameStatus.SPARE
            currentShot++
        }
    }

    private fun lameShot(pinCount: String) {
        val frame = frameArray[currentFrame]
        if (currentShot == 0) {
            frame.first = pinCount
            frame.status = FrameStatus.PARTIAL
            currentShot++
        } else {
            if ((allowedValues[frame.first]!! + allowedValues[pinCount]!!) < sparePincount) {
                frame.second = pinCount
                frame.status = FrameStatus.LAME
                currentShot++
            } else if ((allowedValues[frame.first]!! + allowedValues[pinCount]!!) == sparePincount) {
                spareShot(spareGlyph)
            }
        }
    }

    private fun closingShot (pinCount: String) {
        when (currentShot) {
            0 -> { handleFirstClosingShot(pinCount) }
            1 -> { handleSecondClosingShot (pinCount) }
            2 -> { if(hasExtraShot) handleThirdClosingShot(pinCount) }
            else -> { println("Game is Over.") }
        }

    }

    private fun handleFirstClosingShot(pinCount: String) {
        val frame = frameArray[currentFrame]

        when(pinCount) {
            spareGlyph -> { println("Illegal State") }
            else -> {
                frame.first = pinCount
                currentShot++
                frame.status = FrameStatus.CLOSING_PARTIAL
                if ( pinCount == strikeGlyph) hasExtraShot=true
            }
        }

    }

    private fun handleSecondClosingShot(pinCount: String) {
        val frame = frameArray[currentFrame]

        if (frame.first == strikeGlyph) {
            when (pinCount) {
                spareGlyph -> { println("Illegal State") }
                else -> { frame.second = pinCount; currentShot++ }
            }
        } else {
            when (pinCount) {
                spareGlyph -> {
                    frame.second = pinCount
                    hasExtraShot=true
                    currentShot++
                }
                else -> {
                    if (allowedValues[frame.first]!! + allowedValues[pinCount]!! < sparePincount) {
                        frame.second = pinCount
                        gameOver = true
                        frame.status = FrameStatus.CLOSING_COMPLETE
                        currentShot++
                    } else if ((allowedValues[frame.first]!! + allowedValues[pinCount]!!) == sparePincount) {
                        frame.second = spareGlyph
                        hasExtraShot=true
                        currentShot++
                    }  else {
                        println("Ilegal State.")
                    }
                }
            }
        }
    }

    private fun handleThirdClosingShot(pinCount: String) {
        val frame = frameArray[currentFrame]

        if(!gameOver) {
            if (frame.second == strikeGlyph) {
                when (pinCount) {
                    spareGlyph -> {
                        println("Illegal State 1.")
                    }
                    else -> {
                        frame.extra = pinCount
                        gameOver = true
                        frame.status = FrameStatus.CLOSING_COMPLETE
                    }
                }

            } else if (frame.second == spareGlyph) {
                when (pinCount) {
                    spareGlyph -> {
                        println("Illegal State 1.")
                    }
                    else -> {
                        frame.extra = pinCount
                        gameOver = true
                        frame.status = FrameStatus.CLOSING_COMPLETE
                    }
                }

            } else {
                if (allowedValues[frame.second]!! + allowedValues[pinCount]!! < sparePincount) {
                    frame.extra = pinCount
                    gameOver = true
                    frame.status = FrameStatus.CLOSING_COMPLETE
                } else if ((allowedValues[frame.second]!! + allowedValues[pinCount]!!) == sparePincount) {
                    frame.extra = spareGlyph
                    gameOver = true
                    frame.status = FrameStatus.CLOSING_COMPLETE
                } else {
                    println("Ilegal State 2.")
                }
            }
        } else {
            println("Game is Over.")
        }

    }


    private fun calcScore() {
        for(idx in frameArray.indices) {
            val frame = frameArray[idx]

            frame.pinCount = when (frame.status) {
                FrameStatus.PARTIAL -> allowedValues[frame.first]!!
                FrameStatus.SPARE -> sparePincount
                FrameStatus.STRIKE -> strikePinCount
                FrameStatus.LAME -> allowedValues[frame.first]!! + allowedValues[frame.second]!!
                FrameStatus.CLOSING_PARTIAL, FrameStatus.CLOSING_COMPLETE -> calcClosingPinCount(frame)
                FrameStatus.PENDING -> 0
            }

            frame.frameWeight = when (frame.status) {
                FrameStatus.PARTIAL -> allowedValues[frame.first]!!
                FrameStatus.SPARE -> sparePincount + lookAheadOneShot(idx)
                FrameStatus.STRIKE -> strikePinCount + lookAheadTwoShots(idx)
                FrameStatus.LAME -> allowedValues[frame.first]!! + allowedValues[frame.second]!!
                FrameStatus.CLOSING_PARTIAL, FrameStatus.CLOSING_COMPLETE -> calcClosingPinCount(frame)
                FrameStatus.PENDING -> 0
            }
        }
    }

    private fun lookAheadOneShot(idx: Int): Int {
        val frame = frameArray[idx+1]

        return when(frame.status) {
            FrameStatus.PARTIAL,
            FrameStatus.LAME,
            FrameStatus.SPARE,
            FrameStatus.STRIKE -> allowedValues[frame.first]!!
            FrameStatus.CLOSING_PARTIAL, FrameStatus.CLOSING_COMPLETE -> { allowedValues[frame.first] ?: 0 }
            else -> 0
        }
    }

    private fun lookAheadTwoShots(idx: Int): Int {
        val frame = frameArray[idx+1]

        val lookAheadTwo = when(frame.status) {
            FrameStatus.PARTIAL -> allowedValues[frame.first]!!
            FrameStatus.LAME -> { allowedValues[frame.first]!! + allowedValues[frame.second]!! }
            FrameStatus.SPARE -> strikePinCount
            FrameStatus.STRIKE -> { strikePinCount + getSecondLookAhead(idx+2) }
            FrameStatus.CLOSING_PARTIAL, FrameStatus.CLOSING_COMPLETE -> {
                var subtot = (allowedValues[frame.first] ?: 0) + (allowedValues[frame.second] ?: 0)
                if ( frame.second == spareGlyph) subtot = (subtot + 1 - (allowedValues[frame.first] ?: 0)) + sparePincount
                subtot
            }
            else -> 0
        }

        return lookAheadTwo
    }

    private fun getSecondLookAhead(idxPlusTwo: Int): Int {
        if (idxPlusTwo < (frameArray.size) ) {
            val frame = frameArray[idxPlusTwo]
            return ( allowedValues[frame.first] ?: 0 )
        }
        return 0
    }

    private fun calcClosingPinCount(frame: Frame): Int {
        var subtot = (allowedValues[frame.first] ?: 0) + (allowedValues[frame.second] ?: 0) + (allowedValues[frame.extra] ?: 0)
        if ( frame.second == spareGlyph) subtot = (subtot + 1 - (allowedValues[frame.first] ?: 0)) + sparePincount
        if ( frame.extra == spareGlyph)  subtot = (subtot + 1 - (allowedValues[frame.second] ?: 0)) + sparePincount
        return subtot
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
        val frame = frameArray[lastFrameIndex]
        println("|${frame.first}|${frame.second}|${frame.extra}|")
//   SCOREBOARD - END

//   PINCOUNTS - BEGUN
        for (k in 0..8) print("|${frameArray[k].pinCount}")
        println("|${frame.pinCount}|")
//   PINCOUNTS - END

//   FRAMEWEIGHTS - BEGIN
        for (k in 0..8) print("|${frameArray[k].frameWeight}")
        println("|${frame.frameWeight}|")
//   FRAMEWEIGHTS - END
    }

    fun cleanGame() {
        frameArray  = Array(numFrames) { Frame() }

        currentFrame = 0
        currentShot = 0
        hasExtraShot = false
        gameOver = false

    }

    companion object GameState {
        val allowedValues: Map<String, Int> = mapOf(
            "X" to 10, "/" to -1, "F" to 0, "G" to 0, "-" to 0,
            "0" to 0, "1" to 1, "2" to 2, "3" to 3, "4" to 4,
            "5" to 5, "6" to 6, "7" to 7, "8" to 8, "9" to 9,
            "10" to 10 )

        var frameArray: Array<Frame> = emptyArray()

        var currentFrame = 0
        var currentShot = 0
        var hasExtraShot = false
        var gameOver = false

        val numFrames = 10
        val lastFrameIndex = 9
        val strikePinCount = 10
        val sparePincount = 10
        val strikeGlyph = "X"
        val spareGlyph = "/"
    }

}