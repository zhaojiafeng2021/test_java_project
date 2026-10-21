package com.example.order;

/**
 * 优惠券计算：满减券、折扣券、无门槛券。test_20261021 迭代新增。
 */
public class CouponService {

    public enum CouponType {
        FULL_REDUCTION,
        DISCOUNT,
        NO_THRESHOLD
    }

    public int apply(int amount, CouponType type, int threshold, int value) {
        if (amount <= 0) {
            return 0;
        }
        if (type == null) {
            return amount;
        }
        int result;
        switch (type) {
            case FULL_REDUCTION:
                result = fullReduction(amount, threshold, value);
                break;
            case DISCOUNT:
                result = discount(amount, value);
                break;
            case NO_THRESHOLD:
                result = amount - value;
                break;
            default:
                result = amount;
        }
        return Math.max(result, 0);
    }

    private int fullReduction(int amount, int threshold, int value) {
        if (amount >= threshold) {
            return amount - value;
        }
        return amount;
    }

    private int discount(int amount, int percent) {
        if (percent <= 0 || percent >= 100) {
            throw new IllegalArgumentException("percent must be in (0,100): " + percent);
        }
        return amount * percent / 100;
    }

    public boolean isStackable(CouponType a, CouponType b) {
        if (a == b) {
            return false;
        }
        return a != CouponType.DISCOUNT && b != CouponType.DISCOUNT;
    }
}
