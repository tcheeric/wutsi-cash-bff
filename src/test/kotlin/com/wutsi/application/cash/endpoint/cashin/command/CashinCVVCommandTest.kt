package com.wutsi.application.cash.endpoint.cashin.command

import com.wutsi.application.cash.endpoint.AbstractEndpointTest
import com.wutsi.application.cash.endpoint.cashin.dto.CashinCVVRequest
import com.wutsi.flutter.sdui.Action
import com.wutsi.flutter.sdui.enums.ActionType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class CashinCVVCommandTest : AbstractEndpointTest() {
    @LocalServerPort
    val port: Int = 0

    private lateinit var url: String

    @BeforeEach
    override fun setUp() {
        super.setUp()

        url = "http://localhost:$port/commands/cashin/cvv?amount=10000&payment-token=xxxx&idempotency-key=123"
    }

    @Test
    fun success() {
        // WHEN
        val response = rest.postForEntity(url, CashinCVVRequest(cvv = "019"), Action::class.java)

        // THEN
        assertEquals(200, response.statusCodeValue)

        val action = response.body!!
        assertEquals(ActionType.Route, action.type)
        assertEquals(
            "http://localhost:0/cashin/confirm?amount=10000.0&payment-token=xxxx&idempotency-key=123&cvv=019",
            action.url
        )
    }
}
