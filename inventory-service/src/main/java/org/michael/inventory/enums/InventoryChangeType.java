package org.michael.inventory.enums;

public enum InventoryChangeType {
    RESERVED(1, "预扣"),
    RELEASED(2, "释放"),
    CONFIRMED(3, "真实扣减"),
    ROLLBACK(4, "回滚");

    private Integer code;
    private String name;

    InventoryChangeType(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public Integer getCode() { return this.code; }
}
