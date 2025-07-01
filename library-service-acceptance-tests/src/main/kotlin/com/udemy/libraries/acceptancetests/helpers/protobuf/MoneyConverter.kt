package com.udemy.libraries.acceptancetests.helpers.protobuf

import com.google.protobuf.util.JsonFormat
import com.udemy.dto.money.v1.MoneyOuterClass
import java.math.BigDecimal


@Deprecated("It will be moved out of this library")
class MoneyConverter(private val jsonProtoPrinter: JsonFormat.Printer) {
    companion object {
        const val NANOS_SCALE = 9
    }

    fun moneyOf(amount: BigDecimal, currency: String): MoneyOuterClass.Money {
        return MoneyOuterClass.Money.newBuilder()
            .setCurrencyCode(currency)
            .setUnits(amount.toLong())
            .setNanos(
                amount
                    .stripTrailingZeros()
                    .remainder(BigDecimal.ONE)
                    .movePointRight(NANOS_SCALE)
                    .toInt()
            )
            .build()
    }

    fun amountOf(money: MoneyOuterClass.Money): BigDecimal =
        (money.units.toBigDecimal() + money.nanos.toBigDecimal().movePointLeft(9)).stripTrailingZeros()

    fun money(value: String, currency: String): String {
        val amount = BigDecimal(value)
        val money = this.moneyOf(amount, currency)
        return jsonProtoPrinter.print(money)
    }
}