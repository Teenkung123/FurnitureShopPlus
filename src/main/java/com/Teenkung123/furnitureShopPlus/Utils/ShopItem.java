package com.Teenkung123.furnitureShopPlus.Utils;

public record ShopItem(String namespace, String currency, double price) {
    @Override
    public String toString() {
        return "ShopItem{" +
                "namespace='" + namespace + '\'' +
                ", currency='" + currency + '\'' +
                ", price=" + price +
                '}';
    }
}
