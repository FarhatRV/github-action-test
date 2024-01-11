package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Half boiled code.
 * logic to implementing multithreading is WIP
 */
public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    public static HashMap<String, Coins> nameAndCoin = new HashMap<>();
    public static HashMap<String, Traders> walletAddressAndTraders = new HashMap<>();

    public static void main(String[] args) throws IOException {
        Map<String, Coins> coinNameToCoinMap = new ConcurrentHashMap<>();
        Map<String, Coins> coinCodeToCoinMap = new ConcurrentHashMap<>();

        Path pathToCoinsCSV = Path.of("src/main/resources/coins.csv");
        Path pathToTradersCSV = Path.of("src/main/resources/traders.csv");
        ArrayList<String[]> parsedCoinsCSV = parseCSV(pathToCoinsCSV);
        ArrayList<String[]> parsedTradersCSV = parseCSV(pathToTradersCSV);
        HashMap<String, Long> BuyList = new HashMap<>();
        // do some operations on parsedCSV like constructing a Coin Object...

        int initialCapacityOfCoins = (int) Files.lines(pathToCoinsCSV).count();
        Vector<Coins> coins = new Vector<>(initialCapacityOfCoins);

        int initialCapacityOfTraders = (int) Files.lines(pathToTradersCSV).count();
        Vector<Traders> traders = new Vector<>(initialCapacityOfTraders);

        for (String[] coinString : parsedCoinsCSV) {
            Coins coin = new Coins(Integer.parseInt(coinString[1]), coinString[2], coinString[3], Double.parseDouble(coinString[4]), Long.parseLong(coinString[5]));
            coinNameToCoinMap.put(coin.getCoinName(), coin);
            coinCodeToCoinMap.put(coin.getCoinSymbol(), coin);
            coins.add(coin);
        }

        for (String[] traderString : parsedTradersCSV) {
            Traders trader = new Traders();

            traders.add(trader);
        }

        Coins coinByName = getCoinDetailsByName("", coinNameToCoinMap);
        Coins coinByCode = getCoinDetailsByCode("", coinCodeToCoinMap);

        JsonNode transactionArray = parseJsonFile("src/main/resources/small_transaction.json");
        System.out.println(transactionArray);
        executeTransactions(transactionArray,new CountDownLatch(transactionArray.size()));
    }

    /**
     * Function to parse csv.
     *
     * @param path to csv file.
     * @return parsed csv as ArrayList<String[]> where each String[] represents each row in the csv.
     * @throws IOException
     */
    public static ArrayList<String[]> parseCSV(Path path) throws IOException {
        BufferedReader reader = Files.newBufferedReader(path);
        String line;
        int skipfirstval = 0;
        ArrayList<String[]> csvInfoList = new ArrayList<>();
        while ((line = reader.readLine()) != null) {
            if (skipfirstval == 0) {
                skipfirstval = 1;
                continue;
            }
            String[] csvContent = line.split(",");
            csvInfoList.add(csvContent);
        }
        return csvInfoList;
    }

    /**
     * Gets coin details by name.
     *
     * @param name the name of the coin
     * @param coinNameToCoinMap map of coin name to Coin object
     * @return the Coin object matching the name, or null if not found
     */
    public static Coins getCoinDetailsByName(String name, Map<String, Coins> coinNameToCoinMap) {
        return coinNameToCoinMap.getOrDefault(name, null);
    }

    /**
     * Gets coin details by symbol/code.
     *
     * @param code the symbol/code of the coin
     * @param coinCodeToCoinMap map of coin symbol/code to Coin object
     * @return the Coin object matching the code, or null if not found
     */
    public static Coins getCoinDetailsByCode(String code, Map<String, Coins> coinCodeToCoinMap) {
        return coinCodeToCoinMap.getOrDefault(code, null);
    }

    /**
     * Gets the top N coins by price.
     *
     * @param coins list of Coin objects
     * @param topN number of top coins to return
     * @return top N coins sorted by price descending
     */
    public static List<Coins> getTopNCoins(List<Coins> coins, int topN) {
        return coins.stream()
                .sorted(Comparator.comparingDouble(Coins::getCoinPrice).reversed())
                .limit(topN)
                .collect(Collectors.toList());
    }

    /**
     * Executes transactions concurrently from a Json input.
     *
     * @param jsonTransactions JsonNode containing transactions
     * @param latch countdown latch to control concurrency
     */
    public static void executeTransactions(JsonNode jsonTransactions, CountDownLatch latch) {
        ExecutorService transactionService = Executors.newFixedThreadPool(100);
        for (JsonNode jsonTransaction : jsonTransactions) {
            TransactionType curTransactionType = TransactionType.valueOf(jsonTransaction.get("type").asText());
            JsonNode jsonData = jsonTransaction.get("data");
            String coin = jsonData.get("coin").asText();

            if (coin == null) {
                throw new InputMismatchException("No input for coin name is given!!");
            }

            Long buyQuantity = null;
            String walletAddress = null;
            Long increasedVolume = null;
            Double updatedPrice = null;
            Long sellQuantity = null;

            switch (curTransactionType) {
                case BUY:
                    try {
                        buyQuantity = jsonData.get("quantity").asLong();
                        walletAddress = jsonData.get("wallet_address").asText();
                    } catch (Exception e) {
                        logger.info("Invalid input for current transaction! Skipping this transaction!!");
                    }
                    break;
                case SELL:
                    try {
                        sellQuantity = jsonData.get("quantity").asLong();
                        walletAddress = jsonData.get("wallet_address").asText();
                    } catch (Exception e) {
                        logger.info("Invalid input for current transaction! Skipping this transaction!!");
                    }
                    break;
                case ADD_VOLUME:
                    try {
                        increasedVolume = jsonData.get("volume").asLong();
                    } catch (Exception e) {
                        logger.info("Invalid input for current transaction! Skipping this transaction!!");
                    }
                    break;
                case UPDATE_PRICE:
                    try {
                        updatedPrice = jsonData.get("price").asDouble();
                    } catch (Exception e) {
                        logger.info("Invalid input for current transaction! Skipping this transaction!!");
                    }
            }
            /**
             * Creates an ExecuteTransaction instance to execute a transaction concurrently.
             *
             * @param curTransactionType the TransactionType (BUY, SELL etc.)
             * @param coin the name of the coin
             * @param buyQuantity the buy quantity (for BUY transaction)
             * @param walletAddress the trader's wallet address
             * @param updatedPrice the updated price (for UPDATE_PRICE transaction)
             * @param increasedVolume the increased volume (for ADD_VOLUME transaction)
             * @param sellQuantity the sell quantity (for SELL transaction)
             * @param latch the countdown latch for synchronizing transactions
             */
            ExecuteTransaction curTransaction = new ExecuteTransaction(curTransactionType, coin, buyQuantity, walletAddress, updatedPrice, increasedVolume, sellQuantity, latch);
            transactionService.execute(curTransaction);
        }
        // Shut down the ExecutorService
        transactionService.shutdown();
        try {
            transactionService.awaitTermination(5L, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Parses a Json file into a JsonNode.
     *
     * @param filePath path to the Json file
     * @return the deserialized JsonNode
     * @throws IOException if file read error
     */
    public static JsonNode parseJsonFile(String filePath) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        File file = new File(filePath);
        return objectMapper.readTree(file);
    }
}