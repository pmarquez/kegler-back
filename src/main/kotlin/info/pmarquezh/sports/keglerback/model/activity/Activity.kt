package info.pmarquezh.sports.keglerback.model.activity

import info.pmarquezh.sports.keglerback.enums.ActivityType
import java.time.LocalDate

class Activity ( activityId: String,
                activityName: String,
                activityType: ActivityType,
                multipleDates: Boolean,
                startDate: LocalDate,
                endDate: LocalDate )