package com.example.order;

import java.util.List;

/**
 * 订单金额计算：小计、会员折扣、满减、运费。
 */
public class OrderService {

    public static final int FREE_SHIPPING_THRESHOLD = 9900; // 单位：分
    public static final int SHIPPING_FEE = 800;

    public int subtotal(List<OrderItem> items) {
        if (items == null || items.isEmpty()) {
            return 0;
        }
        int sum = 0;
        for (OrderItem item : items) {
            if (item.quantity() <= 0) {
                throw new IllegalArgumentException("quantity must be positive: " + item.sku());
            }
            sum += item.unitPrice() * item.quantity();
        }
        return sum;
    }

    public int applyMemberDiscount(int amount, MemberLevel level) {
        switch (level) {
            case GOLD:
                return amount * 90 / 100;
            case SILVER:
                return amount * 95 / 100;
            case PLATINUM:
                return amount * 85 / 100;
            default:
                return amount;
        }
    }

    public int applyFullReduction(int amount) {
        if (amount >= 30000) {
            return amount - 5000;
        } else if (amount >= 20000) {
            return amount - 3000;
        } else if (amount >= 10000) {
            return amount - 1000;
        }
        return amount;
    }

    public int shipping(int amount, boolean remoteArea) {
        if (remoteArea) {
            return SHIPPING_FEE * 2;
        }
        return amount >= FREE_SHIPPING_THRESHOLD ? 0 : SHIPPING_FEE;
    }

    public int total(List<OrderItem> items, MemberLevel level, boolean remoteArea) {
        int amount = subtotal(items);
        amount = applyMemberDiscount(amount, level);
        amount = applyFullReduction(amount);
        return amount + shipping(amount, remoteArea);
    }
}
