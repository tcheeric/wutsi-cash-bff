package com.wutsi.application.cash.endpoint.cashout.screen

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.application.cash.endpoint.AbstractEndpointTest
import com.wutsi.platform.account.dto.GetPaymentMethodResponse
import com.wutsi.platform.account.dto.PaymentMethod
import com.wutsi.platform.account.dto.Phone
import com.wutsi.platform.payment.PaymentMethodType
import com.wutsi.platform.payment.dto.ComputeFeesResponse
import com.wutsi.platform.payment.dto.TransactionFee
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class CashoutConfirmScreenTest : AbstractEndpointTest() {
    @LocalServerPort
    val port: Int = 0

    private lateinit var url: String

    val amount = 1000.0
    val fees = 20.0
    private val idempotencyKey = "11111-111111-222222-2222"
    private val token = "xxx"

    @BeforeEach
    override fun setUp() {
        super.setUp()

        url =
            "http://localhost:$port/cashout/confirm?amount=$amount&payment-token=$token&idempotency-key=$idempotencyKey"
    }

    @Test
    fun confirm() {
        // GIVEN
        val response = GetPaymentMethodResponse(
            paymentMethod = PaymentMethod(
                token = "xxxxxx",
                provider = "MTN",
                type = PaymentMethodType.MOBILE.name,
                maskedNumber = "xxxx9999",
                phone = Phone(
                    number = "+237670000001"
                )
            )
        )
        doReturn(response).whenever(accountApi).getPaymentMethod(any(), any())

        val transactionFee = TransactionFee(
            amount = amount,
            fees = fees,
            senderAmount = amount + fees,
            recipientAmount = amount,
            applyFeesToSender = true
        )
        doReturn(ComputeFeesResponse(transactionFee)).whenever(paymentApi).computeFees(any())

        // WHEN/THEN
        assertEndpointEquals("/screens/cashout/confirm.json", url)
    }
}
