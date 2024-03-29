package com.wutsi.application.cash.endpoint.send.command

import com.wutsi.application.cash.endpoint.AbstractCommand
import com.wutsi.application.cash.endpoint.send.dto.SendRecipientRequest
import com.wutsi.flutter.sdui.Action
import com.wutsi.platform.account.WutsiAccountApi
import com.wutsi.platform.account.dto.SearchAccountRequest
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@RestController
@RequestMapping("/commands/send/recipient")
class SendRecipientCommand(
    private val accountApi: WutsiAccountApi
) : AbstractCommand() {
    @PostMapping
    fun index(
        @RequestParam amount: Double,
        @RequestBody @Valid
        request: SendRecipientRequest
    ): Action {
        logger.add("phone_number", request.phoneNumber)

        val action = validate(request)
        if (action != null)
            return action

        return gotoUrl(
            url = urlBuilder.build("send/confirm"),
            parameters = mapOf(
                "amount" to amount.toString(),
                "phone-number" to request.phoneNumber
            )
        )
    }

    private fun validate(request: SendRecipientRequest): Action? {
        val accounts = accountApi.searchAccount(
            SearchAccountRequest(
                phoneNumber = request.phoneNumber
            )
        ).accounts

        if (accounts.isEmpty()) {
            /*
              No user found with this phone number.
              Return no error, the confirmation page will handle this problem
             */
            return null
        } else if (accounts[0].id == securityContext.currentAccountId()) {
            return showError(
                message = getText("prompt.error.self-transfer")
            )
        }

        return null
    }
}
