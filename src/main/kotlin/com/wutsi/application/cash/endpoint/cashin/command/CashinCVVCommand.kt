package com.wutsi.application.cash.endpoint.cashin.command

import com.wutsi.application.cash.endpoint.AbstractCommand
import com.wutsi.application.cash.endpoint.cashin.dto.CashinCVVRequest
import com.wutsi.flutter.sdui.Action
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@RestController
@RequestMapping("/commands/cashin/cvv")
class CashinCVVCommand : AbstractCommand() {
    @PostMapping
    fun index(
        @RequestParam amount: Double,
        @RequestParam("payment-token") paymentToken: String,
        @RequestParam(name = "idempotency-key") idempotencyKey: String,

        @RequestBody @Valid
        request: CashinCVVRequest
    ): Action {
        return gotoUrl(
            url = urlBuilder.build("cashin/confirm?amount=$amount&payment-token=$paymentToken&idempotency-key=$idempotencyKey&cvv=${request.cvv}")
        )
    }
}
