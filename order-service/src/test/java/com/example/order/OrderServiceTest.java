package com.example.order;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 故意不覆盖：PLATINUM 折扣、30000 档满减、偏远地区运费，用于验证部分覆盖。
 */
class OrderServiceTest {

    private final OrderService service = new OrderService();

    @Test
    void subtotalOfEmptyIsZero() {
        assertEquals(0, service.subtotal(List.of()));
        assertEquals(0, service.subtotal(null));
    }

    @Test
    void subtotalRejectsNonPositiveQuantity() {
        assertThrows(IllegalArgumentException.class,
                () -> service.subtotal(List.of(new OrderItem("A", 100, 0))));
    }

    @Test
    void goldMemberGetsTenPercentOff() {
        assertEquals(9000, service.applyMemberDiscount(10000, MemberLevel.GOLD));
        assertEquals(9500, service.applyMemberDiscount(10000, MemberLevel.SILVER));
        assertEquals(10000, service.applyMemberDiscount(10000, MemberLevel.NORMAL));
    }

    @Test
    void fullReductionTiers() {
        assertEquals(17000, service.applyFullReduction(20000));
        assertEquals(9000, service.applyFullReduction(10000));
        assertEquals(5000, service.applyFullReduction(5000));
    }

    @Test
    void totalWithFreeShipping() {
        List<OrderItem> items = List.of(new OrderItem("A", 6000, 2));
        // 12000 -> GOLD 10800 -> 满减 9800 -> 不满 9900 包邮线 +800
        assertEquals(10600, service.total(items, MemberLevel.GOLD, false));
    }
}
