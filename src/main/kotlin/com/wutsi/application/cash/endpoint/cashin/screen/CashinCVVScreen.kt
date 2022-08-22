package com.wutsi.application.cash.endpoint.cashin.screen

import com.wutsi.application.cash.endpoint.AbstractQuery
import com.wutsi.application.cash.endpoint.Page
import com.wutsi.application.shared.Theme
import com.wutsi.flutter.sdui.Action
import com.wutsi.flutter.sdui.AppBar
import com.wutsi.flutter.sdui.Column
import com.wutsi.flutter.sdui.Container
import com.wutsi.flutter.sdui.Divider
import com.wutsi.flutter.sdui.Form
import com.wutsi.flutter.sdui.Icon
import com.wutsi.flutter.sdui.IconButton
import com.wutsi.flutter.sdui.Input
import com.wutsi.flutter.sdui.MoneyText
import com.wutsi.flutter.sdui.Row
import com.wutsi.flutter.sdui.Screen
import com.wutsi.flutter.sdui.Text
import com.wutsi.flutter.sdui.Widget
import com.wutsi.flutter.sdui.enums.ActionType
import com.wutsi.flutter.sdui.enums.Alignment.Center
import com.wutsi.flutter.sdui.enums.CrossAxisAlignment
import com.wutsi.flutter.sdui.enums.InputType
import com.wutsi.flutter.sdui.enums.InputType.Submit
import com.wutsi.flutter.sdui.enums.MainAxisAlignment
import com.wutsi.platform.account.WutsiAccountApi
import com.wutsi.platform.payment.dto.ComputeFeesRequest
import com.wutsi.platform.payment.entity.TransactionType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.text.DecimalFormat

@RestController
@RequestMapping("/cashin/cvv")
class CashinCVVScreen(
    private val accountApi: WutsiAccountApi
) : AbstractQuery() {
    @PostMapping
    fun index(
        @RequestParam amount: Double,
        @RequestParam("payment-token") paymentToken: String,
        @RequestParam("idempotency-key") idempotencyKey: String
    ): Widget {
        val accountId = securityContext.currentAccountId()
        val tenant = tenantProvider.get()
        val balance = getBalance(tenant)
        val fmt = DecimalFormat(tenant.monetaryFormat)
        val paymentMethod = accountApi.getPaymentMethod(accountId, paymentToken).paymentMethod
        val fees = paymentApi.computeFees(
            request = ComputeFeesRequest(
                transactionType = TransactionType.CASHIN.name,
                paymentMethodType = paymentMethod.type,
                amount = amount,
                currency = tenant.currency
            )
        ).fee

        return Screen(
            id = Page.CASHIN_CVV,
            appBar = AppBar(
                elevation = 0.0,
                backgroundColor = Theme.COLOR_WHITE,
                foregroundColor = Theme.COLOR_BLACK,
                title = getText("page.cashin.cvv.app-bar.title", arrayOf(fmt.format(balance.value))),
                actions = listOf(
                    IconButton(
                        icon = Theme.ICON_CANCEL,
                        action = Action(
                            type = ActionType.Route,
                            url = "route:/~"
                        )
                    )
                )
            ),
            child = Container(
                alignment = Center,
                child = Column(
                    children = listOf(
                        Container(padding = 10.0),
                        Container(
                            padding = 10.0,
                            child = Column(
                                children = listOf(
                                    Row(
                                        mainAxisAlignment = MainAxisAlignment.start,
                                        crossAxisAlignment = CrossAxisAlignment.start,
                                        children = listOf(
                                            Icon(
                                                code = getLogoUrl(tenant, paymentMethod) ?: "",
                                                size = 24.0
                                            ),
                                            Container(padding = 5.0),
                                            Text(
                                                caption = formattedAccountNumber(paymentMethod),
                                                size = Theme.TEXT_SIZE_LARGE
                                            )
                                        )
                                    ),
                                    Divider(color = Theme.COLOR_DIVIDER)
                                )
                            )
                        ),
                        Container(
                            padding = 10.0,
                            child = MoneyText(
                                value = amount,
                                currency = tenant.currency,
                                numberFormat = tenant.numberFormat,
                                color = Theme.COLOR_PRIMARY
                            )
                        ),
                        toFeeDetailsWidget(fees, fmt, Page.CASHIN_CONFIRM),

                        Form(
                            children = listOf(
                                Container(
                                    padding = 10.0,
                                    child = Input(
                                        name = "cvv",
                                        maxLength = 3,
                                        caption = getText("page.cashin.cvv.input.cvv"),
                                        type = InputType.Number,
                                        required = true
                                    )
                                ),
                                Container(
                                    padding = 10.0,
                                    child = Column(
                                        mainAxisAlignment = MainAxisAlignment.start,
                                        crossAxisAlignment = CrossAxisAlignment.start,
                                        children = listOf(
                                            Text(
                                                caption = getText("page.cashin.cvv.what-is-cvv-q"),
                                                size = Theme.TEXT_SIZE_LARGE,
                                                bold = true
                                            ),
                                            Text(
                                                caption = getText("page.cashin.cvv.what-is-cvv-a")
                                            )
                                        )
                                    )
                                ),
                                Container(
                                    padding = 10.0,
                                    child = Input(
                                        name = "command",
                                        type = Submit,
                                        caption = getText(
                                            "page.cashin.cvv.button.submit",
                                            arrayOf(fmt.format(amount))
                                        ),
                                        action = Action(
                                            type = ActionType.Command,
                                            url = urlBuilder.build("/commands/cashin/cvv"),
                                            parameters = mapOf(
                                                "amount" to amount.toString(),
                                                "payment-token" to paymentToken,
                                                "idempotency-key" to idempotencyKey
                                            )
                                        )
                                    )
                                )
                            )
                        )
                    )
                )
            )
        ).toWidget()
    }
}
