package dev.avles.gym.`data`

import androidx.room.RoomDatabaseConstructor

public actual object GymDatabaseConstructor : RoomDatabaseConstructor<GymDatabase> {
  actual override fun initialize(): GymDatabase = dev.avles.gym.`data`.GymDatabase_Impl()
}
