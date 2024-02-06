package org.example;

public class Coins {
    Integer coinRank;
    String coinName;
    String coinSymbol;
    Double coinPrice;
    Long circulatingSupply;
    public Coins(Integer coinRank, String coinName, String coinSymbol, Double coinPrice, Long circulatingSupply){
        this.coinRank = coinRank;
        this.coinName = coinName;
        this.coinSymbol = coinSymbol;
        this.coinPrice = coinPrice;
        this.circulatingSupply = circulatingSupply;
    }

    public Double getCoinPrice() {
        return coinPrice;
    }

    public Integer getCoinRank() {
        return coinRank;
    }

    public Long getCirculatingSupply() {
        return circulatingSupply;
    }

    public String getCoinName() {
        return coinName;
    }

    public String getCoinSymbol() {
        return coinSymbol;
    }

    public void setCirculatingSupply(Long circulatingSupply) {
        this.circulatingSupply = circulatingSupply;
    }

    public void setCoinName(String coinName) {
        this.coinName = coinName;
    }

    public void setCoinPrice(Double coinPrice) {
        this.coinPrice = coinPrice;
    }

    public void setCoinRank(Integer coinRank) {
        this.coinRank = coinRank;
    }

    public void setCoinSymbol(String coinSymbol) {
        this.coinSymbol = coinSymbol;
    }

    /**
     * @param Quantity Takes a variable of Long and decrements the circulating supply by quantity
     */
    public void decrementSupplyBy(Long Quantity) {
        Long curSupply =circulatingSupply;
        Long newSupply = curSupply - Quantity;
        setCirculatingSupply(newSupply);
    }

    /**
     * @param Quantity Takes a variable of Long and increments the circulating supply by quantity
     */
    public void incrementSupplyBy(Long Quantity) {
        Long curSupply = circulatingSupply;
        Long newSupply = curSupply + Quantity;
        setCirculatingSupply(newSupply);
    }
    @Override
    public String toString(){
        return "[ Coin Rank: "+coinRank+", Coin Name: " + coinName + ", Coin Symbol: "+ coinSymbol + ", Coin Price: " + coinPrice + ", Available to Buy: " + circulatingSupply + " ]";
    }
}
