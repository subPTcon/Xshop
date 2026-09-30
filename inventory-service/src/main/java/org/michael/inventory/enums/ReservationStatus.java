package org.michael.inventory.enums;

public enum ReservationStatus {
    RESERVED(0, "已预占"),
    CONFIRMED(1, "已确认"),
    RELEASED(2, "已释放");


    private Integer code;
    private String name;

    ReservationStatus(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public Integer getCode() { return this.code;}
}
