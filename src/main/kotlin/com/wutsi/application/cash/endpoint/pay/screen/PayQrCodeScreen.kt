package com.wutsi.application.cash.endpoint.pay.screen

import com.wutsi.application.cash.endpoint.AbstractQuery
import com.wutsi.application.cash.endpoint.Page
import com.wutsi.application.shared.Theme
import com.wutsi.application.shared.service.TenantProvider
import com.wutsi.flutter.sdui.Action
import com.wutsi.flutter.sdui.AppBar
import com.wutsi.flutter.sdui.Center
import com.wutsi.flutter.sdui.Column
import com.wutsi.flutter.sdui.Container
import com.wutsi.flutter.sdui.IconButton
import com.wutsi.flutter.sdui.MoneyText
import com.wutsi.flutter.sdui.QrImage
import com.wutsi.flutter.sdui.Screen
import com.wutsi.flutter.sdui.Text
import com.wutsi.flutter.sdui.Widget
import com.wutsi.flutter.sdui.enums.ActionType.Route
import com.wutsi.flutter.sdui.enums.Alignment
import com.wutsi.flutter.sdui.enums.CrossAxisAlignment
import com.wutsi.platform.qr.WutsiQrApi
import com.wutsi.platform.qr.dto.EncodeQRCodeRequest
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/pay/qr-code")
class PayQrCodeScreen(
    private val tenantProvider: TenantProvider,
    private val qrApi: WutsiQrApi,
) : AbstractQuery() {
    @PostMapping
    fun index(
        @RequestParam(name = "payment-request-id") paymentRequestId: String,
        @RequestParam amount: Double
    ): Widget {
        val tenant = tenantProvider.get()
        return Screen(
            id = Page.PAY_QR_CODE,
            appBar = AppBar(
                elevation = 0.0,
                backgroundColor = Theme.COLOR_WHITE,
                foregroundColor = Theme.COLOR_BLACK,
                title = getText("page.pay-qr-code.app-bar.title"),
                actions = listOf(
                    IconButton(
                        icon = Theme.ICON_CANCEL,
                        action = Action(
                            type = Route,
                            url = "route:/~"
                        )
                    )
                )
            ),
            child = Column(
                crossAxisAlignment = CrossAxisAlignment.center,
                children = listOf(
                    Container(
                        padding = 10.0,
                        child = MoneyText(
                            value = amount,
                            currency = tenant.currencySymbol,
                            numberFormat = tenant.numberFormat,
                        ),
                    ),
                    Center(
                        child = Container(
                            padding = 10.0,
                            alignment = Alignment.Center,
                            child = QrImage(
                                data = qrApi.encode(
                                    EncodeQRCodeRequest(
                                        type = "payment-request",
                                        id = paymentRequestId,
                                        timeToLive = 300
                                    )
                                ).token,
                                size = 230.0,
                                padding = 10.0,
                                embeddedImageSize = 32.0,
                                embeddedImageUrl = tenant.logos.find { it.type == "PICTORIAL" }?.url
                            ),
                        ),
                    ),
                    Container(
                        padding = 10.0,
                        margin = 10.0,
                        alignment = Alignment.Center,
                        background = Theme.COLOR_PRIMARY_LIGHT,
                        border = 1.0,
                        borderRadius = 3.0,
                        child = Column(
                            children = listOf(
                                Text(getText("page.pay-qr-code.message")),
                            )
                        )
                    ),
                ),
            ),
        ).toWidget()
    }
}
