package com.ombre.brain

import android.app.Application
import com.ombre.brain.data.database.OmbreDatabase
import com.ombre.brain.data.repository.MemoryRepository

/**
 * Ombre Brain 应用入口
 *
 * 初始化数据库和全局组件。
 * 在 AndroidManifest.xml 中注册为此 Application 类。
 */
class OmbreBrainApp : Application() {

    /** 数据库实例（懒加载） */
    val database: OmbreDatabase by lazy { OmbreDatabase.getDatabase(this) }

    /** 数据仓库 */
    val repository: MemoryRepository by lazy {
        MemoryRepository(
            memoryDao = database.memoryDao(),
            flashDao = database.flashDao(),
            habitDao = database.habitDao()
        )
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: OmbreBrainApp
            private set
    }
}