package com.example.todomap.background

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class GetLocation(ctx: Context, params: WorkerParameters) : Worker(ctx, params) {

    override fun doWork(): Result {
        return try {

            Result.success() // return statement
        } catch (e: Exception) {
            Result.failure() // return statement
        }
    }
}