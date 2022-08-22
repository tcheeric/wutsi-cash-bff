package com.wutsi.application.cash.endpoint.cashin.command

import com.wutsi.application.cash.endpoint.AbstractCommand
import com.wutsi.application.cash.endpoint.cashin.dto.CashinAmountRequest
import com.wutsi.application.cash.service.IdempotencyKeyGenerator
import com.wutsi.flutter.sdui.Action
import com.wutsi.platform.account.WutsiAccountApi
import com.wutsi.platform.payment.PaymentMethodProvider
import com.wutsi.platform.payment.PaymentMethodType
import com.wutsi.platform.tenant.dto.Tenant
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.text.DecimalFormat
import javax.validation.Valid

@RestController
@RequestMapping("/commands/cashin/amount")
class CashinAmountCommand(
    private val idempotencyKeyGenerator: IdempotencyKeyGenerator,
    private val accountApi: WutsiAccountApi
) : AbstractCommand() {
    @PostMapping
    fun index(
        @RequestBody @Valid
        request: CashinAmountRequest
    ): Action {
        logger.add("amount", request.amount)
        logger.add("payment_token", request.paymentToken)

        // Validate
        val tenant = tenantProvider.get()
        val error = validate(request, tenant)
        if (error != null)
            return error

        // Cash-in
        val idempotencyKey = idempotencyKeyGenerator.generate()
        val paymentMethod =
            accountApi.getPaymentMethod(securityContext.currentAccountId(), request.paymentToken).paymentMethod
        val provider = PaymentMethodProvider.valueOf(paymentMethod.provider.uppercase())
        return if (provider.type == PaymentMethodType.CREDIT_CARD) {
            gotoUrl(
                url = urlBuilder.build("cashin/cvv?amount=${request.amount}&payment-token=${request.paymentToken}&idempotency-key=$idempotencyKey")
            )
        } else {
            gotoUrl(
                url = urlBuilder.build("cashin/confirm?amount=${request.amount}&payment-token=${request.paymentToken}&idempotency-key=$idempotencyKey")
            )
        }
    }

    private fun validate(request: CashinAmountRequest, tenant: Tenant): Action? {
        if (request.amount == 0.0)
            return showError(
                message = getText("prompt.error.amount-required")
            )

        if (request.amount < tenant.limits.minCashin) {
            val amountText = DecimalFormat(tenant.monetaryFormat).format(tenant.limits.minCashin)
            return showError(
                message = getText("prompt.error.min-cashin", arrayOf(amountText))
            )
        }

        return null
    }
}
