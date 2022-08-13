package com.wutsi.application.cash.endpoint.cashout.screen

import com.wutsi.application.cash.endpoint.AbstractQuery
import com.wutsi.application.cash.endpoint.Page
import com.wutsi.application.shared.Theme
import com.wutsi.flutter.sdui.Action
import com.wutsi.flutter.sdui.AppBar
import com.wutsi.flutter.sdui.Column
import com.wutsi.flutter.sdui.Container
import com.wutsi.flutter.sdui.DropdownButton
import com.wutsi.flutter.sdui.DropdownMenuItem
import com.wutsi.flutter.sdui.Form
import com.wutsi.flutter.sdui.Input
import com.wutsi.flutter.sdui.MoneyWithKeyboard
import com.wutsi.flutter.sdui.Screen
import com.wutsi.flutter.sdui.Widget
import com.wutsi.flutter.sdui.enums.ActionType.Command
import com.wutsi.flutter.sdui.enums.Alignment.Center
import com.wutsi.flutter.sdui.enums.InputType.Submit
import com.wutsi.platform.account.WutsiAccountApi
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.text.DecimalFormat

@RestController
@RequestMapping("/cashout")
class CashoutScreen(
    private val accountApi: WutsiAccountApi,
) : AbstractQuery() {
    @PostMapping
    fun index(): Widget {
        val tenant = tenantProvider.get()
        val paymentMethods = accountApi.listPaymentMethods(
            securityContext.currentAccountId()
        ).paymentMethods
        val balance = getBalance(tenant)
        val balanceText = DecimalFormat(tenant.monetaryFormat).format(balance.value)

        return Screen(
            id = Page.CASHOUT,
            appBar = AppBar(
                elevation = 0.0,
                backgroundColor = Theme.COLOR_WHITE,
                foregroundColor = Theme.COLOR_BLACK,
                title = getText("page.cashout.app-bar.title", arrayOf(balanceText))
            ),
            child = Container(
                alignment = Center,
                child = Column(
                    children = listOf(
                        Form(
                            children = listOf(
                                Container(
                                    padding = 10.0,
                                    child = DropdownButton(
                                        value = if (paymentMethods.size == 1) paymentMethods[0].token else null,
                                        name = "paymentToken",
                                        required = true,
                                        hint = getText("page.cashout.payment-token.hint"),
                                        children = paymentMethods.map {
                                            DropdownMenuItem(
                                                caption = formattedAccountNumber(it) ?: it.maskedNumber,
                                                value = it.token,
                                                icon = getLogoUrl(tenant, it)
                                            )
                                        }
                                    )
                                ),
                                MoneyWithKeyboard(
                                    name = "amount",
                                    maxLength = 7,
                                    currency = tenant.currency,
                                    moneyColor = Theme.COLOR_PRIMARY,
                                    numberFormat = tenant.numberFormat,
                                    keyboardButtonSize = 65.0
                                ),
                                Container(
                                    padding = 10.0,
                                    child = Input(
                                        name = "command",
                                        type = Submit,
                                        caption = getText("page.cashout.button.submit"),
                                        action = Action(
                                            type = Command,
                                            url = urlBuilder.build("commands/cashout/amount")
                                        )
                                    )
                                )
                            )
                        )
                    )
                )
            ),
        ).toWidget()
    }
}
