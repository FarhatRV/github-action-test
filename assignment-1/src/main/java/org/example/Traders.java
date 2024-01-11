package org.example;

import org.example.Coins;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.example.Main.nameAndCoin;


public class Traders {
    private Long traderId;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String walletAddress;
    private Double profit;
    private ConcurrentHashMap<String, TreeMap<Double, Long>> coinsQuantityOwnedAtPrice;
    //Used concurrentHashmap to make the storage thread safe
    public Traders(){
        traderId = null;
        firstName = null;
        lastName = null;
        phoneNumber = null;
        walletAddress = null;
        this.profit = 0.0;
        coinsQuantityOwnedAtPrice = new ConcurrentHashMap<>();
    }


    public Double getProfit() {
        return profit;
    }

    public void setProfit(Double profit) {
        synchronized (this) {
            this.profit += profit;
        }
    }

    public String getFirstName() {
        return firstName;
    }
    public String getLastName() {
        return lastName;
    }
    public String getPhoneNumber() {
        return phoneNumber;
    }
    public String getWalletAddress() {
        return walletAddress;
    }

    public Long getTraderId() {
        return traderId;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setWalletAddress(String walletAddress) {
        this.walletAddress = walletAddress;
    }

    public void setTraderId(Long traderId) {
        this.traderId = traderId;
    }
    @Override
    public String toString(){
        String traderString = "[ TraderId: " + traderId + ", FirstName: " + firstName + ", LastName: " + lastName + ", Phone Number: " + phoneNumber + ", Wallet Address: " + walletAddress +  ", Profit Made by Buying and then selling: " + profit +", Profit made if he would sell all the coin at this moment: "+profitMadeIfHeSellAllTheCoin() + ", Coins Owned: [ ";
        for(String coinOwned : coinsQuantityOwnedAtPrice.keySet()){
            traderString += coinOwned + " : [ ";
            TreeMap<Double, Long> particularCoinOwnedAtDifferentPrice = coinsQuantityOwnedAtPrice.get(coinOwned);
            for(Double priceOfBuying: particularCoinOwnedAtDifferentPrice.keySet()){
                traderString += particularCoinOwnedAtDifferentPrice.get(priceOfBuying);
                traderString += "coins At " + priceOfBuying +" price , ";
            }
            traderString+= "], ";
        }
        traderString+= "]";
        return traderString;
    }

    /**
     * @param coinName Name of the coin bought by trader
     * @param price Price of the coin at which buy operation is done
     * @param quantity Quantity of coin which is bought
     *  Adds the coin to the Concurrent hashmap
     */
    public void buyCoin(String coinName, Double price, Long quantity) {
        if (!coinsQuantityOwnedAtPrice.containsKey(coinName)) {
            coinsQuantityOwnedAtPrice.put(coinName, new TreeMap<>());
        }
        TreeMap<Double, Long> givenCoinOwnedQuantityAtPrice = coinsQuantityOwnedAtPrice.get(coinName);
        if (givenCoinOwnedQuantityAtPrice.containsKey(price)) {
            Long quantityPreOwnedAtGivenPrice = givenCoinOwnedQuantityAtPrice.get(price);
            quantity += quantityPreOwnedAtGivenPrice;
        }
        givenCoinOwnedQuantityAtPrice.put(price, quantity);
        coinsQuantityOwnedAtPrice.put(coinName, givenCoinOwnedQuantityAtPrice);
    }

    /**
     * @param coinName A string variable for the Name of coin whose quantity is required
     * @return a long variable which has the total quantity of given coin owned by trader
     */
    public Long getCoinQuantityOwnedByName(String coinName){
        if(!coinsQuantityOwnedAtPrice.containsKey(coinName)){
            return 0L;
        }
        TreeMap<Double, Long> particularCoinQuantityOwnedAtDiffPrices = coinsQuantityOwnedAtPrice.get(coinName);
        Long quantityOwnedOfGivenCoin = 0L;
        Iterator iterator = particularCoinQuantityOwnedAtDiffPrices.entrySet().iterator();
        while (iterator.hasNext()){
            Map.Entry mapElement = (Map.Entry) iterator.next();
            quantityOwnedOfGivenCoin += (Long) mapElement.getValue();
        }
        return quantityOwnedOfGivenCoin;
    }

    /**
     * @param coin A string variable which has the value of coin name
     * @param sellingPrice A double variable which has the value of price at which the given coin is sold
     * @param sellQuantity A long variable which has the value of quantity of given coin that is sold by trader
     * Decrement the quantity of given coin from the hashmap
     */
    public void sellCoin(String coin, Double sellingPrice, Long sellQuantity) {
        TreeMap<Double, Long> particularCoinQuantityOwnedAtDiffPrices = coinsQuantityOwnedAtPrice.get(coin);
        Iterator iterator = particularCoinQuantityOwnedAtDiffPrices.entrySet().iterator();
        Double profitBySellingCurCoin = 0.0;
        while (iterator.hasNext()){
            Map.Entry mapElement = (Map.Entry) iterator.next();
            Long quantityOwnedAtPrice = (Long) mapElement.getValue();
            if(quantityOwnedAtPrice>sellQuantity){
                mapElement.setValue(quantityOwnedAtPrice-sellQuantity);
                Double priceOfBuying = (Double) mapElement.getKey();
                profitBySellingCurCoin += (sellingPrice - priceOfBuying)*sellQuantity;
                break;
            } else {
                Double priceOfBuying = (Double) mapElement.getKey();
                profitBySellingCurCoin += (sellingPrice - priceOfBuying)*quantityOwnedAtPrice;
                sellQuantity -= quantityOwnedAtPrice;
                iterator.remove();
            }
        }
        setProfit(profitBySellingCurCoin);
        coinsQuantityOwnedAtPrice.put(coin, particularCoinQuantityOwnedAtDiffPrices);
    }

    /**
     * @return a double type variable which has the value of profit if traders sell all the coin that he has at that moment
     */
    public Double profitMadeIfHeSellAllTheCoin(){
        Double profit = 0.0;
        for(String coinOwned : coinsQuantityOwnedAtPrice.keySet()){
            TreeMap<Double, Long> particularCoinQuantityOwnedAtDiffPrices =coinsQuantityOwnedAtPrice.get(coinOwned);
            Coins curCoinObject = nameAndCoin.get(coinOwned);
            Double curCoinSellingPrice = curCoinObject.getCoinPrice();
            for(Double priceOfBuying : particularCoinQuantityOwnedAtDiffPrices.keySet()){
                Long quantityOfBuying = particularCoinQuantityOwnedAtDiffPrices.get(priceOfBuying);
                profit += (curCoinSellingPrice-priceOfBuying)*quantityOfBuying;
            }
        }
        return profit;
    }
}
