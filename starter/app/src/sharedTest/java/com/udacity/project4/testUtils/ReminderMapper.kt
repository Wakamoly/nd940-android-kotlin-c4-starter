package com.udacity.project4.testUtils

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem

object ReminderMapper{

    fun ReminderDTO.toDomainModel(): ReminderDataItem {
        return ReminderDataItem(
                this.title,
                this.description,
                this.location,
                this.latitude,
                this.longitude,
                this.id
        )
    }

}