package org.springframework.integration.loanbroker;

import java.util.Random;

import org.boon.Str;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BankStub {


    private Double interestRate = 0.08d;

    public void setBankType(String bankType) {
        this.bankType = bankType;
    }

    private String bankType;

    public static final Logger LOGGER = LoggerFactory.getLogger(BankStub.class);

    public Double quotation(Double loanAmount) throws Exception {

        // Seeding partial failure to verify request timeout.
        if (loanAmount <= 10d && interestRate == 0.05d) {
            throw new IllegalArgumentException("loan amount should be greater than " + loanAmount);
        }

        Thread.sleep(1000);
        LOGGER.info(bankType + ">> Callculating best quotation for price {} is {}. Where approx. calculation time is {} ms.", loanAmount, (loanAmount * interestRate), 1000);
        return loanAmount * interestRate;
    }


    public void setInterestRate(double interestRate) {
        this.interestRate = Double.valueOf(interestRate);
    }

}
