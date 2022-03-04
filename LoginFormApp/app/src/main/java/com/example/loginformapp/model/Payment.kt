package com.example.loginformapp.model

data class Payment (
    val username: String,
    val card_number: String,
    val cvv: String,
    val full_name: String,
    val card_date: String
)