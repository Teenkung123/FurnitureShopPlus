package com.Teenkung123.furnitureShopPlus.Utils;

public record ShopItem(String namespace, String currency, double price)  implements Comparable<ShopItem> {

    @Override
    public int compareTo(ShopItem other) {
        return this.namespace.compareTo(other.namespace);
    }

    @Override
    public String toString() {
        return "ShopItem{" +
                "namespace='" + namespace + '\'' +
                ", currency='" + currency + '\'' +
                ", price=" + price +
                '}';
    }
}
