package org.example;

import java.util.ArrayList;

public class TransactionResults {
    public static ArrayList<TransactionStatus> transactionStatusArrayList = new ArrayList<>();

    public static ArrayList<TransactionStatus> getTransactionStatusArrayList() {
        return transactionStatusArrayList;
    }

    public static void setTransactionStatusArrayList(ArrayList<TransactionStatus> transactionStatusArrayList) {
        TransactionResults.transactionStatusArrayList = transactionStatusArrayList;
    }


}
