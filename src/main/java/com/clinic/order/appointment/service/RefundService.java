package com.clinic.order.appointment.service;

import org.springframework.stereotype.Service;

@Service
public class RefundService {
    /**
     * 第三方退款系统占位：文档要求已支付取消必须先申请退款，退款受理成功后才能回滚预约。
     */
    public boolean requestRefund(String orderNo, Long userId) {
        // 实际项目在此调用微信/支付宝/内部财务系统，并落退款流水。
        return orderNo != null && userId != null;
    }
}
