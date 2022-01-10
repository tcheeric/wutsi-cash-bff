package com.wutsi.application.cash.endpoint.send.command

import com.fasterxml.jackson.databind.ObjectMapper
import com.wutsi.application.cash.endpoint.AbstractCommand
import com.wutsi.application.cash.exception.TransactionException
import com.wutsi.application.cash.service.TenantProvider
import com.wutsi.application.cash.service.URLBuilder
import com.wutsi.flutter.sdui.Action
import com.wutsi.flutter.sdui.enums.ActionType.Route
import com.wutsi.platform.payment.dto.ComputeTransactionFeesRequest
import com.wutsi.platform.payment.dto.CreateTransferRequest
import feign.FeignException
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/commands/send")
class SendCommand(
    private val tenantProvider: TenantProvider,
    private val urlBuilder: URLBuilder,
    private val objectMapper: ObjectMapper,
) : AbstractCommand() {
    @PostMapping
    fun index(
        @RequestParam amount: Double,
        @RequestParam(name = "recipient-id") recipientId: Long,
        @RequestParam(name = "recipient-name") recipientName: String,
    ): Action {
        val tenant = tenantProvider.get()
        try {
            // Get fees
            val fees = paymentApi.computeTransactionFees(
                request = ComputeTransactionFeesRequest(
                    amount = amount,
                    transactionType = "TRANSFER",
                    recipientId = recipientId
                )
            ).fees

            // Perform the transfer
            val response = paymentApi.createTransfer(
                CreateTransferRequest(
                    recipientId = recipientId,
                    amount = amount + fees,
                    currency = tenant.currency
                )
            )
            logger.add("transaction_id", response.id)
            logger.add("transaction_status", response.status)

            return Action(
                type = Route,
                url = urlBuilder.build(
                    "send/success?amount=$amount&recipient-id=$recipientId"
                )
            )
        } catch (ex: FeignException) {
            val error = TransactionException.of(objectMapper, ex).error
            return Action(
                type = Route,
                url = urlBuilder.build(
                    "send/success?error=$error&amount=$amount&recipient-id=$recipientId"
                )
            )
        }
    }
}
