package com.wutsi.application.cash.endpoint.cashin.screen

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
import com.wutsi.flutter.sdui.enums.ActionType
import com.wutsi.flutter.sdui.enums.CrossAxisAlignment
import com.wutsi.flutter.sdui.enums.InputType.Submit
import com.wutsi.flutter.sdui.enums.MainAxisAlignment
import com.wutsi.platform.account.WutsiAccountApi
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.text.DecimalFormat

@RestController
@RequestMapping("/cashin")
class CashinScreen(
    private val accountApi: WutsiAccountApi,
) : AbstractQuery() {
    @PostMapping
    fun index(): Widget {
        val tenant = tenantProvider.get()
        val balance = getBalance(tenant)
        val balanceText = DecimalFormat(tenant.monetaryFormat).format(balance.value)
        val paymentMethods = accountApi.listPaymentMethods(
            securityContext.currentAccountId()
        ).paymentMethods

        return Screen(
            id = Page.CASHIN,
            appBar = AppBar(
                elevation = 0.0,
                backgroundColor = Theme.COLOR_WHITE,
                foregroundColor = Theme.COLOR_BLACK,
                title = getText("page.cashin.app-bar.title", arrayOf(balanceText))
            ),
            child = Column(
                mainAxisAlignment = MainAxisAlignment.start,
                crossAxisAlignment = CrossAxisAlignment.start,
                children = listOf(
                    Form(
                        children = listOf(
                            Container(
                                padding = 10.0,
                                child = DropdownButton(
                                    value = if (paymentMethods.size == 1) paymentMethods[0].token else null,
                                    name = "paymentToken",
                                    required = true,
                                    hint = getText("page.cashin.payment-token.hint"),
                                    children = paymentMethods.map {
                                        DropdownMenuItem(
                                            caption = formattedAccountNumber(it) ?: it.maskedNumber,
                                            value = it.token,
                                            icon = getLogoUrl(tenant, it)
                                        )
                                    }
                                )
                            ),
                            Column(
                                mainAxisAlignment = MainAxisAlignment.center,
                                crossAxisAlignment = CrossAxisAlignment.center,
                                children = listOf(
                                    Container(
                                        child = MoneyWithKeyboard(
                                            name = "amount",
                                            maxLength = 7,
                                            currency = tenant.currencySymbol,
                                            moneyColor = Theme.COLOR_PRIMARY,
                                            numberFormat = tenant.numberFormat,
                                            keyboardButtonSize = 65.0
                                        ),
                                    ),
                                    Container(
                                        padding = 10.0,
                                        child = Input(
                                            name = "command",
                                            type = Submit,
                                            caption = getText("page.cashin.button.submit"),
                                            action = Action(
                                                type = ActionType.Command,
                                                url = urlBuilder.build("commands/cashin/amount")
                                            )
                                        )
                                    )
                                )
                            )
                        )
                    )
                ),
            ),
        ).toWidget()
    }
}
