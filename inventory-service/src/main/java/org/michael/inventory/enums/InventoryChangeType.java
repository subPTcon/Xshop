package org.michael.inventory.enums;

public enum InventoryChangeType {
    RESERVE(1, "预扣"),
    RELEASE(2, "释放"),
    CONFIRME(3, "真实扣减"),
    ROLLBACK(4, "回滚");

    private Integer code;
    private String name;

    InventoryChangeType(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public Integer getCode() { return this.code; }
}
