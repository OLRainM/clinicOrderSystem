package com.clinic.order.appointment.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

public class OrderNoGenerator {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
    private OrderNoGenerator() {}

    public static String next() {
        return "AP" + LocalDateTime.now().format(FMT) + ThreadLocalRandom.current().nextInt(1000, 9999);
    }
}
