package com.wutsi.application.cash.endpoint.cashout.command

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.application.cash.endpoint.AbstractEndpointTest
import com.wutsi.application.cash.endpoint.cashout.dto.CashoutRequest
import com.wutsi.application.cash.service.IdempotencyKeyGenerator
import com.wutsi.flutter.sdui.Action
import com.wutsi.flutter.sdui.enums.ActionType
import com.wutsi.flutter.sdui.enums.DialogType
import com.wutsi.platform.payment.core.Status
import com.wutsi.platform.payment.dto.CreateCashinResponse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.web.server.LocalServerPort
import kotlin.test.assertEquals

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class CashoutAmountCommandTest : AbstractEndpointTest() {
    @LocalServerPort
    val port: Int = 0

    private lateinit var url: String
    private lateinit var request: CashoutRequest

    @MockBean
    private lateinit var idempotencyKeyGenerator: IdempotencyKeyGenerator

    private val idempotencyKey = "11111-111111-222222-2222"
    private val amount = 15000.0
    private val token = "xxx"

    @BeforeEach
    override fun setUp() {
        super.setUp()

        url = "http://localhost:$port/commands/cashout/amount"
        request = CashoutRequest(paymentToken = token, amount = amount)

        doReturn(idempotencyKey).whenever(idempotencyKeyGenerator).generate()
    }

    @Test
    fun success() {
        // GIVEN
        val resp = CreateCashinResponse(id = "111", status = Status.SUCCESSFUL.name)
        doReturn(resp).whenever(paymentApi).createCashin(any())

        // WHEN
        val response = rest.postForEntity(url, request, Action::class.java)

        // THEN
        assertEquals(200, response.statusCodeValue)

        val action = response.body!!
        assertEquals(ActionType.Route, action.type)
        assertEquals(
            "http://localhost:0/cashout/confirm?amount=$amount&payment-token=$token&idempotency-key=$idempotencyKey",
            action.url
        )
    }

    @Test
    fun minCashout() {
        // WHEN
        request = CashoutRequest(paymentToken = "xxx", amount = 1.0)
        val response = rest.postForEntity(url, request, Action::class.java)

        // THEN
        assertEquals(200, response.statusCodeValue)

        val action = response.body!!
        assertEquals(ActionType.Prompt, action.type)
        assertEquals(DialogType.Error.name, action.prompt?.attributes?.get("type"))
        assertEquals(
            getText("prompt.error.min-cashout", arrayOf("5,000 XAF")),
            action.prompt?.attributes?.get("message")
        )
    }

    @Test
    fun noValue() {
        // WHEN
        request = CashoutRequest(paymentToken = "xxx", amount = 0.0)
        val response = rest.postForEntity(url, request, Action::class.java)

        // THEN
        assertEquals(200, response.statusCodeValue)

        val action = response.body!!
        assertEquals(ActionType.Prompt, action.type)
        assertEquals(DialogType.Error.name, action.prompt?.attributes?.get("type"))
        assertEquals(getText("prompt.error.amount-required"), action.prompt?.attributes?.get("message"))
    }

    @Test
    fun notEnoughFunds() {
        // WHEN
        val request = CashoutRequest(
            amount = 1000000.0,
            paymentToken = "xxx"
        )
        val response = rest.postForEntity(url, request, Action::class.java)

        // THEN
        assertEquals(200, response.statusCodeValue)

        val action = response.body!!
        assertEquals(ActionType.Prompt, action.type)
        assertEquals(DialogType.Error.name, action.prompt?.attributes?.get("type"))
        assertEquals(
            getText("prompt.error.transaction-failed.NOT_ENOUGH_FUNDS"),
            action.prompt?.attributes?.get("message")
        )
    }
}
