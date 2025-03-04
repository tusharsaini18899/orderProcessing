package com.example.orderProcessing;

import com.example.orderProcessing.entity.Orders;
import com.example.orderProcessing.utils.Common;

public class MockData {

    public static Orders MOCK_ORDER = new Orders();

    public MockData() {
        MOCK_ORDER.setUserId(1L);
        MOCK_ORDER.setItemIds("item1,item2");
        MOCK_ORDER.setTotalAmount(100.50);
        MOCK_ORDER.setStatus(Common.PENDING);
    }
}
