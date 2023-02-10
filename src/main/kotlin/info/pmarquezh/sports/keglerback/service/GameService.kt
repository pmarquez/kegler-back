package info.pmarquezh.sports.keglerback.service

import info.pmarquezh.sports.keglerback.model.game.Frame
import lombok.extern.slf4j.Slf4j
import org.springframework.stereotype.Service

@Slf4j
@Service
class GameService () {
    val allowedValues: Map<String, Int> = mapOf( "X" to 10, "/" to -1, "F" to 0, "G" to 0, "-" to 0,
                                                 "0" to 0, "1" to 1, "2" to 2, "3" to 3,
                                                 "4" to 4, "5" to 5, "6" to 6, "7" to 7,
                                                 "8" to 8, "9" to 9 )

    val frameArray  = Array<Frame>(10) { Frame()  }

    fun gameShot(pinCount: String): String {
        var status = "OK"
        if ( allowedValues.keys.any { it == pinCount } ) {
            reportGameState()
            println("PinCount: $pinCount")
            status = when (currentFrame) {
                         9 -> handleClosingFrame(pinCount)
                         else -> handleNormalFrame(pinCount)
                     }
            reportGame()
            println()
        } else {
            status = "OutOfMapError"
        }
        return status
    }

    private fun handleNormalFrame (pinCount: String) : String {
        println("Entering handleNormalFrame")
        val frame = frameArray[currentFrame]
        var status = pinCount
        when(pinCount) {
            "X" ->  { if (currentShot==0) {
                          frame.first = pinCount;
                          currentShot=2;
                          status = "Strike."
                      } else {
                          status = "You can only throw a strike in the first shot."
                      }
                    }

            "F","G","-" ->  { if (currentShot==0) {
                          frame.first = pinCount
                      } else {
                          frame.second = pinCount
                      };
                      currentShot++;
                      status = "Fault."
                    }

            "/" ->  { if (currentShot==0) {
                          status = "You cannot throw a spare in the first shot."
                      } else {
                          frame.second = pinCount
                      };
                      currentShot++;
                      status = "Spare."
                    }

            else -> { if (currentShot==0) {
                          frame.first = pinCount;
                          currentShot++
                      } else {
                          if( ( allowedValues[frame.first]!! + allowedValues[pinCount]!! ) <= 10) {
                              frame.second = pinCount;
                              currentShot++ }
                          else {
                              status = "Cannot throw more then 10 pins in a frame."
                          }
                      }
                    }
        }
        if (currentShot > 1) { currentShot = 0; currentFrame++ }
        return status
    }

    private fun handleClosingFrame (pinCount: String) : String {
        println("Entering handleClosingFrame")
        val frame = frameArray[currentFrame]
        var status = pinCount
        when(pinCount) {
            "X" ->  {
                        when (currentShot) {
                            0 -> { frame.first = pinCount; hasExtraShot = true }
                            1 -> frame.second = pinCount
                            else -> frame.extra = pinCount
                        }
                        currentShot++;
                    }

            "F","G","-" ->  { if (currentShot==0) {
                                frame.first = pinCount
                            } else {
                                frame.second = pinCount
                            };
                                currentShot++;
                                status = "Fault."
                            }

            "/" ->  {
                        when (currentShot) {
                            0 -> {
                                status = "You cannot throw a spare in the first shot."
                            }
                            1 -> {
                                if(frame.first=="X") {
                                    status = "You cannot throw a Spare right after a Strike."
                                } else {
                                    frame.second = pinCount
                                    hasExtraShot = true
                                }
                            }
                            else -> {
                                if(frame.second=="X") {
                                    status = "You cannot throw a Spare right after a Strike."
                                } else {
                                    frame.extra = pinCount
                                }
                            }
                        }
                        currentShot++
                        status = "Spare."
                    }

            else -> { if (currentShot==0) {
                          frame.first = pinCount;
                          currentShot++

                      } else if (currentShot==1)  {
                          if (frame.first == "X") {
                              frame.first = pinCount;
                              currentShot++
                          } else if (allowedValues[frame.first]!! + allowedValues[pinCount]!! <= 10) {
                              frame.second = pinCount;
                              currentShot++ }
                          else {
                              status = "Cannot throw more then 10 pins in a frame."
                          }
                      } else if (currentShot==2)  {
                            if (frame.first == "X") {
                                frame.first = pinCount;
                                currentShot++
                            } else if (allowedValues[frame.first]!! + allowedValues[pinCount]!! <= 10) {
                                frame.second = pinCount;
                                currentShot++ }
                            else {
                                status = "Cannot throw more then 10 pins in a frame."
                            }
                      } else {
                            status = "Game is Over."
                      }
                    }
        }
        return status

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