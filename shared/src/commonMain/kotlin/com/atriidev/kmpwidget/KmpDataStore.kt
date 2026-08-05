package com.atriidev.kmpwidget


expect class KmpDataStore {
    fun get(key: String, defaultValue: String) : String
    fun set(key: String,value: String) : Boolean
}