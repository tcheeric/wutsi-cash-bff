package com.wutsi.application.cash.endpoint.transaction.screen

import com.wutsi.application.cash.endpoint.Page
import com.wutsi.application.shared.Theme
import com.wutsi.flutter.sdui.Column
import com.wutsi.flutter.sdui.Screen
import com.wutsi.flutter.sdui.Widget
import com.wutsi.platform.account.WutsiAccountApi
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/transaction/success")
class TransactionSuccessScreen(
    accountApi: WutsiAccountApi
) : AbstractTransactionStatusScreen(accountApi) {
    @PostMapping
    fun index(
        @RequestParam(name = "transaction-id") transactionId: String
    ): Widget {
        val tx = paymentApi.getTransaction(transactionId).transaction
        val tenant = tenantProvider.get()

        return Screen(
            id = Page.TRANSACTION_SUCCESS,
            backgroundColor = Theme.COLOR_GRAY_LIGHT,
            appBar = null,
            safe = true,
            child = Column(
                children = listOf(
                    toSectionWidget(
                        padding = null,
                        child = toSectionInfos(tx, tenant)
                    ),
                    toSectionWidget(
                        child = toTransactionStatusWidget(tx)
                    )
                )
            )
        ).toWidget()
    }
}
