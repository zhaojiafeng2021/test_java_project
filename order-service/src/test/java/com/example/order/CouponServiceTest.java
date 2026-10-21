package com.example.order;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 故意不覆盖：type==null、NO_THRESHOLD、折扣非法参数、isStackable，用于验证增量部分覆盖。
 */
class CouponServiceTest {

    private final CouponService service = new CouponService();

    @Test
    void nonPositiveAmountIsZero() {
        assertEquals(0, service.apply(0, CouponService.CouponType.DISCOUNT, 0, 90));
    }

    @Test
    void fullReductionReachesThreshold() {
        assertEquals(8000, service.apply(10000, CouponService.CouponType.FULL_REDUCTION, 10000, 2000));
        assertEquals(5000, service.apply(5000, CouponService.CouponType.FULL_REDUCTION, 10000, 2000));
    }

    @Test
    void discountNinetyPercent() {
        assertEquals(9000, service.apply(10000, CouponService.CouponType.DISCOUNT, 0, 90));
    }
}
