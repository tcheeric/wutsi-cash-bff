package com.wutsi.application.cash.endpoint.cashout.screen

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.application.cash.endpoint.AbstractEndpointTest
import com.wutsi.platform.account.dto.ListPaymentMethodResponse
import com.wutsi.platform.account.dto.PaymentMethodSummary
import com.wutsi.platform.payment.PaymentMethodProvider
import com.wutsi.platform.payment.PaymentMethodType
import com.wutsi.platform.payment.dto.Balance
import com.wutsi.platform.payment.dto.GetBalanceResponse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class CashoutScreenTest : AbstractEndpointTest() {
    @LocalServerPort
    val port: Int = 0

    private lateinit var url: String

    @BeforeEach
    override fun setUp() {
        super.setUp()
        url = "http://localhost:$port/cashout"
    }

    @Test
    fun index() {
        val balance = Balance(amount = 50000.0, currency = "XAF")
        doReturn(GetBalanceResponse(balance)).whenever(paymentApi).getBalance(any())

        val response = ListPaymentMethodResponse(
            paymentMethods = listOf(
                PaymentMethodSummary(
                    token = "xxxxxx",
                    provider = PaymentMethodProvider.MTN.name,
                    type = PaymentMethodType.MOBILE.name,
                    maskedNumber = "xxxx9999"
                ),
                PaymentMethodSummary(
                    token = "yyyyy",
                    provider = PaymentMethodProvider.ORANGE.name,
                    type = PaymentMethodType.MOBILE.name,
                    maskedNumber = "xxxx1111"
                ),
                PaymentMethodSummary(
                    token = "zzzz",
                    provider = PaymentMethodProvider.WAF.name,
                    type = PaymentMethodType.BANK.name,
                    maskedNumber = "xxxx2222"
                ),
                PaymentMethodSummary(
                    token = "aaaaa",
                    provider = PaymentMethodProvider.UBA.name,
                    type = PaymentMethodType.BANK.name,
                    maskedNumber = "aaaaa2222"
                )
            )
        )
        doReturn(response).whenever(accountApi).listPaymentMethods(any())

        assertEndpointEquals("/screens/cashout/cashout.json", url)
    }
}
