package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.concurrent.CountDownLatch;

import static org.example.Main.nameAndCoin;
import static org.example.Main.walletAddressAndTraders;

public class ExecuteTransaction implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(ExecuteTransaction.class);
    TransactionType type;
    String coinSymbol;
    Long buyQuantity;
    String walletAddress;
    Double updatePrice;
    Long increasedVolume;
    Long sellQuantity;
    Coins coinObject;

    CountDownLatch latch;
    
    public ExecuteTransaction() {}

    public ExecuteTransaction(TransactionType type, String coinSymbol, Long buyQuantity, String walletAddress, Double updatePrice, Long increasedVolume, Long sellQuantity, CountDownLatch latch) {
        this.type = type;
        this.coinSymbol = coinSymbol;
        this.buyQuantity = buyQuantity;
        this.walletAddress = walletAddress;
        this.updatePrice = updatePrice;
        this.increasedVolume = increasedVolume;
        this.sellQuantity = sellQuantity;
        this.coinObject = nameAndCoin.get(coinSymbol);
        this.latch = latch;
    }

    private String getBlockHash() {
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder transactionHash = new StringBuilder();
        Random rnd = new Random();
        /**
         * Introducing delay mimicking complex
         * calculation being performed.
         */
        for (double i = 0; i < 199999999; i++) {
            i = i;
        }
        while (transactionHash.length() < 128) {
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            transactionHash.append(SALTCHARS.charAt(index));
        }
        String hashCode = transactionHash.toString();
        return "0x" + hashCode.toLowerCase();
    }

    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public void run() {
        switch (type) {
            case BUY:
                buyCoin();
                break;
            case SELL:
                sellCoin();
                break;
            case ADD_VOLUME:
                addVolume();
                break;
            case UPDATE_PRICE:
                updatePrice();
                break;
        }
        latch.countDown();
    }

    private void buyCoin() {
        getBlockHash();
        if (coinObject == null) {
            logger.info("No coin exists with the given coin symbol: " + coinSymbol);
            logger.info("Skipping this transaction!");
            return;
        }
        Traders Traders = walletAddressAndTraders.get(walletAddress);
        if (Traders == null) {
            logger.info("No Traders exists with the given wallet address: " + walletAddress);
            return;
        }
        if (buyQuantity == 0 || buyQuantity == null) {
            logger.info("Buy quantity input is wrong for the buy transaction of coin symbol: " + coinSymbol);
            logger.info("Skipping this statement!");
            return;
        }
        synchronized (coinObject) {
            Long coinSupply = coinObject.getCirculatingSupply();
            while (coinSupply <= buyQuantity) {
                try {
                    this.coinObject.wait();
                } catch (InterruptedException e) {
                    logger.error(e.toString());
                    System.exit(0);
                }
                coinSupply = coinObject.getCirculatingSupply();
            }
            Double coinPrice = coinObject.getCoinPrice();
            if (coinPrice == null) {
                logger.info("No coin price is given for the coin: " + coinSymbol);
                logger.info("Skipping this transaction!");
                return;
            }
            coinObject.decrementSupplyBy(buyQuantity);
            Traders.buyCoin(coinSymbol, coinPrice, buyQuantity);
            this.coinObject.notifyAll();
        }
    }

    private void sellCoin() {
        getBlockHash();
        if (coinObject == null) {
            logger.info("No coin exists with the given coin symbol: " + coinSymbol);
            logger.info("Skipping this transaction!");
            return;
        }
        Traders Traders = walletAddressAndTraders.get(walletAddress);
        if (Traders == null) {
            logger.info("No Traders exists with the given wallet address: " + walletAddress);
            return;
        }
        if (sellQuantity == 0 || sellQuantity == null) {
            logger.info("Sell quantity input is wrong for the buy transaction of coin symbol: " + coinSymbol);
            logger.info("Skipping this statement!");
            return;
        }
        synchronized (this.coinObject) {
            Long quantityOwned = Traders.getCoinQuantityOwnedByName(coinSymbol);
            //Thinking of json file as random commands of buy and sell, anyone can happen anytime
            while (quantityOwned < sellQuantity) {
                try {
                    this.coinObject.wait();
                } catch (InterruptedException e) {
                    logger.error(e.toString());
                    System.exit(0);
                }
                quantityOwned = Traders.getCoinQuantityOwnedByName(coinSymbol);
            }
            Double sellingPrice = coinObject.getCoinPrice();
            Traders.sellCoin(coinSymbol, sellingPrice, sellQuantity);
            coinObject.incrementSupplyBy(sellQuantity);
            this.coinObject.notifyAll();
        }
    }

    private void addVolume() {
        //Adding a new coin if add volume is called for a non-existing coin.
        if (coinObject == null) {
            Coins newCoin = new Coins(null, null, coinSymbol, null, increasedVolume);
            nameAndCoin.put(coinSymbol, newCoin);
        } else {
            synchronized (coinObject) {
                Coins coinObject = nameAndCoin.get(coinSymbol);
                coinObject.incrementSupplyBy(increasedVolume);
                this.coinObject.notifyAll();
            }
        }
    }

    private void updatePrice() {
        if (coinObject != null) {
            synchronized (coinObject) {
                if (updatePrice == null) {
                    logger.info("UpdatePrice input is wrong. So Skipping the update price for the coin: " + coinSymbol);
                    return;
                }
                coinObject.setCoinPrice(updatePrice);
            }
        } else {
            logger.info("There is not any coin present with this coin symbol. So skipping this update price transaction with coin symbol: " + coinSymbol);
        }
    }
}
