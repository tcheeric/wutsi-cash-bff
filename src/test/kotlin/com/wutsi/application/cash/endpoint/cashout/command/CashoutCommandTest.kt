package com.wutsi.application.cash.endpoint.cashout.command

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.doThrow
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.application.cash.endpoint.AbstractEndpointTest
import com.wutsi.application.cash.endpoint.cashout.dto.CashoutRequest
import com.wutsi.flutter.sdui.Action
import com.wutsi.flutter.sdui.enums.ActionType
import com.wutsi.flutter.sdui.enums.DialogType
import com.wutsi.platform.payment.WutsiPaymentApi
import com.wutsi.platform.payment.core.ErrorCode
import com.wutsi.platform.payment.core.Status
import com.wutsi.platform.payment.dto.CreateCashoutResponse
import feign.FeignException
import feign.Request
import feign.RequestTemplate
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.web.server.LocalServerPort
import java.nio.charset.Charset
import kotlin.test.assertEquals

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class CashoutCommandTest : AbstractEndpointTest() {
    @LocalServerPort
    public val port: Int = 0

    private lateinit var url: String

    @MockBean
    private lateinit var paymentApi: WutsiPaymentApi

    @BeforeEach
    override fun setUp() {
        super.setUp()

        url = "http://localhost:$port/commands/cashout"
    }

    @Test
    fun success() {
        // GIVEN
        val resp = CreateCashoutResponse(id = "111", status = Status.SUCCESSFUL.name)
        doReturn(resp).whenever(paymentApi).createCashout(any())

        // WHEN
        val request = CashoutRequest(
            paymentToken = "xxx",
            amount = 10000.0
        )
        val response = rest.postForEntity(url, request, Action::class.java)

        // THEN
        kotlin.test.assertEquals(200, response.statusCodeValue)

        val action = response.body
        kotlin.test.assertEquals(ActionType.Route, action.type)
        kotlin.test.assertEquals("http://localhost:0/cashout/success?amount=${request.amount}", action.url)
    }

    @Test
    fun pending() {
        // GIVEN
        val resp = CreateCashoutResponse(id = "111", status = Status.PENDING.name)
        doReturn(resp).whenever(paymentApi).createCashout(any())

        // WHEN
        val request = CashoutRequest(
            paymentToken = "xxx",
            amount = 10000.0
        )
        val response = rest.postForEntity(url, request, Action::class.java)

        // THEN
        assertEquals(200, response.statusCodeValue)

        val action = response.body
        assertEquals(ActionType.Route, action.type)
        assertEquals("http://localhost:0/cashout/pending", action.url)
    }

    @Test
    fun failure() {
        // GIVEN
        val ex = createFeignException("transaction-failed", ErrorCode.NONE)
        doThrow(ex).whenever(paymentApi).createCashout(any())

        // WHEN
        val request = CashoutRequest(
            paymentToken = "xxx",
            amount = 10000.0
        )
        val response = rest.postForEntity(url, request, Action::class.java)

        // THEN
        assertEquals(200, response.statusCodeValue)

        val action = response.body
        assertEquals(ActionType.Prompt, action.type)
        assertEquals(DialogType.Error, action.prompt?.type)
        assertEquals(getText("prompt.error.transaction-failed"), action.prompt?.message)
    }

    private fun createFeignException(errorCode: String, downstreamError: ErrorCode) = FeignException.Conflict(
        "",
        Request.create(
            Request.HttpMethod.POST,
            "https://www.google.ca",
            emptyMap(),
            "".toByteArray(),
            Charset.defaultCharset(),
            RequestTemplate()
        ),
        """
            {
                "error":{
                    "code": "$errorCode",
                    "downstreamCode": "$downstreamError"
                }
            }
        """.trimIndent().toByteArray(),
        emptyMap()
    )
}