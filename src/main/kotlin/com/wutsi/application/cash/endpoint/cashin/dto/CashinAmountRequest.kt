package com.wutsi.application.cash.endpoint.cashin.dto

data class CashinAmountRequest(
    val paymentToken: String = "",
    val amount: Double = 0.0
)
